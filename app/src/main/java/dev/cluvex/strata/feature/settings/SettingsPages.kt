package dev.cluvex.strata.feature.settings

import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.foundation.layout.RowScope
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.animation.animateContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.AssistChip
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.material3.toShape
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material.icons.rounded.Build
import android.os.Build
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.rounded.FolderZip
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.TabRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Download
import dev.cluvex.strata.core.model.ActionMenu
import androidx.compose.material3.InputChip
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import dev.cluvex.strata.core.apk.install.InstallBackend
import dev.cluvex.strata.core.apk.install.InstallAccess
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialShapes
import dev.cluvex.strata.core.model.Zoned
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.automirrored.rounded.WrapText
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.AutoDelete
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.DataObject
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.FolderSpecial
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.ScreenLockPortrait
import androidx.compose.material.icons.rounded.SdStorage
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material.icons.rounded.SettingsBackupRestore
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SpaceBar
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material.icons.rounded.ViewColumn
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import dev.cluvex.strata.BuildConfig
import dev.cluvex.strata.R
import dev.cluvex.strata.core.data.BackupLocations
import dev.cluvex.strata.core.data.EditorPrefs
import dev.cluvex.strata.core.data.SettingsBackup
import dev.cluvex.strata.core.data.StrataSettings
import dev.cluvex.strata.core.fs.LocalFileAccess
import dev.cluvex.strata.core.i18n.tr
import dev.cluvex.strata.core.i18n.trPlural
import dev.cluvex.strata.core.model.ConflictChoice
import dev.cluvex.strata.core.model.DateStyle
import dev.cluvex.strata.core.model.ReaderTheme
import dev.cluvex.strata.core.model.SizeUnits
import dev.cluvex.strata.core.model.SortBy
import dev.cluvex.strata.core.model.StartFolder
import dev.cluvex.strata.core.model.UiDensity
import dev.cluvex.strata.core.model.ViewMode
import dev.cluvex.strata.core.open.OpenResolver
import dev.cluvex.strata.core.open.OpenTarget
import dev.cluvex.strata.core.root.RootManager
import dev.cluvex.strata.core.root.RootState
import dev.cluvex.strata.core.dhizuku.DhizukuBridge
import dev.cluvex.strata.core.shizuku.ShizukuManager
import dev.cluvex.strata.core.text.GrammarCatalog
import dev.cluvex.strata.feature.editor.TextMateEngine
import dev.cluvex.strata.core.util.Formatters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.rounded.Palette


@StringRes
private fun SortBy.label(): Int = when (this) {
    SortBy.NAME -> R.string.common_name
    SortBy.SIZE -> R.string.common_size
    SortBy.DATE -> R.string.common_date
    SortBy.TYPE -> R.string.common_type
}

@Composable
fun FileListPage(settings: StrataSettings, act: SettingsActions, onBack: () -> Unit) {
    val l = settings.list
    val now = remember { System.currentTimeMillis() }
    SettingsPageScaffold(stringResource(R.string.settings_page_file_list), onBack, subtitle = stringResource(R.string.settings_file_list_subtitle)) {
        ThemePreview(settings)
        SettingsGroup(stringResource(R.string.settings_group_layout)) {
            choice(tr(R.string.settings_view_as), ViewMode.entries, l.viewMode, { tr(it.label) }, { v -> act.list { it.copy(viewMode = v) } }, icon = Icons.Rounded.GridView)
            choice(tr(R.string.settings_density), UiDensity.entries, settings.uiDensity, { tr(it.label) }, { d -> act.run { setUiDensity(d) } }, icon = Icons.Rounded.ViewColumn)
            switch(tr(R.string.settings_wrap_names), l.wrapNames, { v -> act.list { it.copy(wrapNames = v) } }, summary = tr(R.string.settings_wrap_names_summary), icon = Icons.AutoMirrored.Rounded.WrapText)
        }
        SettingsGroup(stringResource(R.string.settings_group_files)) {
            val photo = tr(R.string.settings_sample_photo)
            switch(tr(R.string.common_show_hidden), settings.showHidden, { v -> act.run { setShowHidden(v) } }, summary = tr(R.string.settings_show_hidden_summary), icon = if (settings.showHidden) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff)
            switch(tr(R.string.settings_show_extensions), l.showExtensions, { v -> act.list { it.copy(showExtensions = v) } }, summary = tr(R.string.settings_show_extensions_summary, "$photo.jpg", photo), icon = Icons.Rounded.TextFields)
            switch(
                tr(R.string.settings_folders_first),
                settings.sortOrder.dirsFirst,
                { v -> act.run { setSortOrder(settings.sortOrder.copy(dirsFirst = v)) } },
                summary = tr(R.string.settings_folders_first_summary),
                icon = Icons.Rounded.FolderSpecial,
            )
            pick(
                tr(R.string.settings_default_sort),
                SortBy.entries,
                settings.sortOrder.by,
                { tr(it.label()) },
                { by -> act.run { setSortOrder(settings.sortOrder.copy(by = by)) } },
                icon = Icons.AutoMirrored.Rounded.Sort,
            )
            switch(tr(R.string.settings_descending), settings.sortOrder.descending, { v -> act.run { setSortOrder(settings.sortOrder.copy(descending = v)) } }, summary = tr(R.string.settings_descending_summary))
        }
        SettingsGroup(stringResource(R.string.settings_group_previews)) {
            switch(tr(R.string.settings_thumbnails), l.thumbnails, { v -> act.list { it.copy(thumbnails = v) } }, summary = tr(R.string.settings_thumbnails_summary), icon = Icons.Rounded.Image)
            switch(tr(R.string.settings_apk_icons), l.apkIcons, { v -> act.list { it.copy(apkIcons = v) } }, summary = tr(R.string.settings_apk_icons_summary), icon = Icons.Rounded.Android)
        }
        SettingsGroup(stringResource(R.string.common_details)) {
            switch(tr(R.string.settings_show_date), l.showDate, { v -> act.list { it.copy(showDate = v) } }, icon = Icons.Rounded.CalendarMonth)
            switch(tr(R.string.settings_show_size), l.showSize, { v -> act.list { it.copy(showSize = v) } }, icon = Icons.Rounded.Straighten)
            pick(
                tr(R.string.settings_date_format),
                DateStyle.entries,
                l.dateStyle,
                { Formatters.listDate(now - 300_000L, it, l.use24Hour, now) },
                { d -> act.list { it.copy(dateStyle = d) } },
                icon = Icons.Rounded.Schedule,
                detail = { if (it == DateStyle.RELATIVE) tr(R.string.settings_date_relative_detail) else null },
            )
            switch(tr(R.string.settings_24_hour, 24), l.use24Hour, { v -> act.list { it.copy(use24Hour = v) } })
            choice(
                tr(R.string.settings_size_units),
                SizeUnits.entries,
                l.sizeUnits,
                { if (it == SizeUnits.BINARY) tr(R.string.settings_units_binary, 1, 1024) else tr(R.string.settings_units_decimal, 1, 1000) },
                { u -> act.list { it.copy(sizeUnits = u) } },
                icon = Icons.Rounded.Numbers,
            )
        }
    }
}

