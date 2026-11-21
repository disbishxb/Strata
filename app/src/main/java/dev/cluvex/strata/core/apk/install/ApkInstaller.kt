package dev.cluvex.strata.core.apk.install

import android.app.PendingIntent
import dev.cluvex.strata.core.dhizuku.DhizukuBridge
import dev.cluvex.strata.core.shizuku.ShizukuManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.os.Build
import dev.cluvex.strata.R
import dev.cluvex.strata.core.fs.FsUtil
import dev.cluvex.strata.core.i18n.tr
import dev.cluvex.strata.core.root.RootManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipFile
import ru.solrudev.ackpine.installer.createSession
import ru.solrudev.ackpine.dhizuku.dhizuku
import ru.solrudev.ackpine.session.await
import ru.solrudev.ackpine.session.Session
import android.net.Uri

class ApkInstaller(private val context: Context) {
    suspend fun install(
        parts: List<ApkSource>,
        packageName: String?,
        backend: InstallBackend,
        allowDowngrade: Boolean = false,
    ): InstallResult = when (backend) {
        InstallBackend.Session -> installWithSystem(parts, packageName)
        InstallBackend.Root -> installWithRoot(parts, packageName, allowDowngrade)
        InstallBackend.Shizuku -> installWithShizuku(parts, packageName)
        InstallBackend.Dhizuku -> installWithDhizuku(parts, packageName)
    }

    suspend fun installByPolicy(
        parts: List<ApkSource>,
        packageName: String?,
        policy: InstallPolicy,
        availability: Map<InstallBackend, BackendAvailability> = InstallAccess.probe(),
        allowDowngrade: Boolean = false,
    ): InstallResult = install(parts, packageName, policy.resolve(availability), allowDowngrade)

    suspend fun installWithRoot(parts: List<ApkSource>, packageName: String?, allowDowngrade: Boolean = false): InstallResult =
        withContext(Dispatchers.IO) {
            val dir = PmInstall.stagingDir()
            try {
                when (val session = writeRootSession(parts, dir, allowDowngrade)) {
                    is RootSession.Failed -> InstallStatus.fromPm(session.output, packageName)
                    is RootSession.Ready -> InstallStatus.fromPm(shell(PmInstall.commit(session.id)), packageName)
                }
            } finally {
                shell(PmInstall.unstage(dir))
            }
        }

    sealed interface RootSession {
        data class Ready(val id: Int) : RootSession
        data class Failed(val output: String) : RootSession
    }

    internal suspend fun writePrivilegedSession(
        parts: List<ApkSource>,
        dir: String,
        shell: suspend (String) -> String,
    ): RootSession {
        val localStage = File(context.cacheDir, "install/priv-${System.nanoTime()}")
        try {
            val staging = PmInstall.stage(parts.map { it.materialize(localStage) }, dir)
            val copied = shell(staging.command)
            if (!PmInstall.staged(copied)) return RootSession.Failed("Failure [staging failed: ${copied.trim()}]")
            val created = shell(PmInstall.create(staging.parts, false))
            val session = PmInstall.parseSessionId(created) ?: return RootSession.Failed(created)
            for (command in PmInstall.write(session, staging.parts)) {
                val written = shell(command)
                if (!written.contains("Success")) {
                    shell(PmInstall.abandon(session))
                    return RootSession.Failed(written)
                }
            }
            return RootSession.Ready(session)
        } finally {
            FsUtil.deleteRecursively(localStage)
        }
    }

    internal suspend fun writeRootSession(parts: List<ApkSource>, dir: String, allowDowngrade: Boolean): RootSession {
        val localStage = File(context.cacheDir, "install/root-${System.nanoTime()}")
        try {
            val staging = PmInstall.stage(parts.map { it.materialize(localStage) }, dir)
            val copied = shell(staging.command)
            if (!PmInstall.staged(copied)) return RootSession.Failed("Failure [staging failed: ${copied.trim()}]")
            val created = shell(PmInstall.create(staging.parts, allowDowngrade))
            val session = PmInstall.parseSessionId(created) ?: return RootSession.Failed(created)
            for (command in PmInstall.write(session, staging.parts)) {
                val written = shell(command)
                if (!written.contains("Success")) {
                    shell(PmInstall.abandon(session))
                    return RootSession.Failed(written)
                }
            }
            return RootSession.Ready(session)
        } finally {
            FsUtil.deleteRecursively(localStage)
        }
    }

