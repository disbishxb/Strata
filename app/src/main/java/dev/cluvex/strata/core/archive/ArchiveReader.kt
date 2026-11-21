package dev.cluvex.strata.core.archive

import dev.cluvex.strata.core.root.RootManager
import android.content.Context
import dev.cluvex.strata.R
import dev.cluvex.strata.core.fs.FileAccess
import dev.cluvex.strata.core.fs.LocalFileAccess
import dev.cluvex.strata.core.fs.SplitChannel
import dev.cluvex.strata.core.i18n.tr
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStream
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorInputStream
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel
import java.util.Collections

data class ArchiveEntry(
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val compressedSize: Long = -1,
    val time: Long = 0,
    val encrypted: Boolean = false,
    val stored: Boolean = false,
) {
    val name: String get() = path.substringAfterLast('/')
    val parent: String get() = path.substringBeforeLast('/', "")
}

class ArchiveTree(entries: List<ArchiveEntry>) {
    private val byPath: Map<String, ArchiveEntry>
    private val children: Map<String, List<ArchiveEntry>>

    init {
        val all = LinkedHashMap<String, ArchiveEntry>()
        val synthesized = HashSet<String>()
        for (e in entries) {
            val clean = normalize(e.path)
            if (clean.isEmpty()) continue
            val existing = all[clean]
            if (existing == null || clean in synthesized || (existing.isDirectory && !e.isDirectory)) {
                all[clean] = e.copy(path = clean)
                synthesized -= clean
            }
            var parent = clean.substringBeforeLast('/', "")
            while (parent.isNotEmpty() && parent !in all) {
                all[parent] = ArchiveEntry(parent, isDirectory = true, size = 0)
                synthesized += parent
                parent = parent.substringBeforeLast('/', "")
            }
        }
        byPath = all
        children = all.values.groupBy { it.parent }
    }

    val size: Int get() = byPath.size
    fun list(dir: String): List<ArchiveEntry> = children[normalize(dir)].orEmpty()
    operator fun get(path: String): ArchiveEntry? = byPath[normalize(path)]
    fun isDirectory(path: String): Boolean = normalize(path).isEmpty() || byPath[normalize(path)]?.isDirectory == true

    fun files(path: String): List<ArchiveEntry> {
        val p = normalize(path)
        val prefix = "$p/"
        return byPath.values.filter { !it.isDirectory && (p.isEmpty() || it.path == p || it.path.startsWith(prefix)) }
    }

    fun sizeOf(path: String): Long = files(path).sumOf { it.size.coerceAtLeast(0) }

    companion object {
        fun normalize(path: String): String =
            path.replace('\\', '/').split('/').filter { it.isNotEmpty() && it != "." }.joinToString("/")
    }
}

object ArchiveReader {

    private val zipSuffixes = listOf(".zip", ".jar", ".apk", ".apks", ".xapk", ".apkm", ".war", ".aar", ".mtz")
    private val tarballSuffixes = listOf(".tar.gz", ".tgz", ".tar.bz2", ".tbz2", ".tar.xz", ".txz", ".tar.zst", ".tar.zstd", ".tar.lz4", ".tar")
    private val singleSuffixes = listOf(".gz", ".bz2", ".xz", ".zst", ".zstd", ".lz4")

    fun isZip(name: String) = zipSuffixes.any { name.lowercase().endsWith(it) }

    fun supports(name: String): Boolean {
        val n = name.lowercase()
        if (isZip(n) || n.endsWith(".7z") || RarArchive.isRar(n) || tarballSuffixes.any { n.endsWith(it) } || singleSuffixes.any { n.endsWith(it) }) return true
        return false
    }

    fun canUpdate(name: String) = isZip(name)

    suspend fun list(file: File, password: CharArray? = null, access: FileAccess = LocalFileAccess, context: Context? = null): List<ArchiveEntry> {
        val name = file.name.lowercase()
        
        // 1. 优先处理分卷（纯内存虚拟通道）
        if (file.name.matches(Regex(".*\\.\\d{3}$"))) {
            val baseName = file.name.substringBeforeLast('.')
            val parent = file.parentFile ?: throw IllegalArgumentException("No parent for split")
            return try {
                listFromChannel(SplitChannel(baseName, parent, access), file.name, password, context, file)
            } catch (e: Exception) {
                throw IllegalArgumentException("Failed to read split archives: \${e.message}")
            }
        }

        // 2. 处理普通文件（若直接可读，走本地通道）
        if (file.canRead()) {
            return listFromChannel(access.openReadChannel(file), file.name, password, context, file)
        }

        // 3. Root 权限兜底，直接通过 Root 通道读取，不走缓存文件！
        if (RootManager.isGranted) {
            val rootAccess = RootManager.access() ?: throw IllegalArgumentException("Root unavailable")
            return listFromChannel(rootAccess.openReadChannel(file), file.name, password, context, file)
        }

        throw IllegalArgumentException("Cannot read \${file.name}")
    }

