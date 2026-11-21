package dev.cluvex.strata.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.cluvex.strata.R
import dev.cluvex.strata.core.data.StrataSettings
import dev.cluvex.strata.core.i18n.tr
import dev.cluvex.strata.core.model.BehaviorPrefs
import dev.cluvex.strata.core.model.StartFolder

private enum class StartPane { LEFT, RIGHT }

@Composable
fun StartLocationPage(settings: StrataSettings, act: SettingsActions, activeFolder: String, onBack: () -> Unit) {
    val b = settings.behavior
    var choosing by remember { mutableStateOf<StartPane?>(null) }
    var pathFor by remember { mutableStateOf<Pair<StartPane, String>?>(null) }

    SettingsPageScaffold(tr(R.string.settings_start_location), onBack) {
        SettingsGroup(tr(R.string.settings_start_left)) {
            link(
                tr(R.string.settings_start_current),
                summary = describeStart(b, left = true),
                icon = Icons.Rounded.FolderOpen,
            ) { choosing = StartPane.LEFT }
        }
        SettingsGroup(tr(R.string.settings_start_right)) {
            link(
                tr(R.string.settings_start_current),
                summary = describeStart(b, left = false),
                icon = Icons.Rounded.FolderOpen,
            ) { choosing = StartPane.RIGHT }
        }
    }

    choosing?.let { pane ->
        StartFolderChoiceDialog(
            onPick = { f ->
                choosing = null
                if (f == StartFolder.CUSTOM) {
                    val ownPath = if (pane == StartPane.LEFT) b.leftPath else b.rightPath
                    val globalPath = b.startPath
                    pathFor = pane to (ownPath.ifBlank { globalPath.ifBlank { activeFolder } })
                } else {
                    act.behavior { b2 -> applyPane(b2, pane, f, if (pane == StartPane.LEFT) b2.leftPath else b2.rightPath) }
                }
            },
            onDismiss = { choosing = null },
        )
    }

    pathFor?.let { (pane, initial) ->
        TextSettingDialog(
            title = tr(R.string.settings_start_folder),
            initial = initial,
            extra = tr(R.string.settings_use_current_folder) to activeFolder,
            onConfirm = { path ->
                pathFor = null
                act.behavior { b2 -> applyPane(b2, pane, StartFolder.CUSTOM, path) }
            },
            onDismiss = { pathFor = null },
        )
    }
}

private fun applyPane(b: BehaviorPrefs, pane: StartPane, f: StartFolder, path: String): BehaviorPrefs =
    if (pane == StartPane.LEFT) b.copy(leftStart = f, leftPath = path)
    else b.copy(rightStart = f, rightPath = path)

fun describeStart(b: BehaviorPrefs, left: Boolean): String {
    val own = if (left) b.leftStart else b.rightStart
    val ownPath = if (left) b.leftPath else b.rightPath
    val f = own ?: b.startFolder
    val p = if (own != null) ownPath else b.startPath
    return when (f) {
        StartFolder.HOME -> tr(R.string.model_start_home)
        StartFolder.LAST -> tr(R.string.model_start_last)
        StartFolder.CUSTOM -> p.ifBlank { tr(R.string.model_start_custom) }
    }
}

@Composable
private fun StartFolderChoiceDialog(onPick: (StartFolder) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr(R.string.settings_start_location)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                StartFolder.entries.forEach { f ->
                    TextButton(onClick = { onPick(f) }, modifier = Modifier.fillMaxWidth()) {
                        Text(tr(f.label), modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(tr(R.string.common_cancel)) } },
    )
}