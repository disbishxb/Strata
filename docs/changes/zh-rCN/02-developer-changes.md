# 开发者更新

> 除“用户可见”外的所有改动；相对基线 Strata 1.0.0。

## 1. 新增模块与文件
- `core/control/AppControlBridge.kt`：应用控制后端与能力。
- `core/dhizuku/DhizukuBridge.kt`：Dhizuku 初始化/权限/委托/挂起/隐藏。
- `core/export/FileListExport.kt`：文件列表导出。
- `core/fs/RestrictedDirs.kt`：受限目录标记。
- `core/fs/SplitChannel.kt`：分卷虚拟通道。
- `core/media/BundleIconLoader.kt`：Bundle 图标。
- `core/model/ActionMenu.kt`、`core/model/DrawerBox.kt`：长按菜单与抽屉盒模型。
- `core/text/StgxRegistry.kt`、`StgxParser.kt`、`StgxIcons.kt`、`StgxIconLoader.kt`：STGX。
- `core/util/AppIconLoader.kt`、`core/util/QrCode.kt`：图标与二维码。
- `feature/apps/AppControlSheet.kt`：应用控制 sheet。
- `feature/drawerbox/DrawerBoxScreen.kt`：抽屉盒设置屏。
- `feature/export/ExportListDialog.kt`：导出对话框。
- `feature/settings/DrawerBoxPage.kt`、`StartLocationPage.kt`：设置页。
- `feature/termux/CommandRunnerScreen.kt`：命令运行器。
- 资源：`ic_python_logo.xml`、`colors_python.xml`、`art_unkown.webp`、`art_warning.webp`、`strings_export.xml` 各语言。

## 2. API 与架构变化
- `InstallBackend`/`InstallPolicy`/`InstallAccess`：安装后端策略。
- `ApkInstaller`：新增 `installByPolicy`、`installWithShizuku`、`installWithDhizuku`、`installWithThirdParty`、`installBundleWithThirdParty`、`writePrivilegedSession`。
- `AppControlBridge`：`Backend`、`Capability`、`resolve/resolveFor`、`forceStop/setEnabled/setHidden/setSuspended`、`autoLiftLimit`、`hiddenPackages/suspendedPackages`。
- `DhizukuBridge`：`Status`、`init/refresh/ensurePermission/ensurePackageDelegation/setPackagesSuspended/setApplicationHidden/isApplicationHidden/isSuspended`。
- `RootManager`：新增 `flavor`、`probeSuExec`、`detectFlavor`、`copyFileToCache`。
- `ShizukuManager`：新增 `service`、`exec`；AIDL 新增 `exec`。
- `ShizukuFsService`：新增 `exec`。
- `FileAccess`：新增 `materialize` 默认实现。
- `DirectoryLister`：新增 `missing`/`fallback`/`remote` 参数与 `Listing.viaRoot`。
- `RemoteTransfers`：新增 `viaTemp`、`measure`、`Progress.SILENT`、`Place.of`。
- `SafFileSystem`：新增 `termuxHome`、`label`、`release`、`TERMUX_AUTHORITY`。
- `SftpFileSystem`：新增 host key 捕获/拒绝/`newlyTrustedHostKey`。
- `SmbFileSystem`：新增 `splitUser`、`NoShareException`、`listShares`。
- `WebDavFileSystem`：新增 `PinningTrust`、`fingerprint`、`parseMultistatus`、`decodeHref`。
- `RemoteUri`：新增 `id/path/parent/child/name/display`。
- `TermuxViewModel`：新增 `CommandRunState`、`runCommand`、`cancelCommand`、`clearCommandResult`、`writeLog`。
- `TextMateEngine`：新增 STGX 动态语法、`loadDynamicGrammar`、`previewColorsFor`、`themes`。
- `GrammarCatalog`：新增 `extensionsOf`、`includedScopes`。
- `TextEncoding`：新增 `choices`（带 label）、`labelOf`、windows-1252 启发式、GB 严格解码、`detectSample`。
- `TextEditorViewModel`：新增 `charsetLabel`、`resolveLanguage` STGX 兜底、`setSaveCharset(choice)`。
- `EditorController`：新增 `colorUnderCursor/replaceColor/insertText/replaceSelection/select/lineText`。
- `SettingsRepository`：新增安装后端/优先级/第三方/控制后端/抽屉盒字段与方法。
- `SettingsPage`：新增 `DRAWER_BOX`、`START_LOCATION`、`GRAMMAR`。
- `MainActivity`：新增 `L10n.source` 初始化、`incomingPdf/incomingFile`。
- `StrataApp`：新增 `StgxRegistry.reload`、`DhizukuBridge.init`。
- `AndroidStrings`：每次 `AppLocales.wrap`。
- `core/fs/RestrictedDirs.kt`：新增 `ownAppDirsInRestricted(context, path)`。
- `feature/explorer/components/PaneColumn.kt`：`EmptyFolder` 新增 `onOpen` 参数，通过 `ExpressiveEmptyState` 的 `action` 槽位渲染私有文件夹列表。
- `feature/explorer/ExplorerViewModel.kt`：`navigateUp` 在 root 与 Shizuku 都关闭且当前为私有文件夹时，恢复对应 `Android/data` / `Android/obb` 的受限 pane 状态。
- 新增字符串 `explorer_restricted_own_dirs`（en / zh-rCN / ru / fa）。

