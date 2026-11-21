# 20 File Explorer

> Meta-rule: after changing code, update this volume's byte sizes and entries; verify with `stat -c '%s %n'`. Byte unit: byte.

## A. Logic identical in fork and 1.0.0

### A.1 File routing table
| Path | 1.0.0 | fork | Responsibility |
|---|---|---|---|
| `feature/explorer/ExplorerScreen.kt` | 37180 | 41867 | Explorer screen; Dialog enum; PaneColumn/bottom bar/dialog assembly |
| `feature/explorer/ExplorerViewModel.kt` | 66400 | 69199 | Explorer VM; navigation/selection/operations/remote/vault/tasks |
| `feature/explorer/ExplorerState.kt` | 6606 | 6606 | Pane/PaneState/ExplorerUiState/visibleItems |
| `components/FileRow.kt` | 17533 | 26926 | List row/grid tile/icon/thumbnail/drag-drop |
| `components/PaneColumn.kt` | 28876 | 30082 | Single pane; path header; list/grid; scroll memory; drag-drop |
| `components/ItemActionSheet.kt` | 12562 | 14562 | Long-press action sheet; zones and availability |
| `components/ExplorerBottomBar.kt` | 17564 | 17843 | Browse/select bottom bar; Zoned actions |
| `components/ExplorerDialogs.kt` | 11228 | 11228 | Input/Confirm/Delete/Sort |
| `components/ToolDialogs.kt` | 24682 | 31337 | Create archive/password/create link/split/merge |
| `components/TransferDialog.kt` | 4194 | 4194 | Conflict selection |
| `components/ChecksumDialog.kt` | 3908 | 3908 | Checksum |
| `components/BatchRenameDialog.kt` | 13413 | 13521 | Batch rename |
| `components/BookmarksSheet.kt` | 12280 | 12280 | Bookmarks |
| `components/TaskOverlay.kt` | 1727 | 1727 | Task overlay |
| `dnd/FileDrag.kt` | 11034 | 11034 | Drag state/gesture/dropSpot/anchor |
| `dnd/DragLayer.kt` | 12167 | 12167 | Drag layer; edge scroll; drop animation |
| `dnd/DropVisuals.kt` | 5060 | 5060 | DropLook/DropAnimation/Halo |
| `core/bookmarks/Bookmarks.kt` | 4259 | 4259 | Bookmark model/add-remove-edit/groups |
| `core/bookmarks/BookmarkRepository.kt` | 1067 | 1067 | DataStore bookmarks |

### A.2 Work logic
- **Navigation**: navigate(pane,path,refresh,initial,history,reveal,fallbacks); listFirst multi-candidate; failure prompt or askAboutStorage.
- **Selection**: toggleSelect/selectAll/invert/clear; canRecycle.
- **Operations**: deleteSelection/copySelectionToClipboard/paste/beginTransfer/transferToOther/undoMove.
- **Remote**: openRemote downloads to cache; checkRemoteEdits/uploadRemoteEdits.
- **Vault**: openVault/moveToVault/restoreFromVault/lockVault/leaveVault.
- **Archive**: extractArchive/createArchive; password prompt.
- **Split/merge**: splitFile/mergeFiles; optional PDF merge.
- **Signing**: signApk goes through BuildRun; Quick/Saved/New/Existing.
- **dex/jar**: smaliToDex/jarToDex.
- **Rename**: previewRename/applyRename.
- **Bookmarks**: add/update/remove/move/renameGroup/deleteGroup.
- **Drag-drop**: dropInto → DropRules.verdict → moveDropped/RemoteTransfers; undo.
- **UI**: ExplorerScreen's Dialog enum and when; BackHandler; TaskOverlay/BuildDialog; StoragePrompt/TransferPrompt/PasswordPrompt.

- **Restricted Android directories**: when `RESTRICTED_ANDROID_DIR` or `isRestrictedAndroidDir(path)` and neither root nor Shizuku is on, `EmptyFolder` shows the no-permission illustration plus the app's own private folder returned by `ownAppDirsInRestricted` (tappable). `navigateUp` restores the restricted `Android/data` / `Android/obb` pane state when the current path is `Android/data/<pkg>` or `Android/obb/<pkg>` and both are off, instead of walking to `Android/`.

## B. Logic unique to the fork

### B.1 Added-file routing table
| Path | fork | Responsibility |
|---|---|---|
| `feature/explorer/components/TaskOverlay.kt` | 1727 | Task overlay (present in both versions; emphasized here because the fork still uses it) |

> This module has no fork-added files; changes are concentrated in existing files.

### B.2 Added/changed logic
- **ExplorerScreen**: adds `ExportListDialog`, `NewProjectSheet`, `ProjectRunSheet`, `TerminalGate`, `CommandRunner` integration; `mergeSplitAndOpen` supports `.001` split volumes; `handleOpen` split branch; `selectionInOrder` for export.
- **ExplorerViewModel**: adds restricted-dir handling (`isRestrictedAndroidDir`), `signApk(originalDir/Name)`, `listForExport/archiveAccessForExport`, `revealCreated`, `showInFolder`, `locateIncoming`, `saveIncomingCopy`.
- **ExplorerState**: adds `StoragePrompt` (Password/HostKey/Vault).
- **FileRow**: adds STGX icons/colors, Bundle icons, `AppIconWithFolder`, caching and materialization.
- **ItemActionSheet**: adds EXPORT/VAULT/RESTORE/CIPHER, STGX icons.
- **ExplorerBottomBar**: adds EXPORT/VAULT/RESTORE.
- **ToolDialogs**: adds split-volume option, PDF merge option.

## C. Logic unique to 1.0.0, removed or changed in the fork
- 1.0.0 has no `ExportListDialog`, `NewProjectSheet`, `ProjectRunSheet`, `TerminalGate`, `CommandRunner` integration.
- 1.0.0 has no `.001` split-volume merge-open.
- 1.0.0 has no `StoragePrompt` (vault/host key/password are fork additions).
- 1.0.0 `FileRow` has no STGX/Bundle icons or materialization.
- 1.0.0 `ItemActionSheet`/`ExplorerBottomBar` have no EXPORT/VAULT/RESTORE.
