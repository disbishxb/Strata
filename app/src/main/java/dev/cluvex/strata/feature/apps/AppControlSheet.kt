package dev.cluvex.strata.feature.apps

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.cluvex.strata.R
import dev.cluvex.strata.core.apk.InstalledApp
import dev.cluvex.strata.core.control.AppControlBridge.Backend
import dev.cluvex.strata.core.control.AppControlBridge.Capability

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppControlSheet(
    app: InstalledApp,
    self: Boolean,
    hidden: Boolean,
    suspended: Boolean,
    backend: Backend,
    resolvedBackend: Backend,
    availableBackends: Set<Backend>,
    controlLoaded: Boolean,
    capabilities: Set<Capability>,
    onPickBackend: (Backend) -> Unit,
    onForceStop: () -> Unit,
    onSetEnabled: (Boolean) -> Unit,
    onSetHidden: (Boolean) -> Unit,
    onSetSuspended: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(bottom = 24.dp)) {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text(app.label, style = MaterialTheme.typography.titleLarge)
                Text(app.packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    stringResource(R.string.apps_control_lift_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                if (backend == Backend.PRIVILEGED) {
                    Text(
                        text = stringResource(R.string.settings_system_privilege_not_app),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                if (self) Text(
                    stringResource(R.string.apps_control_self),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            if (controlLoaded) {
                HorizontalDivider()
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    // 强制写死顺序，保证“系统”一定会出来（不在 availableBackends 里就是灰色的）
                    val options = listOf(Backend.AUTO, Backend.ROOT, Backend.PRIVILEGED, Backend.DHIZUKU, Backend.SHIZUKU)
                    options.forEach { b ->
                        val isAvailable = b in availableBackends
                        FilterChip(
                            selected = backend == b,
                            onClick = { if (isAvailable) onPickBackend(b) },
                            enabled = isAvailable,
                            label = { Text(backendLabel(b)) },
                        )
                    }
                }
            }
            HorizontalDivider()
            if (!controlLoaded) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 3.dp)
                        Text(stringResource(R.string.apps_control_loading), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                val base = !self
                
                RowAction(stringResource(R.string.apps_control_force_stop), base && Capability.FORCE_STOP in capabilities, Icons.Rounded.Stop, onForceStop)

                if (app.enabled) {
                    RowAction(stringResource(R.string.apps_control_disable), base && Capability.DISABLE in capabilities, Icons.Rounded.Block) { onSetEnabled(false) }
                } else {
                    RowAction(stringResource(R.string.apps_control_enable), base && Capability.DISABLE in capabilities, Icons.Rounded.PlayCircle) { onSetEnabled(true) }
                }

                if (hidden) {
                    RowAction(stringResource(R.string.apps_control_unhide), base && Capability.HIDE in capabilities, Icons.Rounded.Visibility) { onSetHidden(false) }
                } else {
                    RowAction(stringResource(R.string.apps_control_hide), base && Capability.HIDE in capabilities, Icons.Rounded.VisibilityOff) { onSetHidden(true) }
                }

                if (suspended) {
                    RowAction(stringResource(R.string.apps_control_unsuspend), base && Capability.SUSPEND in capabilities, Icons.Rounded.PlayCircle) { onSetSuspended(false) }
                } else {
                    RowAction(stringResource(R.string.apps_control_suspend), base && Capability.SUSPEND in capabilities, Icons.Rounded.PauseCircle) { onSetSuspended(true) }
                }
            }
        }
    }
}

@Composable
private fun backendLabel(b: Backend): String = stringResource(
    when (b) {
        Backend.AUTO -> R.string.apps_backend_auto
        Backend.ROOT -> R.string.apps_backend_root
        Backend.SHIZUKU -> R.string.apps_backend_shizuku
        Backend.PRIVILEGED -> R.string.apps_backend_privileged
        Backend.DHIZUKU -> R.string.apps_backend_dhizuku
    }
)

@Composable
private fun RowAction(
    label: String,
    enabled: Boolean,
    icon: ImageVector,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.padding(end = 12.dp))
        Text(label, modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
    }
}
