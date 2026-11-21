package dev.cluvex.strata.feature.termux

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import kotlinx.coroutines.delay
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cluvex.strata.R
import dev.cluvex.strata.core.remote.RemoteUri
import dev.cluvex.strata.core.termux.TermuxApi
import dev.cluvex.strata.core.termux.TermuxBridge
import dev.cluvex.strata.feature.explorer.ExplorerUiState
import dev.cluvex.strata.ui.rememberTextClipboard

private sealed interface CwdChoice {
    data object TermuxHome : CwdChoice
    data object Left : CwdChoice
    data object Right : CwdChoice
    data class Custom(val path: String) : CwdChoice
}

private val TerminalBg = Color(0xFF1E1E1E)
private val TerminalFg = Color(0xFFD4D4D4)

private fun needsInteractive(cmd: String): Boolean {
    val t = cmd.trimStart().trimEnd()
    return when {
        t == "tsu" || t.startsWith("tsu ") -> true
        t == "su" || t == "sudo" -> true
        else -> false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandRunnerScreen(
    termux: TermuxViewModel,
    explorer: ExplorerUiState,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val clipboard = rememberTextClipboard()
    val session = termux.session
    val termuxState by session.state.collectAsStateWithLifecycle()
    val state by termux.commandState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { session.refresh() }
    LifecycleResumeEffect(Unit) {
        session.refresh()
        onPauseOrDispose { }
    }
    var command by remember { mutableStateOf("") }
    var cwdChoice by remember { mutableStateOf<CwdChoice>(CwdChoice.TermuxHome) }
    var customPath by remember { mutableStateOf("") }
    var cwdMenu by remember { mutableStateOf(false) }
    var customDialog by remember { mutableStateOf(false) }
    val leftPath = explorer.left.path.takeUnless { RemoteUri.isRemote(it) }
    val rightPath = explorer.right.path.takeUnless { RemoteUri.isRemote(it) }
    val cwd = when (val c = cwdChoice) {
        CwdChoice.TermuxHome -> TermuxApi.HOME
        CwdChoice.Left -> leftPath ?: TermuxApi.HOME
        CwdChoice.Right -> rightPath ?: TermuxApi.HOME
        is CwdChoice.Custom -> c.path
    }
    val ready = termuxState.termuxInstalled && termuxState.permission && termuxState.issue == null
    val interactiveOnly = needsInteractive(command)
    BackHandler(onBack = onBack)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.command_runner_title))
                        val statusColor = when {
                            termuxState.checking -> MaterialTheme.colorScheme.onSurfaceVariant
                            ready -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.error
                        }
                        val statusText = when {
                            termuxState.checking -> stringResource(R.string.command_runner_status_checking)
                            ready -> stringResource(R.string.command_runner_status_connected)
                            else -> stringResource(R.string.command_runner_status_disconnected)
                        }
                        Text(statusText, style = MaterialTheme.typography.labelSmall, color = statusColor)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (!ready) {
                 Column(Modifier.verticalScroll(rememberScrollState())) {
                     TermuxSetupStep(termuxState, session)
                 }
                return@Scaffold
            }
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.FolderOpen, contentDescription = null, modifier = Modifier.size(20.dp))
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(stringResource(R.string.command_runner_cwd), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    BasicTextField(
                        value = displayPath(cwd),
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
                        cursorBrush = SolidColor(Color.Transparent),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box {
                    TextButton(onClick = { cwdMenu = true }) {
                        Text(stringResource(R.string.command_runner_change))
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null)
                    }
                    DropdownMenu(expanded = cwdMenu, onDismissRequest = { cwdMenu = false }) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.command_runner_cwd_termux_home)) }, onClick = { cwdChoice = CwdChoice.TermuxHome; cwdMenu = false })
                        DropdownMenuItem(text = { Text(stringResource(R.string.command_runner_cwd_left)) }, enabled = leftPath != null, onClick = { cwdChoice = CwdChoice.Left; cwdMenu = false })
                        DropdownMenuItem(text = { Text(stringResource(R.string.command_runner_cwd_right)) }, enabled = rightPath != null, onClick = { cwdChoice = CwdChoice.Right; cwdMenu = false })
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.command_runner_cwd_custom)) },
                            onClick = {
                                customPath = cwd
                                cwdMenu = false
                                customDialog = true
                            }
                        )
                    }
                }
            }

                        OutlinedTextField(
                value = command,
                onValueChange = { command = it },
                placeholder = { Text(stringResource(R.string.command_runner_input_hint)) },
                singleLine = false,
                minLines = 4,
                textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 200.dp),
            )

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End), verticalAlignment = Alignment.CenterVertically) {
                if (state.running) {
                    TextButton(onClick = { termux.cancelCommand() }) { Text(stringResource(R.string.command_runner_cancel)) }
                } else {
                    FilledTonalButton(
                        onClick = {
                            val script = "cd " + shellQuote(cwd) + " && " + command
                            clipboard.setText(script)
                            TermuxBridge.launch(context)
                        },
                        enabled = command.isNotBlank(),
                    ) {
                        Icon(Icons.Rounded.Terminal, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(stringResource(R.string.command_runner_run_in_termux), Modifier.padding(start = 6.dp))
                    }
                    Button(
                        onClick = { termux.runCommand(command, cwd) },
                        enabled = command.isNotBlank() && !state.running && !interactiveOnly,
                    ) { Text(stringResource(R.string.command_runner_run)) }
                }
            }
            if (interactiveOnly) {
                Text(
                    stringResource(R.string.command_runner_needs_terminal),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

                        Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(TerminalBg)
                    .padding(12.dp),
            ) {
                when {
                    state.running -> Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = TerminalFg)
                        Text(stringResource(R.string.command_runner_running), Modifier.padding(start = 12.dp), style = MaterialTheme.typography.bodyMedium, color = TerminalFg)
                    }
                    state.cancelled -> Text(
                        stringResource(R.string.command_runner_cancelled),
                        color = TerminalFg,
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    )
                    state.error != null -> Text(
                        stringResource(R.string.command_runner_error, state.error ?: ""),
                        color = Color(0xFFFF6B6B),
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    )
                    state.result != null -> {
                        val r = state.result!!
                        val ok = r.ok
                        val exit = r.exitCode
                        val text = (r.stdout + r.stderr).trimEnd()
                        val shown = text.lines().take(200).joinToString("\n")
                        Column(Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    if (ok) stringResource(R.string.command_runner_success, exit ?: 0) else stringResource(R.string.command_runner_failed, exit ?: -1),
                                    color = if (ok) Color(0xFF6BCB77) else Color(0xFFFF6B6B),
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                    modifier = Modifier.weight(1f),
                                )
                                TextButton(onClick = { clipboard.setText(r.stdout + r.stderr) }) {
                                    Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text(stringResource(R.string.common_copy), Modifier.padding(start = 4.dp))
                                }
                            }
                            SelectionContainer {
                                Text(
                                    shown,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = TerminalFg,
                                    softWrap = false,
                                    style = TextStyle(textDirection = TextDirection.Ltr),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 400.dp)
                                        .horizontalScroll(rememberScrollState())
                                        .verticalScroll(rememberScrollState())
                                        .padding(top = 6.dp),
                                )
                            }
                            if (state.totalLines > state.shownLines && state.logPath != null) {
                                Text(
                                    stringResource(R.string.command_runner_truncated, state.totalLines, state.logPath ?: ""),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TerminalFg.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                            }
                        }
                    }
                    else -> Text(
                        stringResource(R.string.command_runner_idle),
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = TerminalFg.copy(alpha = 0.6f),
                    )
                }
            }
        
        }
    }
    if (customDialog) {
        var textFieldValue by remember { 
            mutableStateOf(
                TextFieldValue(
                    text = customPath,
                    selection = TextRange(0, customPath.length)
                )
            ) 
        }
        val focusRequester = remember { FocusRequester() }
        val keyboardController = LocalSoftwareKeyboardController.current
        LaunchedEffect(Unit) {
            delay(200)
            focusRequester.requestFocus()
            keyboardController?.show()
            textFieldValue = textFieldValue.copy(selection = TextRange(0, textFieldValue.text.length))
        }
        AlertDialog(
            onDismissRequest = { customDialog = false },
            title = { Text(stringResource(R.string.command_runner_cwd_custom)) },
            text = {
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it },
                    singleLine = true,
                    textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                textFieldValue = textFieldValue.copy(
                                    selection = TextRange(0, textFieldValue.text.length)
                                )
                            }
                        }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    customPath = textFieldValue.text.trim()
                    cwdChoice = CwdChoice.Custom(customPath)
                    customDialog = false
                }, enabled = textFieldValue.text.isNotBlank()) { Text(stringResource(R.string.common_save)) }
            },
            dismissButton = { TextButton(onClick = { customDialog = false }) { Text(stringResource(R.string.common_cancel)) } },
        )
    }
}

private fun shellQuote(s: String): String =
    if (s.isBlank()) "''" else "'" + s.replace("'", "'\\''") + "'"

private fun displayPath(path: String): String {
    val home = "/data/data/com.termux/files/home"
    val cleanPath = path.trimEnd('/')
    return when {
        cleanPath == home -> "~"
        cleanPath.startsWith("$home/") -> "~" + cleanPath.removePrefix(home)
        else -> cleanPath
    }
}
