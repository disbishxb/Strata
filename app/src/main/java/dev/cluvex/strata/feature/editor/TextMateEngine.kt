package dev.cluvex.strata.feature.editor

import android.content.Context
import android.content.res.AssetManager
import dev.cluvex.strata.core.text.EditorTheme
import dev.cluvex.strata.core.text.GrammarCatalog
import dev.cluvex.strata.core.text.GrammarEntry
import dev.cluvex.strata.core.text.StgxRegistry
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.model.DefaultGrammarDefinition
import io.github.rosemoe.sora.langs.textmate.registry.model.GrammarDefinition
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import org.eclipse.tm4e.core.registry.IGrammarSource
import org.eclipse.tm4e.core.registry.IThemeSource

object TextMateEngine {

    private val lock = Any()

    @Volatile
    private var ready = false
    private lateinit var assets: AssetManager
    private val loadedScopes = HashSet<String>()

    lateinit var catalog: GrammarCatalog
        private set
    lateinit var themes: List<EditorTheme>
        private set

    fun init(context: Context) {
        if (ready) return
        synchronized(lock) {
            if (ready) return
            assets = context.applicationContext.assets
            GrammarRegistry.getInstance()
            FileProviderRegistry.getInstance().addFileProvider(AssetsFileResolver(assets))

            // 1) 内置层：官方 TextMate（languages.json 已恢复完整）
            catalog = GrammarCatalog.parse(read("textmate/languages.json"), read("textmate/filetypes.json"))
            themes = GrammarCatalog.parseThemes(read("textmate/themes.json"))
            val registry = ThemeRegistry.getInstance()
            for (t in themes) {
                val stream = FileProviderRegistry.getInstance().tryGetInputStream(t.path)
                registry.loadTheme(ThemeModel(IThemeSource.fromInputStream(stream, t.path, null), t.id).apply { isDark = t.dark }, false)
            }
            registry.setTheme(themes.first().id)

            // 2) extension layer: stgx grammars (registry is the single source)
            kotlinx.coroutines.runBlocking { StgxRegistry.load(context) }
            val dyn = StgxRegistry.allEntries().filter { it.grammar.highlight }
            if (dyn.isNotEmpty()) {
                val defs = dyn.map { e ->
                    DefaultGrammarDefinition.withGrammarSource(
                        IGrammarSource.fromString(IGrammarSource.ContentType.JSON, e.textMateJson),
                        e.scope, e.scope
                    )
                }
                GrammarRegistry.getInstance().loadGrammars(defs)
                loadedScopes += dyn.map { it.scope }
            }

            ready = true
        }
    }

    fun theme(id: String): EditorTheme? = themes.firstOrNull { it.id == id }

    // 批量取当前主题的 tokenColors 预览色：每个 scope -> (target -> #RRGGBB)
    fun previewColorsFor(scopes: List<String>, targets: List<String> = listOf("keyword.control", "string", "comment", "constant.numeric")): Map<String, Map<String, String>> {
        val currentId = ThemeRegistry.getInstance().currentThemeModel?.name
        val themePath = (themes.firstOrNull { it.id == currentId } ?: themes.firstOrNull())?.path ?: return emptyMap()
        val json = runCatching { assets.open(themePath).bufferedReader().use { it.readText() } }.getOrNull() ?: return emptyMap()
        val root = runCatching { kotlinx.serialization.json.Json.parseToJsonElement(json) }.getOrNull() as? kotlinx.serialization.json.JsonObject ?: return emptyMap()
        val tokenColors = root["tokenColors"] as? kotlinx.serialization.json.JsonArray ?: return emptyMap()

        data class Entry(val scopes: List<String>, val fg: String)
        val entries = ArrayList<Entry>()
        for (tc in tokenColors) {
            val obj = tc as? kotlinx.serialization.json.JsonObject ?: continue
            val settings = obj["settings"] as? kotlinx.serialization.json.JsonObject ?: continue
            val fg = (settings["foreground"] as? kotlinx.serialization.json.JsonPrimitive)?.content ?: continue
            val scopeEl = obj["scope"] ?: continue
            val list = when (scopeEl) {
                is kotlinx.serialization.json.JsonPrimitive -> listOf(scopeEl.content)
                is kotlinx.serialization.json.JsonArray -> scopeEl.mapNotNull { (it as? kotlinx.serialization.json.JsonPrimitive)?.content }
                else -> emptyList()
            }
            if (list.isNotEmpty()) entries += Entry(list, fg)
        }

        fun matches(entry: String, target: String): Boolean {
            val e = entry.trim()
            return e == target || e.startsWith("$target.") || e.endsWith(".$target") || e.contains(",$target") ||
                e.split(",").any { it.trim() == target || it.trim().startsWith("$target.") }
        }

        // 后出现的覆盖先出现的（与 VS Code 语义一致）
        val out = LinkedHashMap<String, Map<String, String>>()
        for (scope in scopes) {
            val map = LinkedHashMap<String, String>()
            for (t in targets) {
                var color: String? = null
                for (e in entries) if (e.scopes.any { matches(it, t) }) color = e.fg
                if (color != null) map[t] = color
            }
            if (map.isNotEmpty()) out[scope] = map
        }
        return out
    }

    fun activateTheme(id: String): ThemeModel {
        val registry = ThemeRegistry.getInstance()
        if (registry.currentThemeModel?.name != id) registry.setTheme(id)
        return registry.currentThemeModel
    }

    // 动态加载内存中的 stgx（保留 fork 能力）
    fun loadDynamicGrammar(scopeName: String, jsonString: String) {
        synchronized(lock) {
            runCatching {
                val source = IGrammarSource.fromString(IGrammarSource.ContentType.JSON, jsonString)
                val def = DefaultGrammarDefinition.withGrammarSource(source, scopeName, scopeName)
                GrammarRegistry.getInstance().loadGrammars(listOf(def))
                loadedScopes += scopeName
            }.onFailure { it.printStackTrace() }
        }
    }

    fun language(scope: String, autoComplete: Boolean = true): TextMateLanguage {
        synchronized(lock) {
            ThemeRegistry.getInstance().currentThemeModel?.let { GrammarRegistry.getInstance().setTheme(it) }

            val needed = catalog.closure(scope) { includesOf(it) }.filter { it.scopeName !in loadedScopes }
            if (needed.isNotEmpty()) {
                GrammarRegistry.getInstance().loadGrammars(needed.map(::definition))
                loadedScopes += needed.map { it.scopeName }
            }
        }
        return TextMateLanguage.create(scope, autoComplete)
    }

    private fun definition(e: GrammarEntry): GrammarDefinition {
        val source = IGrammarSource.fromInputStream(FileProviderRegistry.getInstance().tryGetInputStream(e.grammar), e.grammar, null)
        val def = if (e.languageConfiguration != null) {
            DefaultGrammarDefinition.withLanguageConfiguration(source, e.languageConfiguration, e.name, e.scopeName)
        } else {
            DefaultGrammarDefinition.withGrammarSource(source, e.name, e.scopeName)
        }
        return if (e.embeddedLanguages.isEmpty()) def else def.withEmbeddedLanguages(e.embeddedLanguages)
    }

    private fun includesOf(entry: GrammarEntry): Set<String> =
        runCatching { GrammarCatalog.includedScopes(read(entry.grammar)) }.getOrDefault(emptySet())

    private fun read(path: String): String = assets.open(path).bufferedReader().use { it.readText() }
}
