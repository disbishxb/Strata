# 30 文件系统与远程

> 元规则：改代码后更新本册字节数与条目；用 `stat -c '%s %n'` 核对。字节单位 byte。

## A. fork 与 1.0.0 相同的逻辑

### A.1 文件路由表
| 路径 | 1.0.0 | fork | 职责 |
|---|---|---|---|
| `core/fs/FileOperations.kt` | 10298 | 9778 | 复制/移动/删除/建链；OpProgress/Conflict/LinkRequest |
| `core/fs/DirectoryLister.kt` | 1596 | 1596 | 本地/root/远程列举；Listing |
| `core/fs/FileAccess.kt` | 1591 | 2301 | 访问抽象；LocalFileAccess；fork 增 materialize |
| `core/fs/FsUtil.kt` | 5532 | 5532 | 文件工具；复制树/移动/唯一名/删除 |
| `core/fs/TopLevel.kt` | 889 | 889 | 根目录探测 |
| `core/fs/Mounts.kt` | 2052 | 2052 | 挂载解析/读写/remount |
| `core/fs/DropRules.kt` | 1823 | 1823 | 拖放判定；DropSpot |
| `core/remote/CryptoProviders.kt` | 484 | 484 | BouncyCastle 注册 |
| `core/remote/FtpFileSystem.kt` | 7008 | 7008 | FTP/FTPS |
| `core/remote/RemoteConnectionRepository.kt` | 1316 | 1316 | 连接 DataStore |
| `core/remote/RemoteConnections.kt` | 1217 | 1217 | 连接编解码 |
| `core/remote/RemoteFileSystem.kt` | 1928 | 1928 | 接口/异常/工厂 |
| `core/remote/RemoteModel.kt` | 2880 | 2880 | RemoteConnection/RemoteEntry/RemotePaths |
| `core/remote/RemoteSessions.kt` | 3205 | 3205 | 会话缓存/重连/信任 |
| `core/remote/RemoteTransfers.kt` | 10729 | 10729 | Place/传输/删除 |
| `core/remote/RemoteUri.kt` | 1594 | 1594 | remote:// 编解码 |
| `core/remote/SafFileSystem.kt` | 8988 | 8988 | SAF |
| `core/remote/SftpFileSystem.kt` | 6971 | 6971 | SFTP |
| `core/remote/SmbFileSystem.kt` | 9830 | 9830 | SMB |
| `core/remote/WebDavFileSystem.kt` | 12891 | 12891 | WebDAV |
| `feature/remote/RemoteScreen.kt` | 29659 | 29659 | 远程屏 |
| `feature/remote/RemoteViewModel.kt` | 13198 | 13198 | 远程 VM |
| `feature/remote/RemoteDialogs.kt` | 4101 | 4101 | 主机密钥/密码对话框 |

### A.2 工作逻辑
- **列举**：DirectoryLister 按 local → root → fallback → remote 顺序；remote 走 RemoteUri。
- **文件操作**：FileOperations.copy/move/delete/createLinks 返回 Flow<OpProgress>；冲突策略 Conflict；keepTime。
- **远程会话**：RemoteSessions.use(id) { fs } 加锁、断线重连、host key 信任回调。
- **远程传输**：RemoteTransfers.transfer/delete；Place.Local/Remote；临时目录；进度。
- **远程 URI**：RemoteUri.of/parse/parent/child/name/display。
- **各 FS**：FTP/SFTP/SMB/WebDAV/Saf 实现 RemoteFileSystem；connect/list/stat/open/upload/mkdir/delete/rename。
- **远程 UI**：RemoteScreen 连接列表/浏览/上传/下载；RemoteViewModel 连接/导航/上传/下载/重命名/删除。
- **远程对话框**：StoragePasswordDialog、HostKeyChangedDialog。

## B. fork 独有的逻辑

### B.1 新增文件路由表
| 路径 | fork | 职责 |
|---|---|---|
| `core/fs/SplitChannel.kt` | 2747 | 分卷读取虚拟 SeekableByteChannel |
| `core/fs/RestrictedDirs.kt` | 425 | 受限 Android 目录标记与判定 |

### B.2 新增/改动逻辑
- **FileAccess**：fork 增 `materialize(file, cacheDir)` 默认实现（受限目录物化到缓存）。
- **FileOperations**：fork 的 `itemOf` 区分 root/local；`transfer` 逻辑与 1.0.0 有差异（冲突/同目录/自身）。
- **DirectoryLister**：fork 增加 `missing`/`fallback`/`remote` 参数与 `Listing.viaRoot`。
- **RemoteTransfers**：fork 增加 `viaTemp`、`measure`、`Progress.SILENT`、`Place.of`。
- **SafFileSystem**：fork 增加 `termuxHome`、`label`、`release`、`TERMUX_AUTHORITY`。
- **SftpFileSystem**：fork 增加 host key 捕获/拒绝/`newlyTrustedHostKey`。
- **SmbFileSystem**：fork 增加 `splitUser`、`NoShareException`、`listShares`。
- **WebDavFileSystem**：fork 增加 `PinningTrust`、`fingerprint`、`parseMultistatus`、`decodeHref`。
- **RemoteUri**：fork 增加 `id/path/parent/child/name/display`。

- `RestrictedDirs.kt` 新增 `ownAppDirsInRestricted(context, path)`：返回本应用在 `Android/data`/`Android/obb` 下的私有目录（`getExternalFilesDir(null)?.parentFile` / `context.obbDir`），存在且是目录才返回。

## C. 1.0.0 独有、fork 已删或改掉
- 1.0.0 无 `SplitChannel`、`RestrictedDirs`。
- 1.0.0 `FileAccess` 无 `materialize`。
- 1.0.0 `DirectoryLister` 无 missing/fallback/remote 参数。
- 1.0.0 `RemoteTransfers` 无 viaTemp/measure/Progress.SILENT。
- 1.0.0 `SafFileSystem` 无 Termux 相关。
- 1.0.0 `SftpFileSystem` 无 host key 捕获。
- 1.0.0 `SmbFileSystem` 无 shares 列举。
- 1.0.0 `WebDavFileSystem` 无证书固定。
