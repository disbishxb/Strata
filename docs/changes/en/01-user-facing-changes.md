# User-Facing Changes

> Only changes users can perceive; relative to baseline Strata 1.0.0.
> Use this file as the source for release summaries.

## 1. Installing and Updating Apps
- Added a Shizuku install backend: install APKs via Shizuku.
- Added a Dhizuku install backend: install APKs via Dhizuku (device owner).
- Added third-party installers: hand an APK to a chosen installer app to
  complete installation.
- Added install backend priority: drag to reorder Root / Shizuku / Dhizuku and
  use the first available one in order.
- Added install backend status hints: show whether each backend is available
  and why.
- Added signature verification toggle description and a third-party installer
  settings page.
- Added compatibility for non-standard suffixes such as `.apk.1` (copy as
  `.apk` before installing).

## 2. App Management
- Added an "App Control" panel: force stop, enable/disable, hide/unhide,
  suspend/unsuspend.
- Added backend selection: Auto / Root / Shizuku / Dhizuku / privileged system
  app.
- Added Dhizuku suspend/restore.
- Added a "This app" marker and a self-protection prompt.
- Added app list filters: All / User / System / Launchable / Restricted /
  Debug-signed.
- Added restricted-type filters: Disabled / Hidden / Suspended.
- Added sorting: name / package name / install time / update time / size;
  ascending or descending.
- Added signature info viewing and a key-rotation prompt.
- Added permission guidance: app-list access explanation, re-request, open
  settings.

## 3. File Browsing and Operations
- Added STGX custom syntax and icons: assign syntax, icon, and color per
  extension.
- Added a grammar (STGX) management page: import, export, built-in list,
  preview, sort, failure area.
- Added automatic merging and opening of split volumes
  (`.001/.002/...`).
- Added file list export (directory/archive/multi-select), with absolute or
  relative paths, recursion, format, and encoding.
- Added a "This folder only" sorting option.
- When Android/data or Android/obb is restricted, the screen lists this app's own private folder there, which can be opened.
- Backing out of that private folder returns to the restricted directory screen instead of Android/.
- Added restricted directory access under root mode / Shizuku.
- Added remote edit detection and upload prompts.
- Added vault-related entry points and prompts.
- Added customizable long-press menu (show/hide/sort).
- Added customizable toolbar (bottom bar/selection bar/long-press).
- Added drawer box: "stow" sidebar items into the Storage box.

## 4. Open With
- Added an STGX file open-action panel: edit, manage, install/replace/uninstall.
- Added more built-in openers and default-open settings.
- Added an "Open as" type selector (auto/text/image/audio/video/any).

## 5. Editor
- Added STGX syntax selection and highlighting.
- Added color picker and insert/replace.
- Added Process Text (process selected text with another app).
- Added encoding selection and save encoding.
- Added line-ending selection and saving.
- Added paged editing for large files and a read-only prompt.
- Added external change detection with reload/keep prompts.
- Added find and replace, regex, whole word, case sensitivity.
- Added symbol bar, indent guides, sticky scroll, bracket highlighting, and
  other options.
- The keyboard is now dismissed automatically when leaving the editor.

## 6. Settings
- Added "Install method", "Install priority", and "Third-party installer"
  settings groups.
- Added a "Long-press menu" settings page.
- Added a "Drawer box" settings page and sidebar preview.
- Added a "Start location" settings page: independent left/right pane
  settings.
- Added Dhizuku access settings.
- Added system privilege explanation.
- Added a support page QR code and fork maintainer info.
- Added settings export/import (including new fields).
- Added Chinese/Persian/Russian export strings.

## 7. Termux
- Added a "Command runner": choose working directory, enter a command, view
  output, copy, cancel.
- Added step-by-step guidance for Termux installation/permissions/external
  apps/storage.
- Added language/project templates and install prompts.
- Added an SSH terminal and openssh install prompt.

## 8. Other
- Added Python icon and color scheme.
- Added more illustrations (unknown, warning).
- Added Chinese/Persian/Russian export strings.
- Added permissions: component enabled state, force stop, suspend apps,
- Added Japanese UI: "日本語" is now selectable in Settings → Language, and in the system per-app language list on Android 13+; all 14 string modules are translated.
  installed app list.