@Composable
fun BehaviorPage(settings: StrataSettings, act: SettingsActions, activeFolder: String, onBack: () -> Unit, onOpenStartLocation: () -> Unit = {}) {
    val b = settings.behavior
    SettingsPageScaffold(stringResource(R.string.settings_page_behavior), onBack, subtitle = stringResource(R.string.settings_behavior_subtitle)) {
        SettingsGroup(stringResource(R.string.settings_group_starting)) {
            link(
                tr(R.string.settings_start_in),
                summary = tr(R.string.settings_summary_start_left_right, describeStart(b, left = true), describeStart(b, left = false)),
                icon = Icons.Rounded.FolderOpen,
            ) { onOpenStartLocation() }
            switch(tr(R.string.settings_two_panes), settings.dualPanePreferred, { v -> act.run { setDualPanePreferred(v) } }, summary = tr(R.string.settings_two_panes_summary), icon = Icons.Rounded.ViewColumn)
        }
        SettingsGroup(stringResource(R.string.settings_group_gestures)) {
            switch(tr(R.string.settings_double_back), b.doubleBackToExit, { v -> act.behavior { it.copy(doubleBackToExit = v) } }, summary = tr(R.string.settings_double_back_summary), icon = Icons.AutoMirrored.Rounded.ExitToApp)
            switch(tr(R.string.settings_icon_tap), b.iconTapSelects, { v -> act.behavior { it.copy(iconTapSelects = v) } }, summary = tr(R.string.settings_icon_tap_summary), icon = Icons.Rounded.SelectAll)
        }
        SettingsGroup(stringResource(R.string.settings_group_working)) {
            switch(
                tr(R.string.settings_screen_on_tasks),
                b.keepScreenOnTasks,
                { v -> act.behavior { it.copy(keepScreenOnTasks = v) } },
                summary = tr(R.string.settings_screen_on_tasks_summary),
                icon = Icons.Rounded.LightMode,
            )
        }
    }
}

@Composable
fun OperationsPage(settings: StrataSettings, act: SettingsActions, onOpenRecycleBin: () -> Unit, onBack: () -> Unit) {
    val o = settings.operations
    SettingsPageScaffold(stringResource(R.string.settings_page_operations), onBack, subtitle = stringResource(R.string.settings_operations_subtitle)) {
        SettingsGroup(stringResource(R.string.settings_group_copy_move)) {
            val photo = tr(R.string.settings_sample_photo)
            pick(
                tr(R.string.settings_conflict),
                ConflictChoice.entries,
                o.conflict,
                { tr(it.label) },
                { c -> act.operations { it.copy(conflict = c) } },
                icon = Icons.Rounded.DataObject,
                detail = {
                    when (it) {
                        ConflictChoice.ASK -> tr(R.string.settings_conflict_ask_detail)
                        ConflictChoice.KEEP_BOTH -> tr(R.string.settings_conflict_keep_both_detail, "$photo (1).jpg")
                        ConflictChoice.OVERWRITE -> tr(R.string.settings_conflict_replace_detail)
                        ConflictChoice.SKIP -> tr(R.string.settings_conflict_skip_detail)
                    }
                },
            )
            switch(
                tr(R.string.settings_keep_mtime),
                o.keepModifiedTime,
                { v -> act.operations { it.copy(keepModifiedTime = v) } },
                summary = tr(R.string.settings_keep_mtime_summary),
                icon = Icons.Rounded.Schedule,
            )
        }
        SettingsGroup(stringResource(R.string.common_recycle_bin)) {
            switch(
                tr(R.string.settings_trash_default),
                settings.trashByDefault,
                { v -> act.run { setTrashByDefault(v) } },
                summary = tr(R.string.settings_trash_default_summary),
                icon = Icons.Rounded.Delete,
            )
            choice(
                tr(R.string.settings_trash_clean),
                listOf(0, 7, 30, 90),
                settings.trashAutoCleanDays,
                { if (it == 0) tr(R.string.settings_never) else trPlural(R.plurals.settings_days, it, it) },
                { d -> act.run { setTrashAutoCleanDays(d) } },
                icon = Icons.Rounded.AutoDelete,
            )
            link(tr(R.string.settings_open_recycle_bin), icon = Icons.Rounded.Delete, tint = Tint.NEUTRAL, onClick = onOpenRecycleBin)
        }
    }
}

