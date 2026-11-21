# 90 保险库与安全与其他

> 元规则：改代码后更新本册字节数与条目；用 `stat -c '%s %n'` 核对。字节单位 byte。

## A. fork 与 1.0.0 相同的逻辑

### A.1 文件路由表
| 路径 | 1.0.0 | fork | 职责 |
|---|---|---|---|
| `core/vault/Vault.kt` | 1437 | 1437 | 入口/连接/打开目录/清理 |
| `core/vault/VaultCipher.kt` | 6971 | 6971 | 头/加密/解密/密封/打开/nonce |
| `core/vault/VaultFileSystem.kt` | 1442 | 1442 | RemoteFileSystem 实现 |
| `core/vault/VaultModel.kt` | 1727 | 1727 | 条目/索引/密钥/异常/Hex |
| `core/vault/VaultStore.kt` | 19305 | 19305 | 创建/解锁/锁/读写/索引/垃圾回收 |
| `core/vault/VaultTransfers.kt` | 8501 | 8501 | moveIn/restore/进度 |
| `ui/security/LockScreen.kt` | 7208 | 7208 | AppLockGate/LockScreen |
| `ui/security/AppLock.kt` | 3538 | 3538 | 锁策略/生命周期 |
| `ui/security/DeviceAuth.kt` | 4755 | 4755 | 生物识别/设备凭据 |
| `ui/security/VaultKeystore.kt` | 2532 | 2532 | 保险库密钥 |
| `feature/vault/VaultAccess.kt` | 5828 | 5828 | 创建/解锁/生物识别/销毁 |
| `feature/vault/VaultDialogs.kt` | 15853 | 15853 | 设置/解锁/改密/删除 |
| `feature/trash/RecycleBinScreen.kt` | 12146 | 12146 | 回收站屏 |
| `feature/trash/RecycleBinViewModel.kt` | 4406 | 4406 | 回收站 VM |
| `feature/share/ShareScreen.kt` | 12791 | 12791 | 分享屏 |
| `feature/share/ShareService.kt` | 9137 | 9137 | 前台服务/通知/状态 |

### A.2 工作逻辑
- **保险库**：Vault.init/store/root/connection；VaultStore.create/unlock/lock/changePassword/setBiometric/destroy/list/stat/write/delete/rename/saveIndex/collectGarbage。
- **加密**：VaultCipher.header/encrypt/decrypt/seal/open/nonce；DecryptingStream。
- **FS**：VaultFileSystem.list/stat/openInput/upload/mkdir/delete/rename。
- **传输**：VaultTransfers.moveIn/restore；进度。
- **安全 UI**：AppLockGate/LockScreen；VaultSetupDialog/VaultUnlockDialog/VaultPasswordDialog/VaultDeleteDialog。
- **回收站**：RecycleBinScreen/VM；list/restore/purge/empty。
- **分享**：ShareScreen 配置；ShareService 启动/停止/通知；LanAddress。

## B. fork 独有的逻辑
- 本模块源码与 1.0.0 一致；无新增文件或结构性改动。
- 差异仅在依赖（如 `DhizukuBridge` 影响 AppControl，不影响本模块）。

## C. 1.0.0 独有、fork 已删或改掉
- 无。