package dev.cluvex.strata.core.model

import dev.cluvex.strata.core.fs.FileAccess
import dev.cluvex.strata.core.fs.LocalFileAccess
import java.io.File

data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val isHidden: Boolean,
    val canRead: Boolean,
    val canWrite: Boolean,
    val isSymlink: Boolean = false,
    val childCount: Int = -1,
    val access: FileAccess = LocalFileAccess,
) {
    val extension: String
        get() = if (isDirectory) "" else name.substringAfterLast('.', "").lowercase()

    val parentPath: String?
        get() = File(path).parent

    companion object {
        fun of(file: File, access: FileAccess = LocalFileAccess): FileItem {
            return FileItem(
                name = file.name,
                path = file.absolutePath,
                isDirectory = file.isDirectory,
                size = if (file.isDirectory) 0L else file.length(),
                lastModified = file.lastModified(),
                isHidden = file.isHidden || file.name.startsWith('.'),
                canRead = file.canRead() || access.isRoot,
                canWrite = file.canWrite() || access.isRoot,
                isSymlink = dev.cluvex.strata.core.fs.FsUtil.isSymlink(file),
                access = access,
            )
        }
    }
}
