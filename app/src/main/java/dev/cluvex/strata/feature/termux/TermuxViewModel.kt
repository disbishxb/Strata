package dev.cluvex.strata.feature.termux

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.cluvex.strata.core.i18n.tr
import dev.cluvex.strata.R
import dev.cluvex.strata.core.termux.AndroidTermux
import dev.cluvex.strata.core.termux.TermuxCommand
import dev.cluvex.strata.core.termux.TermuxResult
import dev.cluvex.strata.core.termux.TermuxRun
import dev.cluvex.strata.core.termux.TermuxSession
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class CommandRunState(
    val running: Boolean = false,
    val cancelled: Boolean = false,
    val result: TermuxResult? = null,
    val error: String? = null,
    val logPath: String? = null,
    val totalLines: Int = 0,
    val shownLines: Int = 0,
)

class TermuxViewModel(app: Application) : AndroidViewModel(app) {
    val session = TermuxSession(AndroidTermux(app), viewModelScope)

    private val _commandState = MutableStateFlow(CommandRunState())
    val commandState: StateFlow<CommandRunState> = _commandState.asStateFlow()

    private var commandJob: Job? = null

    fun runCommand(cmd: String, cwd: String) {
        if (cmd.isBlank()) return
        commandJob?.cancel()
        _commandState.update { CommandRunState(running = true) }
        commandJob = viewModelScope.launch {
            val (run, constructError) = try {
                val port = AndroidTermux(getApplication())
                port.run(TermuxCommand.bash(cmd, workdir = cwd, background = true, label = tr(dev.cluvex.strata.R.string.command_runner_title)), timeoutMs = 60_000) to null
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null to (e.message ?: e.javaClass.simpleName)
            }
            if (run == null) {
                _commandState.update { CommandRunState(running = false, error = constructError) }
                return@launch
            }
            val (result, error) = when (run) {
                is TermuxRun.Done -> run.result to null
                TermuxRun.TimedOut -> null to tr(R.string.termux_error_timeout)
                TermuxRun.NotInstalled -> null to tr(R.string.termux_error_not_installed)
                TermuxRun.NoPermission -> null to tr(R.string.termux_error_no_permission)
                TermuxRun.Opened -> null to tr(R.string.termux_error_opened)
                is TermuxRun.Error -> null to run.message
            }
            val all = (result?.stdout.orEmpty() + result?.stderr.orEmpty()).trimEnd()
            val lines = all.lines()
            val total = lines.size
            val shown = minOf(total, 200)
            val log = if (total > shown) writeLog(cmd, all) else null
            _commandState.update {
                CommandRunState(
                    running = false,
                    result = result,
                    error = error,
                    logPath = log?.absolutePath,
                    totalLines = total,
                    shownLines = shown,
                )
            }
        }
    }

    fun cancelCommand() {
        commandJob?.cancel()
        commandJob = null
        _commandState.update { CommandRunState(running = false, cancelled = true) }
    }

    fun clearCommandResult() {
        commandJob?.cancel()
        commandJob = null
        _commandState.update { CommandRunState() }
    }

    private suspend fun writeLog(cmd: String, text: String): File? = withContext(Dispatchers.IO) {
        runCatching {
            val dir = File(getApplication<Application>().cacheDir, "cmdlogs").apply { mkdirs() }
            val f = File(dir, "strata-cmd-${System.currentTimeMillis()}.log")
            f.writeText("# cmd: $cmd\n\n$text")
            f
        }.getOrNull()
    }
}
