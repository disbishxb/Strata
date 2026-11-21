# 10 Entry & Shell

> Meta-rule: after changing code, update this volume's byte sizes and entries; verify with `stat -c '%s %n'`. Byte unit: byte.

## A. Logic identical in fork and 1.0.0

### A.1 File routing table
| Path | 1.0.0 | fork | Responsibility |
|---|---|---|---|
| `MainActivity.kt` | 6426 | 6317 | Sole Activity; LAUNCHER + VIEW(file/content) |
| `StrataApp.kt` | 1858 | 2021 | Application; global initialization |
| `ui/AppShell.kt` | 29656 | 38559 | Shell: routing, drawer, AnimatedContent, OpenDialog, Screen dispatch |
| `ui/RouteStack.kt` | 1739 | 1739 | Navigation stack; RouteEntry implements ViewModelStoreOwner |
| `ui/SheetStates.kt` | 715 | 715 | Global sheet state |
| `ui/LocalUiDensity.kt` | 319 | 319 | LocalUiDensity / LocalSettings |
| `ui/Bidi.kt` | 1066 | 1066 | RTL isolation and text direction |
| `ui/ActionIcons.kt` | 6751 | 6925 | Action icon mapping |
| `ui/TextClipboard.kt` | 1121 | 1121 | Clipboard wrapper |
| `ui/i18n/AppLocales.kt` | 3212 | 3263 | Language selection/wrap; AndroidStrings |
| `core/i18n/L10n.kt` | 1126 | 1126 | String source registry; tr/trPlural/separator |
| `core/i18n/AppLanguage.kt` | 1053 | 1135 | Language enum and tag parsing |
| `core/data/SettingsRepository.kt` | 28926 | 35722 | DataStore settings read/write |
| `core/data/SettingsBackup.kt` | 3847 | 3847 | Settings backup codec |
| `core/data/BackupLocations.kt` | 892 | 892 | Backup locations |
| `core/data/StartFolders.kt` | 655 | 935 | Start-folder resolution |
| `core/permission/StorageAccess.kt` | 2990 | 2990 | Storage permission state and request |
| `ui/security/AppLock.kt` | 3538 | 3538 | App-lock policy and lifecycle |
| `ui/security/DeviceAuth.kt` | 4755 | 4755 | Biometrics/device credentials |
| `ui/security/VaultKeystore.kt` | 2532 | 2532 | Vault keystore |
| `ui/security/LockScreen.kt` | 7208 | 7208 | Lock-screen UI |
| `ui/theme/Theme.kt` | 7052 | 7052 | StrataTheme, colors, pure black |
| `ui/theme/Type.kt` | 362 | 362 | Typography |
| `ui/theme/Shape.kt` | 1453 | 1453 | Shapes |
| `ui/theme/KindColors.kt` | 1534 | 2345 | File-type colors |
| `core/open/Opener.kt` | 4653 | 4791 | Opener/OpenTarget/OpenResolver/OpenAs |
| `core/open/OpenDefaults.kt` | 1129 | 1129 | Default open-with codec |
| `core/open/IncomingFiles.kt` | 2654 | 2654 | External Uri resolution |
| `core/open/IncomingPaths.kt` | 3471 | 3471 | External path normalization |
| `core/util/IntentUtils.kt` | 2970 | 2970 | FileProvider Uri, open, share |
| `core/root/RootManager.kt` | 3259 | 6860 | root probe/bind/exec |
| `core/vault/Vault.kt` | 1437 | 1437 | Vault entry/connection |
| `core/sound/UiSounds.kt` | 2864 | 2864 | Sound policy and device |
| `core/sound/SoundPoolDevice.kt` | 4866 | 4866 | SoundPool implementation |
| `core/media/ApkIcons.kt` | 1646 | 1646 | Coil APK icons |
| `ui/drawer/StrataDrawer.kt` | 26357 | 27590 | Drawer UI |
| `ui/expressive/ExpressiveArt.kt` | 11223 | 11509 | Illustrations |
| `ui/expressive/ExpressiveDialog.kt` | 5237 | 5237 | Dialog |
| `ui/expressive/ExpressiveMenu.kt` | 2862 | 2862 | Menu |
| `ui/expressive/MorphShape.kt` | 2961 | 2961 | Morphing shape |

