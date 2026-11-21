package dev.cluvex.strata.core.text

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

@Serializable
data class StgxGrammar(
    val iconBackground: String? = null,
    val iconBackgroundDark: String? = null,
    val iconForeground: String? = null,
    val id: String,
    val name: String,
    val extensions: List<String>,
    val icon: String? = null,
    val iconTint: String? = null,
    val hue: Int? = null,
    val highlight: Boolean = true,
    val comments: StgxComments? = null,
    val rules: List<StgxRule> = emptyList(),
    val scope: String? = null
)

@Serializable
data class StgxComments(
    val line: String? = null,
    val block: List<String>? = null,
)

@Serializable
data class StgxRule(
    val scope: String,
    val match: String? = null,
    val begin: String? = null,
    val end: String? = null,
    val patterns: List<StgxRule> = emptyList(),
)

object StgxParser {
    private val json = Json { ignoreUnknownKeys = false }

    private val STGX_TINT_KINDS = setOf(
        "folder", "image", "video", "audio", "archive", "apk", "code", "shell",
        "python", "text", "json", "pdf", "font", "document", "encrypted", "binary",
    )

    fun parse(text: String): Result<StgxGrammar> = runCatching {
        val g = json.decodeFromString<StgxGrammar>(text)
        require(g.id.isNotBlank()) { "id is empty" }
        require(g.id.all { it.isLetterOrDigit() || it == '_' || it == '-' }) { "id has invalid characters: ${g.id}" }
        require(g.extensions.isNotEmpty()) { "extensions is empty" }
        require(g.extensions.all { it.isNotBlank() && !it.contains('.') }) { "extension must not contain a dot: $g" }
        require(g.rules.isNotEmpty()) { "rules is empty" }
        g.hue?.let { require(it in 0..360) { "hue must be 0..360, got $it" } }
        g.iconTint?.let { require(it.lowercase() in STGX_TINT_KINDS) { "iconTint unknown: $it (allowed: ${STGX_TINT_KINDS.joinToString()})" } }
        validateRules(g.rules)
        g
    }

    private fun validateRules(rules: List<StgxRule>) {
        for (r in rules) {
            require(r.scope.isNotBlank()) { "rule scope is empty" }
            val hasMatch = r.match != null
            val hasBeginEnd = r.begin != null && r.end != null
            require(hasMatch != hasBeginEnd) { "rule needs exactly one of match or begin+end: scope=${r.scope}" }
            if (r.patterns.isNotEmpty()) {
                require(hasBeginEnd) { "patterns only on begin/end rules: scope=${r.scope}" }
                validateRules(r.patterns)
            }
        }
    }

    fun toJson(g: StgxGrammar): String = json.encodeToString(g)
}

object StgxCompiler {
    fun scopeOf(g: StgxGrammar): String = if (!g.scope.isNullOrBlank()) g.scope else "source.stgx.ext.${g.id}"

    fun compile(g: StgxGrammar): String = buildJsonObject {
        put("name", g.name)
        put("scopeName", scopeOf(g))
        putJsonArray("fileTypes") { g.extensions.forEach { add(JsonPrimitive(it)) } }
        put("patterns", compileRules(g.rules))
    }.toString()

    private fun compileRules(rules: List<StgxRule>): JsonArray = buildJsonArray {
        for (r in rules) add(compileRule(r))
    }

    private fun compileRule(r: StgxRule): JsonObject = buildJsonObject {
        put("name", r.scope)
        val m = r.match
        if (m != null) {
            put("match", m)
        } else {
            put("begin", r.begin ?: error("begin is null"))
            put("end", r.end ?: error("end is null"))
            if (r.patterns.isNotEmpty()) put("patterns", compileRules(r.patterns))
        }
    }
}