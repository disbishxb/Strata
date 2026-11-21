# 10 入口与外壳

> 元规则：改代码后更新本册字节数与条目；用 `stat -c '%s %n'` 核对。字节单位 byte。

## A. fork 与 1.0.0 相同的逻辑

### A.1 文件路由表
| 路径 | 1.0.0 | fork | 职责 |
|---|---|---|---|
| `MainActivity.kt` | 6426 | 6317 | 唯一 Activity；LAUNCHER + VIEW(file/content) |
| `StrataApp.kt` | 1858 | 2021 | Application；全局初始化 |
| `ui/AppShell.kt` | 29656 | 38559 | 外壳：路由、抽屉、AnimatedContent、OpenDialog、Screen 分发 |
| `ui/RouteStack.kt` | 1739 | 1739 | 导航栈；RouteEntry 实现 ViewModelStoreOwner |
| `ui/SheetStates.kt` | 715 | 715 | 全局 sheet 状态 |
| `ui/LocalUiDensity.kt` | 319 | 319 | LocalUiDensity / LocalSettings |
| `ui/Bidi.kt` | 1066 | 1066 | RTL 隔离与文本方向 |
| `ui/ActionIcons.kt` | 6751 | 6925 | 动作图标映射 |
| `ui/TextClipboard.kt` | 1121 | 1121 | 剪贴板封装 |
| `ui/i18n/AppLocales.kt` | 3212 | 3263 | 语言选择/包装；AndroidStrings |
| `core/i18n/L10n.kt` | 1126 | 1126 | 字符串源注册表；tr/trPlural/separator |
| `core/i18n/AppLanguage.kt` | 1053 | 1135 | 语言枚举与 tag 解析 |
| `core/data/SettingsRepository.kt` | 28926 | 35722 | DataStore 设置读写 |
| `core/data/SettingsBackup.kt` | 3847 | 3847 | 设置备份编解码 |
| `core/data/BackupLocations.kt` | 892 | 892 | 备份位置 |
| `core/data/StartFolders.kt` | 655 | 935 | 启动目录解析 |
| `core/permission/StorageAccess.kt` | 2990 | 2990 | 存储权限状态与请求 |
| `ui/security/AppLock.kt` | 3538 | 3538 | 应用锁策略与生命周期 |
| `ui/security/DeviceAuth.kt` | 4755 | 4755 | 生物识别/设备凭据 |
| `ui/security/VaultKeystore.kt` | 2532 | 2532 | 保险库密钥 |
| `ui/security/LockScreen.kt` | 7208 | 7208 | 锁屏 UI |
| `ui/theme/Theme.kt` | 7052 | 7052 | StrataTheme、颜色、纯黑 |
| `ui/theme/Type.kt` | 362 | 362 | 字体 |
| `ui/theme/Shape.kt` | 1453 | 1453 | 形状 |
| `ui/theme/KindColors.kt` | 1534 | 2345 | 文件类型色 |
| `core/open/Opener.kt` | 4653 | 4791 | Opener/OpenTarget/OpenResolver/OpenAs |
| `core/open/OpenDefaults.kt` | 1129 | 1129 | 默认打开方式编解码 |
| `core/open/IncomingFiles.kt` | 2654 | 2654 | 外部 Uri 定位 |
| `core/open/IncomingPaths.kt` | 3471 | 3471 | 外部路径规范化 |
| `core/util/IntentUtils.kt` | 2970 | 2970 | FileProvider Uri、打开、分享 |
| `core/root/RootManager.kt` | 3259 | 6860 | root 探测/绑定/执行 |
| `core/vault/Vault.kt` | 1437 | 1437 | 保险库入口/连接 |
| `core/sound/UiSounds.kt` | 2864 | 2864 | 音效策略与设备 |
| `core/sound/SoundPoolDevice.kt` | 4866 | 4866 | SoundPool 实现 |
| `core/media/ApkIcons.kt` | 1646 | 1646 | Coil APK 图标 |
| `ui/drawer/StrataDrawer.kt` | 26357 | 27590 | 抽屉 UI |
| `ui/expressive/ExpressiveArt.kt` | 11223 | 11509 | 插画 |
| `ui/expressive/ExpressiveDialog.kt` | 5237 | 5237 | 对话框 |
| `ui/expressive/ExpressiveMenu.kt` | 2862 | 2862 | 菜单 |
| `ui/expressive/MorphShape.kt` | 2961 | 2961 | 形变形状 |

