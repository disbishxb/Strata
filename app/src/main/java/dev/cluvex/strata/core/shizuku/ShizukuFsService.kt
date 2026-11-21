package dev.cluvex.strata.core.shizuku

import android.os.IBinder
import com.topjohnwu.superuser.nio.FileSystemManager

class ShizukuFsService : IShizukuFileService.Stub() {
    override fun destroy() {
        System.exit(0)
    }

    override fun getFileSystemService(): IBinder = FileSystemManager.getService()

    override fun exec(cmd: String): String = runCatching {
        val p = Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
        val out = p.inputStream.bufferedReader().readText()
        val err = p.errorStream.bufferedReader().readText()
        p.waitFor()
        (out + err).trim()
    }.getOrDefault("")
}
