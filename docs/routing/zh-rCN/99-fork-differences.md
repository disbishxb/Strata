# 99 fork 差异总表

> 与 1.0.0 对比。字节单位 byte。

## 1. 新增文件
| 路径 | 字节 | 所属分册 |
|---|---|---|
| `core/control/AppControlBridge.kt` | 10622 | 40 |
| `core/dhizuku/DhizukuBridge.kt` | 5106 | 40 |
| `core/export/FileListExport.kt` | 12350 | 10/20 |
| `core/fs/RestrictedDirs.kt` | 1054 | 30 |
| `core/fs/SplitChannel.kt` | 2747 | 30 |
| `core/media/BundleIconLoader.kt` | 1347 | 10 |
| `core/model/ActionMenu.kt` | 615 | 10 |
| `core/model/DrawerBox.kt` | 1632 | 10/50 |
| `core/text/StgxRegistry.kt` | 4982 | 80 |
| `core/text/StgxParser.kt` | 3932 | 80 |
| `core/text/StgxIcons.kt` | 3159 | 80 |
| `core/text/StgxIconLoader.kt` | 1246 | 80 |
| `core/util/AppIconLoader.kt` | 2793 | 10 |
| `core/util/QrCode.kt` | 2403 | 10 |
| `feature/apps/AppControlSheet.kt` | 7667 | 40 |
| `feature/drawerbox/DrawerBoxScreen.kt` | 7175 | 50 |
| `feature/export/ExportListDialog.kt` | 14778 | 20 |
| `feature/settings/DrawerBoxPage.kt` | 12877 | 50 |
| `feature/settings/StartLocationPage.kt` | 4505 | 50 |
| `feature/termux/CommandRunnerScreen.kt` | 19487 | 70 |
| `feature/grammar/GrammarScreen.kt` | 30791 | 80 |
| `res/values/strings_grammar.xml` | 3784 | A0 |
| `res/values-zh-rCN/strings_grammar.xml` | 3760 | A0 |
| `res/values-ru/strings_grammar.xml` | 4493 | A0 |
| `res/values-fa/strings_grammar.xml` | 4244 | A0 |
| `res/drawable/ic_python_logo.xml` | 2868 | A0 |
| `res/values/colors_python.xml` | 77 | A0 |
| `res/values-night/colors_python.xml` | 77 | A0 |
| `res/values/strings_export.xml` | 1243 | A0 |
| `res/values-fa/strings_export.xml` | 1445 | A0 |
| `res/values-ru/strings_export.xml` | 1513 | A0 |
| `res/values-zh-rCN/strings_export.xml` | 1263 | A0 |
| `res/drawable-nodpi/art_unkown.webp` | 见快照 | A0 |
| `res/drawable-nodpi/art_warning.webp` | 见快照 | A0 |

| `res/values-ja/strings_apk.xml` | 25675 | A0 |
| `res/values-ja/strings_common.xml` | 4568 | A0 |
| `res/values-ja/strings_devtools.xml` | 25316 | A0 |
| `res/values-ja/strings_editor.xml` | 14152 | A0 |
| `res/values-ja/strings_explorer.xml` | 18991 | A0 |
| `res/values-ja/strings_export.xml` | 1474 | A0 |
| `res/values-ja/strings_files.xml` | 19050 | A0 |
| `res/values-ja/strings_format.xml` | 2165 | A0 |
| `res/values-ja/strings_grammar.xml` | 4436 | A0 |
| `res/values-ja/strings_model.xml` | 12975 | A0 |
| `res/values-ja/strings_network.xml` | 29175 | A0 |
| `res/values-ja/strings_pdf.xml` | 9346 | A0 |
| `res/values-ja/strings_security.xml` | 10458 | A0 |
| `res/values-ja/strings_settings.xml` | 45541 | A0 |

## 2. 删除文件
- 无。

