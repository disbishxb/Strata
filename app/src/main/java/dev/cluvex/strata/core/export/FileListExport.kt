package dev.cluvex.strata.core.export

import dev.cluvex.strata.R
import dev.cluvex.strata.core.archive.ArchiveEntry
import dev.cluvex.strata.core.i18n.tr
import dev.cluvex.strata.core.model.FileItem
import dev.cluvex.strata.core.util.Formatters
import java.io.IOException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

data class FileListRow(
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val time: Long,
    val depth: Int,
)

data class FileListExport(
    val sourceLabel: String,
    val isArchive: Boolean,
    val rows: List<FileListRow>,
    val generatedAt: Long = System.currentTimeMillis(),
    val skipped: List<String> = emptyList(),
)

enum class FileListFormat { MARKDOWN, CSV }

object FileListExporter {

    private const val MAX_ENTRIES = 300_000
    private const val PROGRESS_STEP = 512
    private const val PREVIEW_ROWS = 400

    /** 目录列表：level1Only = 只列一级内容、不下钻；recursive = 全部下钻；两者都 false 也只列一级。 */
    suspend fun collectDirectory(
        rootPath: String,
        absolutePath: Boolean,
        recursive: Boolean,
        level1Only: Boolean,
        appendSlash: Boolean,
        lister: suspend (String) -> List<FileItem>?,
        onProgress: suspend (scanned: Int) -> Unit = {},
    ): FileListExport {
        val rows = ArrayList<FileListRow>()
        val skipped = ArrayList<String>()
        val visited = HashSet<String>()
        val prefix = rootPath.trimEnd('/')

        suspend fun walk(dir: String, rel: String, depth: Int) {
            currentCoroutineContext().ensureActive()
            if (rows.size >= MAX_ENTRIES) return
            val key = dir.trimEnd('/').ifEmpty { "/" }
            if (!visited.add(key)) return
            val children = lister(dir)
            if (children == null) {
                if (depth == 0) throw IOException("无法读取目录: $dir")
                skipped += dir
                return
            }
            val sorted = children.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            for (f in sorted) {
                currentCoroutineContext().ensureActive()
                if (rows.size >= MAX_ENTRIES) return
                val childRel = if (rel.isEmpty()) f.name else "$rel/${f.name}"
                val displayPath = if (absolutePath) {
                    if (prefix.isEmpty()) "/${f.name}" else "$prefix/$childRel"
                } else {
                    childRel
                }
                var finalPath = displayPath
                if (f.isDirectory && appendSlash) finalPath += "/"

                rows += FileListRow(
                    path = finalPath,
                    isDirectory = f.isDirectory,
                    size = if (f.isDirectory) -1 else f.size,
                    time = f.lastModified,
                    depth = depth + 1,
                )
                if (rows.size % PROGRESS_STEP == 0) onProgress(rows.size)

                if (f.isDirectory) {
                    if (level1Only) continue
                    if (!recursive) continue
                    walk(f.path, childRel, depth + 1)
                }
            }
        }
        walk(rootPath.trimEnd('/').ifEmpty { "/" }, "", 0)
        onProgress(rows.size)
        return FileListExport(rootPath, isArchive = false, rows = rows, skipped = skipped)
    }

    /** 归档列表：level1Only / recursive 语义与目录一致——只保留根下直接子项。 */
    fun collectArchive(
        entries: List<ArchiveEntry>,
        archivePath: String,
        absolutePath: Boolean,
        recursive: Boolean,
        level1Only: Boolean,
        appendSlash: Boolean,
    ): FileListExport {
        val prefix = archivePath.trimEnd('/')
        val rows = entries.take(MAX_ENTRIES).mapNotNull { e ->
            val p = e.path.trim('/')
            val depth = p.count { it == '/' }

            if (level1Only && depth > 0) return@mapNotNull null
            if (!recursive && !level1Only && depth > 0) return@mapNotNull null

            val displayPath = if (absolutePath) "$prefix/$p" else p
            var finalPath = displayPath
            if (e.isDirectory && appendSlash && !finalPath.endsWith("/")) finalPath += "/"

            FileListRow(
                path = finalPath,
                isDirectory = e.isDirectory,
                size = if (e.isDirectory) -1 else e.size,
                time = e.time,
                depth = depth,
            )
        }
        return FileListExport(archivePath, isArchive = true, rows = rows)
    }