## 3. 依赖
- 新增 `io.github.iamr0s:Dhizuku-API:2.5.3`。
- 新增 `ru.solrudev.ackpine:ackpine-core/ktx/dhizuku/dhizuku-ktx:0.25.4`。
- 其余与 1.0.0 基本一致。

## 4. 构建
- `app/build.gradle.kts`：新增 Dhizuku/Ackpine、`strata.*` 属性、`cargoHostBuild`、ABI split、资源排除、`legacy29` 变体 targetSdk 29。
- `gradle/libs.versions.toml`：新增 Dhizuku/Ackpine 版本。
- `gradle.properties`：新增 `strata.versionName/Code`。

## 5. Manifest
- 新增权限：`CHANGE_COMPONENT_ENABLED_STATE`、`FORCE_STOP_PACKAGES`、`SUSPEND_APPS`、`com.android.permission.GET_INSTALLED_APPS`。
- 新增 `queries`：Termux、插件 FILE 动作、PROCESS_TEXT。
- `ShareService` 前台类型 specialUse 与描述。
- 新增 `PluginShareProvider`。

## 6. 数据格式
- 设置新增字段：安装后端/优先级/第三方/控制后端/抽屉盒。
- 备份导出/导入包含新字段；私有字段仍排除。
- STGX 语法文件格式与注册表。
- 保险库/书签/远程连接格式不变。

## 7. 内部行为与性能
- 受限目录物化到缓存（ApkDialogViewModel、FileAccess.materialize）。
- 分卷读取走 SplitChannel。
- 远程传输走临时目录。
- 图片/图标缓存与 Coil 工厂。
- STGX 语法动态加载与图标缓存。
- 编码探测改进（windows-1252/GB）。
- 编辑大文件分页与只读保护。

## 8. 测试
- 新增/调整与安装后端、应用控制、STGX、编码、导出相关的测试（按仓库实际）。

## 9. 本地化

- 新增日语（`ja`）：`values-ja/` 下 14 个 `strings_*.xml`（common / format / export / pdf / security / network / settings / grammar / files / model / devtools / editor / explorer / apk）。
- `AppLanguage.kt` 增加 `JAPANESE` 枚举与 `"ja"` 映射；`SecurityPages.kt` 的语言 `when` 补 `JAPANESE` 分支；`locales_config.xml` 增加 `<locale android:name="ja" />`；`values/strings_security.xml` 增加 `language_native_ja`、`language_japanese`。
- 其他语言同步：`values-fa`、`values-ru`、`values-zh-rCN` 的 `strings_security.xml` 补 `language_japanese`。
