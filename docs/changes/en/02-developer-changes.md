# Developer Changes

> All changes that are not "user-visible"; relative to baseline Strata 1.0.0.

## 1. New Modules and Files
- `core/control/AppControlBridge.kt`: app control backends and capabilities.
- `core/dhizuku/DhizukuBridge.kt`: Dhizuku init/permission/delegation/
  suspend/hide.
- `core/export/FileListExport.kt`: file list export.
- `core/fs/RestrictedDirs.kt`: restricted directory markers.
- `core/fs/SplitChannel.kt`: split-volume virtual channel.
- `core/media/BundleIconLoader.kt`: Bundle icons.
- `core/model/ActionMenu.kt`, `core/model/DrawerBox.kt`: long-press menu and
  drawer box models.
- `core/text/StgxRegistry.kt`, `StgxParser.kt`, `StgxIcons.kt`,
  `StgxIconLoader.kt`: STGX.
- `core/util/AppIconLoader.kt`, `core/util/QrCode.kt`: icons and QR codes.
- `feature/apps/AppControlSheet.kt`: app control sheet.
- `feature/drawerbox/DrawerBoxScreen.kt`: drawer box settings screen.
- `feature/export/ExportListDialog.kt`: export dialog.
- `feature/settings/DrawerBoxPage.kt`, `StartLocationPage.kt`: settings pages.
- `feature/termux/CommandRunnerScreen.kt`: command runner.
- Resources: `ic_python_logo.xml`, `colors_python.xml`, `art_unkown.webp`,
  `art_warning.webp`, `strings_export.xml` for each language.

## 2. API and Architecture Changes
- `InstallBackend`/`InstallPolicy`/`InstallAccess`: install backend policy.
- `ApkInstaller`: added `installByPolicy`, `installWithShizuku`,
  `installWithDhizuku`, `installWithThirdParty`,
  `installBundleWithThirdParty`, `writePrivilegedSession`.
- `AppControlBridge`: `Backend`, `Capability`, `resolve/resolveFor`,
  `forceStop/setEnabled/setHidden/setSuspended`, `autoLiftLimit`,
  `hiddenPackages/suspendedPackages`.
- `DhizukuBridge`: `Status`, `init/refresh/ensurePermission/
  ensurePackageDelegation/setPackagesSuspended/setApplicationHidden/
  isApplicationHidden/isSuspended`.
- `RootManager`: added `flavor`, `probeSuExec`, `detectFlavor`,
  `copyFileToCache`.
- `ShizukuManager`: added `service`, `exec`; AIDL added `exec`.
- `ShizukuFsService`: added `exec`.
- `FileAccess`: added `materialize` default implementation.
- `DirectoryLister`: added `missing`/`fallback`/`remote` parameters and
  `Listing.viaRoot`.
- `RemoteTransfers`: added `viaTemp`, `measure`, `Progress.SILENT`,
  `Place.of`.
- `SafFileSystem`: added `termuxHome`, `label`, `release`, `TERMUX_AUTHORITY`.
- `SftpFileSystem`: added host key capture/reject/`newlyTrustedHostKey`.
- `SmbFileSystem`: added `splitUser`, `NoShareException`, `listShares`.
- `WebDavFileSystem`: added `PinningTrust`, `fingerprint`, `parseMultistatus`,
  `decodeHref`.
- `RemoteUri`: added `id/path/parent/child/name/display`.
- `TermuxViewModel`: added `CommandRunState`, `runCommand`, `cancelCommand`,
  `clearCommandResult`, `writeLog`.
- `TextMateEngine`: added STGX dynamic syntax, `loadDynamicGrammar`,
  `previewColorsFor`, `themes`.
- `GrammarCatalog`: added `extensionsOf`, `includedScopes`.
- `TextEncoding`: added `choices` (with labels), `labelOf`, windows-1252
  heuristic, strict GB decoding, `detectSample`.
- `TextEditorViewModel`: added `charsetLabel`, `resolveLanguage` STGX fallback,
  `setSaveCharset(choice)`.
- `EditorController`: added `colorUnderCursor/replaceColor/insertText/
  replaceSelection/select/lineText`.
- `SettingsRepository`: added install backend/priority/third-party/control
  backend/drawer box fields and methods.
- `SettingsPage`: added `DRAWER_BOX`, `START_LOCATION`, `GRAMMAR`.
- `MainActivity`: added `L10n.source` init, `incomingPdf/incomingFile`.
- `StrataApp`: added `StgxRegistry.reload`, `DhizukuBridge.init`.
- `AndroidStrings`: `AppLocales.wrap` on every call.
- `core/fs/RestrictedDirs.kt`: new `ownAppDirsInRestricted(context, path)`.
- `feature/explorer/components/PaneColumn.kt`: `EmptyFolder` now takes `onOpen` and renders the private folder list through `ExpressiveEmptyState`'s `action` slot.
- `feature/explorer/ExplorerViewModel.kt`: `navigateUp` restores the restricted `Android/data` / `Android/obb` pane state when root and Shizuku are both off and the current folder is the private one.
- New string `explorer_restricted_own_dirs` (en / zh-rCN / ru / fa).

## 3. Dependencies
- Added `io.github.iamr0s:Dhizuku-API:2.5.3`.
- Added `ru.solrudev.ackpine:ackpine-core/ktx/dhizuku/dhizuku-ktx:0.25.4`.
- Otherwise largely the same as 1.0.0.

## 4. Build
- `app/build.gradle.kts`: added Dhizuku/Ackpine, `strata.*` properties,
  `cargoHostBuild`, ABI splits, resource excludes, `legacy29` variant with
  targetSdk 29.
- `gradle/libs.versions.toml`: added Dhizuku/Ackpine versions.
- `gradle.properties`: added `strata.versionName/Code`.

## 5. Manifest
- Added permissions: `CHANGE_COMPONENT_ENABLED_STATE`, `FORCE_STOP_PACKAGES`,
  `SUSPEND_APPS`, `com.android.permission.GET_INSTALLED_APPS`.
- Added `queries`: Termux, plugin FILE actions, PROCESS_TEXT.
- `ShareService` foreground type specialUse and description.
- Added `PluginShareProvider`.

## 6. Data Formats
- New settings fields: install backend/priority/third-party/control backend/
  drawer box.
- Backup export/import includes the new fields; private fields remain excluded.
- STGX syntax file format and registry.
- Vault/bookmark/remote connection formats unchanged.

## 7. Internal Behavior and Performance
- Restricted directories are materialized into cache (ApkDialogViewModel,
  FileAccess.materialize).
- Split-volume reads go through SplitChannel.
- Remote transfers go through a temporary directory.
- Image/icon caching and Coil factories.
- Dynamic STGX grammar loading and icon caching.
- Encoding detection improvements (windows-1252/GB).
- Paged editing and read-only protection for large files.

## 8. Tests
- Added/adjusted tests related to install backends, app control, STGX,
  encoding, and export (per the actual repository).

## 9. Localization

- Added Japanese (`ja`): 14 `strings_*.xml` files under `values-ja/` (common / format / export / pdf / security / network / settings / grammar / files / model / devtools / editor / explorer / apk).
- `AppLanguage.kt` gains the `JAPANESE` enum and `"ja"` mapping; `SecurityPages.kt` language `when` gains the `JAPANESE` branch; `locales_config.xml` gains `<locale android:name="ja" />`; `values/strings_security.xml` gains `language_native_ja` and `language_japanese`.
- Other locales synced: `values-fa`, `values-ru`, `values-zh-rCN` gain `language_japanese` in their `strings_security.xml`.