    suspend fun installWithShizuku(parts: List<ApkSource>, packageName: String?): InstallResult =
        withContext(Dispatchers.IO) {
            val dir = PmInstall.stagingDir()
            try {
                when (val session = writePrivilegedSession(parts, dir) { shizukuShell(it) }) {
                    is RootSession.Failed -> InstallStatus.fromPm(session.output, packageName)
                    is RootSession.Ready -> InstallStatus.fromPm(shizukuShell(PmInstall.commit(session.id)), packageName)
                }
            } finally {
                shizukuShell(PmInstall.unstage(dir))
            }
        }

    private suspend fun shizukuShell(command: String): String =
        ShizukuManager.exec("$command 2>&1").orEmpty()

    suspend fun installWithDhizuku(parts: List<ApkSource>, packageName: String?): InstallResult {
        if (!DhizukuBridge.ensurePermission()) {
            return InstallResult.Failure(InstallFailure.BLOCKED, tr(R.string.install_reason_dhizuku_blocked))
        }

        // Ackpine 需要 Uri 作为安装入口。多 part 时用 base APK（第一个）创建会话，
        // Ackpine 的 Dhizuku 插件会以设备所有者身份执行安装。
        val mainPart = parts.first()
        val uri = withContext(Dispatchers.IO) {
            when (mainPart) {
                is ApkSource.FileApk -> Uri.fromFile(mainPart.file)
                is ApkSource.BundleEntry -> {
                    val tmp = File(context.cacheDir, "ackpine-${System.nanoTime()}-${mainPart.safeName}")
                    mainPart.open().use { input -> tmp.outputStream().use { input.copyTo(it) } }
                    Uri.fromFile(tmp)
                }
            }
        }

        val installer = ru.solrudev.ackpine.installer.PackageInstaller.getInstance(context)
        val session = installer.createSession(uri) {
            dhizuku()
            // 如需降级安装，取消下面注释：
            // dhizuku { requestDowngrade = true }
        }

        return when (val state = session.await()) {
            is Session.State.Succeeded -> InstallResult.Success(packageName)
            is Session.State.Failed -> {
                val failure = state.failure
                val reason = when {
                    failure.message?.contains("DOWNGRADE", ignoreCase = true) == true -> InstallFailure.DOWNGRADE
                    failure.message?.contains("CONFLICT", ignoreCase = true) == true -> InstallFailure.CONFLICT
                    failure.message?.contains("SIGNATURE", ignoreCase = true) == true -> InstallFailure.SIGNATURE_CONFLICT
                    failure.message?.contains("STORAGE", ignoreCase = true) == true -> InstallFailure.STORAGE
                    failure.message?.contains("BLOCKED", ignoreCase = true) == true -> InstallFailure.BLOCKED
                    else -> InstallFailure.OTHER
                }
                InstallResult.Failure(reason, failure.message ?: "Ackpine install failed")
            }
        }
    }

