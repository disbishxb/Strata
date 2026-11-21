package dev.cluvex.strata.feature.settings

import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.MoveToInbox
import androidx.compose.material.icons.rounded.Outbox
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.cluvex.strata.R
import dev.cluvex.strata.core.data.StrataSettings
import dev.cluvex.strata.core.model.DrawerBoxEntry
import dev.cluvex.strata.core.model.DrawerComponent
import dev.cluvex.strata.core.model.Zoned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val ZONE_RELEASED = 0
private const val ZONE_STOWED = 1

private fun List<DrawerBoxEntry>.toZoned(): Zoned<DrawerComponent> = Zoned(
    listOf(
        filter { !it.stowed }.map { it.component },
        filter { it.stowed }.map { it.component },
    ),
)

private fun Zoned<DrawerComponent>.toEntries(): List<DrawerBoxEntry> = buildList {
    zones.getOrNull(ZONE_RELEASED).orEmpty().forEach { add(DrawerBoxEntry(it, stowed = false)) }
    zones.getOrNull(ZONE_STOWED).orEmpty().forEach { add(DrawerBoxEntry(it, stowed = true)) }
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DrawerBoxPage(settings: StrataSettings, act: SettingsActions, onBack: () -> Unit) {
    val scroll = rememberScrollState()
    val persistentScope = remember { CoroutineScope(SupervisorJob() + Dispatchers.IO) }

    var layout by remember { mutableStateOf(settings.drawerBox.toZoned()) }
    var pending by remember { mutableStateOf<Zoned<DrawerComponent>?>(null) }
    
    LaunchedEffect(settings.drawerBox) {
        val waiting = pending
        val incoming = settings.drawerBox.toZoned()
        if (waiting == null || waiting == incoming) {
            layout = incoming
            pending = null
        }
    }

    fun commit(next: Zoned<DrawerComponent>) {
        layout = next
        pending = next
        persistentScope.launch {
            act.repo.setDrawerBox(next.toEntries())
        }
    }

    val caps = listOf(Int.MAX_VALUE, Int.MAX_VALUE)
    val labels: Map<DrawerComponent, String> = DrawerComponent.entries.associateWith { stringResource(it.label) }

    val releasedLabel = stringResource(R.string.drawer_box_zone_released)
    val stowedLabel = stringResource(R.string.drawer_box_zone_stowed)
    val releasedEmpty = stringResource(R.string.settings_drawer_box_released_empty)
    val stowedEmpty = stringResource(R.string.settings_drawer_box_stowed_empty)
    val toStowedFmt = stringResource(R.string.drawer_box_to_stowed)
    val toReleasedFmt = stringResource(R.string.drawer_box_to_released)

    // 实时预览状态
    var previewZoned by remember { mutableStateOf(layout) }

    SettingsPageScaffold(
        stringResource(R.string.settings_page_drawer_box),
        onBack,
        subtitle = stringResource(R.string.settings_drawer_box_subtitle),
        scrollState = scroll,
    ) {
        SettingNote(stringResource(R.string.settings_drawer_box_note))

        SidebarPreview(previewZoned)

        ZoneEditor(
            zoned = layout,
            caps = caps,
            styles = listOf(
                ZoneStyle(releasedLabel, releasedEmpty, MaterialShapes.Cookie9Sided, Tint.SECONDARY),
                ZoneStyle(stowedLabel, stowedEmpty, MaterialShapes.Cookie6Sided, Tint.TERTIARY),
            ),
            buttons = { zone ->
                if (zone == ZONE_RELEASED) {
                    listOf(
                        ZoneButton(
                            icon = Icons.Rounded.MoveToInbox,
                            description = { name -> toStowedFmt.replace("%1\$s", name) },
                            zone = ZONE_STOWED,
                            atEnd = true,
                        ),
                    )
                } else {
                    listOf(
                        ZoneButton(
                            icon = Icons.Rounded.Outbox,
                            description = { name -> toReleasedFmt.replace("%1\$s", name) },
                            zone = ZONE_RELEASED,
                            atEnd = true,
                        ),
                    )
                }
            },
            label = { labels[it] ?: it.name },
            icon = { iconFor(it) },
            scroll = scroll,
            onPreview = { previewZoned = it }, // 拖拽时实时更新预览
            onChange = { next -> commit(next) },
            onSpill = { /* caps unlimited */ },
        )

        OutlinedButton(
            onClick = {
                val defaults = DrawerBoxEntry.defaults().toZoned()
                commit(defaults)
                previewZoned = defaults
            },
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp, start = 16.dp, end = 16.dp),
        ) {
            Icon(Icons.Rounded.Restore, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text(stringResource(R.string.settings_drawer_box_reset))
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SidebarPreview(zoned: Zoned<DrawerComponent>) {
    val colors = MaterialTheme.colorScheme
    val released = zoned.zones.getOrNull(ZONE_RELEASED).orEmpty()
    val stowedCount = zoned.zones.getOrNull(ZONE_STOWED)?.size ?: 0
    Surface(
        shape = RoundedCornerShape(32.dp),
        color = colors.surfaceContainerHighest,
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
    ) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 12.dp).animateContentSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.settings_drawer_box_preview),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.weight(1f).padding(start = 10.dp),
                )
                AnimatedContent(
                    targetState = stowedCount,
                    transitionSpec = {
                        (fadeIn() + scaleIn(initialScale = 0.9f)).togetherWith(fadeOut() + scaleOut(targetScale = 0.9f))
                    },
                ) { n ->
                    if (n > 0) {
                        Surface(shape = CircleShape, color = colors.tertiaryContainer) {
                            Row(
                                Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    Icons.Rounded.Inbox,
                                    contentDescription = null,
                                    tint = colors.onTertiaryContainer,
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    stringResource(R.string.drawer_box_card_count, n),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.onTertiaryContainer,
                                )
                            }
                        }
                    } else {
                        Spacer(Modifier.size(0.dp))
                    }
                }
            }
            Surface(
                shape = CircleShape,
                color = colors.surfaceContainer,
                modifier = Modifier.fillMaxWidth().height(64.dp),
            ) {
                AnimatedContent(
                    targetState = released.isEmpty(),
                    transitionSpec = {
                        (fadeIn() + scaleIn(initialScale = 0.94f)).togetherWith(fadeOut() + scaleOut(targetScale = 0.94f))
                    },
                ) { empty ->
                    if (empty) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(
                                stringResource(R.string.settings_drawer_box_preview_empty),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                            )
                        }
                    } else {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                            userScrollEnabled = false,
                        ) {
                            items(released, key = { it.name }) { c ->
                                Box(Modifier.animateItem().size(44.dp), contentAlignment = Alignment.Center) {
                                    Box(
                                        Modifier
                                            .size(40.dp)
                                            .clip(MaterialShapes.Cookie6Sided.toShape())
                                            .background(colors.secondaryContainer),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            iconFor(c),
                                            contentDescription = null,
                                            tint = colors.onSecondaryContainer,
                                            modifier = Modifier.size(22.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
