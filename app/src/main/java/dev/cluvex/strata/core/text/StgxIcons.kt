package dev.cluvex.strata.core.text

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.Audiotrack
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.FontDownload
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.DataObject
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FolderZip
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector

object StgxIcons {
    private val BUILT_IN: Map<String, ImageVector> = mapOf(
        "terminal" to Icons.Rounded.Terminal,
        "shell" to Icons.Rounded.Terminal,
        "data_object" to Icons.Rounded.DataObject,
        "json" to Icons.Rounded.DataObject,
        "description" to Icons.Rounded.Description,
        "markdown" to Icons.Rounded.Description,
        "text" to Icons.Rounded.Description,
        "folder_zip" to Icons.Rounded.FolderZip,
        "archive" to Icons.Rounded.FolderZip,
        "zip" to Icons.Rounded.FolderZip,
        "code" to Icons.Rounded.Code,
        "image" to Icons.Rounded.Image,
        "video" to Icons.Rounded.Movie,
        "audio" to Icons.Rounded.Audiotrack,
        "pdf" to Icons.Rounded.PictureAsPdf,
        "font" to Icons.Rounded.FontDownload,
        "apk" to Icons.Rounded.Android,
    )

    // drawable 资源型图标（无法放进 ImageVector 常量表）
    private val BUILT_IN_DRAWABLE: Map<String, Int> = mapOf(
        "python" to dev.cluvex.strata.R.drawable.ic_python_logo,
    )

    fun vectorOf(spec: String?): ImageVector? {
        if (spec.isNullOrBlank()) return null
        return BUILT_IN[spec.lowercase()]
    }

    fun drawableOf(spec: String?): Int? {
        if (spec.isNullOrBlank()) return null
        return BUILT_IN_DRAWABLE[spec.lowercase()]
    }

    fun bitmapOf(spec: String?): ImageBitmap? {
        if (spec.isNullOrBlank()) return null
        if (BUILT_IN.containsKey(spec.lowercase())) return null
        if (BUILT_IN_DRAWABLE.containsKey(spec.lowercase())) return null
        if (!StgxIconLoader.isBase64Spec(spec)) return null
        return StgxIconLoader.load(spec)
    }

    fun defaultVector(): ImageVector = Icons.Rounded.Code
    fun vectorOfExtension(ext: String): ImageVector? = vectorOf(StgxRegistry.iconForExtension(ext))
    fun bitmapOfExtension(ext: String): ImageBitmap? = bitmapOf(StgxRegistry.iconForExtension(ext))
    fun drawableOfExtension(ext: String): Int? = drawableOf(StgxRegistry.iconForExtension(ext))

    // 统一入口：.stgx 的 icon 优先，原生 .py 兜底为 "python"
    fun drawableOfFile(name: String): Int? {
        val ext = name.substringAfterLast('.', "").lowercase()
        val spec = StgxRegistry.iconForExtension(ext) ?: if (ext == "py") "python" else null
        return drawableOf(spec)
    }
}
