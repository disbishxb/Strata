package dev.cluvex.strata.core.root

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ipc.RootService
import com.topjohnwu.superuser.nio.FileSystemManager
import dev.cluvex.strata.core.fs.FileAccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

enum class RootState { UNKNOWN, GRANTED, DENIED }

/** 当前设备上检测到的 Root 管理器类型，仅用于日志/提示。 */
enum class RootFlavor { NONE, MAGISK, KERNELSU, APATCH, UNKNOWN }

object RootManager {

    @Volatile
    private var appContext: Context? = null

    @Volatile
    private var remoteFs: FileSystemManager? = null

    private val bindMutex = Mutex()
    private val _state = MutableStateFlow(RootState.UNKNOWN)
    val state: StateFlow<RootState> = _state.asStateFlow()

    /** 最近一次检测到的 Root 管理器类型。 */
    @Volatile
    var flavor: RootFlavor = RootFlavor.NONE
        private set

    val isGranted: Boolean get() = _state.value == RootState.GRANTED

    fun init(context: Context) {
        appContext = context.applicationContext
        Shell.enableVerboseLogging = false
        Shell.setDefaultBuilder(
            Shell.Builder.create()
                .setFlags(Shell.FLAG_MOUNT_MASTER)
                .setTimeout(20),
        )
    }

    /** 重置内部状态，用于用户在设置页关闭 Root 开关后重新探测。 */
    fun reset() {
        remoteFs = null
        flavor = RootFlavor.NONE
        _state.value = RootState.UNKNOWN
    }

    suspend fun request(): Boolean = withContext(Dispatchers.IO) {
        // 1) libsu：覆盖 Magisk 与已授权的 KernelSU
        val libsuGranted = runCatching { Shell.getShell().isRoot }.getOrDefault(false)
        if (libsuGranted) {
            flavor = detectFlavor()
            _state.value = RootState.GRANTED
            return@withContext true
        }

        // 2) su -c id 回退：覆盖 APatch 的 su 兼容层
        val execGranted = probeSuExec()
        if (execGranted) {
            flavor = detectFlavor()
            _state.value = RootState.GRANTED
            return@withContext true
        }

        // 3) 都失败：判定为 DENIED，但仍记录检测到的 flavor 便于排查
        flavor = detectFlavor()
        _state.value = RootState.DENIED
        false
    }

    /** 直接执行 `su -c id`，输出包含 uid=0 视为拿到 root。 */
    private fun probeSuExec(): Boolean = runCatching {
        val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
        val out = process.inputStream.bufferedReader().use { it.readText() }
        val err = process.errorStream.bufferedReader().use { it.readText() }
        val finished = process.waitFor(3, TimeUnit.SECONDS)
        if (!finished) {
            process.destroyForcibly()
            return@runCatching false
        }
        val text = out + "\n" + err
        process.exitValue() == 0 && text.contains("uid=0")
    }.getOrDefault(false)

    /** 通过特征文件/路径判断当前 Root 管理器类型。 */
    private fun detectFlavor(): RootFlavor {
        // KernelSU
        if (File("/data/adb/ksud").exists() ||
            File("/data/adb/ksu").exists() ||
            File("/system/bin/ksud").exists()
        ) return RootFlavor.KERNELSU

        // APatch
        if (File("/data/adb/apd").exists() ||
            File("/data/adb/ap").exists() ||
            File("/data/adb/ap/su_path").exists()
        ) return RootFlavor.APATCH

        // Magisk
        if (File("/data/adb/magisk").exists() ||
            File("/data/adb/magisk.db").exists()
        ) return RootFlavor.MAGISK

        // 无特征文件但 su 可用，归为 UNKNOWN
        return if (probeSuExec()) RootFlavor.UNKNOWN else RootFlavor.NONE
    }

    suspend fun access(): RootFileAccess? = fileSystem()?.let { RootFileAccess(it) }

    private suspend fun fileSystem(): FileSystemManager? {
        remoteFs?.let { return it }
        if (!request()) return null
        return bindMutex.withLock { remoteFs ?: bind()?.also { remoteFs = it } }
    }

    private suspend fun bind(): FileSystemManager? {
        val ctx = appContext ?: return null
        return withContext(Dispatchers.Main) {
            withTimeoutOrNull(20_000) {
                suspendCancellableCoroutine { cont ->
                    val connection = object : ServiceConnection {
                        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                            val fs = service?.let { runCatching { FileSystemManager.getRemote(it) }.getOrNull() }
                            if (cont.isActive) cont.resume(fs)
                        }

                        override fun onServiceDisconnected(name: ComponentName?) {
                            remoteFs = null
                        }
                    }
                    RootService.bind(Intent(ctx, StrataRootService::class.java), connection)
                }
            }
        }
    }

    suspend fun copyFileToCache(srcPath: String, destFile: File): Boolean = withContext(Dispatchers.IO) {
        val fs = fileSystem()
        if (fs != null) {
            runCatching {
                val src = fs.getFile(srcPath)
                if (!src.exists()) return@runCatching false
                val dest = fs.getFile(destFile.absolutePath)
                val input = src.newInputStream()
                val output = dest.newOutputStream(false)
                input.use { i ->
                    output.use { o ->
                        i.copyTo(o)
                    }
                }
                return@runCatching destFile.exists() && destFile.length() > 0
            }.getOrDefault(false)
        } else {
            // 如果没有 FileSystemManager，退化为 Shell
            runCatching {
                Shell.cmd("cat \"$srcPath\" > \"${destFile.absolutePath}\"").exec()
                destFile.exists() && destFile.length() > 0
            }.getOrDefault(false)
        }
    }

    suspend fun exec(vararg commands: String): Shell.Result = withContext(Dispatchers.IO) { Shell.cmd(*commands).exec() }

    suspend fun accessIf(needsRoot: Boolean, rootEnabled: Boolean): FileAccess? =
        if (needsRoot && rootEnabled) access() else null
}