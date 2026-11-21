package dev.cluvex.strata.core.text

import android.content.Context
import android.util.Log
import java.io.File

data class StgxEntry(
    val grammar: StgxGrammar,
    val scope: String,
    val textMateJson: String,
    val isBuiltIn: Boolean,
    val file: File?,
)

data class StgxFailure(val file: File, val reason: String)

object StgxRegistry {
    @Volatile private var loaded = false
    private val entries = mutableListOf<StgxEntry>()
    private val failures = mutableListOf<StgxFailure>()
    private val extToEntry = mutableMapOf<String, StgxEntry>()

    val importedGrammars: List<StgxGrammar> get() = entries.filter { !it.isBuiltIn }.map { it.grammar }
    val builtInGrammars: List<StgxGrammar> get() = entries.filter { it.isBuiltIn }.map { it.grammar }
    val failedFiles: List<File> get() = failures.map { it.file }
    fun failedEntries(): List<StgxFailure> = failures.toList()

    fun isLoaded(): Boolean = loaded
    fun allEntries(): List<StgxEntry> = entries.toList()

    fun load(context: Context) = synchronized(this) {
        if (loaded) return
        val byId = LinkedHashMap<String, StgxEntry>()
        val newFailures = mutableListOf<StgxFailure>()

        fun addOne(label: String, file: File?, isBuiltIn: Boolean, text: String) {
            StgxParser.parse(text)
                .onSuccess { g ->
                    val entry = StgxEntry(g, StgxCompiler.scopeOf(g), StgxCompiler.compile(g), isBuiltIn, file)
                    val existing = byId[g.id]
                    when {
                        existing == null -> byId[g.id] = entry
                        // 用户目录覆盖同 id 内置
                        existing.isBuiltIn && !isBuiltIn -> byId[g.id] = entry
                        // 同层重复：先加载的胜出，后者进 failures
                        else -> newFailures.add(StgxFailure(file ?: File(label), "duplicate id: ${g.id}"))
                    }
                }
                .onFailure { e ->
                    newFailures.add(StgxFailure(file ?: File(label), e.message ?: e.javaClass.simpleName))
                }
        }

        runCatching {
            context.assets.list("grammars")?.forEach { name ->
                if (!name.endsWith(".stgx")) return@forEach
                val text = context.assets.open("grammars/$name").bufferedReader().use { it.readText() }
                addOne("assets/grammars/$name", null, true, text)
            }
        }.onFailure { Log.w("StgxRegistry", "listing built-in failed", it) }

        val userDir = File(context.filesDir, "stgx_extensions")
        if (userDir.isDirectory) {
            userDir.listFiles { f -> f.extension.lowercase() == "stgx" }?.forEach { file ->
                runCatching { file.readText() }
                    .onSuccess { addOne(file.path, file, false, it) }
                    .onFailure { newFailures.add(StgxFailure(file, "read failed: ${it.message}")) }
            }
        }

        entries.clear(); entries.addAll(byId.values)
        failures.clear(); failures.addAll(newFailures)
        extToEntry.clear()
        for (e in entries) for (ext in e.grammar.extensions) extToEntry[ext.lowercase()] = e
        loaded = true
        if (failures.isNotEmpty()) Log.w("StgxRegistry", "failed: ${failures.size}")
    }

    fun reload(context: Context) { synchronized(this) { loaded = false }; load(context) }

    fun entryForExtension(ext: String): StgxEntry? = extToEntry[ext.lowercase()]
    fun entryForFile(name: String): StgxEntry? {
        val ext = name.substringAfterLast('.', "").lowercase()
        return if (ext.isEmpty()) null else extToEntry[ext]
    }
    fun iconForExtension(ext: String): String? = entryForExtension(ext)?.grammar?.icon

    fun tintOfExtension(ext: String): dev.cluvex.strata.core.util.FileKind? {
        val name = entryForExtension(ext)?.grammar?.iconTint ?: return null
        return runCatching { dev.cluvex.strata.core.util.FileKind.valueOf(name.uppercase()) }.getOrNull()
    }

    fun hueOfExtension(ext: String): Int? = entryForExtension(ext)?.grammar?.hue
    fun iconForFile(name: String): String? = entryForFile(name)?.grammar?.icon
    fun scopeForFile(name: String): String? = entryForFile(name)?.scope

    // 这里是我们唯一新增的：用于获取自定义颜色的方法
    fun colorsOfExtension(ext: String): Triple<androidx.compose.ui.graphics.Color?, androidx.compose.ui.graphics.Color?, androidx.compose.ui.graphics.Color?> {
        val entry = entryForExtension(ext) ?: return Triple(null, null, null)
        val g = entry.grammar
        fun parse(c: String?): androidx.compose.ui.graphics.Color? {
            if (c.isNullOrBlank()) return null
            return try {
                androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(c))
            } catch (e: Exception) { null }
        }
        return Triple(parse(g.iconBackground), parse(g.iconBackgroundDark), parse(g.iconForeground))
    }
}