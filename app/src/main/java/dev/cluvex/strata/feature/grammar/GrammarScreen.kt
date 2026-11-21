package dev.cluvex.strata.feature.grammar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DataObject
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.FolderZip
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import dev.cluvex.strata.R
import dev.cluvex.strata.core.open.OpenResolver
import dev.cluvex.strata.core.text.StgxGrammar
import dev.cluvex.strata.core.text.StgxParser
import dev.cluvex.strata.core.text.StgxRegistry
import dev.cluvex.strata.feature.editor.TextMateEngine
import dev.cluvex.strata.feature.settings.SettingsPageScaffold
import dev.cluvex.strata.ui.expressive.Art
import dev.cluvex.strata.ui.expressive.ExpressiveHero
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class GrammarCatalogRow(
    val name: String,
    val scopeName: String,
    val grammarPath: String,
    val extensions: List<String>,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GrammarPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var refreshKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(refreshKey) {
        withContext(Dispatchers.IO) { StgxRegistry.reload(context) }
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(stringResource(R.string.grammar_extensions), stringResource(R.string.grammar_built_in))

    val builtinFileSyntaxes = listOf(
        GrammarCatalogRow(stringResource(R.string.grammar_apk_installer), stringResource(R.string.grammar_built_in), "app.intent.apk", listOf("apk", "apk.1")),
        GrammarCatalogRow(stringResource(R.string.grammar_split_installer), stringResource(R.string.grammar_built_in), "app.intent.split_apk", OpenResolver.splitBundles.toList()),
        GrammarCatalogRow(stringResource(R.string.grammar_archive_viewer), stringResource(R.string.grammar_built_in), "app.intent.archive", OpenResolver.browsable.toList()),
        GrammarCatalogRow(stringResource(R.string.grammar_pdf_reader), stringResource(R.string.grammar_built_in), "app.intent.pdf", listOf("pdf")),
        GrammarCatalogRow(stringResource(R.string.grammar_dex_editor), stringResource(R.string.grammar_built_in), "app.intent.dex", listOf("dex", "odex")),
        GrammarCatalogRow(stringResource(R.string.grammar_arsc_editor), stringResource(R.string.grammar_built_in), "app.intent.arsc", listOf("arsc")),
        GrammarCatalogRow(stringResource(R.string.grammar_ninepatch_editor), stringResource(R.string.grammar_built_in), "app.intent.ninepatch", listOf("9.png")),
        GrammarCatalogRow(stringResource(R.string.grammar_font_viewer), stringResource(R.string.grammar_built_in), "app.intent.font", OpenResolver.fonts.toList()),
        GrammarCatalogRow(stringResource(R.string.grammar_markdown_reader), stringResource(R.string.grammar_built_in), "app.intent.markdown", OpenResolver.markdown.toList()),
        GrammarCatalogRow(stringResource(R.string.grammar_keys_import), stringResource(R.string.grammar_built_in), "app.intent.keys", listOf("jks", "keystore", "bks", "p12", "pfx", "pk8", "x509.pem")),
        GrammarCatalogRow(stringResource(R.string.grammar_stgx_viewer), stringResource(R.string.grammar_built_in), "app.intent.stgx", listOf("stgx")),
    )

    var searchQuery by remember { mutableStateOf("") }
    var builtinSearch by remember { mutableStateOf("") }
    var builtinSortMode by remember { mutableStateOf(BuiltinSortMode.NAME_ASC) }
    var sortMode by remember { mutableStateOf(ImportSortMode.NAME_ASC) }

    val pickStgx = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val appContext = context.applicationContext
            scope.launch(Dispatchers.IO) {
                runCatching {
                    val name = appContext.contentResolver.query(uri, null, null, null, null)?.use { c ->
                        if (c.moveToFirst()) {
                            val idx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                            if (idx >= 0) c.getString(idx) else null
                        } else null
                    } ?: "imported.stgx"
                    val dir = File(appContext.filesDir, "stgx_extensions").apply { mkdirs() }
                    val dest = File(dir, name)
                    appContext.contentResolver.openInputStream(uri)?.use { input ->
                        dest.outputStream().use { output -> input.copyTo(output) }
                    }
                    StgxRegistry.reload(appContext)
                    withContext(Dispatchers.Main) { refreshKey++ }
                }
            }
        }
    }

    val builtIn by produceState(emptyList<GrammarCatalogRow>()) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                TextMateEngine.init(context.applicationContext)
                TextMateEngine.catalog.entries.map { e ->
                    GrammarCatalogRow(
                        name = e.name,
                        scopeName = e.scopeName,
                        grammarPath = e.grammar,
                        extensions = runCatching { TextMateEngine.catalog.extensionsOf(e.scopeName) }.getOrDefault(emptyList()),
                    )
                }
            }.getOrDefault(emptyList())
        }
    }

    val imported by produceState(emptyList<StgxGrammar>(), refreshKey, context) {
        value = withContext(Dispatchers.IO) {
            StgxRegistry.load(context.applicationContext)
            StgxRegistry.importedGrammars
        }
    }

    val failed by produceState(emptyList<dev.cluvex.strata.core.text.StgxFailure>(), refreshKey, context) {
        value = withContext(Dispatchers.IO) {
            StgxRegistry.load(context.applicationContext)
            StgxRegistry.failedEntries()
        }
    }

    val previewByScope by produceState(emptyMap<String, Map<String, String>>(), builtIn) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                if (builtIn.isEmpty()) emptyMap()
                else { TextMateEngine.init(context.applicationContext); TextMateEngine.previewColorsFor(builtIn.map { it.scopeName }) }
            }.getOrDefault(emptyMap())
        }
    }

    val importPreviewByScope by produceState(emptyMap<String, Map<String, String>>(), imported) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                if (imported.isEmpty()) emptyMap()
                else {
                    TextMateEngine.init(context.applicationContext)
                    val entries = StgxRegistry.allEntries().filter { !it.isBuiltIn }
                    TextMateEngine.previewColorsFor(entries.map { it.scope })
                }
            }.getOrDefault(emptyMap())
        }
    }

    // id -> scope, for the import preview
    val idToScope = remember(imported) {
        StgxRegistry.allEntries().filter { !it.isBuiltIn }.associate { it.grammar.id to it.scope }
    }

    SettingsPageScaffold(
        title = stringResource(R.string.grammar_title),
        onBack = onBack,
        subtitle = stringResource(R.string.grammar_subtitle),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.weight(1f)) {
                tabs.forEachIndexed { index, title ->
                    SegmentedButton(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        shape = SegmentedButtonDefaults.itemShape(index, tabs.size),
                        label = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    )
                }
            }
            FilledTonalIconButton(onClick = { pickStgx.launch(arrayOf("application/json", "text/plain", "*/*")) }) {
                Icon(Icons.Rounded.FileOpen, contentDescription = stringResource(R.string.grammar_import))
            }
        }

        if (selectedTab == 0) {
            val q = searchQuery.trim()
            val filteredImported = imported
                .filter { it.name.contains(q, ignoreCase = true) || it.id.contains(q, ignoreCase = true) }
                .sortedByImport(sortMode)
            val filteredFailed = failed.filter { it.file.name.contains(q, ignoreCase = true) }

            ImportGrammarSearchRow(
                value = searchQuery,
                onChange = { searchQuery = it },
                placeholder = stringResource(R.string.grammar_search_hint),
                current = sortMode,
                onSelect = { sortMode = it },
            )

            if (filteredImported.isEmpty() && filteredFailed.isEmpty()) {
                GrammarEmptyState(
                    title = stringResource(R.string.grammar_empty_title),
                    body = stringResource(R.string.grammar_empty_body),
                )
            } else {
                filteredImported.forEach { grammar ->
                    ImportedGrammarCard(
                        grammar = grammar,
                        preview = importPreviewByScope[idToScope[grammar.id]].orEmpty(),
                        onExport = { exportGrammar(context, grammar) },
                        onDelete = { deleteGrammar(context, grammar); refreshKey++ },
                    )
                    Spacer(Modifier.height(8.dp))
                }
                if (filteredFailed.isNotEmpty()) {
                    GrammarFailedSection(
                        failures = filteredFailed,
                        onDelete = { f ->
                            if (f.file.delete()) {
                                StgxRegistry.reload(context)
                                refreshKey++
                                android.widget.Toast.makeText(context, context.getString(R.string.grammar_deleted), android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
                }
            }
        } else {
            val allBuiltIns = builtIn + builtinFileSyntaxes
            val filteredBuiltIn = allBuiltIns
                .filter { it.name.contains(builtinSearch, ignoreCase = true) || it.extensions.any { ext -> ext.contains(builtinSearch, ignoreCase = true) } }
                .sortedByBuiltin(builtinSortMode)

            BuiltinGrammarSearchRow(
                value = builtinSearch,
                onChange = { builtinSearch = it },
                placeholder = stringResource(R.string.grammar_builtin_search_hint),
                current = builtinSortMode,
                onSelect = { builtinSortMode = it },
            )

            if (filteredBuiltIn.isEmpty()) {
                GrammarEmptyState(title = stringResource(R.string.grammar_no_match), body = null)
            } else {
                filteredBuiltIn.forEach { row ->
                    BuiltinGrammarCard(
                        row = row,
                        preview = previewByScope[row.scopeName].orEmpty(),
                        onExport = { exportBuiltin(context, row) },
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

enum class ImportSortMode { NAME_ASC, NAME_DESC, EXT_ASC, EXT_DESC, SOURCE }
fun ImportSortMode.labelRes(): Int = when (this) {
    ImportSortMode.NAME_ASC -> R.string.grammar_sort_name_asc
    ImportSortMode.NAME_DESC -> R.string.grammar_sort_name_desc
    ImportSortMode.EXT_ASC -> R.string.grammar_sort_ext_asc
    ImportSortMode.EXT_DESC -> R.string.grammar_sort_ext_desc
    ImportSortMode.SOURCE -> R.string.grammar_sort_source
}
@Composable fun ImportSortMode.label(): String = stringResource(labelRes())
private fun List<StgxGrammar>.sortedByImport(mode: ImportSortMode): List<StgxGrammar> = when (mode) {
    ImportSortMode.NAME_ASC -> sortedBy { it.name.lowercase() }
    ImportSortMode.NAME_DESC -> sortedByDescending { it.name.lowercase() }
    ImportSortMode.EXT_ASC -> sortedBy { it.extensions.firstOrNull()?.lowercase() ?: "" }
    ImportSortMode.EXT_DESC -> sortedByDescending { it.extensions.firstOrNull()?.lowercase() ?: "" }
    ImportSortMode.SOURCE -> sortedBy { it.id.lowercase() }
}

enum class BuiltinSortMode { NAME_ASC, EXT_COUNT }
fun BuiltinSortMode.labelRes(): Int = when (this) {
    BuiltinSortMode.NAME_ASC -> R.string.grammar_sort_name_asc
    BuiltinSortMode.EXT_COUNT -> R.string.grammar_sort_ext_count
}
@Composable fun BuiltinSortMode.label(): String = stringResource(labelRes())
private fun List<GrammarCatalogRow>.sortedByBuiltin(mode: BuiltinSortMode): List<GrammarCatalogRow> = when (mode) {
    BuiltinSortMode.NAME_ASC -> sortedBy { it.name.lowercase() }
    BuiltinSortMode.EXT_COUNT -> sortedByDescending { it.extensions.size }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ImportGrammarSearchRow(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    current: ImportSortMode,
    onSelect: (ImportSortMode) -> Unit,
) {
    GrammarSearchRowImpl(value, onChange, placeholder, current, ImportSortMode.entries, { it.label() }, onSelect)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BuiltinGrammarSearchRow(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    current: BuiltinSortMode,
    onSelect: (BuiltinSortMode) -> Unit,
) {
    GrammarSearchRowImpl(value, onChange, placeholder, current, BuiltinSortMode.entries, { it.label() }, onSelect)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun <M> GrammarSearchRowImpl(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    current: M,
    options: List<M>,
    labelOf: @Composable (M) -> String,
    onSelect: (M) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            placeholder = { Text(placeholder) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.weight(1f),
        )
        var expanded by remember { mutableStateOf(false) }
        Box {
            FilterChip(
                selected = true,
                onClick = { expanded = true },
                label = { Text(labelOf(current), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Sort, contentDescription = null, modifier = Modifier.size(18.dp)) },
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(labelOf(opt)) },
                        onClick = { onSelect(opt); expanded = false },
                    )
                }
            }
        }
    }
}

@Composable
private fun GrammarEmptyState(title: String, body: String?) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        dev.cluvex.strata.ui.expressive.ExpressiveHero(
            art = dev.cluvex.strata.ui.expressive.Art.UNKNOWN,
            size = 160.dp,
        )
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
        if (body != null) {
            Text(
                body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun GrammarFailedSection(
    failures: List<dev.cluvex.strata.core.text.StgxFailure>,
    onDelete: (dev.cluvex.strata.core.text.StgxFailure) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Build, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.width(12.dp))
            Text(
                stringResource(R.string.grammar_failed_count, failures.size),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f),
            )
            Icon(
                if (expanded) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
                contentDescription = null, tint = MaterialTheme.colorScheme.error,
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column {
                failures.forEach { f ->
                    HorizontalDivider(color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(f.file.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                            Text(stringResource(R.string.grammar_failed_reason, f.reason), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { onDelete(f) }) {
                            Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.grammar_delete), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

private fun exportGrammar(context: android.content.Context, grammar: StgxGrammar) {
    try {
        val json = StgxParser.toJson(grammar)
        val downloads = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloads, "${grammar.name.replace(" ", "_")}.stgx")
        file.writeText(json)
        android.widget.Toast.makeText(context, context.getString(R.string.grammar_exported_to, file.absolutePath), android.widget.Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, context.getString(R.string.grammar_export_failed, e.message ?: ""), android.widget.Toast.LENGTH_SHORT).show()
    }
}

private fun deleteGrammar(context: android.content.Context, grammar: StgxGrammar) {
    val dir = File(context.filesDir, "stgx_extensions")
    var deleted = false
    dir.listFiles()?.forEach { f ->
        if (f.extension == "stgx") {
            runCatching { StgxParser.parse(f.readText()).getOrNull()?.let { g -> if (g.id == grammar.id && f.delete()) deleted = true } }
        }
    }
    if (deleted) {
        StgxRegistry.reload(context)
        android.widget.Toast.makeText(context, context.getString(R.string.grammar_deleted_named, grammar.name), android.widget.Toast.LENGTH_SHORT).show()
    } else {
        android.widget.Toast.makeText(context, context.getString(R.string.grammar_delete_failed), android.widget.Toast.LENGTH_SHORT).show()
    }
}

private fun exportBuiltin(context: android.content.Context, row: GrammarCatalogRow) {
    try {
        val text = context.assets.open(row.grammarPath).bufferedReader().use { it.readText() }
        val downloads = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloads, "${row.name.replace(" ", "_")}.tmLanguage.json")
        file.writeText(text)
        android.widget.Toast.makeText(context, context.getString(R.string.grammar_exported_to, file.absolutePath), android.widget.Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, context.getString(R.string.grammar_export_failed, e.message ?: ""), android.widget.Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun GrammarCardShell(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String?,
    badges: List<String>,
    preview: Map<String, String>,
    actions: @Composable RowScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon()
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                actions()
            }
            if (badges.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 10.dp),
                ) {
                    badges.forEach { b ->
                        Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(6.dp)) {
                            Text(b, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }
            if (preview.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                GrammarPreviewRow(preview)
            }
        }
    }
}

@Composable
private fun ImportedGrammarCard(
    grammar: StgxGrammar,
    preview: Map<String, String>,
    onExport: () -> Unit,
    onDelete: () -> Unit,
) {
    GrammarCardShell(
        icon = { GrammarIcon(grammar.icon, grammar.name) },
        title = grammar.name,
        subtitle = stringResource(R.string.grammar_id, grammar.id),
        badges = grammar.extensions.map { ".$it" },
        preview = preview,
        actions = {
            IconButton(onClick = onExport) { Icon(Icons.Rounded.SaveAlt, contentDescription = stringResource(R.string.grammar_export), tint = MaterialTheme.colorScheme.primary) }
            IconButton(onClick = onDelete) { Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.grammar_delete), tint = MaterialTheme.colorScheme.error) }
        },
    )
}

@Composable
private fun BuiltinGrammarCard(
    row: GrammarCatalogRow,
    preview: Map<String, String>,
    onExport: () -> Unit,
) {
    val isIntent = row.grammarPath.startsWith("app.intent.")
    GrammarCardShell(
        icon = { Icon(if (isIntent) Icons.Rounded.Description else scopeIcon(row.scopeName, row.name), contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = row.name,
        subtitle = if (isIntent) null else stringResource(R.string.grammar_scope, row.scopeName),
        badges = row.extensions.map { if (it == "apk.1") ".apk.1" else ".$it" },
        preview = if (isIntent) emptyMap() else preview,
        actions = {
            if (isIntent) {
                Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = RoundedCornerShape(4.dp)) {
                    Text(stringResource(R.string.grammar_file_syntax), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                }
            } else {
                IconButton(onClick = onExport) { Icon(Icons.Rounded.SaveAlt, contentDescription = stringResource(R.string.grammar_export), tint = MaterialTheme.colorScheme.primary) }
            }
        },
    )
}

@Composable
private fun GrammarIcon(spec: String?, name: String) {
    val bmp = dev.cluvex.strata.core.text.StgxIcons.bitmapOf(spec)
    if (bmp != null) {
        Image(bitmap = bmp, contentDescription = name, modifier = Modifier.size(28.dp))
    } else {
        Icon(
            dev.cluvex.strata.core.text.StgxIcons.vectorOf(spec) ?: dev.cluvex.strata.core.text.StgxIcons.defaultVector(),
            contentDescription = null, tint = MaterialTheme.colorScheme.primary,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GrammarPreviewRow(preview: Map<String, String>) {
    val labels = mapOf("keyword.control" to "keyword", "string" to "string", "comment" to "comment", "constant.numeric" to "number")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        preview.forEach { (scope, hex) ->
            val c = runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(MaterialTheme.colorScheme.primary)
            Surface(color = c, shape = RoundedCornerShape(4.dp)) {
                Text(labels[scope] ?: scope, color = Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
        }
    }
}

private fun scopeIcon(scopeName: String, name: String): androidx.compose.ui.graphics.vector.ImageVector {
    val s = scopeName.lowercase(); val n = name.lowercase()
    return when {
        s.contains("python") -> Icons.Rounded.Code
        s.contains("javascript") || s.contains("typescript") -> Icons.Rounded.DataObject
        s.contains("markdown") -> Icons.Rounded.Description
        s.contains("shell") || s.contains("batchfile") || s.contains("powershell") -> Icons.Rounded.Terminal
        s.contains("css") || s.contains("scss") || s.contains("less") -> Icons.Rounded.Palette
        s.contains("json") -> Icons.Rounded.DataObject
        s.contains("dockerfile") || n.contains("archive") -> Icons.Rounded.FolderZip
        else -> Icons.Rounded.Code
    }
}
