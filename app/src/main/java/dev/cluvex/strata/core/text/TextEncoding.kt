package dev.cluvex.strata.core.text

import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import org.mozilla.universalchardet.UniversalDetector

enum class LineEnding(val chars: String, val label: String) {
    LF("\n", "LF"),
    CRLF("\r\n", "CRLF"),
    CR("\r", "CR"),
}

data class DecodedText(
    val text: String,
    val charset: Charset,
    val bom: Boolean,
    val lineEnding: LineEnding,
)

data class EncodingChoice(val label: String, val charset: Charset)

object TextEncoding {

    private val UTF_16LE: Charset = Charsets.UTF_16LE
    private val UTF_16BE: Charset = Charsets.UTF_16BE

    val choices: List<EncodingChoice> = listOf(
        "UTF-8", "UTF-16LE", "UTF-16BE", "ASCII", "GB2312", "GB18030", "GBK", "Big5", "Shift_JIS", "EUC-JP", "EUC-KR",
        "windows-1256", "windows-1251", "windows-1252", "ISO-8859-1",
    ).mapNotNull { name ->
        runCatching { Charset.forName(name) }.getOrNull()?.let { EncodingChoice(name, it) }
    }.distinctBy { it.label }

    // 自动检测/未指定 label 时用：优先匹配与 charset.name() 同名的 label（GBK→GBK），
    // 否则退回第一个同 charset 的 label（GB2312 场景），最后退回 displayName()。
    fun labelOf(cs: Charset): String =
        choices.firstOrNull { it.charset == cs && it.label == cs.name() }?.label
            ?: choices.firstOrNull { it.charset == cs }?.label
            ?: cs.displayName()


    fun decode(bytes: ByteArray, charset: Charset? = null): DecodedText {
        val (detected, bomLength) = if (charset != null) charset to bomLength(bytes, charset) else detect(bytes)
        val body = ByteBuffer.wrap(bytes, bomLength, bytes.size - bomLength)
        val text = detected.newDecoder()
            .onMalformedInput(CodingErrorAction.REPLACE)
            .onUnmappableCharacter(CodingErrorAction.REPLACE)
            .decode(body)
            .toString()
        return DecodedText(text, detected, bomLength > 0, lineEnding(text))
    }

    fun detect(bytes: ByteArray): Pair<Charset, Int> {
        if (startsWith(bytes, 0xEF, 0xBB, 0xBF)) return Charsets.UTF_8 to 3
        if (startsWith(bytes, 0xFF, 0xFE)) return UTF_16LE to 2
        if (startsWith(bytes, 0xFE, 0xFF)) return UTF_16BE to 2
        if (isValidUtf8(bytes)) return Charsets.UTF_8 to 0
        looksLikeWindows1252(bytes)?.let { return it to 0 }
        probeCharset(bytes)?.let { return it to 0 }
        return Charsets.ISO_8859_1 to 0
    }

    fun detectSample(bytes: ByteArray, complete: Boolean): Pair<Charset, Int> {
        if (startsWith(bytes, 0xEF, 0xBB, 0xBF)) return Charsets.UTF_8 to 3
        if (startsWith(bytes, 0xFF, 0xFE)) return UTF_16LE to 2
        if (startsWith(bytes, 0xFE, 0xFF)) return UTF_16BE to 2
        val utf8 = if (complete) isValidUtf8(bytes) else isValidUtf8Prefix(bytes)
        if (utf8) return Charsets.UTF_8 to 0
        looksLikeWindows1252(bytes)?.let { return it to 0 }
        if (complete) probeCharset(bytes)?.let { return it to 0 }
        return Charsets.ISO_8859_1 to 0
    }

    fun isValidUtf8Prefix(bytes: ByteArray): Boolean {
        val decoder = Charsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
        val out = java.nio.CharBuffer.allocate(8192)
        val input = ByteBuffer.wrap(bytes)
        while (true) {
            val r = decoder.decode(input, out, false)
            if (r.isError) return false
            if (r.isOverflow) { out.clear(); continue }
            return true
        }
    }

