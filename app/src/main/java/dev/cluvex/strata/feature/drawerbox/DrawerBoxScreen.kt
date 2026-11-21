package dev.cluvex.strata.feature.drawerbox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.MoveToInbox
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.cluvex.strata.R
import dev.cluvex.strata.core.data.StrataSettings
import dev.cluvex.strata.core.model.DrawerBoxEntry
import dev.cluvex.strata.core.model.DrawerComponent

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DrawerBoxScreen(
    settings: StrataSettings,
    onOpen: (DrawerComponent) -> Unit,
    onRelease: (DrawerComponent) -> Unit,
    onBack: () -> Unit,
) {
    var ascending by rememberSaveable { mutableStateOf(true) }
    val stowed = remember(settings.drawerBox, ascending) {
        settings.drawerBox.filter { it.stowed }
            .sortedBy { it.component.name }
            .let { if (ascending) it else it.reversed() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.drawer_box)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    IconButton(onClick = { ascending = !ascending }) {
                        Icon(
                            Icons.AutoMirrored.Rounded.Sort,
                            contentDescription = stringResource(
                                if (ascending) R.string.drawer_box_sort_desc else R.string.drawer_box_sort_asc
                            ),
                        )
                    }
                },
            )
        },
    ) { pad ->
        if (stowed.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pad).padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(
                        Icons.Rounded.Inbox,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                    Text(stringResource(R.string.drawer_box_empty_title), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.drawer_box_empty_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            return@Scaffold
        }
        LazyColumn(
            Modifier.fillMaxSize().padding(pad).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(stowed, key = { it.component.name }) { entry ->
                DrawerBoxCard(
                    entry = entry,
                    onOpen = { onOpen(entry.component) },
                    onRelease = { onRelease(entry.component) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DrawerBoxCard(entry: DrawerBoxEntry, onOpen: () -> Unit, onRelease: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onOpen,
        shape = RoundedCornerShape(28.dp),
        color = colors.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(MaterialShapes.Cookie6Sided.toShape()).background(colors.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(iconFor(entry.component), contentDescription = null, tint = colors.onSecondaryContainer, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                Text(stringResource(entry.component.label), style = MaterialTheme.typography.titleMedium)
            }
            IconButton(onClick = onRelease) {
                Icon(Icons.Rounded.MoveToInbox, contentDescription = stringResource(R.string.drawer_box_release), tint = colors.primary)
            }
        }
    }
}

private fun iconFor(c: DrawerComponent): ImageVector = when (c) {
    DrawerComponent.LANGUAGES -> Icons.Rounded.Terminal
    DrawerComponent.RUN_COMMAND -> Icons.Rounded.Code
    DrawerComponent.APPS -> Icons.Rounded.Apps
    DrawerComponent.COLOR_PICKER -> Icons.Rounded.Palette
    DrawerComponent.SIGNING_KEYS -> Icons.Rounded.Key
    DrawerComponent.PLUGINS -> Icons.Rounded.Extension
    DrawerComponent.GRAMMAR -> Icons.Rounded.Style
}