### A.2 工作逻辑
1. **启动**：MainActivity.onCreate → installSplashScreen → enableEdgeToEdge → DeviceAuth.attach → SettingsRepository → 同步取首帧设置（≤500ms）→ 应用 FLAG_SECURE/AppLock/UiSounds/Formatters → setContent → StrataTheme → AppLockGate → AppShell。
2. **全局初始化**：StrataApp.onCreate → AppLocales.install → Vault.init → AppLock.track → RootManager.init → SafFileSystem.appContext → ShizukuManager.init → UiSounds.device。
3. **路由**：RouteStack.push/pop/replace；AppShell 用 AnimatedContent + contentKey=key；离开栈后清 VM 与 saveable。
4. **抽屉**：StrataDrawerContent 接收 DrawerActions；onShortcut/onBookmark/onStorage 等统一关抽屉+入栈。
5. **i18n**：AppLocales.wrap 在 API<33 包装 Context；AndroidStrings 每次 ctx() 重新 wrap（fork 改动）。支持语言：en / fa / ru / zh-CN / ja（fork 新增日语）。
6. **设置仓库**：DataStore 读写；editAppearance/List/Behavior/Operations/Security/Editor/Reader/Actions。
7. **安全**：AppLock.apply → AppLockPolicy；DeviceAuth 生物识别；LockScreen 解锁。
8. **打开方式**：OpenResolver.resolve → defaults 优先 → builtIn；OpenTarget BuiltIn/App。
9. **root/vault/sound/theme**：各自 init 与全局状态。

## B. fork 独有的逻辑

### B.1 新增文件路由表
| 路径 | fork | 职责 |
|---|---|---|
| `core/control/AppControlBridge.kt` | 10622 | 应用启用/停用/隐藏/挂起；AUTO 后端 |
| `core/dhizuku/DhizukuBridge.kt` | 5106 | Dhizuku 初始化/权限/委托/挂起/隐藏 |
| `core/export/FileListExport.kt` | 12350 | 文件列表导出 |
| `core/fs/RestrictedDirs.kt` | 1054 | 受限 Android 目录标记 |
| `core/fs/SplitChannel.kt` | 2747 | 分卷读取虚拟通道 |
| `core/media/BundleIconLoader.kt` | 1347 | APKS/XAPK/APKM 图标 |
| `core/model/ActionMenu.kt` | 615 | 长按菜单项 |
| `core/model/DrawerBox.kt` | 1632 | 抽屉盒模型 |
| `core/text/StgxRegistry.kt` | 4982 | STGX 注册表 |
| `core/text/StgxParser.kt` | 3932 | STGX 解析/编译 |
| `core/text/StgxIcons.kt` | 3159 | STGX 图标 |
| `core/text/StgxIconLoader.kt` | 1246 | base64 图标加载 |
| `core/util/AppIconLoader.kt` | 2793 | 应用图标加载 |
| `core/util/QrCode.kt` | 2403 | 二维码 |
| `feature/apps/AppControlSheet.kt` | 7667 | 应用控制 sheet |
| `feature/drawerbox/DrawerBoxScreen.kt` | 7175 | 抽屉盒设置屏 |
| `feature/export/ExportListDialog.kt` | 14778 | 导出对话框 |
| `feature/settings/DrawerBoxPage.kt` | 12877 | 抽屉盒设置页 |
| `feature/settings/StartLocationPage.kt` | 4505 | 启动位置页 |
| `feature/termux/CommandRunnerScreen.kt` | 19487 | 命令运行器 |

### B.2 新增/改动逻辑
- **AppControlBridge**：Backend AUTO/ROOT/SHIZUKU/DHIZUKU/PRIVILEGED；forceStop/setEnabled/setHidden/setSuspended；autoLiftLimit；remember/recallBackend。
- **DhizukuBridge**：init/refresh/ensurePermission/ensurePackageDelegation/setPackagesSuspended/setApplicationHidden。
- **StgxRegistry**：load/reload/entryForExtension/iconForExtension/colorsOfExtension；与 TextMateEngine 联动。
- **DrawerBox**：DrawerBoxEntry/DrawerComponent；SettingsRepository.setDrawerBox/toggleDrawerComponent/moveDrawerComponent。
- **SettingsRepository 新增**：installPriority/installWithShizuku/installWithDhizuku/controlBackend/drawerBox；setThirdPartyInstaller。
- **AppShell**：Route 新增 Customize/ApkInfo(originalPath)/Grammar/CommandRunner/DrawerBox；OpenDialog 新增 StgxAction；ShellActions.signApk 带 originalDir/Name；openFile 增 stgx 分支；termuxVm 注入；`Route.Grammar` 调用 `GrammarPage(onBack)`。
- **MainActivity**：L10n.source 初始化；incomingPdf/incomingFile。
- **StrataApp**：StgxRegistry.reload、DhizukuBridge.init。
- **AndroidStrings**：每次 AppLocales.wrap（API<33 语言切换即时生效）。
- **RootManager**：probeSuExec、detectFlavor、copyFileToCache。
- **ShizukuManager**：IShizukuFileService.exec、service 字段、exec()。
- **安装后端**：ApkInstaller.installByPolicy/installWithRoot/installWithShizuku/installWithDhizuku/installWithThirdParty；InstallBackend/InstallPolicy/InstallAccess。
- **RootService**：StrataRootService。

## C. 1.0.0 独有、fork 已删或改掉
- 1.0.0 `RootManager` 无 flavor/probeSuExec/copyFileToCache；fork 已扩。
- 1.0.0 `ShizukuManager` 无 exec/service；fork 已扩。
- 1.0.0 `AndroidStrings` 直接用构造 context；fork 改为 ctx()。
- 1.0.0 `AppShell` 无 STGX/CommandRunner/DrawerBox/Grammar；fork 已增。