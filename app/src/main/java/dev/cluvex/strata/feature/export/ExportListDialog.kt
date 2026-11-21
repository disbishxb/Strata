package dev.cluvex.strata.feature.export

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import dev.cluvex.strata.R
import dev.cluvex.strata.core.archive.ArchiveEntry
import dev.cluvex.strata.core.export.FileListExport
import dev.cluvex.strata.core.export.FileListExporter
import dev.cluvex.strata.core.export.FileListFormat
import dev.cluvex.strata.core.i18n.tr
import dev.cluvex.strata.core.model.FileItem
import dev.cluvex.strata.core.util.IntentUtils
import dev.cluvex.strata.feature.explorer.TaskState
import java.io.File
import java.io.OutputStreamWriter
import java.nio.charset.Charset
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ExportListDialog(
    item: FileItem?,
    selection: List<FileItem>,
    archive: Boolean,
    listDir: suspend (String) -> List<FileItem>?,
    listArchive: suspend (String) -> List<ArchiveEntry>,
    onDismiss: () -> Unit,
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var absolutePath by remember { mutableStateOf(true) }
    var recursive by remember { mutableStateOf(true) }
    var level1Only by remember { mutableStateOf(false) }
    var appendSlash by remember { mutableStateOf(true) }
    var format by remember { mutableStateOf(FileListFormat.MARKDOWN) }
    var encoding by remember { mutableStateOf("UTF-8") }
    var bom by remember { mutableStateOf(false) }

    var step by remember { mutableStateOf(0) }
    var task by remember { mutableStateOf<TaskState?>(null) }
    var preview by remember { mutableStateOf("") }
    var export by remember { mutableStateOf<FileListExport?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var job by remember { mutableStateOf<Job?>(null) }

    val isSelectionMode = item == null

    fun writeTo(uri: android.net.Uri, ex: FileListExport) {
        val charset = when (encoding) {
            "GBK" -> Charset.forName("GBK")
            else -> Charsets.UTF_8
        }
        ctx.contentResolver.openOutputStream(uri)?.use { os ->
            if (bom && (encoding == "UTF-8" || encoding == "GBK")) {
                os.write(if (encoding == "UTF-8") byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
                        else byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            }
            OutputStreamWriter(os, charset).buffered().use { w ->
                when (format) {
                    FileListFormat.MARKDOWN -> FileListExporter.renderTo(ex, w)
                    FileListFormat.CSV -> FileListExporter.renderCsvTo(ex, w)
                }
            }
        } ?: error("openOutputStream returned null")
    }

    val mime = remember(format) { if (format == FileListFormat.CSV) "text/csv" else "text/markdown" }
    val saveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(mime)) { uri ->
        val ex = export ?: return@rememberLauncherForActivityResult
        if (uri != null) {
            scope.launch {
                val ok = runCatching {
                    withContext(Dispatchers.IO) { writeTo(uri, ex) }
                }.isSuccess
                if (ok) onDismiss()
                else error = ctx.getString(R.string.export_list_failed, "write failed")
            }
        }
    }

    val doExport: () -> Unit = {
        step = 1
        error = null
        export = null
        preview = ""
        task = TaskState(tr(R.string.export_list_scanning), "", 0f, true)
        job = scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val ex = if (isSelectionMode) {
                        FileListExporter.collectSelection(
                            items = selection, 
                            absolutePath = absolutePath, 
                            recursive = recursive, 
                            level1Only = level1Only, 
                            appendSlash = appendSlash, 
                            lister = listDir,
                            onProgress = { n ->
                                withContext(Dispatchers.Main) {
                                    task = TaskState(tr(R.string.export_list_scanning), "$n", 0f, true)
                                }
                            }
                        )
                    } else if (archive) {
                        FileListExporter.collectArchive(
                            listArchive(item!!.path), item.path, absolutePath, recursive, level1Only, appendSlash
                        )
                    } else {
                        FileListExporter.collectDirectory(
                            item!!.path, absolutePath, recursive, level1Only, appendSlash, listDir,
                            onProgress = { n ->
                                withContext(Dispatchers.Main) {
                                    task = TaskState(tr(R.string.export_list_scanning), "$n", 0f, true)
                                }
                            },
                        )
                    }
                    export = ex
                    preview = FileListExporter.preview(ex, format)
                }
            }.onFailure {
                if (it is CancellationException) throw it
                error = it.message ?: it.javaClass.simpleName
            }
            task = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.export_list_title)) },
        text = {
            if (step == 0) {
                Column {
                    RadioRow(selected = absolutePath, onClick = { absolutePath = true }, label = stringResource(R.string.export_list_abs))
                    RadioRow(selected = !absolutePath, onClick = { absolutePath = false }, label = stringResource(R.string.export_list_rel))
                    
                    if (item != null || selection.any { it.isDirectory }) {
                        Spacer(Modifier.height(8.dp))
                        CheckRow(checked = level1Only, onCheckedChange = {
                            level1Only = it
                            if (it) recursive = false
                        }, label = stringResource(R.string.export_list_level1))
                        CheckRow(checked = recursive, onCheckedChange = {
                            recursive = it
                            if (it) level1Only = false
                        }, label = stringResource(R.string.export_list_recur))
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    CheckRow(checked = appendSlash, onCheckedChange = { appendSlash = it }, label = stringResource(R.string.export_list_slash))
                    
                    Spacer(Modifier.height(8.dp))
                    Text(tr(R.string.export_list_format), style = MaterialTheme.typography.labelMedium)
                    Row(Modifier.fillMaxWidth()) {
                        RadioRow(
                            selected = format == FileListFormat.MARKDOWN,
                            onClick = { format = FileListFormat.MARKDOWN; export?.let { preview = FileListExporter.preview(it, format) } },
                            label = "Markdown",
                            modifier = Modifier.weight(1f)
                        )
                        RadioRow(
                            selected = format == FileListFormat.CSV,
                            onClick = { format = FileListFormat.CSV; export?.let { preview = FileListExporter.preview(it, format) } },
                            label = "CSV",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    Text(tr(R.string.export_list_encoding), style = MaterialTheme.typography.labelMedium)
                    Row(Modifier.fillMaxWidth()) {
                        listOf("UTF-8", "GBK").forEach { e ->
                            RadioRow(
                                selected = encoding == e,
                                onClick = { encoding = e },
                                label = e,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    CheckRow(checked = bom, onCheckedChange = { bom = it }, label = tr(R.string.export_list_bom))
                }
            } else {
                Column(Modifier.heightIn(max = 480.dp)) {
                    when {
                        task != null -> Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(stringResource(R.string.export_list_scanning) + " " + (task?.detail ?: ""))
                        }
                        error != null -> Text(stringResource(R.string.export_list_failed, error!!))
                        (export?.rows?.isEmpty() == true) -> Text(stringResource(R.string.export_list_empty))
                        else -> {
                            Text(
                                stringResource(
                                    R.string.export_list_summary,
                                    export?.rows?.size ?: 0,
                                    export?.rows?.count { !it.isDirectory } ?: 0,
                                    if (format == FileListFormat.CSV) "CSV" else "Markdown"
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = preview,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp),
                                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (step == 0) {
                TextButton(onClick = doExport) { Text(stringResource(R.string.common_ok)) }
            } else {
                Row {
                    TextButton(
                        enabled = export != null && export?.rows?.isNotEmpty() == true && task == null,
                        onClick = {
                            val name = if (isSelectionMode) "selection" else {
                                item?.let { if (it.isDirectory) it.name else it.name.substringBeforeLast('.') } ?: "export"
                            }
                            val ext = if (format == FileListFormat.CSV) "csv" else "md"
                            saveLauncher.launch("$name-list.$ext")
                        },
                    ) { Text(stringResource(R.string.export_list_save)) }

                    TextButton(
                        enabled = export != null && export?.rows?.isNotEmpty() == true && task == null,
                        onClick = {
                            val ex = export ?: return@TextButton
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    val ext = if (format == FileListFormat.CSV) "csv" else "md"
                                    val out = File(ctx.cacheDir, "filelist-${System.currentTimeMillis()}.$ext")
                                    out.bufferedWriter().use { w ->
                                        when (format) {
                                            FileListFormat.MARKDOWN -> FileListExporter.renderTo(ex, w)
                                            FileListFormat.CSV -> FileListExporter.renderCsvTo(ex, w)
                                        }
                                    }
                                    withContext(Dispatchers.Main) {
                                        IntentUtils.share(ctx, listOf(FileItem.of(out)))
                                    }
                                }
                                onDismiss()
                            }
                        },
                    ) { Text(stringResource(R.string.export_list_share)) }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                when {
                    task != null -> job?.cancel()
                    step == 1 -> step = 0
                    else -> onDismiss()
                }
            }) {
                Text(
                    when {
                        task != null -> stringResource(R.string.common_cancel)
                        step == 1 -> stringResource(R.string.common_back)
                        else -> stringResource(R.string.common_cancel)
                    }
                )
            }
        },
    )
}

@Composable
private fun RadioRow(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    Row(
        modifier.clickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun CheckRow(checked: Boolean, onCheckedChange: (Boolean) -> Unit, label: String) {
    Row(
        Modifier.fillMaxWidth().toggleable(value = checked, onValueChange = onCheckedChange).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = null)
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
}