    private suspend fun listFromChannel(channel: SeekableByteChannel, fileName: String, password: CharArray?, context: Context?, file: File): List<ArchiveEntry> {
        val name = fileName.lowercase()
        

        return when {
            isZip(name) -> {
                ZipFile.builder().setSeekableByteChannel(channel).get().use { zip ->
                    Collections.list(zip.entries).map { e ->
                        ArchiveEntry(e.name, e.isDirectory, e.size, e.compressedSize, e.time.coerceAtLeast(0), e.generalPurposeBit.usesEncryption(), e.method == ZipArchiveEntry.STORED)
                    }
                }
            }
            name.endsWith(".7z") -> {
                try {
                    SevenZFile.builder().setSeekableByteChannel(channel)
                        .apply { if (password != null && password.isNotEmpty()) setPassword(password) }
                        .get().use { sevenZ ->
                            sevenZ.entries.map { e ->
                                ArchiveEntry(e.name, e.isDirectory, if (e.hasStream()) e.size else 0, time = if (e.hasLastModifiedDate) e.lastModifiedTime.toMillis() else 0)
                            }
                        }
                } catch (e: org.apache.commons.compress.PasswordRequiredException) {
                    throw PasswordRequiredException(fileName)
                } catch (e: java.io.IOException) {
                    if (password != null && password.isNotEmpty()) throw WrongPasswordException(fileName)
                    throw e
                } finally {
                    SevenZKeys.clear()
                }
            }
            RarArchive.isRar(name) -> {
                throw IllegalArgumentException("RAR require stream capabilities, falling back to file path")
            }
                        tarballSuffixes.any { name.endsWith(it) } -> {
                val input = decompress(name, ChannelInputStream(channel))
                TarArchiveInputStream(input).use { tar ->
                    val out = ArrayList<ArchiveEntry>()
                    var e = tar.nextEntry
                    while (e != null) {
                        out += ArchiveEntry(e.name, e.isDirectory, e.size, time = e.modTime.time)
                        e = tar.nextEntry
                    }
                    out
                }
            }
            singleSuffixes.any { name.endsWith(it) } -> {
                listOf(ArchiveEntry(fileName.substringBeforeLast('.'), false, channel.size(), channel.size(), 0))
            }
            else -> throw IllegalArgumentException("Unsupported archive format: \$fileName")
        }
    }

    private fun decompress(name: String, input: InputStream): InputStream = when {
        name.endsWith(".gz") || name.endsWith(".tgz") -> GzipCompressorInputStream(input)
        name.endsWith(".bz2") || name.endsWith(".tbz2") -> BZip2CompressorInputStream(input)
        name.endsWith(".xz") || name.endsWith(".txz") -> XZCompressorInputStream(input)
        name.endsWith(".zst") || name.endsWith(".zstd") -> ZstdCompressorInputStream(input)
        name.endsWith(".lz4") -> FramedLZ4CompressorInputStream(input)
        else -> input
    }
}


class ChannelInputStream(private val channel: SeekableByteChannel) : InputStream() {
    private val buffer = ByteBuffer.allocate(8192)
    private var remaining = 0

    override fun read(): Int {
        if (remaining == 0) {
            buffer.clear()
            val read = channel.read(buffer)
            if (read <= 0) return -1
            buffer.flip()
            remaining = read
        }
        remaining--
        return buffer.get().toInt() and 0xFF
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (len == 0) return 0
        if (remaining == 0) {
            if (len >= buffer.capacity()) {
                buffer.clear()
                buffer.limit(len)
                val read = channel.read(buffer)
                if (read <= 0) return -1
                buffer.flip()
                buffer.get(b, off, read)
                return read
            }
            buffer.clear()
            val read = channel.read(buffer)
            if (read <= 0) return -1
            buffer.flip()
            remaining = read
        }
        val toRead = minOf(remaining, len)
        buffer.get(b, off, toRead)
        remaining -= toRead
        return toRead
    }

    override fun close() {
        channel.close()
    }
}
