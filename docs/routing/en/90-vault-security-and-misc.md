# 90 Vault, Security & Misc

> Meta-rule: after changing code, update this volume's byte sizes and entries; verify with `stat -c '%s %n'`. Byte unit: byte.

## A. Logic identical in fork and 1.0.0

### A.1 File routing table
| Path | 1.0.0 | fork | Responsibility |
|---|---|---|---|
| `core/vault/Vault.kt` | 1437 | 1437 | Entry/connection/open directory/cleanup |
| `core/vault/VaultCipher.kt` | 6971 | 6971 | Header/encrypt/decrypt/seal/open/nonce |
| `core/vault/VaultFileSystem.kt` | 1442 | 1442 | RemoteFileSystem implementation |
| `core/vault/VaultModel.kt` | 1727 | 1727 | Entry/index/keys/exceptions/Hex |
| `core/vault/VaultStore.kt` | 19305 | 19305 | Create/unlock/lock/read-write/index/garbage collection |
| `core/vault/VaultTransfers.kt` | 8501 | 8501 | moveIn/restore/progress |
| `ui/security/LockScreen.kt` | 7208 | 7208 | AppLockGate/LockScreen |
| `ui/security/AppLock.kt` | 3538 | 3538 | Lock policy/lifecycle |
| `ui/security/DeviceAuth.kt` | 4755 | 4755 | Biometrics/device credentials |
| `ui/security/VaultKeystore.kt` | 2532 | 2532 | Vault keystore |
| `feature/vault/VaultAccess.kt` | 5828 | 5828 | Create/unlock/biometrics/destroy |
| `feature/vault/VaultDialogs.kt` | 15853 | 15853 | Setup/unlock/change password/delete |
| `feature/trash/RecycleBinScreen.kt` | 12146 | 12146 | Recycle-bin screen |
| `feature/trash/RecycleBinViewModel.kt` | 4406 | 4406 | Recycle-bin VM |
| `feature/share/ShareScreen.kt` | 12791 | 12791 | Share screen |
| `feature/share/ShareService.kt` | 9137 | 9137 | Foreground service/notification/state |

### A.2 Work logic
- **Vault**: Vault.init/store/root/connection; VaultStore.create/unlock/lock/changePassword/setBiometric/destroy/list/stat/write/delete/rename/saveIndex/collectGarbage.
- **Encryption**: VaultCipher.header/encrypt/decrypt/seal/open/nonce; DecryptingStream.
- **FS**: VaultFileSystem.list/stat/openInput/upload/mkdir/delete/rename.
- **Transfers**: VaultTransfers.moveIn/restore; progress.
- **Security UI**: AppLockGate/LockScreen; VaultSetupDialog/VaultUnlockDialog/VaultPasswordDialog/VaultDeleteDialog.
- **Recycle bin**: RecycleBinScreen/VM; list/restore/purge/empty.
- **Sharing**: ShareScreen config; ShareService start/stop/notification; LanAddress.

## B. Logic unique to the fork
- This module's source is identical to 1.0.0; no added files or structural changes.
- The only differences are dependencies (e.g. `DhizukuBridge` affects AppControl, not this module).

## C. Logic unique to 1.0.0, removed or changed in the fork
- None.