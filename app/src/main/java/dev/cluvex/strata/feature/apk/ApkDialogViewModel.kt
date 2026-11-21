package dev.cluvex.strata.feature.apk

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.cluvex.strata.R
import dev.cluvex.strata.core.apk.ApkCloner
import dev.cluvex.strata.core.apk.ApkManifestInfo
import dev.cluvex.strata.core.apk.ApkManifestReader
import dev.cluvex.strata.core.apk.ApkSignature
import dev.cluvex.strata.core.apk.ApkSignatureInspector
import dev.cluvex.strata.core.apk.ApkSigning
import dev.cluvex.strata.core.apk.CloneIconRenderer
import dev.cluvex.strata.core.apk.InstalledApp
import dev.cluvex.strata.core.apk.InstalledApps
import dev.cluvex.strata.core.apk.ProtectionDetector
import dev.cluvex.strata.core.apk.SigningKeys
import dev.cluvex.strata.core.apk.VersionRelation
import dev.cluvex.strata.core.apk.install.ApkInstaller
import dev.cluvex.strata.core.apk.install.ApkSource
import dev.cluvex.strata.core.apk.install.InstallAccess
import dev.cluvex.strata.core.apk.install.InstallBackend
import dev.cluvex.strata.core.apk.install.InstallResult
import dev.cluvex.strata.core.data.SettingsRepository
import dev.cluvex.strata.core.fs.FsUtil
import dev.cluvex.strata.core.fs.LocalFileAccess
import dev.cluvex.strata.core.i18n.tr
import dev.cluvex.strata.core.root.RootManager
import dev.cluvex.strata.core.shizuku.ShizukuManager
import dev.cluvex.strata.feature.build.runBuild
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

sealed interface InstallPhase {
    data object Idle : InstallPhase
    data object Verifying : InstallPhase
    data object NeedsSignature : InstallPhase
    data class Installing(val backend: InstallBackend) : InstallPhase {
        val viaRoot: Boolean get() = backend == InstallBackend.Root
    }
    data class Done(val result: InstallResult, val viaRoot: Boolean) : InstallPhase
    data object Cloning : InstallPhase
}

data class ApkDialogState(
    val path: String = "",
    val actualPath: String = "",
    val loading: Boolean = true,
    val manifest: ApkManifestInfo? = null,
    val icon: Bitmap? = null,
    val size: Long = 0L,
    val installed: InstalledApp? = null,
    val signature: ApkSignature? = null,
    val protection: List<String>? = null,
    val install: InstallPhase = InstallPhase.Idle,
    val build: dev.cluvex.strata.core.build.BuildRun? = null,
    val message: String? = null,
) {
    val fileName: String get() = File(path).name
    val notAnApk: Boolean get() = !loading && manifest == null
    val relation: VersionRelation get() = manifest?.let { VersionRelation.of(it.versionCode, installed) } ?: VersionRelation.NOT_INSTALLED
}

data class InstallPolicy(val rootMode: Boolean, val installWithRoot: Boolean, val verifyFirst: Boolean)

class ApkDialogViewModel(app: Application) : AndroidViewModel(app) {
    private val settingsRepository = SettingsRepository(app)

    private val pm = app.packageManager
    private val installer = ApkInstaller(app)
    private val _state = MutableStateFlow(ApkDialogState())
    val state: StateFlow<ApkDialogState> = _state.asStateFlow()

    private var loadJob: Job? = null
    private var signatureJob: Deferred<ApkSignature>? = null

    // 如果是从受限目录复制过来的缓存文件，在这里记录以便清理
    private var materializedFile: File? = null