@Composable
fun ReaderSettingsPage(settings: StrataSettings, act: SettingsActions, onBack: () -> Unit) {
    val r = settings.reader
    var forgotten by remember { mutableStateOf(false) }
    SettingsPageScaffold(stringResource(R.string.settings_page_reader), onBack, subtitle = stringResource(R.string.settings_reader_subtitle)) {
        SettingsGroup(stringResource(R.string.settings_group_pages)) {
            choice(tr(R.string.settings_page_color), ReaderTheme.entries.toList(), r.theme, { tr(it.label) }, { t -> act.reader { it.copy(theme = t) } }, icon = Icons.Rounded.Contrast, summary = tr(R.string.settings_page_color_summary))
            switch(tr(R.string.settings_reader_screen_on), r.keepScreenOn, { v -> act.reader { it.copy(keepScreenOn = v) } }, icon = Icons.Rounded.LightMode)
        }
        SettingsGroup(stringResource(R.string.settings_group_memory), footer = stringResource(R.string.settings_memory_footer)) {
            link(
                tr(R.string.settings_forget_spots),
                summary = tr(if (forgotten) R.string.settings_forget_spots_done else R.string.settings_forget_spots_summary),
                icon = Icons.Rounded.History,
                chevron = false,
            ) {
                act.run { forgetReadingSpots() }
                forgotten = true
            }
        }
    }
}