### A.2 Work logic
1. **Startup**: MainActivity.onCreate → installSplashScreen → enableEdgeToEdge → DeviceAuth.attach → SettingsRepository → synchronous first-frame settings (≤500ms) → apply FLAG_SECURE/AppLock/UiSounds/Formatters → setContent → StrataTheme → AppLockGate → AppShell.
2. **Global init**: StrataApp.onCreate → AppLocales.install → Vault.init → AppLock.track → RootManager.init → SafFileSystem.appContext → ShizukuManager.init → UiSounds.device.
3. **Routing**: RouteStack.push/pop/replace; AppShell uses AnimatedContent + contentKey=key; clears VM and saveable after leaving the stack.
4. **Drawer**: StrataDrawerContent receives DrawerActions; onShortcut/onBookmark/onStorage etc. uniformly close the drawer and push.
5. **i18n**: AppLocales.wrap wraps Context on API<33; AndroidStrings re-wraps via ctx() each time (fork change). Supported languages: en / fa / ru / zh-CN / ja (Japanese added in fork).
6. **Settings repository**: DataStore read/write; editAppearance/List/Behavior/Operations/Security/Editor/Reader/Actions.
7. **Security**: AppLock.apply → AppLockPolicy; DeviceAuth biometrics; LockScreen unlock.
8. **Open-with**: OpenResolver.resolve → defaults first → builtIn; OpenTarget BuiltIn/App.
9. **root/vault/sound/theme**: each has its own init and global state.

## B. Logic unique to the fork

### B.1 Added-file routing table
| Path | fork | Responsibility |
|---|---|---|
| `core/control/AppControlBridge.kt` | 10622 | App enable/disable/hide/suspend; AUTO backend |
| `core/dhizuku/DhizukuBridge.kt` | 5106 | Dhizuku init/permission/delegation/suspend/hide |
| `core/export/FileListExport.kt` | 12350 | File-list export |
| `core/fs/RestrictedDirs.kt` | 1054 | Restricted Android dir markers |
| `core/fs/SplitChannel.kt` | 2747 | Split-volume virtual channel |
| `core/media/BundleIconLoader.kt` | 1347 | APKS/XAPK/APKM icons |
| `core/model/ActionMenu.kt` | 615 | Long-press menu item |
| `core/model/DrawerBox.kt` | 1632 | Drawer-box model |
| `core/text/StgxRegistry.kt` | 4982 | STGX registry |
| `core/text/StgxParser.kt` | 3932 | STGX parse/compile |
| `core/text/StgxIcons.kt` | 3159 | STGX icons |
| `core/text/StgxIconLoader.kt` | 1246 | base64 icon loading |
| `core/util/AppIconLoader.kt` | 2793 | App icon loading |
| `core/util/QrCode.kt` | 2403 | QR code |
| `feature/apps/AppControlSheet.kt` | 7667 | App control sheet |
| `feature/drawerbox/DrawerBoxScreen.kt` | 7175 | Drawer-box settings screen |
| `feature/export/ExportListDialog.kt` | 14778 | Export dialog |
| `feature/settings/DrawerBoxPage.kt` | 12877 | Drawer-box settings page |
| `feature/settings/StartLocationPage.kt` | 4505 | Start-location page |
| `feature/termux/CommandRunnerScreen.kt` | 19487 | Command runner |

### B.2 Added/changed logic
- **AppControlBridge**: Backend AUTO/ROOT/SHIZUKU/DHIZUKU/PRIVILEGED; forceStop/setEnabled/setHidden/setSuspended; autoLiftLimit; remember/recallBackend.
- **DhizukuBridge**: init/refresh/ensurePermission/ensurePackageDelegation/setPackagesSuspended/setApplicationHidden.
- **StgxRegistry**: load/reload/entryForExtension/iconForExtension/colorsOfExtension; integrates with TextMateEngine.
- **DrawerBox**: DrawerBoxEntry/DrawerComponent; SettingsRepository.setDrawerBox/toggleDrawerComponent/moveDrawerComponent.
- **SettingsRepository additions**: installPriority/installWithShizuku/installWithDhizuku/controlBackend/drawerBox; setThirdPartyInstaller.
- **AppShell**: Route adds Customize/ApkInfo(originalPath)/Grammar/CommandRunner/DrawerBox; OpenDialog adds StgxAction; ShellActions.signApk takes originalDir/Name; openFile adds stgx branch; termuxVm injection; `Route.Grammar` calls `GrammarPage(onBack)`.
- **MainActivity**: L10n.source init; incomingPdf/incomingFile.
- **StrataApp**: StgxRegistry.reload, DhizukuBridge.init.
- **AndroidStrings**: AppLocales.wrap each time (instant language switch on API<33).
- **RootManager**: probeSuExec, detectFlavor, copyFileToCache.
- **ShizukuManager**: IShizukuFileService.exec, service field, exec().
- **Install backends**: ApkInstaller.installByPolicy/installWithRoot/installWithShizuku/installWithDhizuku/installWithThirdParty; InstallBackend/InstallPolicy/InstallAccess.
- **RootService**: StrataRootService.

## C. Logic unique to 1.0.0, removed or changed in the fork
- 1.0.0 `RootManager` has no flavor/probeSuExec/copyFileToCache; the fork extended it.
- 1.0.0 `ShizukuManager` has no exec/service; the fork extended it.
- 1.0.0 `AndroidStrings` uses the constructed context directly; the fork switched to ctx().
- 1.0.0 `AppShell` has no STGX/CommandRunner/DrawerBox/Grammar; the fork added them.