    fun load(path: String) {
        if (_state.value.path == path && !_state.value.loading && _state.value.install !is InstallPhase.Installing) {
            refreshInstalled()
            return
        }
        loadJob?.cancel()
        
        // 清理旧的物化文件
        materializedFile?.delete()
        materializedFile = null
        
        // 先展示原始路径，loading 中
        _state.value = ApkDialogState(path = path)
        
        loadJob = viewModelScope.launch {
            var actualPath = path
            val originalFile = File(path)
            
            // 1. 物化：如果原文件不可读，先复制到本地缓存
            if (!originalFile.canRead()) {
                withContext(Dispatchers.IO) {
                    val cacheDir = File(getApplication<Application>().cacheDir, "apk_view").apply { mkdirs() }
                    val access = RootManager.access() ?: LocalFileAccess
                    val local = access.materialize(originalFile, cacheDir)
                    if (local.exists()) {
                        materializedFile = local
                        actualPath = local.absolutePath
                    }
                }
            }
            
            val file = File(actualPath)
            
            // 2. 解析 Manifest 和图标
            val (manifest, icon) = withContext(Dispatchers.IO) {
                val m = runCatching { ApkManifestReader(pm).read(actualPath) }.getOrNull()
                m to (if (m != null) loadIcon(actualPath) else null)
            }
            
            // 3. 查找是否已安装
            val installed = manifest?.let { withContext(Dispatchers.IO) { InstalledApps(pm).find(it.packageName) } }
            
            // 这里的 path 保持为 originalFile.absolutePath，但后续内部操作需要用 actualPath（缓存路径）
            _state.update {
                it.copy(
                    path = originalFile.absolutePath, // 给 UI 显示用
                    actualPath = actualPath,          // 物化后的可读路径
                    loading = false, 
                    manifest = manifest, 
                    icon = icon, 
                    size = file.length(), // 用缓存文件的大小
                    installed = installed
                )
            }
            if (manifest == null) return@launch
            
            // 4. 异步解析签名和加固
            val sigPath = actualPath
            val sig = async(Dispatchers.Default) { ApkSignatureInspector.inspect(File(sigPath)) }
            signatureJob = sig
            
            launch(Dispatchers.IO) {
                val found = runCatching { ProtectionDetector.detect(File(sigPath)) }.getOrDefault(emptyList())
                _state.update { it.copy(protection = found) }
            }
            val signature = sig.await()
            _state.update { it.copy(signature = signature) }
        }
    }

    fun refreshInstalled() {
        val pkg = _state.value.manifest?.packageName ?: return
        viewModelScope.launch {
            val installed = withContext(Dispatchers.IO) { InstalledApps(pm).find(pkg) }
            _state.update { it.copy(installed = installed) }
        }
    }

    fun install(policy: InstallPolicy, force: Boolean = false, allowDowngrade: Boolean = false) {
        val s = _state.value
        val manifest = s.manifest ?: return
        if (busy) return
        viewModelScope.launch {
            if (policy.verifyFirst && !force) {
                _state.update { it.copy(install = InstallPhase.Verifying) }
                val signature = _state.value.signature ?: signatureJob?.await() ?: ApkSignatureInspector.inspect(File(s.path))
                if (!signature.verified) {
                    _state.update { it.copy(install = InstallPhase.NeedsSignature) }
                    return@launch
                }
            }
            // 如果存在物化缓存，用缓存安装；否则用原路径
            val installFile = materializedFile ?: File(s.path)
            runInstall(installFile, manifest.packageName, policy, allowDowngrade)
        }
    }

    fun cloneAndInstall(policy: InstallPolicy, newPackage: String, newLabel: String?, iconUri: Uri? = null) {
        val s = _state.value
        if (s.manifest == null || busy) return
        val plan = buildList {
            add(ApkCloner.STEP_READ)
            add(ApkCloner.STEP_RENAME)
            add(ApkCloner.STEP_REBASE)
            if (iconUri != null) add(ApkCloner.STEP_ICON)
            add(ApkCloner.STEP_WRITE)
            add(ApkSigning.STEP_SIGN)
            add(ApkSigning.STEP_VERIFY)
        }
        viewModelScope.launch {
            _state.update { it.copy(install = InstallPhase.Cloning) }
            val app = getApplication<Application>()
            val result = runBuild(
                tr(R.string.apk_clone_build_title),
                "${s.manifest.packageName} → $newPackage",
                plan,
                publish = { run -> _state.update { it.copy(build = run) } },
                summary = { out: File -> tr(R.string.apk_clone_wrote, out.name) },
            ) { log ->
                val unsigned = File(app.cacheDir, "clones").apply { mkdirs() }.let { File(it, "clone-${System.nanoTime()}.apk") }
                try {
                    val icon = iconUri?.let { uri ->
                        val bytes = app.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: throw IOException(tr(R.string.apk_error_open_picture))
                        CloneIconRenderer.render(CloneIconRenderer.decode(bytes))
                    }
                    val sourceFile = materializedFile ?: File(s.path)
                    ApkCloner.clone(sourceFile, unsigned, newPackage, newLabel, icon, log)
                    val out = cloneOutputFile(s, newLabel, newPackage)
                    ApkSigning.sign(unsigned, out, SigningKeys.default(app.filesDir), log = log)
                    out
                } finally {
                    unsigned.delete()
                }
            }
            result.onSuccess { signedClone ->
                val originalDir = File(s.path).parentFile
                if (originalDir != null && !originalDir.canWrite()) {
                    val access = RootManager.access() ?: if (ShizukuManager.isReady) ShizukuManager.access() else null
                    if (access != null) {
                        runCatching {
                            val target = access.child(access.file(originalDir.path), signedClone.name)
                            access.openOutput(target).use { out -> signedClone.inputStream().use { it.copyTo(out) } }
                        }
                    }
                }
                runInstall(signedClone, newPackage, policy, allowDowngrade = false)
            }.onFailure {
                _state.update { it.copy(install = InstallPhase.Idle) }
            }
        }
    }

