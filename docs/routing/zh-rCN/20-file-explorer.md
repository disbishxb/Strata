# 20 文件浏览器

> 元规则：改代码后更新本册字节数与条目；用 `stat -c '%s %n'` 核对。字节单位 byte。

## A. fork 与 1.0.0 相同的逻辑

### A.1 文件路由表
| 路径 | 1.0.0 | fork | 职责 |
|---|---|---|---|
| `feature/explorer/ExplorerScreen.kt` | 37180 | 41867 | 浏览器屏幕；Dialog 枚举；PaneColumn/底栏/弹窗装配 |
| `feature/explorer/ExplorerViewModel.kt` | 66400 | 69199 | 浏览器 VM；导航/选择/操作/远程/保险库/任务 |
| `feature/explorer/ExplorerState.kt` | 6606 | 6606 | Pane/PaneState/ExplorerUiState/visibleItems |
| `components/FileRow.kt` | 17533 | 26926 | 列表行/网格 tile/图标/缩略图/拖放 |
| `components/PaneColumn.kt` | 28876 | 30082 | 单栏；路径头；列表/网格；滚动记忆；拖放 |
| `components/ItemActionSheet.kt` | 12562 | 14562 | 长按动作面板；分区与可用性 |
| `components/ExplorerBottomBar.kt` | 17564 | 17843 | 浏览/选择底栏；Zoned 动作 |
| `components/ExplorerDialogs.kt` | 11228 | 11228 | Input/Confirm/Delete/Sort |
| `components/ToolDialogs.kt` | 24682 | 31337 | 建归档/密码/建链/拆分/合并 |
| `components/TransferDialog.kt` | 4194 | 4194 | 冲突选择 |
| `components/ChecksumDialog.kt` | 3908 | 3908 | 校验和 |
| `components/BatchRenameDialog.kt` | 13413 | 13521 | 批量重命名 |
| `components/BookmarksSheet.kt` | 12280 | 12280 | 书签 |
| `components/TaskOverlay.kt` | 1727 | 1727 | 任务遮罩 |
| `dnd/FileDrag.kt` | 11034 | 11034 | 拖拽状态/手势/dropSpot/anchor |
| `dnd/DragLayer.kt` | 12167 | 12167 | 拖影；边缘滚动；落点动画 |
| `dnd/DropVisuals.kt` | 5060 | 5060 | DropLook/DropAnimation/Halo |
| `core/bookmarks/Bookmarks.kt` | 4259 | 4259 | 书签模型/增删改/组 |
| `core/bookmarks/BookmarkRepository.kt` | 1067 | 1067 | DataStore 书签 |

### A.2 工作逻辑
- **导航**：navigate(pane,path,refresh,initial,history,reveal,fallbacks)；listFirst 多候选；失败提示或 askAboutStorage。
- **选择**：toggleSelect/selectAll/invert/clear；canRecycle。
- **操作**：deleteSelection/copySelectionToClipboard/paste/beginTransfer/transferToOther/undoMove。
- **远程**：openRemote 下载到缓存；checkRemoteEdits/uploadRemoteEdits。
- **保险库**：openVault/moveToVault/restoreFromVault/lockVault/leaveVault。
- **归档**：extractArchive/createArchive；密码提示。
- **拆分/合并**：splitFile/mergeFiles；PDF 合并可选。
- **签名**：signApk 走 BuildRun；Quick/Saved/New/Existing。
- **dex/jar**：smaliToDex/jarToDex。
- **重命名**：previewRename/applyRename。
- **书签**：add/update/remove/move/renameGroup/deleteGroup。
- **拖放**：dropInto → DropRules.verdict → moveDropped/RemoteTransfers；undo。
- **UI**：ExplorerScreen 的 Dialog 枚举与 when；BackHandler；TaskOverlay/BuildDialog；StoragePrompt/TransferPrompt/PasswordPrompt。

- **受限 Android 目录**：`EmptyFolder` 在 `RESTRICTED_ANDROID_DIR` 或 `isRestrictedAndroidDir(path)` 且未开 root/Shizuku 时，除无权限插画外，还列出 `ownAppDirsInRestricted` 返回的本应用私有文件夹（可点开）。`navigateUp` 在无 root/Shizuku 且当前为 `Android/data/<pkg>` 或 `Android/obb/<pkg>` 时，直接恢复对应 `Android/data`/`Android/obb` 的受限 pane 状态，不再走到 `Android/`。

## B. fork 独有的逻辑

### B.1 新增文件路由表
| 路径 | fork | 职责 |
|---|---|---|
| `feature/explorer/components/TaskOverlay.kt` | 1727 | 任务遮罩（两版都有，此处强调 fork 仍用） |

> 本模块无 fork 新增文件；改动集中在既有文件。

### B.2 新增/改动逻辑
- **ExplorerScreen**：新增 `ExportListDialog`、`NewProjectSheet`、`ProjectRunSheet`、`TerminalGate`、`CommandRunner` 相关；`mergeSplitAndOpen` 支持 .001 分卷；`handleOpen` 分卷分支；`selectionInOrder` 用于导出。
- **ExplorerViewModel**：新增受限目录处理（`isRestrictedAndroidDir`）、`signApk(originalDir/Name)`、`listForExport/archiveAccessForExport`、`revealCreated`、`showInFolder`、`locateIncoming`、`saveIncomingCopy`。
- **ExplorerState**：新增 `StoragePrompt`（Password/HostKey/Vault）。
- **FileRow**：新增 STGX 图标/颜色、Bundle 图标、`AppIconWithFolder`、缓存与物化。
- **ItemActionSheet**：新增 EXPORT/VAULT/RESTORE/CIPHER、STGX 图标。
- **ExplorerBottomBar**：新增 EXPORT/VAULT/RESTORE。
- **ToolDialogs**：新增分卷选项、PDF 合并选项。

## C. 1.0.0 独有、fork 已删或改掉
- 1.0.0 无 `ExportListDialog`、`NewProjectSheet`、`ProjectRunSheet`、`TerminalGate`、`CommandRunner` 相关接入。
- 1.0.0 无分卷 `.001` 合并打开。
- 1.0.0 无 `StoragePrompt`（保险库/主机密钥/密码在 fork 新增）。
- 1.0.0 `FileRow` 无 STGX/Bundle 图标与物化。
- 1.0.0 `ItemActionSheet`/`ExplorerBottomBar` 无 EXPORT/VAULT/RESTORE。
