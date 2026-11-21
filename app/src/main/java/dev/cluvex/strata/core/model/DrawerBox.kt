package dev.cluvex.strata.core.model

import androidx.annotation.StringRes
import dev.cluvex.strata.R

/** Drawer entries that can be tucked into the "storage box". Order = display order. */
enum class DrawerComponent(@StringRes val label: Int) {
    LANGUAGES(R.string.drawer_languages),
    RUN_COMMAND(R.string.drawer_run_command),
    APPS(R.string.apps_title),
    COLOR_PICKER(R.string.color_picker_title),
    SIGNING_KEYS(R.string.keys_title),
    PLUGINS(R.string.plugin_title),
    GRAMMAR(R.string.grammar_title),
}

/** stowed = true -> lives inside the storage box; false -> shown normally. */
data class DrawerBoxEntry(val component: DrawerComponent, val stowed: Boolean = false) {

    companion object {
        fun defaults(): List<DrawerBoxEntry> = DrawerComponent.entries.map { DrawerBoxEntry(it) }

        fun decode(raw: String?): List<DrawerBoxEntry> {
            if (raw.isNullOrBlank()) return defaults()
            val parsed = raw.split(',')
                .mapNotNull { token ->
                    val parts = token.split(':')
                    val comp = DrawerComponent.entries.firstOrNull { it.name == parts[0] } ?: return@mapNotNull null
                    DrawerBoxEntry(comp, parts.getOrNull(1) == "1")
                }
            val known = parsed.map { it.component }.toSet()
            val missing = DrawerComponent.entries.filterNot { it in known }.map { DrawerBoxEntry(it) }
            return parsed + missing
        }

        fun encode(entries: List<DrawerBoxEntry>): String =
            entries.joinToString(",") { "${it.component.name}:${if (it.stowed) 1 else 0}" }
    }
}
