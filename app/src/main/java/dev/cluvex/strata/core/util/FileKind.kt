package dev.cluvex.strata.core.util

import androidx.compose.ui.res.vectorResource
import androidx.compose.material.icons.rounded.Terminal

import androidx.annotation.StringRes
import dev.cluvex.strata.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.automirrored.rounded.TextSnippet
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.Audiotrack
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DataObject
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderZip
import androidx.compose.material.icons.rounded.FontDownload
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.ui.graphics.vector.ImageVector
import dev.cluvex.strata.core.model.FileItem

enum class FileKind {
    FOLDER, IMAGE, VIDEO, AUDIO, ARCHIVE, APK, CODE, SHELL, PYTHON, TEXT, JSON, PDF, FONT, DOCUMENT, ENCRYPTED, BINARY, SIGNING_KEY;

    val icon: ImageVector

        get() = when (this) {
            FOLDER -> Icons.Rounded.Folder
            IMAGE -> Icons.Rounded.Image
            VIDEO -> Icons.Rounded.Movie
            AUDIO -> Icons.Rounded.Audiotrack
            ARCHIVE -> Icons.Rounded.FolderZip
            APK -> Icons.Rounded.Android
            CODE -> Icons.Rounded.Code
            SHELL -> Icons.Rounded.Terminal
            PYTHON -> Icons.Rounded.Code
            TEXT -> Icons.Rounded.Description
            JSON -> Icons.Rounded.DataObject
            PDF -> Icons.Rounded.PictureAsPdf
            FONT -> Icons.Rounded.FontDownload
            DOCUMENT -> Icons.Rounded.Description
            ENCRYPTED -> Icons.Rounded.Lock
            BINARY -> Icons.AutoMirrored.Rounded.InsertDriveFile
            SIGNING_KEY -> Icons.Rounded.Key
        }

    @get:StringRes
    val label: Int
        get() = when (this) {
            FOLDER -> R.string.common_folder
            IMAGE -> R.string.model_open_as_image
            VIDEO -> R.string.model_open_as_video
            AUDIO -> R.string.model_open_as_audio
            ARCHIVE -> R.string.files_kind_archive
            APK -> R.string.files_kind_apk
            CODE -> R.string.files_kind_code
            SHELL -> R.string.files_kind_code
            PYTHON -> R.string.files_kind_code
            TEXT -> R.string.model_open_as_text
            JSON -> R.string.files_kind_code
            PDF -> R.string.files_kind_pdf
            FONT -> R.string.files_kind_font
            DOCUMENT -> R.string.files_kind_document
            ENCRYPTED -> R.string.files_kind_encrypted
            BINARY -> R.string.common_file
            SIGNING_KEY -> R.string.model_opener_signing_key
        }
}

val customFileIcons = mutableMapOf<String, ImageVector>()

object FileKinds {

    private val image = setOf("png", "jpg", "jpeg", "gif", "webp", "bmp", "heic", "heif", "ico", "svg")
    private val video = setOf("mp4", "mkv", "webm", "avi", "mov", "3gp", "flv", "ts", "m4v")
    private val audio = setOf("mp3", "flac", "wav", "ogg", "m4a", "aac", "opus", "mid", "amr")
    private val archive = setOf(
        "zip", "rar", "7z", "tar", "gz", "tgz", "bz2", "xz", "zst", "zstd", "lz4",
        "jar", "apks", "xapk", "apkm", "war", "mtz",
        "001", "002", "003", "004", "005", "006", "007", "008", "009", "r00", "r01",
    )
    private val code = setOf(
        "kt", "kts", "java", "c", "cc", "cpp", "h", "hpp", "cs", "go", "rs", "swift",
        "rb", "js", "ts", "tsx", "jsx", "php", "lua", "dart", "bat",
        "gradle", "smali", "asm", "sql", "html", "css", "scss", "vue", "zig", "stgx",
    )
    private val python = setOf("py", "pyi", "pyt", "pyw")
    private val shell = setOf(
        "sh", "bash", "zsh", "csh", "ksh", "fish", "ash", "rc",
        "bashrc", "bash_profile", "bash_login", "bash_logout", "bash_aliases",
        "zshrc", "zshenv", "zprofile", "zlogin", "zlogout",
        "profile", "cshrc", "kshrc", "fishrc", "ebuild", "eclass",
    )
    private val text = setOf(
        "txt", "md", "xml", "yml", "yaml", "toml", "ini", "cfg", "conf",
        "properties", "prop", "log", "csv", "tsv", "mtl", "mtsx", "diff", "patch",
    )
    private val font = setOf("ttf", "otf", "woff", "woff2", "ttc")
    private val keyFiles = setOf("jks", "keystore", "bks", "p12", "pfx", "pk8")
    private val document = setOf("doc", "docx", "xls", "xlsx", "ppt", "pptx", "odt", "rtf", "epub")

    fun getCustomIcon(ext: String): ImageVector? =
        customFileIcons[ext.lowercase()] ?: dev.cluvex.strata.core.text.StgxIcons.vectorOfExtension(ext.lowercase())

    fun of(item: FileItem): FileKind {

        if (item.isDirectory) return FileKind.FOLDER
        val ext = item.extension
        return when {
            item.name.endsWith(".apk.1", ignoreCase = true) -> FileKind.APK
            ext == "apk" -> FileKind.APK
            ext == "json" -> FileKind.JSON
            ext == "pdf" -> FileKind.PDF
            ext == "senc" -> FileKind.ENCRYPTED
            ext in keyFiles || item.name.endsWith(".x509.pem", ignoreCase = true) -> FileKind.SIGNING_KEY
            ext in image -> FileKind.IMAGE
            ext in video -> FileKind.VIDEO
            ext in audio -> FileKind.AUDIO
            ext in archive -> FileKind.ARCHIVE
            ext in python -> FileKind.PYTHON
            ext in shell -> FileKind.SHELL
            ext in code -> FileKind.CODE
            ext in text -> FileKind.TEXT
            ext in font -> FileKind.FONT
            ext in document -> FileKind.DOCUMENT
            else -> FileKind.BINARY
        }
    }

    fun isImage(item: FileItem) = of(item) == FileKind.IMAGE
    fun isVideo(item: FileItem) = of(item) == FileKind.VIDEO
    fun isThumbnailable(item: FileItem) =
        isImage(item) || isVideo(item) || item.extension == "apk" || item.name.endsWith(".apk.1", ignoreCase = true)
    fun isTextEditable(item: FileItem): Boolean {
        val k = of(item)
        return k == FileKind.TEXT || k == FileKind.CODE || k == FileKind.SHELL || k == FileKind.PYTHON || k == FileKind.JSON
    }
    fun isArchive(item: FileItem) = of(item) == FileKind.ARCHIVE
}
