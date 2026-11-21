package dev.cluvex.strata.core.fs

import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel

class SplitChannel(
    private val baseName: String,
    private val parentDir: File,
    private val access: FileAccess = LocalFileAccess
) : SeekableByteChannel {

    private val files: List<File> by lazy {
        val list = mutableListOf<File>()
        var part = 1
        while (true) {
            val partFile = File(parentDir, String.format("%s.%03d", baseName, part))
            if (!partFile.exists()) break
            list += partFile
            part += 1
        }
        if (list.isEmpty()) throw IOException("No split files found for $baseName")
        list
    }

    private val channels = arrayOfNulls<SeekableByteChannel>(files.size)
    private val offsets = LongArray(files.size)
    private var totalSize: Long = 0
    private var position: Long = 0

    init {
        var offset = 0L
        for (i in files.indices) {
            val ch = access.openReadChannel(files[i])
            channels[i] = ch
            offsets[i] = offset
            offset += ch.size()
        }
        totalSize = offset
    }

    override fun read(dst: ByteBuffer): Int {
        if (position >= totalSize) return -1
        var bytesRead = 0
        while (dst.hasRemaining()) {
            val index = findIndex(position)
            if (index == -1) break
            
            val ch = channels[index]!!
            ch.position(position - offsets[index])
            val toRead = minOf(dst.remaining().toLong(), (offsets.getOrNull(index + 1) ?: totalSize) - position).toInt()
            
            val limit = dst.limit()
            dst.limit(dst.position() + toRead)
            val n = ch.read(dst)
            dst.limit(limit)
            
            if (n <= 0) break
            bytesRead += n
            position += n
        }
        return if (bytesRead == 0) -1 else bytesRead
    }

    private fun findIndex(pos: Long): Int {
        for (i in offsets.indices.reversed()) {
            if (pos >= offsets[i]) return i
        }
        return 0
    }

    override fun write(src: ByteBuffer?): Int = throw IOException("Read-only")
    override fun position(): Long = position
    override fun position(newPosition: Long): SeekableByteChannel {
        if (newPosition > totalSize) throw IOException("Position out of bounds")
        position = newPosition
        return this
    }
    override fun size(): Long = totalSize
    override fun isOpen(): Boolean = channels.any { it?.isOpen == true }
    override fun close() = channels.forEach { it?.close() }
    override fun truncate(size: Long): SeekableByteChannel = throw IOException("Read-only")
}