    fun closeBuild() = _state.update { it.copy(build = null) }

    private suspend fun runInstall(file: File, packageName: String, policy: InstallPolicy, allowDowngrade: Boolean) {
        val settings = settingsRepository.settings.first()
        // .apk.1 等非标准后缀：复制成 .apk 再安装，避免第三方/PM 因文件名拒绝
        val normalized = if (file.name.endsWith(".apk.1", ignoreCase = true)) {
            withContext(Dispatchers.IO) {
                val copy = File(getApplication<Application>().cacheDir, "install/${System.nanoTime()}-${file.name.removeSuffix(".1")}")
                copy.parentFile?.mkdirs()
                file.inputStream().use { input -> copy.outputStream().use { input.copyTo(it) } }
                copy
            }
        } else file
        val parts = listOf(ApkSource.FileApk(normalized))

        // 第三方安装器优先
        val thirdParty = InstallAccess.thirdPartyOf(settings)
        if (thirdParty.active) {
            _state.update { it.copy(install = InstallPhase.Installing(InstallBackend.Session)) }
            val result = try {
                installer.installWithThirdParty(parts, packageName, thirdParty.packageName)
            } catch (e: Exception) {
                InstallResult.Failure(
                    dev.cluvex.strata.core.apk.install.InstallFailure.OTHER,
                    e.message ?: e.javaClass.simpleName,
                )
            }
            _state.update { it.copy(install = InstallPhase.Done(result, viaRoot = false)) }
            refreshInstalled()
            return
        }

        val availability = InstallAccess.probe()
        val backend = InstallAccess.policyOf(settings).resolve(availability)
        val viaRoot = backend == InstallBackend.Root
        _state.update { it.copy(install = InstallPhase.Installing(backend)) }

        val result = try {
            installer.install(parts, packageName, backend, allowDowngrade)
        } catch (e: Exception) {
            InstallResult.Failure(dev.cluvex.strata.core.apk.install.InstallFailure.OTHER, e.message ?: e.javaClass.simpleName)
        }
        
        _state.update { it.copy(install = InstallPhase.Done(result, viaRoot)) }
        refreshInstalled()
    }

    private val busy: Boolean
        get() = _state.value.install.let { it is InstallPhase.Installing || it is InstallPhase.Verifying || it == InstallPhase.Cloning }

    private fun cloneOutputFile(s: ApkDialogState, newLabel: String?, newPackage: String): File {
        val base = (newLabel?.takeIf { it.isNotBlank() } ?: s.manifest?.label ?: s.fileName.removeSuffix(".apk"))
            .replace(Regex("[\\\\/:*?\"<>|\\u0000-\\u001F]"), "_").trim().ifEmpty { newPackage }
        val dir = File(s.actualPath.ifBlank { s.path }).parentFile?.takeIf { it.canWrite() }
            ?: Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return FsUtil.uniqueSibling(File(dir, "$base-clone.apk"), isDirectory = false)
    }

    fun resetInstall() = _state.update { it.copy(install = InstallPhase.Idle) }

    fun extractIcon() {
        val s = _state.value
        val icon = s.icon ?: return
        viewModelScope.launch {
            val saved = withContext(Dispatchers.IO) {
                runCatching {
                    val base = (s.manifest?.label?.takeIf { it.isNotBlank() } ?: s.manifest?.packageName ?: "icon")
                        .replace(Regex("[\\\\/:*?\"<>|]"), "_")
                    val dir = File(s.path).parentFile?.takeIf { it.canWrite() }
                        ?: Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val target = FsUtil.uniqueSibling(File(dir, "${base}_icon.png"), isDirectory = false)
                    target.outputStream().use { icon.compress(Bitmap.CompressFormat.PNG, 100, it) }
                    target.absolutePath
                }
            }
            _state.update {
                it.copy(message = saved.fold({ p -> tr(R.string.apk_icon_saved, p) }, { e -> tr(R.string.apk_icon_save_failed, e.message) }))
            }
        }
    }

    fun consumeMessage() = _state.update { it.copy(message = null) }

    private fun loadIcon(path: String): Bitmap? = runCatching {
        @Suppress("DEPRECATION")
        val app = pm.getPackageArchiveInfo(path, 0)?.applicationInfo ?: return null
        app.sourceDir = path
        app.publicSourceDir = path
        val drawable = app.loadIcon(pm)
        val side = ICON_PX
        drawable.toBitmap(side, side, Bitmap.Config.ARGB_8888)
    }.getOrNull()

    override fun onCleared() {
        materializedFile?.delete()
        super.onCleared()
    }

    private companion object {
        const val ICON_PX = 432
    }
}