@Composable
fun EditorSettingsPage(settings: StrataSettings, act: SettingsActions, onBack: () -> Unit) {
    val e = settings.editor
    val context = LocalContext.current
    val themes by produceState(emptyList<Pair<String, String>>()) {
        value = withContext(Dispatchers.IO) {
            runCatching { GrammarCatalog.parseThemes(context.assets.open("textmate/themes.json").bufferedReader().use { it.readText() }) }
                .getOrDefault(emptyList()).map { it.id to it.name }
        }
    }
    SettingsPageScaffold(stringResource(R.string.settings_page_editor), onBack, subtitle = stringResource(R.string.settings_editor_subtitle)) {
        SettingsGroup(stringResource(R.string.settings_group_look)) {
            val options = listOf("auto" to tr(R.string.settings_editor_follow_app)) + themes
            pick(
                tr(R.string.settings_editor_theme),
                options.map { it.first },
                e.theme,
                { id -> options.firstOrNull { it.first == id }?.second ?: id },
                { id -> act.editor { it.copy(theme = id) } },
                icon = Icons.Rounded.Style,
            )
            slider(
                tr(R.string.settings_font_size),
                e.textSize,
                8f..32f,
                steps = 23,
                valueLabel = { tr(R.string.settings_font_size_value, it.roundToInt()) },
                onChange = { v -> act.editor { it.copy(textSize = v.roundToInt().toFloat()) } },
                icon = Icons.Rounded.FormatSize,
            )
            switch(tr(R.string.settings_line_numbers), e.lineNumbers, { v -> act.editor { it.copy(lineNumbers = v) } })
            switch(tr(R.string.settings_pin_line_numbers), e.pinLineNumbers, { v -> act.editor { it.copy(pinLineNumbers = v) } }, summary = tr(R.string.settings_pin_line_numbers_summary))
            switch(tr(R.string.settings_highlight_line), e.highlightLine, { v -> act.editor { it.copy(highlightLine = v) } })
            switch(tr(R.string.settings_highlight_brackets), e.highlightBrackets, { v -> act.editor { it.copy(highlightBrackets = v) } })
            switch(tr(R.string.settings_indent_guides), e.indentGuides, { v -> act.editor { it.copy(indentGuides = v) } }, summary = tr(R.string.settings_indent_guides_summary))
            switch(tr(R.string.settings_show_whitespace), e.showWhitespace, { v -> act.editor { it.copy(showWhitespace = v) } }, icon = Icons.Rounded.SpaceBar)
        }
        SettingsGroup(stringResource(R.string.settings_group_typing)) {
            choice(tr(R.string.settings_tab_width), EditorPrefs.TAB_WIDTHS, e.tabWidth, { Formatters.number(it.toLong()) }, { w -> act.editor { it.copy(tabWidth = w) } })
            switch(tr(R.string.settings_auto_indent), e.autoIndent, { v -> act.editor { it.copy(autoIndent = v) } }, summary = tr(R.string.settings_auto_indent_summary))
            switch(tr(R.string.settings_auto_close), e.autoClosePairs, { v -> act.editor { it.copy(autoClosePairs = v) } })
            switch(tr(R.string.settings_word_wrap), e.wordWrap, { v -> act.editor { it.copy(wordWrap = v) } }, icon = Icons.AutoMirrored.Rounded.WrapText)
            switch(tr(R.string.settings_symbol_bar), e.symbolBar, { v -> act.editor { it.copy(symbolBar = v) } }, summary = tr(R.string.settings_symbol_bar_summary))
        }
        SettingsGroup(stringResource(R.string.settings_group_scrolling)) {
            switch(tr(R.string.settings_sticky_scroll), e.stickyScroll, { v -> act.editor { it.copy(stickyScroll = v) } }, summary = tr(R.string.settings_sticky_scroll_summary))
            switch(tr(R.string.settings_pinch_zoom), e.pinchZoom, { v -> act.editor { it.copy(pinchZoom = v) } })
            switch(tr(R.string.settings_cursor_animation), e.cursorAnimation, { v -> act.editor { it.copy(cursorAnimation = v) } })
            switch(tr(R.string.settings_editor_screen_on), e.keepScreenOn, { v -> act.editor { it.copy(keepScreenOn = v) } }, summary = tr(R.string.settings_editor_screen_on_summary))
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppsPage(
    settings: StrataSettings,
    act: SettingsActions,
    onBack: () -> Unit,
    onRequestRoot: () -> Unit = {},
) {
    val scroll = rememberScrollState()
    var editDir by remember { mutableStateOf(false) }
    var editPattern by remember { mutableStateOf(false) }
    var editThirdPartyPackage by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val appContext = LocalContext.current.applicationContext
    // 用一个不随 Composable 销毁的 CoroutineScope，保证退出页面时写入不被取消
    val persistentScope = remember { kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO) }

    var access by remember { mutableStateOf(InstallAccess.probe()) }
    var requestingBackend by remember { mutableStateOf<InstallBackend?>(null) }

    // 乐观更新：拖拽立刻更新本地 UI，异步写入磁盘
    var localPriority by remember { mutableStateOf(settings.installPriority) }

    // 只在用户首次进入页面、或从数据源变化时同步；避免拖拽中途被 reset
    LaunchedEffect(settings.installPriority) {
        if (localPriority != settings.installPriority) {
            // 只有外部数据真的变化（不是我们刚写入的同一份）才更新
            localPriority = settings.installPriority
        }
    }

    LaunchedEffect(settings.installWithRoot, settings.installWithShizuku, settings.installWithDhizuku, settings.installPriority) {
        ShizukuManager.refresh()
        DhizukuBridge.refresh()
        access = InstallAccess.probe()
    }

    val policy = InstallAccess.policyOf(settings)
    val effective = policy.resolve(access)

    SettingsPageScaffold(
        stringResource(R.string.settings_page_apps),
        onBack,
        subtitle = stringResource(R.string.settings_apps_subtitle),
        scrollState = scroll,
    ) {
        SettingsGroup(stringResource(R.string.settings_group_installing)) {
            switch(
                tr(R.string.settings_verify),
                settings.verifyBeforeInstall,
                { v -> act.run { setVerifyBeforeInstall(v) } },
                summary = tr(R.string.settings_verify_summary),
                icon = Icons.Rounded.CheckCircle,
            )
        }

        SettingsGroup(
            title = stringResource(R.string.settings_group_install_backends),
            footer = stringResource(R.string.settings_install_backends_footer),
        ) {
            InstallBackend.entries.filter { it.needsPrivilege }.forEach { backend ->
                val enabled = when (backend) {
                    InstallBackend.Root -> settings.installWithRoot
                    InstallBackend.Shizuku -> settings.installWithShizuku
                    InstallBackend.Dhizuku -> settings.installWithDhizuku
                    InstallBackend.Session -> true
                }
                val av = access[backend]
                val available = av?.available == true
                val isRequesting = requestingBackend == backend

                switch(
                    title = backend.label(),
                    checked = enabled,
                    onChange = { v ->
                        if (v) {
                            requestingBackend = backend
                            scope.launch {
                                var granted = false
                                if (backend == InstallBackend.Root) {
                                    onRequestRoot()
                                    var tries = 0
                                    while (tries < 150) {
                                        access = InstallAccess.probe()
                                        if (access[backend]?.available == true) { granted = true; break }
                                        kotlinx.coroutines.delay(100)
                                        tries++
                                    }
                                } else if (backend == InstallBackend.Shizuku) {
                                    granted = ShizukuManager.ensurePermission()
                                } else if (backend == InstallBackend.Dhizuku) {
                                    granted = DhizukuBridge.ensurePermission()
                                }
                                
                                if (granted || access[backend]?.available == true) {
                                    when (backend) {
                                        InstallBackend.Root -> act.run { setInstallWithRoot(true) }
                                        InstallBackend.Shizuku -> act.run { setInstallWithShizuku(true) }
                                        InstallBackend.Dhizuku -> act.run { setInstallWithDhizuku(true) }
                                        InstallBackend.Session -> Unit
                                    }
                                }
                                access = InstallAccess.probe()
                                requestingBackend = null
                            }
                        } else {
                            when (backend) {
                                InstallBackend.Root -> act.run { setInstallWithRoot(false) }
                                InstallBackend.Shizuku -> act.run { setInstallWithShizuku(false) }
                                InstallBackend.Dhizuku -> act.run { setInstallWithDhizuku(false) }
                                InstallBackend.Session -> Unit
                            }
                            access = InstallAccess.probe()
                        }
                    },
                    summary = when {
                        isRequesting -> tr(R.string.common_loading)
                        !enabled -> backend.hintWhenOff()
                        available -> tr(R.string.settings_install_backend_ready)
                        else -> av?.reason ?: tr(R.string.settings_install_backend_unavailable)
                    },
                    icon = backend.icon(),
                )
            }
        }

        SettingsGroup(
            title = stringResource(R.string.settings_group_install_priority),
            footer = stringResource(R.string.settings_install_priority_footer, effective.label()),
        ) {
            custom(title = null) {
                PriorityList(
                    items = localPriority,
                    enabled = !settings.useThirdPartyInstaller,
                    onMove = { from, to ->
                        val next = localPriority.toMutableList()
                        val item = next.removeAt(from)
                        next.add(to, item)
                        localPriority = next
                        persistentScope.launch { act.repo.setInstallPriority(next) }
                    }
                )
                Text(
                    text = stringResource(R.string.settings_install_priority_explain),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
        
        SettingsGroup(stringResource(R.string.settings_group_third_party_installer)) {
            switch(
                title = tr(R.string.settings_third_party_enable),
                checked = settings.useThirdPartyInstaller,
                onChange = { v ->
                    act.run { setThirdPartyInstaller(v, settings.thirdPartyInstallerPackage) }
                },
                summary = tr(R.string.settings_third_party_enable_summary),
                icon = Icons.Rounded.Extension,
            )
            val tpEnabled = settings.useThirdPartyInstaller
            link(
                tr(R.string.settings_third_party_package),
                summary = settings.thirdPartyInstallerPackage.ifBlank {
                    tr(R.string.settings_third_party_package_unset)
                },
                icon = Icons.Rounded.Android,
                enabled = tpEnabled,
            ) {
                if (tpEnabled) editThirdPartyPackage = true
            }
        }

        SettingsGroup(stringResource(R.string.settings_group_extracting)) {
            link(tr(R.string.settings_apk_folder), summary = settings.apkExtractDir.ifBlank { tr(R.string.settings_apk_folder_default) }, icon = Icons.Rounded.Download) { editDir = true }
            link(
                tr(R.string.settings_name_pattern),
                summary = tr(R.string.settings_name_pattern_summary, settings.apkNamePattern, tr(R.string.settings_name_pattern_legend)),
                icon = Icons.Rounded.TextFields,
            ) { editPattern = true }
        }
    }
    if (editDir) {
        TextSettingDialog(
            title = stringResource(R.string.settings_apk_folder),
            initial = settings.apkExtractDir,
            hint = stringResource(R.string.settings_apk_folder_hint),
            onConfirm = { v -> editDir = false; act.run { setApkExtraction(v, settings.apkNamePattern) } },
            onDismiss = { editDir = false },
        )
    }
    if (editPattern) {
        TextSettingDialog(
            title = stringResource(R.string.settings_name_pattern),
            initial = settings.apkNamePattern,
            hint = stringResource(R.string.settings_name_pattern_legend),
            onConfirm = { v -> editPattern = false; act.run { setApkExtraction(settings.apkExtractDir, v.ifBlank { "{A}_{V}" }) } },
            onDismiss = { editPattern = false },
        )
    }
    if (editThirdPartyPackage) {
        TextSettingDialog(
            title = stringResource(R.string.settings_third_party_package),
            initial = settings.thirdPartyInstallerPackage,
            hint = stringResource(R.string.settings_third_party_package_hint),
            onConfirm = { v ->
                editThirdPartyPackage = false
                act.run { setThirdPartyInstaller(settings.useThirdPartyInstaller, v) }
            },
            onDismiss = { editThirdPartyPackage = false },
        )
    }
}


@Composable
fun AccessPage(
    settings: StrataSettings,
    act: SettingsActions,
    rootState: RootState,
    onRootAccess: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val shizuku by ShizukuManager.state.collectAsState()

    val liveRootState by RootManager.state.collectAsState()

    LaunchedEffect(Unit) { ShizukuManager.refresh() }

    var requestingRoot by remember { mutableStateOf(false) }
    var requestingShizuku by remember { mutableStateOf(false) }
    var requestingDhizuku by remember { mutableStateOf(false) }

    LaunchedEffect(settings.rootAccess) {
        if (settings.rootAccess && RootManager.state.value == RootState.UNKNOWN) {
            RootManager.request()
        }
    }

    LaunchedEffect(requestingShizuku) {
        if (requestingShizuku) {
            val granted = ShizukuManager.ensurePermission()
            act.run { setShizukuAccess(granted) }
            requestingShizuku = false
        }
    }

    LaunchedEffect(requestingDhizuku) {
        if (requestingDhizuku) {
            val granted = DhizukuBridge.ensurePermission()
            act.run { setDhizukuAccess(granted) }
            requestingDhizuku = false
        }
    }

    SettingsPageScaffold(
        stringResource(R.string.settings_page_access),
        onBack,
        subtitle = stringResource(R.string.settings_access_subtitle),
    ) {
        SettingsGroup(stringResource(R.string.settings_group_root)) {
            switch(
                tr(R.string.settings_root_access),
                settings.rootAccess,
                { enabled ->
                    if (enabled && !settings.rootAccess) {
                        requestingRoot = true
                        onRootAccess(true)
                        scope.launch {
                            val granted = RootManager.request()
                            if (granted) {
                                act.run { setRootAccess(true) }
                            } else {
                                act.run { setRootAccess(false) }
                            }
                            requestingRoot = false
                        }
                    } else if (!enabled) {
                        requestingRoot = false
                        RootManager.reset()
                        act.run { setRootAccess(false) }
                    }
                },
                summary = when {
                    requestingRoot -> tr(R.string.common_loading)
                    liveRootState == RootState.DENIED -> tr(R.string.settings_root_denied)
                    settings.rootAccess && liveRootState == RootState.GRANTED -> tr(R.string.settings_root_on)
                    settings.rootAccess -> tr(R.string.settings_root_on)
                    else -> tr(R.string.settings_root_off)
                },
                icon = Icons.Rounded.Shield,
                tint = Tint.ERROR,
            )
        }

        SettingsGroup(
            stringResource(R.string.settings_shizuku),
            footer = stringResource(R.string.settings_shizuku_footer, 11),
        ) {
            switch(
                tr(R.string.settings_shizuku_access),
                settings.shizukuAccess,
                { enabled ->
                    if (enabled && !settings.shizukuAccess) {
                        requestingShizuku = true
                    } else if (!enabled) {
                        requestingShizuku = false
                        act.run { setShizukuAccess(false) }
                    }
                },
                summary = when {
                    requestingShizuku -> tr(R.string.common_loading)
                    !settings.shizukuAccess -> tr(R.string.settings_shizuku_off)
                    !ShizukuManager.isInstalled -> tr(R.string.settings_shizuku_install)
                    shizuku == ShizukuManager.Status.NOT_RUNNING -> tr(R.string.settings_shizuku_start)
                    shizuku == ShizukuManager.Status.NEEDS_PERMISSION -> tr(R.string.settings_shizuku_allow)
                    else -> tr(R.string.settings_shizuku_on)
                },
                icon = Icons.Rounded.SdStorage,
            )
        }

        SettingsGroup(
            stringResource(R.string.settings_dhizuku),
            footer = stringResource(R.string.settings_dhizuku_footer),
        ) {
            switch(
                tr(R.string.settings_dhizuku_access),
                settings.dhizukuAccess,
                { enabled ->
                    if (enabled && !settings.dhizukuAccess) {
                        requestingDhizuku = true
                    } else if (!enabled) {
                        requestingDhizuku = false
                        act.run { setDhizukuAccess(false) }
                    }
                },
                summary = when {
                    requestingDhizuku -> tr(R.string.common_loading)
                    !settings.dhizukuAccess -> tr(R.string.settings_dhizuku_off)
                    DhizukuBridge.state.value == DhizukuBridge.Status.UNAVAILABLE -> tr(R.string.settings_dhizuku_install)
                    DhizukuBridge.state.value == DhizukuBridge.Status.NEEDS_PERMISSION -> tr(R.string.settings_dhizuku_allow)
                    else -> tr(R.string.settings_dhizuku_on)
                },
                icon = Icons.Rounded.AdminPanelSettings,
            )
        }
    }
}

@Composable
fun OpeningPage(settings: StrataSettings, act: SettingsActions, onBack: () -> Unit) {
    SettingsPageScaffold(stringResource(R.string.settings_page_opening), onBack, subtitle = stringResource(R.string.settings_opening_subtitle)) {
        if (settings.openDefaults.isEmpty()) {
            Text(
                text = stringResource(R.string.settings_opening_none),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            SettingsGroup(stringResource(R.string.settings_group_defaults), footer = stringResource(R.string.settings_defaults_footer)) {
                settings.openDefaults.toSortedMap().forEach { (ext, target) ->
                    link(
                        if (ext == OpenResolver.NO_EXTENSION) tr(R.string.settings_no_extension) else ".$ext",
                        summary = when (target) {
                            is OpenTarget.BuiltIn -> tr(target.opener.label)
                            is OpenTarget.App -> target.packageName
                        },
                        chevron = false,
                        trailing = {
                            IconButton(onClick = { act.run { removeOpenDefault(ext) } }) {
                                Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.settings_remove_default, ext))
                            }
                        },
                    ) {}
                }
            }
        }
    }
}

@Composable
fun BackupPage(act: SettingsActions, activeFolder: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    androidx.activity.compose.BackHandler(onBack = onBack)
    val snackbar = remember { SnackbarHostState() }
    val downloads = remember { Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) }
    val target = remember(activeFolder) { BackupLocations.exportFolder(activeFolder, downloads) }
    var refresh by remember { mutableIntStateOf(0) }
    val backups by produceState(emptyList<File>(), target, refresh) {
        value = withContext(Dispatchers.IO) { BackupLocations.find(listOf(target, File(downloads, "Strata"))) }
    }
    var importing by remember { mutableStateOf<(suspend () -> String)?>(null) }
    var importName by remember { mutableStateOf<String?>(null) }
    var resetting by remember { mutableStateOf(false) }

    fun importText(read: suspend () -> String) {
        scope.launch {
            val message = runCatching { act.repo.importBackup(read()) }
                .fold({ n -> trPlural(R.plurals.settings_restored, n, n) }, { e -> e.message ?: tr(R.string.settings_backup_read_failed) })
            snackbar.showSnackbar(message)
        }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            importName = uri.lastPathSegment
            importing = {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        val bytes = input.readNBytesCompat(BackupLocations.MAX_BYTES.toInt() + 1)
                        require(bytes.size <= BackupLocations.MAX_BYTES) { tr(R.string.settings_backup_too_big) }
                        String(bytes, Charsets.UTF_8)
                    } ?: throw IllegalArgumentException(tr(R.string.settings_backup_open_failed))
                }
            }
        }
    }

    SettingsPageScaffold(stringResource(R.string.settings_page_backup), onBack, subtitle = stringResource(R.string.settings_backup_subtitle), snackbar = snackbar) {
        SettingsGroup(stringResource(R.string.settings_group_export), footer = stringResource(R.string.settings_export_footer)) {
            link(tr(R.string.settings_export), summary = tr(R.string.settings_export_summary, target.path), icon = Icons.Rounded.FileOpen, tint = Tint.PRIMARY) {
                scope.launch {
                    val result = runCatching {
                        val text = act.repo.exportBackup()
                        withContext(Dispatchers.IO) {
                            target.mkdirs()
                            val file = LocalFileAccess.file(File(target, SettingsBackup.fileName(System.currentTimeMillis())).path)
                            LocalFileAccess.openOutput(file).use { it.write(text.toByteArray()) }
                            file
                        }
                    }
                    refresh++
                    snackbar.showSnackbar(result.fold({ tr(R.string.settings_saved, it.name) }, { e -> tr(R.string.settings_save_failed, e.message ?: e.javaClass.simpleName) }))
                }
            }
        }
        SettingsGroup(stringResource(R.string.settings_group_import)) {
            backups.take(8).forEach { f ->
                link(f.name, summary = "${f.parent} · ${Formatters.date(f.lastModified())}", icon = Icons.Rounded.SettingsBackupRestore) {
                    importName = f.name
                    importing = { withContext(Dispatchers.IO) { LocalFileAccess.openInput(LocalFileAccess.file(f.path)).use { String(it.readBytes(), Charsets.UTF_8) } } }
                }
            }
            link(tr(R.string.settings_choose_file), summary = tr(R.string.settings_choose_file_summary), icon = Icons.Rounded.FolderOpen) {
                picker.launch(arrayOf("application/json", "text/plain", "application/octet-stream"))
            }
        }
        SettingsGroup(stringResource(R.string.settings_group_reset)) {
            link(tr(R.string.settings_reset_all), summary = tr(R.string.settings_reset_all_summary), icon = Icons.Rounded.RestartAlt, tint = Tint.ERROR) {
                resetting = true
            }
        }
    }
    importing?.let { read ->
        val name = importName
        AlertDialog(
            onDismissRequest = { importing = null },
            title = { Text(stringResource(R.string.settings_restore_title)) },
            text = { Text(if (name != null) stringResource(R.string.settings_restore_body, name) else stringResource(R.string.settings_restore_body_unnamed)) },
            confirmButton = { TextButton(onClick = { importing = null; importText(read) }) { Text(stringResource(R.string.settings_restore)) } },
            dismissButton = { TextButton(onClick = { importing = null }) { Text(stringResource(R.string.common_cancel)) } },
        )
    }
    if (resetting) {
        AlertDialog(
            onDismissRequest = { resetting = false },
            title = { Text(stringResource(R.string.settings_reset_title)) },
            text = { Text(stringResource(R.string.settings_reset_body)) },
            confirmButton = {
                TextButton(onClick = {
                    resetting = false
                    scope.launch {
                        act.repo.resetToDefaults()
                        snackbar.showSnackbar(tr(R.string.settings_reset_done))
                    }
                }) { Text(stringResource(R.string.settings_reset), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { resetting = false }) { Text(stringResource(R.string.common_cancel)) } },
        )
    }
}

private fun java.io.InputStream.readNBytesCompat(limit: Int): ByteArray {
    val out = java.io.ByteArrayOutputStream()
    val buf = ByteArray(8192)
    while (out.size() < limit) {
        val n = read(buf, 0, minOf(buf.size, limit - out.size()))
        if (n < 0) break
        out.write(buf, 0, n)
    }
    return out.toByteArray()
}

private const val LICENSE_ID = "GNU AGPL-3.0-or-later"

private fun components(): List<Pair<String, String>> = listOf(
    tr(R.string.settings_credit_compose) to "Apache-2.0",
    tr(R.string.settings_credit_kotlin) to "Apache-2.0",
    "MaterialKolor" to "MIT",
    "Coil" to "Apache-2.0",
    "Telephoto" to "Apache-2.0",
    "Media3 (ExoPlayer)" to "Apache-2.0",
    tr(R.string.settings_credit_sora) to "LGPL-2.1",
    "Oniguruma" to "BSD-2-Clause",
    "jadx" to "Apache-2.0",
    tr(R.string.settings_credit_smali) to "BSD-3-Clause",
    "dex2jar" to "Apache-2.0",
    tr(R.string.settings_credit_d8) to tr(R.string.settings_license_d8),
    "apksig" to "Apache-2.0",
    "ARSCLib" to "Apache-2.0",
    "Bouncy Castle" to "MIT",
    tr(R.string.settings_credit_commons) to "Apache-2.0",
    "XZ for Java" to "0BSD",
    "zstd-jni" to "BSD-2-Clause",
    "zip4j" to "Apache-2.0",
    "UnRAR" to tr(R.string.settings_license_unrar),
    "sshj" to "Apache-2.0",
    "smbj" to "Apache-2.0",
    "smbj-rpc (dcerpc)" to "BSD-3-Clause",
    tr(R.string.settings_credit_okhttp) to "Apache-2.0",
    "dnsjava" to "BSD-3-Clause",
    tr(R.string.settings_credit_ftpserver) to "Apache-2.0",
    "Ktor" to "Apache-2.0",
    "libsu" to "Apache-2.0",
    "Shizuku API" to "MIT",
    "Multiplatform Markdown Renderer" to "Apache-2.0",
    tr(R.string.settings_credit_pdfium) to tr(R.string.settings_license_pdfium),
    tr(R.string.settings_credit_sounds) to tr(R.string.settings_credit_sounds_license),
)

@Composable
fun AboutPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val icon = remember { runCatching { context.packageManager.getApplicationIcon(context.packageName).toBitmap(192, 192).asImageBitmap() }.getOrNull() }
    SettingsPageScaffold(stringResource(R.string.settings_page_about), onBack) {
        Row(Modifier.fillMaxWidth().padding(top = 16.dp, start = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) Image(icon, contentDescription = null, modifier = Modifier.size(64.dp))
            Column(Modifier.padding(start = 16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Strata", style = MaterialTheme.typography.headlineSmall)
                Text(
                    stringResource(R.string.settings_about_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("about-version"),
                )
                Text(stringResource(R.string.settings_about_tagline), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        SettingsGroup(stringResource(R.string.settings_group_license)) {
            link(
                LICENSE_ID,
                summary = tr(R.string.settings_license_summary),
                icon = Icons.Rounded.Gavel,
                chevron = false,
            ) {}
        }
        SettingsGroup(stringResource(R.string.settings_group_device)) {
            link(tr(R.string.settings_device_android, Build.VERSION.RELEASE, Build.VERSION.SDK_INT), summary = "${Build.MANUFACTURER} ${Build.MODEL}", icon = Icons.Rounded.PhoneAndroid, chevron = false) {}
        }
        SettingsGroup(stringResource(R.string.settings_group_components), footer = stringResource(R.string.settings_components_footer)) {
            components().forEach { (name, license) -> link(name, summary = license, chevron = false) {} }
        }
    }
}

@Composable
fun TextSettingDialog(
    title: String,
    initial: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    hint: String? = null,
    extra: Pair<String, String>? = null,
) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    supportingText = hint?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (extra != null) TextButton(onClick = { text = extra.second }) { Text(extra.first) }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(text.trim()) }) { Text(stringResource(R.string.common_save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } },
    )
}



private fun InstallBackend.label(): String = when (this) {
    InstallBackend.Session -> tr(R.string.install_backend_session)
    InstallBackend.Root -> tr(R.string.install_backend_root)
    InstallBackend.Shizuku -> tr(R.string.install_backend_shizuku)
    InstallBackend.Dhizuku -> tr(R.string.install_backend_dhizuku)
}

private fun InstallBackend.icon(): ImageVector = when (this) {
    InstallBackend.Session -> Icons.Rounded.Android
    InstallBackend.Root -> Icons.Rounded.Shield
    InstallBackend.Shizuku -> Icons.Rounded.SdStorage
    InstallBackend.Dhizuku -> Icons.Rounded.AdminPanelSettings
}

private fun InstallBackend.hintWhenOff(): String = when (this) {
    InstallBackend.Root -> tr(R.string.install_backend_root_hint)
    InstallBackend.Shizuku -> tr(R.string.install_backend_shizuku_hint)
    InstallBackend.Dhizuku -> tr(R.string.install_backend_dhizuku_hint)
    InstallBackend.Session -> ""
}





@Composable
fun PriorityList(
    items: List<InstallBackend>,
    onMove: (from: Int, to: Int) -> Unit,
    enabled: Boolean = true,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .graphicsLayer(alpha = if (enabled) 1f else 0.38f),
    ) {
        items.forEachIndexed { index, backend ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    Icon(
                        imageVector = backend.icon(),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = backend.label(),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .weight(1f),
                    )
                    
                    IconButton(
                        onClick = { if (enabled && index > 0) onMove(index, index - 1) },
                        enabled = enabled && index > 0,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowUpward,
                            contentDescription = stringResource(R.string.explorer_move_up),
                            tint = if (enabled && index > 0) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                    }
                    
                    IconButton(
                        onClick = { if (enabled && index < items.lastIndex) onMove(index, index + 1) },
                        enabled = enabled && index < items.lastIndex,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowDownward,
                            contentDescription = stringResource(R.string.explorer_move_down),
                            tint = if (enabled && index < items.lastIndex) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }
    }
}
