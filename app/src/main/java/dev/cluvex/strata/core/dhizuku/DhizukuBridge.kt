package dev.cluvex.strata.core.dhizuku

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.pm.PackageManager
import com.rosan.dhizuku.api.Dhizuku
import com.rosan.dhizuku.api.DhizukuRequestPermissionListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object DhizukuBridge {

    enum class Status { UNAVAILABLE, NEEDS_PERMISSION, READY }

    private val _state = MutableStateFlow(Status.UNAVAILABLE)
    val state: StateFlow<Status> = _state.asStateFlow()
    val isReady: Boolean get() = _state.value == Status.READY

    @Volatile private var appContext: Context? = null
    @Volatile private var initialized = false
    @Volatile private var delegated = false

    fun init(context: Context) {
        appContext = context.applicationContext
        initialized = runCatching { Dhizuku.init(context.applicationContext) }.getOrDefault(false)
        refresh()
    }

    fun refresh() {
        _state.value = when {
            !initialized -> Status.UNAVAILABLE
            isPermissionGranted() -> Status.READY
            else -> Status.NEEDS_PERMISSION
        }
        if (_state.value != Status.READY) delegated = false
    }

    private fun isPermissionGranted(): Boolean =
        runCatching { Dhizuku.isPermissionGranted() }.getOrDefault(false)

    /** 申请 Dhizuku 运行时授权（普通权限，不是委托范围）。 */
    suspend fun ensurePermission(): Boolean {
        if (!initialized) return false
        if (isPermissionGranted()) { refresh(); return true }

        val granted = suspendCancellableCoroutine { cont ->
            val listener = object : DhizukuRequestPermissionListener() {
                override fun onRequestPermission(grantResult: Int) {
                    if (cont.isActive) cont.resume(grantResult == PackageManager.PERMISSION_GRANTED)
                }
            }
            runCatching { Dhizuku.requestPermission(listener) }.onFailure {
                if (cont.isActive) cont.resume(false)
            }
        }
        refresh()
        return granted
    }

    /**
     * 申请 DELEGATION_PACKAGE_ACCESS 委托范围。
     * 成功后，Strata 可以用标准 DevicePolicyManager 调用 setPackagesSuspended。
     * 需要 Dhizuku 版本 >= 5。
     */
    fun ensurePackageDelegation(): Boolean {
        if (!initialized) return false
        if (delegated) return true
        if (!isPermissionGranted()) return false

        val version = runCatching { Dhizuku.getVersionCode() }.getOrDefault(0)
        if (version < 5) return false

        return runCatching {
            Dhizuku.setDelegatedScopes(arrayOf(DevicePolicyManager.DELEGATION_PACKAGE_ACCESS))
            delegated = true
            true
        }.getOrDefault(false)
    }

    /** 获取标准 DevicePolicyManager（需要已申请委托）。 */
    fun devicePolicyManager(): DevicePolicyManager? {
        val ctx = appContext ?: return null
        return runCatching {
            ctx.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
        }.getOrNull()
    }

    /** 挂起或恢复应用。返回 null 表示成功，非 null 为失败原因。 */
    fun setPackagesSuspended(packages: Array<String>, suspended: Boolean): String? {
        if (!ensurePackageDelegation()) return "delegation unavailable"
        val dpm = devicePolicyManager() ?: return "device policy manager unavailable"
        return runCatching {
            // admin 传 null：Strata 不是设备管理员，Dhizuku 才是
            dpm.setPackagesSuspended(null, packages, suspended)
        }.fold(
            onSuccess = { null },
            onFailure = { it.message ?: it.javaClass.simpleName },
        )
    }

    /** 隐藏或取消隐藏应用。返回 null 表示成功，非 null 为失败原因。 */
    fun setApplicationHidden(pkg: String, hidden: Boolean): String? {
        if (!ensurePackageDelegation()) return "delegation unavailable"
        val dpm = devicePolicyManager() ?: return "device policy manager unavailable"
        return runCatching {
            val ok = dpm.setApplicationHidden(null as android.content.ComponentName?, pkg, hidden)
            if (ok) null else "setApplicationHidden returned false"
        }.getOrElse { it.message ?: it.javaClass.simpleName }
    }

    /** 读取某个包是否被隐藏（需已申请委托）。 */
    fun isApplicationHidden(pkg: String): Boolean {
        val dpm = devicePolicyManager() ?: return false
        return runCatching { dpm.isApplicationHidden(null as android.content.ComponentName?, pkg) }.getOrDefault(false)
    }

    /** 读取某个包是否处于挂起状态。 */
    fun isSuspended(context: Context, pkg: String): Boolean = runCatching {
        val info = context.packageManager.getApplicationInfo(pkg, 0)
        (info.flags and android.content.pm.ApplicationInfo.FLAG_SUSPENDED) != 0
    }.getOrDefault(false)
}