    /** 选中项导出：如果是目录，则递归扫描其内容。 */
    suspend fun collectSelection(
        items: List<FileItem>,
        absolutePath: Boolean,
        recursive: Boolean,
        level1Only: Boolean,
        appendSlash: Boolean,
        lister: suspend (String) -> List<FileItem>?,
        onProgress: suspend (scanned: Int) -> Unit = {},
    ): FileListExport {
        val allRows = ArrayList<FileListRow>()
        val allSkipped = ArrayList<String>()

        for (item in items) {
            val p = if (absolutePath) item.path else item.name
            var finalPath = p
            if (item.isDirectory && appendSlash) finalPath += "/"
            
            allRows += FileListRow(finalPath, item.isDirectory, if (item.isDirectory) -1 else item.size, item.lastModified, 0)
            
            if (item.isDirectory && recursive) {
                val subExport = try {
                    collectDirectory(item.path, absolutePath, recursive, level1Only, appendSlash, lister, onProgress)
                } catch (e: IOException) {
                    FileListExport("", false, emptyList(), skipped = listOf(item.path))
                }
                allSkipped.addAll(subExport.skipped)
                // 将子目录里的相对路径补全为完整路径
                val prefix = p.trimEnd('/') + "/"
                val subRows = subExport.rows.map { r ->
                    if (r.path.startsWith(prefix)) r else r.copy(path = prefix + r.path.trimStart('/'))
                }
                allRows.addAll(subRows)
            }
        }
        return FileListExport("selection", isArchive = false, rows = allRows, skipped = allSkipped)
    }

    fun render(ex: FileListExport): String = buildString { renderTo(ex, this) }

    fun preview(ex: FileListExport, format: FileListFormat): String = buildString {
        when (format) {
            FileListFormat.MARKDOWN -> renderTo(ex, this, maxPreviewRows = PREVIEW_ROWS)
            FileListFormat.CSV -> {
                renderCsvTo(ex, this)
                // 截断过长的 CSV 预览，防止卡顿
                val lines = this.toString().lines()
                if (lines.size > PREVIEW_ROWS) {
                    clear()
                    append(lines.take(PREVIEW_ROWS).joinToString("\n"))
                    append("\n... (已截断，共 ").append(lines.size).append(" 行)")
                }
            }
        }
    }

    fun renderTo(ex: FileListExport, out: Appendable, maxPreviewRows: Int = Int.MAX_VALUE) {
        val files = ex.rows.filter { !it.isDirectory }
        val dirCount = ex.rows.count { it.isDirectory }
        val total = files.sumOf { it.size.coerceAtLeast(0) }
        val typeStr = tr(if (ex.isArchive) R.string.export_list_kind_archive else R.string.export_list_kind_dir)
        val dateStr = Formatters.date(ex.generatedAt)
        val sizeStr = Formatters.size(total)

        out.append("# Strata 文件列表\n")
        out.append("# 类型: ").append(typeStr).append('\n')
        out.append("# 时间: ").append(dateStr).append('\n')
        out.append("# 总计: ").append(ex.rows.size.toString())
            .append(" 项 (目录 ").append(dirCount.toString())
            .append(", 文件 ").append(files.size.toString())
            .append("), 大小 ").append(sizeStr).append('\n')
        out.append('\n')
        out.append("## 目录结构\n")
        out.append("```text\n")
        renderTree(ex, out, maxPreviewRows)
        out.append("```\n")
        if (ex.skipped.isNotEmpty()) {
            out.append('\n')
            out.append("## 未能读取\n")
            ex.skipped.take(50).forEach { out.append("- ").append(it).append('\n') }
            if (ex.skipped.size > 50) {
                out.append("- … 共 ").append(ex.skipped.size.toString()).append(" 项\n")
            }
        }
    }

