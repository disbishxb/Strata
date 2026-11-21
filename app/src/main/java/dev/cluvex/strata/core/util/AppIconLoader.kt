package dev.cluvex.strata.core.util

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.LruCache
import java.io.File

object AppIconLoader {
    private val cache = LruCache<String, Bitmap>(100)

    private val packageRegexes = listOf(
        Regex(".*/Android/(?:data|obb|media)/([a-zA-Z][a-zA-Z0-9_]*(?:\\.[a-zA-Z0-9_]+)+)$"),
        Regex(".*/data/(?:data|user(?:/\\d+|_de/\\d+)?)/([a-zA-Z][a-zA-Z0-9_]*(?:\\.[a-zA-Z0-9_]+)+)$"),
        Regex(".*/data/(?:app-staging|app-private)/([a-zA-Z][a-zA-Z0-9_]*(?:\\.[a-zA-Z0-9_]+)+)(?:-[a-zA-Z0-9]+)?$"),
    )

    suspend fun loadIcon(context: Context, path: String): Bitmap? {
        val clean = path.trimEnd('/')
        cache.get(clean)?.let { return it }

        if (clean.endsWith(".apk", ignoreCase = true) || clean.endsWith(".apk.1", ignoreCase = true)) {
            val bmp = loadApkIcon(context, clean) ?: return null
            cache.put(clean, bmp)
            return bmp
        }

        var pkg: String? = null
        for (regex in packageRegexes) {
            val match = regex.matchEntire(clean)
            if (match != null) { pkg = match.groupValues[1]; break }
        }
        if (pkg == null) return null

        return try {
            context.packageManager.getApplicationInfo(pkg, 0)
            val drawable = context.packageManager.getApplicationIcon(pkg)
            drawable.toBitmap(128, 128)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    private fun loadApkIcon(context: Context, apkPath: String): Bitmap? {
        // 受限目录读不到文件：返回 null，调用方用默认 KindIcon 兜底
        if (!File(apkPath).canRead()) return null
        return loadApkIconDirect(context, apkPath)
    }

    private fun loadApkIconDirect(context: Context, apkPath: String): Bitmap? {
        return try {
            val pm = context.packageManager
            @Suppress("DEPRECATION")
            val info = pm.getPackageArchiveInfo(apkPath, 0) ?: return null
            val appInfo = info.applicationInfo ?: return null
            appInfo.sourceDir = apkPath
            appInfo.publicSourceDir = apkPath
            appInfo.loadIcon(pm).toBitmap(128, 128)
        } catch (e: Exception) {
            null
        }
    }

    private fun Drawable.toBitmap(width: Int, height: Int): Bitmap {
        if (this is BitmapDrawable) return bitmap
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, width, height)
        draw(canvas)
        return bitmap
    }
}