## 3. 体积显著变化的既有文件
| 路径 | 1.0.0 | fork | 所属分册 |
|---|---|---|---|
| `MainActivity.kt` | 6426 | 6317 | 10 |
| `StrataApp.kt` | 1858 | 2021 | 10 |
| `ui/AppShell.kt` | 29656 | 38559 | 10 |
| `ui/drawer/StrataDrawer.kt` | 26357 | 27590 | 10 |
| `ui/theme/KindColors.kt` | 1534 | 2345 | 10 |
| `core/data/SettingsRepository.kt` | 28926 | 35722 | 10/50 |
| `core/data/StartFolders.kt` | 655 | 935 | 10 |
| `core/root/RootManager.kt` | 3259 | 6860 | 70 |
| `core/shizuku/ShizukuManager.kt` | 5389 | 5942 | 70 |
| `core/shizuku/ShizukuFsService.kt` | 317 | 635 | 70 |
| `core/apk/install/ApkInstaller.kt` | 6301 | 15998 | 40 |
| `core/apk/install/InstallResult.kt` | 4334 | 4532 | 40 |
| `core/apk/install/PmInstall.kt` | 2424 | 2457 | 40 |
| `core/apk/ApkCloner.kt` | 10471 | 10471 | 40 |
| `core/apk/InstalledApps.kt` | 3135 | 3372 | 40 |
| `feature/apk/ApkDialog.kt` | 27729 | 29901 | 40 |
| `feature/apk/ApkDialogViewModel.kt` | 11031 | 15596 | 40 |
| `feature/apk/SplitInstallDialog.kt` | 13145 | 13726 | 40 |
| `feature/apk/SplitInstallViewModel.kt` | 6600 | 7810 | 40 |
| `feature/apps/AppsScreen.kt` | 23440 | 35834 | 40 |
| `feature/apps/AppsViewModel.kt` | 5993 | 22157 | 40 |
| `feature/explorer/ExplorerScreen.kt` | 37180 | 41867 | 20 |
| `feature/explorer/ExplorerViewModel.kt` | 66400 | 70115 | 20 |
| `feature/explorer/components/FileRow.kt` | 17533 | 26926 | 20 |
| `feature/explorer/components/ItemActionSheet.kt` | 12562 | 14562 | 20 |
| `feature/explorer/components/PaneColumn.kt` | 28876 | 32610 | 20 |
| `feature/explorer/components/ExplorerBottomBar.kt` | 17564 | 17843 | 20 |
| `feature/explorer/components/ToolDialogs.kt` | 24682 | 31337 | 20 |
| `feature/editor/TextMateEngine.kt` | 3956 | 7928 | 80 |
| `feature/editor/TextEditorScreen.kt` | 44876 | 45544 | 80 |
| `feature/editor/TextEditorViewModel.kt` | 20456 | 21515 | 80 |
| `core/text/TextEncoding.kt` | 5502 | 8989 | 80 |
| `core/text/GrammarCatalog.kt` | 4382 | 4612 | 80 |
| `feature/settings/SettingsScreen.kt` | 22672 | 23904 | 50 |
| `feature/settings/SettingsPages.kt` | 37458 | 55798 | 50 |
| `feature/settings/SupportPage.kt` | 4756 | 8849 | 50 |
| `feature/termux/TermuxViewModel.kt` | 396 | 4132 | 70 |
| `app/build.gradle.kts` | 9300 | 10500 | A0 |
| `gradle/libs.versions.toml` | 10200 | 10300 | A0 |
| `gradle.properties` | 304 | 396 | A0 |
| `native/src/crypt.rs` | 6000 | 6177 | A0 |
| `native/src/hexio.rs` | 8200 | 8388 | A0 |
| `native/src/lib.rs` | 14300 | 14677 | A0 |
| `native/src/pdfium.rs` | 35500 | 36403 | A0 |
| `native/src/rar.rs` | 12200 | 12473 | A0 |
| `res/values/strings.xml` | 129 | 1199 | A0 |
| `res/values/strings_apk.xml` | 19473 | 21535 | A0 |
| `res/values/strings_explorer.xml` | 16602 | 16983 | A0 |
| `res/values/strings_files.xml` | 10290 | 16676 | A0 |
| `res/values/strings_model.xml` | 11839 | 11839 | A0 |
| `res/values/strings_network.xml` | 23459 | 23778 | A0 |
| `res/values/strings_security.xml` | 8747 | 8880 | A0 |
| `res/values/strings_settings.xml` | 32834 | 42734 | A0 |
| `res/values-zh-rCN/strings_apk.xml` | 19391 | 21415 | A0 |
| `res/values-zh-rCN/strings_explorer.xml` | 16167 | 16546 | A0 |
| `res/values-zh-rCN/strings_files.xml` | 10152 | 16696 | A0 |
| `res/values-zh-rCN/strings_network.xml` | 23536 | 23843 | A0 |
| `res/values-zh-rCN/strings_security.xml` | 8097 | 8416 | A0 |
| `res/values-zh-rCN/strings_settings.xml` | 32409 | 43293 | A0 |

## 4. 依赖差异
- 新增 `io.github.iamr0s:Dhizuku-API:2.5.3`。
- 新增 `ru.solrudev.ackpine:ackpine-core:0.25.4`、`ackpine-ktx`、`ackpine-dhizuku`、`ackpine-dhizuku-ktx`。
- 其余依赖与 1.0.0 基本一致。

## 5. Manifest 差异
- fork 新增权限：`CHANGE_COMPONENT_ENABLED_STATE`、`FORCE_STOP_PACKAGES`、`SUSPEND_APPS`、`com.android.permission.GET_INSTALLED_APPS`。
- fork 新增 `queries`：Termux、插件 FILE 动作、PROCESS_TEXT。
- fork `ShareService` 前台类型 specialUse 与描述。
- fork 新增 `PluginShareProvider`。
- 其余组件与 1.0.0 一致。