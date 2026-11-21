package dev.cluvex.strata.core.text

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

object StgxIconLoader {
    private const val MAX_BYTES = 1_000_000
    private val cache = object : LruCache<String, ImageBitmap?>(32) {
        override fun sizeOf(key: String, value: ImageBitmap?): Int = 1
    }

    fun isBase64Spec(spec: String): Boolean =
        spec.startsWith("data:image/") ||
            (spec.length > 64 && spec.all { it.isLetterOrDigit() || it in "+/=\n\r" })

    fun load(spec: String): ImageBitmap? {
        cache.get(spec)?.let { return it }
        val result = runCatching {
            val b64 = if (spec.startsWith("data:")) spec.substringAfter(',') else spec
            val bytes = Base64.decode(b64, Base64.DEFAULT)
            if (bytes.size > MAX_BYTES) { Log.w("StgxIconLoader", "too large: ${bytes.size}"); return null }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }.onFailure { Log.w("StgxIconLoader", "decode failed", it) }.getOrNull()
        cache.put(spec, result)
        return result
    }
}