    fun isValidUtf8(bytes: ByteArray): Boolean = try {
        Charsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes))
        true
    } catch (e: CharacterCodingException) {
        false
    }

    fun encode(text: String, charset: Charset, bom: Boolean): ByteArray {
        val body = text.toByteArray(charset)
        if (!bom) return body
        val mark = when (charset) {
            Charsets.UTF_8 -> byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
            UTF_16LE -> byteArrayOf(0xFF.toByte(), 0xFE.toByte())
            UTF_16BE -> byteArrayOf(0xFE.toByte(), 0xFF.toByte())
            else -> return body
        }
        return mark + body
    }

    fun canEncode(text: String, charset: Charset): Boolean = charset.newEncoder().canEncode(text)

    fun lineEnding(text: String): LineEnding {
        var crlf = 0
        var lf = 0
        var cr = 0
        var i = 0
        while (i < text.length) {
            when (text[i]) {
                '\r' -> if (i + 1 < text.length && text[i + 1] == '\n') { crlf++; i++ } else cr++
                '\n' -> lf++
            }
            i++
        }
        return when {
            crlf >= lf && crlf >= cr && crlf > 0 -> LineEnding.CRLF
            cr > lf -> LineEnding.CR
            else -> LineEnding.LF
        }
    }

    fun convertLineEndings(text: String, ending: LineEnding): String {
        val out = StringBuilder(text.length)
        var i = 0
        while (i < text.length) {
            val c = text[i]
            when (c) {
                '\r' -> {
                    out.append(ending.chars)
                    if (i + 1 < text.length && text[i + 1] == '\n') i++
                }
                '\n' -> out.append(ending.chars)
                else -> out.append(c)
            }
            i++
        }
        return out.toString()
    }

    fun looksBinary(bytes: ByteArray): Boolean {
        if (startsWith(bytes, 0xFF, 0xFE) || startsWith(bytes, 0xFE, 0xFF)) return false
        val n = minOf(bytes.size, 8192)
        for (i in 0 until n) if (bytes[i] == 0.toByte()) return true
        return false
    }

    fun bomLength(bytes: ByteArray, charset: Charset): Int = when (charset) {
        Charsets.UTF_8 -> if (startsWith(bytes, 0xEF, 0xBB, 0xBF)) 3 else 0
        UTF_16LE -> if (startsWith(bytes, 0xFF, 0xFE)) 2 else 0
        UTF_16BE -> if (startsWith(bytes, 0xFE, 0xFF)) 2 else 0
        else -> 0
    }

    // windows-1252 启发式：ISO-8859-1 在 0x80..0x9F 是控制字符，windows-1252 在那里是可打印字符。
    // 若这些字节出现且整体不像 GB 双字节（避免把 GBK 误判），返回 windows-1252。
    private fun looksLikeWindows1252(bytes: ByteArray): Charset? {
        val n = minOf(bytes.size, 8192)
        var hits = 0
        var gbPairs = 0
        var i = 0
        while (i < n) {
            val b = bytes[i].toInt() and 0xFF
            if (b in 0x80..0x9F) hits++
            // GB 双字节：首字节 0x81..0xFE，次字节 0x40..0xFE（排除 0x7F）
            if (b in 0x81..0xFE && i + 1 < n) {
                val b2 = bytes[i + 1].toInt() and 0xFF
                if (b2 in 0x40..0xFE && b2 != 0x7F) { gbPairs++; i++ }
            }
            i++
        }
        if (hits == 0) return null
        // 若 GB 双字节组合明显多于 windows-1252 特征字节，优先认为是 GB 系，交给探测器
        if (gbPairs > hits * 2) return null
        return runCatching { Charset.forName("windows-1252") }.getOrNull()
    }

    // 编码探测。短文本（探测器不可靠）走 GB2312 严格解码优先，与 MT 行为对齐；
    // 长文本用 juniversalchardet。样本截断时由调用方决定是否调用。
    private fun probeCharset(bytes: ByteArray): Charset? {
        if (bytes.isEmpty()) return null
        if (bytes.size < 256) {
            // 短文本：探测器统计不足，容易把 GBK 误判成泰文等双字节编码。
            // 优先尝试 GB2312 严格解码；成功即认定 GB 系（GB2312 ⊂ GBK，显示结果一致）。
            canDecodeStrictly(bytes, Charset.forName("GB2312"))?.let { return it }
            return null
        }
        return runCatching {
            val d = UniversalDetector(null)
            d.handleData(bytes, 0, bytes.size)
            d.dataEnd()
            val name = d.detectedCharset ?: return null
            // 探测器可能返回 GB18030/GBK/GB2312 等，统一 forName 解析；解析失败则放弃
            Charset.forName(name)
        }.getOrNull()
    }

    // 严格解码：所有字节都能按 charset 合法解码才返回 charset，否则 null。
    private fun canDecodeStrictly(bytes: ByteArray, charset: Charset): Charset? = try {
        charset.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes))
        charset
    } catch (_: CharacterCodingException) {
        null
    }

    private fun startsWith(bytes: ByteArray, vararg prefix: Int): Boolean =
        bytes.size >= prefix.size && prefix.indices.all { bytes[it] == prefix[it].toByte() }
}
