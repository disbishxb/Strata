package dev.cluvex.strata.core.apk.install

import dev.cluvex.strata.core.dhizuku.DhizukuBridge
import dev.cluvex.strata.R
import dev.cluvex.strata.core.i18n.tr
import dev.cluvex.strata.core.root.RootManager
import dev.cluvex.strata.core.shizuku.ShizukuManager

enum class InstallBackend {
    Session,
    Root,
    Shizuku,
    Dhizuku;

    val needsPrivilege: Boolean get() = this != Session
}

/** 用户可参与优先级排序的后端（不含 Session）。 */
val PRIVILEGED_BACKENDS: List<InstallBackend> =
    listOf(InstallBackend.Root, InstallBackend.Shizuku, InstallBackend.Dhizuku)

data class BackendAvailability(
    val backend: InstallBackend,
    val available: Boolean,
    val reason: String? = null,
)

/** 安装策略：开关 + 优先级顺序 + 校验。 */
data class InstallPolicy(
    val enabled: Set<InstallBackend> = emptySet(),
    /** 用户自定义顺序，默认 Root > Shizuku > Dhizuku。 */
    val priority: List<InstallBackend> = PRIVILEGED_BACKENDS,
    val verifyBeforeInstall: Boolean = true,
) {
    /** 按优先级返回第一个"已开启且可用"的后端，否则回退 Session。 */
    fun resolve(availability: Map<InstallBackend, BackendAvailability>): InstallBackend {
        val ordered = (priority + PRIVILEGED_BACKENDS).distinct()
        for (backend in ordered) {
            if (backend !in enabled) continue
            if (availability[backend]?.available == true) return backend
        }
        return InstallBackend.Session
    }
}

data class ThirdPartyInstaller(
    val enabled: Boolean,
    val packageName: String,
) {
    val active: Boolean get() = enabled && packageName.isNotBlank()
}

object InstallAccess {
    fun thirdPartyOf(settings: dev.cluvex.strata.core.data.StrataSettings): ThirdPartyInstaller =
        ThirdPartyInstaller(
            enabled = settings.useThirdPartyInstaller,
            packageName = settings.thirdPartyInstallerPackage,
        )

    fun probe(): Map<InstallBackend, BackendAvailability> {
        val root = RootManager.isGranted
        val shizuku = ShizukuManager.isReady
        val dhizuku = DhizukuBridge.isReady

        return mapOf(
            InstallBackend.Session to BackendAvailability(InstallBackend.Session, true),
            InstallBackend.Root to BackendAvailability(
                InstallBackend.Root,
                available = root,
                reason = if (root) null else tr(R.string.install_reason_root_denied),
            ),
            InstallBackend.Shizuku to BackendAvailability(
                InstallBackend.Shizuku,
                available = shizuku,
                reason = when (ShizukuManager.state.value) {
                    ShizukuManager.Status.READY -> null
                    ShizukuManager.Status.NOT_RUNNING ->
                        if (ShizukuManager.isInstalled) tr(R.string.install_reason_shizuku_not_running) else tr(R.string.install_reason_shizuku_not_installed)
                    ShizukuManager.Status.NEEDS_PERMISSION -> tr(R.string.install_reason_shizuku_denied)
                },
            ),
            InstallBackend.Dhizuku to BackendAvailability(
                InstallBackend.Dhizuku,
                available = dhizuku,
                reason = when (DhizukuBridge.state.value) {
                    DhizukuBridge.Status.READY -> null
                    DhizukuBridge.Status.UNAVAILABLE -> tr(R.string.install_reason_dhizuku_unavailable)
                    DhizukuBridge.Status.NEEDS_PERMISSION -> tr(R.string.install_reason_dhizuku_denied)
                },
            ),
        )
    }

    /** 从设置构造策略。 */
    fun policyOf(settings: dev.cluvex.strata.core.data.StrataSettings): InstallPolicy {
        val enabled = buildSet {
            if (settings.installWithRoot) add(InstallBackend.Root)
            if (settings.installWithShizuku) add(InstallBackend.Shizuku)
            if (settings.installWithDhizuku) add(InstallBackend.Dhizuku)
        }
        return InstallPolicy(
            enabled = enabled,
            priority = settings.installPriority,
            verifyBeforeInstall = settings.verifyBeforeInstall,
        )
    }
}
