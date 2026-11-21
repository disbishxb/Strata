# 50 Settings

> Meta-rule: after changing code, update this volume's byte sizes and entries; verify with `stat -c '%s %n'`. Byte unit: byte.

## A. Logic identical in fork and 1.0.0

### A.1 File routing table
| Path | 1.0.0 | fork | Responsibility |
|---|---|---|---|
| `feature/settings/SettingsScreen.kt` | 22672 | 23904 | Settings home/search/navigation; SettingsPage/SettingsEntry/SettingsIndex; SettingsActions |
| `feature/settings/SettingsPages.kt` | 37458 | 55798 | Settings pages: FileList/Behavior/Operations/Reader/Editor/Apps/Access/Opening/Backup/About; fork adds Install backends/priority/third-party/long-press menu |
| `feature/settings/AppearancePage.kt` | 18684 | 18684 | Appearance: theme mode/pure black/wallpaper color/custom colors/palette/contrast/font size/corners/icon shape/colored icons/animation/haptics/sound |
| `feature/settings/SecurityPages.kt` | 13566 | 13657 | Security: app lock/delay/lock screen/vault/secure screen; language page |
| `feature/settings/ToolbarPage.kt` | 19353 | 19353 | Toolbar zone editing: browse/select/long-press |
| `feature/settings/ZoneEditor.kt` | 23032 | 23032 | Drag zone editor: ZoneStyle/ZoneButton/ZoneDrag |
| `feature/settings/SettingsComponents.kt` | 15609 | 15609 | Settings components: SettingsPageScaffold/SettingsGroup/switch/link/choice/pick/slider/SettingNote/ConnectedChoice/Tint |
| `feature/settings/SupportPage.kt` | 4756 | 8849 | Support page; fork adds QR code/original author/fork maintainer |

### A.2 Work logic
- **Entry**: SettingsScreen holds page state; SettingsHome search + groups; SettingsIndex.search matches Chinese and English.
- **Dispatch**: when(page) to each Page; BackHandler returns home.
- **SettingsActions**: appearance/list/behavior/operations/security/editor/reader/actions; run { repo.block() }.
- **Settings components**: SettingsPageScaffold (large top bar + scroll + snackbar); SettingsGroup (SegmentedListItem); switch/link/choice/pick/slider.
- **ZoneEditor**: Zoned zones; drag/up-down move/in-out move/overflow callback.
- **Pages**: FileList/Behavior/Operations/Reader/Editor/Apps/Access/Opening/Backup/About.
- **Appearance page**: ThemePreview, SeedSwatches, StyleChips, IconShapes, TextSize slider.
- **Security page**: AppLock toggle/delay/lock screen; vault setup/unlock/change password/delete; secure screen.
- **Support page**: wallet list + copy.
- **Backup page**: export/import/reset.

## B. Logic unique to the fork

### B.1 Added-file routing table
| Path | fork | Responsibility |
|---|---|---|
| `feature/settings/DrawerBoxPage.kt` | 12877 | Drawer-box settings page: SidebarPreview + ZoneEditor |
| `feature/settings/StartLocationPage.kt` | 4505 | Start-location page: left/right pane selection and custom path |

### B.2 Added/changed logic
- **SettingsScreen**: `SettingsPage` adds `DRAWER_BOX`, `START_LOCATION`, `GRAMMAR`; `SettingsIndex.home` adds DRAWER_BOX; `SettingsIndex.entries` adds drawer_box/grammar/start_location entries; search supports English fallback.
- **SettingsPages**: `AppsPage` adds install-backend toggles/priority drag/third-party installer; `AccessPage` adds Dhizuku; `BehaviorPage` links to `StartLocationPage`; `FileListPage`/`OperationsPage`/`EditorSettingsPage` etc. are extended.
- **SupportPage**: the fork adds QR code, original author/fork maintainer group, save to gallery.
- **AppearancePage**: no structural additions, but depends on the fork's `KindColors`/`Stgx`.
- **SecurityPages**: no structural additions, but depends on the fork's `VaultAccess`.
- **ToolbarPage**: no structural additions.
- **ZoneEditor**: no structural additions.
- **SettingsComponents**: no structural additions.

## C. Logic unique to 1.0.0, removed or changed in the fork
- 1.0.0 has no `DrawerBoxPage.kt`, `StartLocationPage.kt`.
- 1.0.0 `SettingsPage` has no DRAWER_BOX/START_LOCATION/GRAMMAR.
- 1.0.0 `AppsPage` has no install backends/priority/third-party installer.
- 1.0.0 `AccessPage` has no Dhizuku.
- 1.0.0 `SupportPage` has no QR code/original author/fork maintainer.