    fun renderCsvTo(ex: FileListExport, out: Appendable) {
        fun esc(s: String): String =
            if (s.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
                "\"" + s.replace("\"", "\"\"") + "\""
            } else s
        out.append("path,is_directory,size,modified\n")
        for (r in ex.rows) {
            out.append(esc(r.path)).append(',')
                .append(if (r.isDirectory) "1" else "0").append(',')
                .append(r.size.coerceAtLeast(0).toString()).append(',')
                .append(Formatters.isoTime(r.time)).append('\n')
        }
        if (ex.skipped.isNotEmpty()) {
            out.append('\n')
            out.append("# 未能读取:\n")
            ex.skipped.forEach { out.append("# ").append(esc(it)).append('\n') }
        }
    }

    private fun renderTree(ex: FileListExport, out: Appendable, maxRows: Int) {
        if (ex.rows.isEmpty()) return

        data class Node(
            val name: String,
            val isDirectory: Boolean,
            val size: Long,
            val children: MutableMap<String, Node> = LinkedHashMap(),
        )

        val label = ex.sourceLabel.trimEnd('/')
        val rootName = java.io.File(label).name.ifEmpty { "源" }

        val roots = LinkedHashMap<String, Node>()

        for (r in ex.rows) {
            val t = r.path.trim('/')
            // 彻底移除原来的简单 contains 替换逻辑，改为完整拆分路径
            val parts = t.split('/').filter { it.isNotEmpty() }
            if (parts.isEmpty()) continue

            var current = roots.getOrPut(parts[0]) {
                if (parts.size == 1) Node(parts[0], r.isDirectory, r.size)
                else Node(parts[0], true, -1)
            }
            for (i in 1 until parts.size) {
                val part = parts[i]
                val isLast = i == parts.lastIndex
                current = current.children.getOrPut(part) {
                    Node(part, if (isLast) r.isDirectory else true, if (isLast) r.size else -1)
                }
            }
        }

        fun getWidth(nodes: Collection<Node>, prefixLen: Int): Int {
            var maxW = prefixLen + 4
            for (n in nodes) {
                if (!n.isDirectory) maxW = maxOf(maxW, prefixLen + n.name.length)
                else maxW = maxOf(maxW, getWidth(n.children.values, prefixLen + 4))
            }
            return minOf(maxW, 60)
        }
        val maxNameWidth = getWidth(roots.values, 0)

        var emitted = 0
        fun rec(nodes: Collection<Node>, prefix: String, isRootList: Boolean) {
            val list = nodes.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            list.forEachIndexed { i, n ->
                if (emitted >= maxRows) return
                val last = i == list.lastIndex
                // 如果是根列表，就不画连接符前缀，直接显示名字
                val connector = if (isRootList) "" else if (last) "└── " else "├── "
                val displayName = if (n.isDirectory) n.name + "/" else n.name
                val sizeStr = if (n.isDirectory) "" else {
                    val padding = " ".repeat(maxOf(2, maxNameWidth - displayName.length))
                    padding + "(" + Formatters.size(n.size.coerceAtLeast(0)) + ")"
                }
                out.append(prefix).append(connector).append(displayName).append(sizeStr).append('\n')
                emitted++
                rec(n.children.values, prefix + (if (isRootList) "" else if (last) "    " else "│   "), false)
            }
        }
        // 根节点不显示连接线
        rec(roots.values, "", true)
        
        if (maxRows != Int.MAX_VALUE && emitted >= maxRows) {
            out.append("… 预览已截断\n")
        }
    }
}