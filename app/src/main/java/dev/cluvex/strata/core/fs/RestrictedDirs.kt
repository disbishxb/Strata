package dev.cluvex.strata.core.fs

import android.content.Context
import dev.cluvex.strata.core.model.FileItem
import java.io.File


/** 受限 Android 目录在 root/Shizuku 都没开时的错误标记。 */
const val RESTRICTED_ANDROID_DIR = "__restricted_android_dir__"

/** /sdcard/Android/data、/sdcard/Android/obb、/sdcard/Android/media 及其等价路径。 */
fun isRestrictedAndroidDir(path: String): Boolean {
    val p = path.trimEnd('/')
    return p.endsWith("/Android/data") || p.endsWith("/Android/obb")
}


/** 在 Android/data 或 Android/obb 无权限时，返回本应用自身在该目录下的可访问文件夹。 */
fun ownAppDirsInRestricted(context: Context, path: String): List<FileItem> {
    val p = path.trimEnd('/')
    val own = when {
        p.endsWith("/Android/data") -> context.getExternalFilesDir(null)?.parentFile
        p.endsWith("/Android/obb") -> context.obbDir
        else -> null
    } ?: return emptyList()
    return listOfNotNull(own.takeIf { it.exists() && it.isDirectory }?.let { FileItem.of(it) })
}
