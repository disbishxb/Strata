package dev.cluvex.strata.core.media

import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import dev.cluvex.strata.core.apk.SplitBundles
import java.io.File
import java.util.zip.ZipFile

object BundleIconLoader {
    private val cache = LruCache<String, Bitmap>(50)

    fun load(context: Context, path: String, modified: Long): Bitmap? {
        val key = "$path:$modified"
        cache.get(key)?.let { return it }
        val bmp = runCatching { loadUncached(context, path) }.getOrNull() ?: return null
        cache.put(key, bmp)
        return bmp
    }

    private fun loadUncached(context: Context, path: String): Bitmap? {
        val file = File(path)
        if (!file.canRead()) return null
        val bundle = SplitBundles.read(file)
        val base = bundle.base ?: return null
        val tmp = File(context.cacheDir, "bundle_icon/${System.nanoTime()}-base.apk").apply { parentFile?.mkdirs() }
        return try {
            ZipFile(file).use { zip ->
                val entry = zip.getEntry(base.entry) ?: return null
                zip.getInputStream(entry).use { input ->
                    tmp.outputStream().use { input.copyTo(it) }
                }
            }
            ApkIcons.load(context, tmp.absolutePath)
        } finally {
            tmp.delete()
        }
    }
}