    suspend fun installWithThirdParty(
        parts: List<ApkSource>,
        packageName: String?,
        installerPackage: String,
    ): InstallResult = withContext(Dispatchers.IO) {
        if (installerPackage.isBlank()) {
            return@withContext InstallResult.Failure(
                InstallFailure.OTHER,
                tr(R.string.install_reason_third_party_unavailable, installerPackage),
            )
        }
        val dir = File(context.cacheDir, "thirdparty").apply { mkdirs() }
        val main = parts.first()
        val staged = File(dir, "tp-${System.nanoTime()}-${main.safeName}")
        try {
            main.open().use { input -> staged.outputStream().use { input.copyTo(it) } }
        } catch (e: Exception) {
            staged.delete()
            return@withContext InstallResult.Failure(InstallFailure.OTHER, e.message ?: e.javaClass.simpleName)
        }
        val uri = try {
            androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                staged,
            )
        } catch (e: Exception) {
            staged.delete()
            return@withContext InstallResult.Failure(InstallFailure.OTHER, e.message ?: e.javaClass.simpleName)
        }
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            setPackage(installerPackage)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return@withContext try {
            context.startActivity(intent)
            InstallResult.ThirdPartyDispatched(packageName, installerPackage)
        } catch (e: android.content.ActivityNotFoundException) {
            staged.delete()
            InstallResult.Failure(InstallFailure.OTHER, tr(R.string.install_reason_third_party_unavailable, installerPackage))
        } catch (e: Exception) {
            staged.delete()
            InstallResult.Failure(InstallFailure.OTHER, e.message ?: e.javaClass.simpleName)
        }
    }

    suspend fun installBundleWithThirdParty(
        bundleFile: File,
        packageName: String?,
        installerPackage: String,
    ): InstallResult = withContext(Dispatchers.IO) {
        if (installerPackage.isBlank()) {
            return@withContext InstallResult.Failure(
                InstallFailure.OTHER,
                tr(R.string.install_reason_third_party_unavailable, installerPackage),
            )
        }
        val dir = File(context.cacheDir, "thirdparty").apply { mkdirs() }
        val staged = File(dir, "tp-${System.nanoTime()}-${bundleFile.name.replace(Regex("[^A-Za-z0-9._-]"), "_")}")
        try {
            bundleFile.inputStream().use { input -> staged.outputStream().use { input.copyTo(it) } }
        } catch (e: Exception) {
            staged.delete()
            return@withContext InstallResult.Failure(InstallFailure.OTHER, e.message ?: e.javaClass.simpleName)
        }
        val uri = try {
            androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                staged,
            )
        } catch (e: Exception) {
            staged.delete()
            return@withContext InstallResult.Failure(InstallFailure.OTHER, e.message ?: e.javaClass.simpleName)
        }
        val mimes = listOf(
            "application/vnd.android.package-archive",
            "application/zip",
            "application/octet-stream",
        )
        for (mime in mimes) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mime)
                setPackage(installerPackage)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(intent)
                return@withContext InstallResult.ThirdPartyDispatched(packageName, installerPackage)
            } catch (_: android.content.ActivityNotFoundException) {
                // try next mime
            }
        }
        staged.delete()
        InstallResult.Failure(
            InstallFailure.OTHER,
            tr(R.string.install_reason_third_party_unavailable, installerPackage),
        )
    }

    suspend fun installWithSystem(parts: List<ApkSource>, packageName: String?): InstallResult {
        val installer = context.packageManager.packageInstaller
        val sessionId = withContext(Dispatchers.IO) {
            val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
                packageName?.let { setAppPackageName(it) }
                setSize(parts.sumOf { it.size.coerceAtLeast(0) })
                if (Build.VERSION.SDK_INT >= 26) setInstallReason(PackageManager.INSTALL_REASON_USER)
                if (Build.VERSION.SDK_INT >= 31) setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
                if (Build.VERSION.SDK_INT >= 33) setPackageSource(PackageInstaller.PACKAGE_SOURCE_LOCAL_FILE)
            }
            val id = installer.createSession(params)
            try {
                installer.openSession(id).use { session ->
                    parts.forEachIndexed { i, part ->
                        part.open().use { input ->
                            session.openWrite("${i}_${part.safeName}", 0, part.size).use { out ->
                                input.copyTo(out, BUFFER)
                                session.fsync(out)
                            }
                        }
                    }
                    session.commit(statusReceiver(id).intentSender)
                }
            } catch (e: Exception) {
                runCatching { installer.abandonSession(id) }
                throw e
            }
            id
        }
        return InstallEvents.events.first { it.sessionId == sessionId }.result
    }

    private fun statusReceiver(sessionId: Int): PendingIntent {
        val intent = Intent(context, InstallResultReceiver::class.java).setAction(InstallResultReceiver.ACTION)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= 31) PendingIntent.FLAG_MUTABLE else 0)
        return PendingIntent.getBroadcast(context, sessionId, intent, flags)
    }

    private suspend fun shell(command: String): String {
        val result = RootManager.exec("$command 2>&1")
        return result.out.joinToString("\n")
    }

    private companion object {
        const val BUFFER = 1 shl 16
    }
}

sealed interface ApkSource {
    val name: String
    val size: Long
    fun open(): InputStream

    fun materialize(dir: File): ApkPart

    val safeName: String get() = name.replace(Regex("[^A-Za-z0-9._-]"), "_")

    data class FileApk(val file: File) : ApkSource {
        override val name: String get() = file.name
        override val size: Long get() = file.length()
        override fun open(): InputStream = file.inputStream()
        override fun materialize(dir: File) = ApkPart(name, file.absolutePath, size)
    }

    data class BundleEntry(val bundle: File, val entry: String, override val size: Long) : ApkSource {
        override val name: String get() = entry.substringAfterLast('/')

        override fun open(): InputStream {
            val zip = ZipFile(bundle)
            val e = zip.getEntry(entry) ?: run { zip.close(); throw IOException(tr(R.string.apk_error_entry_missing, entry, bundle.name)) }
            val input = zip.getInputStream(e)
            return object : java.io.FilterInputStream(input) {
                override fun close() {
                    try { super.close() } finally { zip.close() }
                }
            }
        }

        override fun materialize(dir: File): ApkPart {
            dir.mkdirs()
            val out = File(dir, safeName)
            open().use { input -> out.outputStream().use { input.copyTo(it) } }
            return ApkPart(name, out.absolutePath, out.length())
        }
    }
}
