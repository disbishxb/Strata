# 30 Filesystem & Remote

> Meta-rule: after changing code, update this volume's byte sizes and entries; verify with `stat -c '%s %n'`. Byte unit: byte.

## A. Logic identical in fork and 1.0.0

### A.1 File routing table
| Path | 1.0.0 | fork | Responsibility |
|---|---|---|---|
| `core/fs/FileOperations.kt` | 10298 | 9778 | Copy/move/delete/create links; OpProgress/Conflict/LinkRequest |
| `core/fs/DirectoryLister.kt` | 1596 | 1596 | Local/root/remote listing; Listing |
| `core/fs/FileAccess.kt` | 1591 | 2301 | Access abstraction; LocalFileAccess; fork adds materialize |
| `core/fs/FsUtil.kt` | 5532 | 5532 | File utilities; copy tree/move/unique name/delete |
| `core/fs/TopLevel.kt` | 889 | 889 | Root-directory detection |
| `core/fs/Mounts.kt` | 2052 | 2052 | Mount parsing/read-write/remount |
| `core/fs/DropRules.kt` | 1823 | 1823 | Drop rules; DropSpot |
| `core/remote/CryptoProviders.kt` | 484 | 484 | BouncyCastle registration |
| `core/remote/FtpFileSystem.kt` | 7008 | 7008 | FTP/FTPS |
| `core/remote/RemoteConnectionRepository.kt` | 1316 | 1316 | Connection DataStore |
| `core/remote/RemoteConnections.kt` | 1217 | 1217 | Connection codec |
| `core/remote/RemoteFileSystem.kt` | 1928 | 1928 | Interface/exceptions/factory |
| `core/remote/RemoteModel.kt` | 2880 | 2880 | RemoteConnection/RemoteEntry/RemotePaths |
| `core/remote/RemoteSessions.kt` | 3205 | 3205 | Session cache/reconnect/trust |
| `core/remote/RemoteTransfers.kt` | 10729 | 10729 | Place/transfer/delete |
| `core/remote/RemoteUri.kt` | 1594 | 1594 | remote:// codec |
| `core/remote/SafFileSystem.kt` | 8988 | 8988 | SAF |
| `core/remote/SftpFileSystem.kt` | 6971 | 6971 | SFTP |
| `core/remote/SmbFileSystem.kt` | 9830 | 9830 | SMB |
| `core/remote/WebDavFileSystem.kt` | 12891 | 12891 | WebDAV |
| `feature/remote/RemoteScreen.kt` | 29659 | 29659 | Remote screen |
| `feature/remote/RemoteViewModel.kt` | 13198 | 13198 | Remote VM |
| `feature/remote/RemoteDialogs.kt` | 4101 | 4101 | Host key/password dialogs |

### A.2 Work logic
- **Listing**: DirectoryLister in local → root → fallback → remote order; remote goes through RemoteUri.
- **File operations**: FileOperations.copy/move/delete/createLinks return Flow<OpProgress>; conflict policy Conflict; keepTime.
- **Remote sessions**: RemoteSessions.use(id) { fs } locking, reconnect, host-key trust callback.
- **Remote transfers**: RemoteTransfers.transfer/delete; Place.Local/Remote; temp dir; progress.
- **Remote URI**: RemoteUri.of/parse/parent/child/name/display.
- **Each FS**: FTP/SFTP/SMB/WebDAV/Saf implement RemoteFileSystem; connect/list/stat/open/upload/mkdir/delete/rename.
- **Remote UI**: RemoteScreen connection list/browse/upload/download; RemoteViewModel connect/navigate/upload/download/rename/delete.
- **Remote dialogs**: StoragePasswordDialog, HostKeyChangedDialog.

## B. Logic unique to the fork

### B.1 Added-file routing table
| Path | fork | Responsibility |
|---|---|---|
| `core/fs/SplitChannel.kt` | 2747 | Split-volume virtual SeekableByteChannel |
| `core/fs/RestrictedDirs.kt` | 425 | Restricted Android dir markers and checks |

### B.2 Added/changed logic
- **FileAccess**: the fork adds a default `materialize(file, cacheDir)` implementation (materialize restricted dirs to cache).
- **FileOperations**: the fork's `itemOf` distinguishes root/local; `transfer` logic differs from 1.0.0 (conflict/same-dir/self).
- **DirectoryLister**: the fork adds `missing`/`fallback`/`remote` params and `Listing.viaRoot`.
- **RemoteTransfers**: the fork adds `viaTemp`, `measure`, `Progress.SILENT`, `Place.of`.
- **SafFileSystem**: the fork adds `termuxHome`, `label`, `release`, `TERMUX_AUTHORITY`.
- **SftpFileSystem**: the fork adds host-key capture/rejection/`newlyTrustedHostKey`.
- **SmbFileSystem**: the fork adds `splitUser`, `NoShareException`, `listShares`.
- **WebDavFileSystem**: the fork adds `PinningTrust`, `fingerprint`, `parseMultistatus`, `decodeHref`.
- **RemoteUri**: the fork adds `id/path/parent/child/name/display`.

- `RestrictedDirs.kt`: new `ownAppDirsInRestricted(context, path)` returning this app's private folder under `Android/data`/`Android/obb` (`getExternalFilesDir(null)?.parentFile` / `context.obbDir`), only when it exists and is a directory.

## C. Logic unique to 1.0.0, removed or changed in the fork
- 1.0.0 has no `SplitChannel`, `RestrictedDirs`.
- 1.0.0 `FileAccess` has no `materialize`.
- 1.0.0 `DirectoryLister` has no missing/fallback/remote params.
- 1.0.0 `RemoteTransfers` has no viaTemp/measure/Progress.SILENT.
- 1.0.0 `SafFileSystem` has no Termux integration.
- 1.0.0 `SftpFileSystem` has no host-key capture.
- 1.0.0 `SmbFileSystem` has no share listing.
- 1.0.0 `WebDavFileSystem` has no certificate pinning.
