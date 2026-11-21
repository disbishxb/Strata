package dev.cluvex.strata.core.control

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import dev.cluvex.strata.core.data.StrataSettings
import dev.cluvex.strata.core.dhizuku.DhizukuBridge
import dev.cluvex.strata.core.root.RootManager
import dev.cluvex.strata.core.shizuku.ShizukuManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 应用控制：root / shizuku / dhizuku / 特权系统应用
 * AUTO 优先级：root > shizuku > dhizuku > privileged
 * Shizuku(adb) 不支持 HIDE。
 */
object AppControlBridge {

    enum class Capability { FORCE_STOP, DISABLE, HIDE, SUSPEND }

    enum class Backend { AUTO, ROOT, SHIZUKU, DHIZUKU, PRIVILEGED }

    /** 每个包最后一次成功控制使用的后端，用于 AUTO 下解控时复用 */
    private val lastBackend = java.util.concurrent.ConcurrentHashMap<String, Backend>()

    fun rememberBackend(pkg: String, backend: Backend) {
        if (backend != Backend.AUTO) lastBackend[pkg] = backend
    }

    fun recallBackend(pkg: String): Backend? = lastBackend[pkg]

    fun privileged(context: Context, settings: StrataSettings): Boolean {
        val app = context.applicationInfo
        val preinstalled = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0 &&
            (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0
        if (!preinstalled) return false
        val pm = context.packageManager
        val changeState = pm.checkPermission("android.permission.CHANGE_COMPONENT_ENABLED_STATE", context.packageName)
        val forceStop = pm.checkPermission("android.permission.FORCE_STOP_PACKAGES", context.packageName)
        return changeState == PackageManager.PERMISSION_GRANTED || forceStop == PackageManager.PERMISSION_GRANTED
    }

    fun available(context: Context, settings: StrataSettings): Set<Backend> = buildSet {
        add(Backend.AUTO)
        if (settings.rootAccess) add(Backend.ROOT)
        if (settings.shizukuAccess && ShizukuManager.isReady) add(Backend.SHIZUKU)
        if (settings.dhizukuAccess && DhizukuBridge.isReady) add(Backend.DHIZUKU)
        add(Backend.PRIVILEGED) // 系统特权：探测到即显示，不受用户开关控制
    }

    /** 解析 AUTO 到实际后端；不可用返回 null */
    /** 带包名的解析：AUTO 时优先复用该包上次使用的后端 */
    fun resolveFor(pkg: String, backend: Backend, context: Context, settings: StrataSettings): Backend? {
        if (backend == Backend.AUTO) {
            lastBackend[pkg]?.let { prev ->
                if (prev in available(context, settings)) return prev
            }
        }
        return resolve(backend, context, settings)
    }

    fun resolve(backend: Backend, context: Context, settings: StrataSettings): Backend? = when (backend) {
        Backend.AUTO -> when {
            settings.rootAccess -> Backend.ROOT
            privileged(context, settings) -> Backend.PRIVILEGED
            settings.dhizukuAccess && DhizukuBridge.isReady -> Backend.DHIZUKU
            settings.shizukuAccess && ShizukuManager.isReady -> Backend.SHIZUKU
            else -> null
        }
        else -> if (backend in available(context, settings)) backend else null
    }


    suspend fun isStillRestricted(context: Context, pkg: String, op: String): Boolean {
        return when (op) {
            "enabled" -> !context.packageManager.getApplicationInfo(pkg, 0).enabled
            "hidden" -> pkg in hiddenPackages(context, StrataSettings())
            "suspended" -> context.packageManager.isPackageSuspended(pkg)
            else -> false
        }
    }

    suspend fun autoLiftLimit(context: Context, pkg: String, op: String, settings: StrataSettings): String? {
        // 强行停止是即时动作，无需循环解控
        if (op == "force_stop") {
            val best = resolve(Backend.AUTO, context, settings) ?: return "No backend available"
            return forceStop(context, best, pkg)
        }

        val priorities = when (op) {
            "enabled" -> listOf(Backend.ROOT, Backend.PRIVILEGED, Backend.SHIZUKU)
            "hidden" -> listOf(Backend.ROOT, Backend.PRIVILEGED, Backend.DHIZUKU)
            "suspended" -> listOf(Backend.ROOT, Backend.DHIZUKU, Backend.SHIZUKU)
            else -> emptyList()
        }
        
        val avail = available(context, settings)
        var lastErr = "No backend available"
        
        for (b in priorities) {
            if (b in avail) {
                val err = when (op) {
                    "enabled" -> setEnabled(context, b, pkg, true)
                    "hidden" -> setHidden(context, b, pkg, false)
                    "suspended" -> setSuspended(context, b, pkg, false)
                    else -> "unknown"
                }
                if (err == null) {
                    kotlinx.coroutines.delay(300) // 给系统一点反应时间
                    if (!isStillRestricted(context, pkg, op)) {
                        return null
                    } else {
                        lastErr = "Still restricted with $b"
                    }
                } else {
                    lastErr = err
                }
            }
        }
        return lastErr
    }

    fun capabilities(backend: Backend): Set<Capability> = when (backend) {
        Backend.ROOT -> setOf(Capability.FORCE_STOP, Capability.DISABLE, Capability.HIDE, Capability.SUSPEND)
        Backend.SHIZUKU -> setOf(Capability.FORCE_STOP, Capability.DISABLE, Capability.SUSPEND)
        Backend.DHIZUKU -> setOf(Capability.HIDE, Capability.SUSPEND)
        Backend.PRIVILEGED -> setOf(Capability.FORCE_STOP, Capability.DISABLE)
        Backend.AUTO -> emptySet()
    }

    suspend fun forceStop(context: Context, backend: Backend, pkg: String): String? = when (backend) {
        Backend.ROOT -> runShell { RootManager.exec("am force-stop $pkg") }
        Backend.SHIZUKU -> runShizuku("am force-stop $pkg")
        Backend.PRIVILEGED -> reflectForceStop(context, pkg)
        else -> "backend unsupported"
    }

    suspend fun setEnabled(context: Context, backend: Backend, pkg: String, enabled: Boolean): String? {
        val cmd = if (enabled) "pm enable $pkg" else "pm disable-user --user 0 $pkg"
        return when (backend) {
            Backend.ROOT -> runShell { RootManager.exec(cmd) }
            Backend.SHIZUKU -> runShizuku(cmd)
            Backend.PRIVILEGED -> runCatching {
                val pm = context.packageManager
                pm.setApplicationEnabledSetting(
                    pkg,
                    if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    else PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER,
                    0,
                )
                null
            }.getOrElse { it.message ?: it.javaClass.simpleName }
            else -> "backend unsupported"
        }
    }

    suspend fun setHidden(context: Context, backend: Backend, pkg: String, hidden: Boolean): String? = when (backend) {
        Backend.ROOT -> runShell { RootManager.exec(if (hidden) "pm hide $pkg" else "pm unhide $pkg") }
        Backend.DHIZUKU -> withContext(Dispatchers.IO) { DhizukuBridge.setApplicationHidden(pkg, hidden) }
        else -> "backend unsupported"
    }

    suspend fun setSuspended(context: Context, backend: Backend, pkg: String, suspended: Boolean): String? = when (backend) {
        Backend.ROOT -> runShell { RootManager.exec(if (suspended) "pm suspend $pkg" else "pm unsuspend $pkg") }
        Backend.SHIZUKU -> runShizuku(if (suspended) "pm suspend $pkg" else "pm unsuspend $pkg")
        Backend.DHIZUKU -> withContext(Dispatchers.IO) { DhizukuBridge.setPackagesSuspended(arrayOf(pkg), suspended) }
        else -> "backend unsupported"
    }

    suspend fun hiddenPackages(context: Context, settings: StrataSettings): Set<String> = withContext(Dispatchers.IO) {
        runCatching {
            when {
                settings.rootAccess -> diffPm(RootManager.exec("pm list packages").out, RootManager.exec("pm list packages -u").out)
                settings.shizukuAccess && ShizukuManager.isReady -> {
                    val shown = ShizukuManager.exec("pm list packages").orEmpty().lines()
                    val all = ShizukuManager.exec("pm list packages -u").orEmpty().lines()
                    diffPm(shown, all)
                }
                settings.dhizukuAccess && DhizukuBridge.isReady -> {
                    val pm = context.packageManager
                    @Suppress("DEPRECATION")
                    pm.getInstalledPackages(0x00002000).mapNotNull { it.packageName }
                        .filter { DhizukuBridge.isApplicationHidden(it) }
                        .toSet()
                }
                else -> emptySet()
            }
        }.getOrDefault(emptySet())
    }

    suspend fun suspendedPackages(context: Context, settings: StrataSettings): Set<String> = withContext(Dispatchers.IO) {
        runCatching {
            val pm = context.packageManager
            @Suppress("DEPRECATION")
            pm.getInstalledPackages(0x00002000).mapNotNull { it.packageName }
                .filter { pm.isPackageSuspended(it) }
                .toSet()
        }.getOrDefault(emptySet())
    }

    private fun diffPm(shown: List<String>, all: List<String>): Set<String> {
        val norm = { l: String -> l.trim().removePrefix("package:") }
        return all.map(norm).toSet() - shown.map(norm).toSet()
    }

    private suspend fun runShell(block: suspend () -> com.topjohnwu.superuser.Shell.Result): String? =
        runCatching { val r = block(); if (r.isSuccess) null else r.out.joinToString(" ").ifEmpty { "exit ${r.code}" } }
            .getOrElse { it.message ?: it.javaClass.simpleName }

    private suspend fun runShizuku(cmd: String): String? {
        val out = ShizukuManager.exec(cmd) ?: return "shizuku unavailable"
        return if (out.contains("Exception", ignoreCase = true) || out.contains("Error:", ignoreCase = true)) out else null
    }


    private fun reflectForceStop(context: Context, pkg: String): String? {
        val pm = context.packageManager
        val names = listOf("forceStopPackage", "forceStopPackageAsUser", "forceStopPackageAsUserIfNotRestricted")
        for (name in names) {
            runCatching {
                val m = pm.javaClass.getMethod(name, String::class.java, Int::class.javaPrimitiveType)
                m.invoke(pm, pkg, 0)
                return null
            }
        }
        return "force-stop reflect failed"
    }
}
