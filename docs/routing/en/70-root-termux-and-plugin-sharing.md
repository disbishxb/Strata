# 70 root, Termux & Plugin Sharing

> Meta-rule: after changing code, update this volume's byte sizes and entries; verify with `stat -c '%s %n'`. Byte unit: byte.

## A. Logic identical in fork and 1.0.0

### A.1 File routing table
| Path | 1.0.0 | fork | Responsibility |
|---|---|---|---|
| `core/root/RootCommands.kt` | 1376 | 1376 | stat/chmod/chown/readlink/parseStat |
| `core/root/RootFileAccess.kt` | 2153 | 2153 | root FileAccess implementation |
| `core/root/RootManager.kt` | 3259 | 6860 | Probe/bind/exec/filesystem |
| `core/root/RootPaths.kt` | 525 | 525 | Restricted paths/needs-root check |
| `core/root/RootPosixApi.kt` | 1407 | 1407 | POSIX implementation |
| `core/root/StrataRootService.kt` | 320 | 320 | root service |
| `core/shizuku/ShizukuFsService.kt` | 317 | 635 | Shizuku FS service |
| `core/shizuku/ShizukuManager.kt` | 5389 | 5942 | Shizuku state/permission/bind/exec |
| `core/termux/RunCommands.kt` | 5899 | 5899 | Run-command codec/scopes/templates |
| `core/termux/TermuxApi.kt` | 5519 | 5519 | Constants/commands/results/issues |
| `core/termux/TermuxBridge.kt` | 5717 | 5717 | Run/receive/install/launch |
| `core/termux/TermuxLanguages.kt` | 11273 | 11273 | Language table/templates/detection/install |
| `core/termux/TermuxProjects.kt` | 7053 | 7053 | Project types/templates/detection/run |
| `core/termux/TermuxSession.kt` | 13087 | 13087 | Session/state/run/install/create |
| `core/termux/TermuxSsh.kt` | 1316 | 1316 | SSH command/install |
| `core/plugin/PluginFiles.kt` | 1327 | 1327 | Copy/save |
| `core/plugin/PluginPins.kt` | 1518 | 1518 | pin model/trust |
| `core/plugin/PluginProtocol.kt` | 3698 | 3698 | Protocol/extension/interpretation |
| `core/plugin/PluginShare.kt` | 2175 | 2175 | Stage/release/receive |
| `core/plugin/Plugins.kt` | 4742 | 4742 | Discovery/signatures/reply/authority |
| `core/share/LanFtpServer.kt` | 4403 | 4403 | FTP service |
| `core/share/LanGuards.kt` | 2505 | 2505 | SafeRoot/name/host/LAN/throttle |
| `core/share/LanHttpServer.kt` | 10280 | 10280 | HTTP service/API |
| `feature/termux/LanguagesScreen.kt` | 6476 | 6476 | Languages screen |
| `feature/termux/NewProjectSheet.kt` | 8286 | 8286 | New-project sheet |
| `feature/termux/TermuxUi.kt` | 29506 | 29506 | Install steps/output/run sheet/gate |
| `feature/termux/TermuxViewModel.kt` | 396 | 4132 | Session wrapper; fork adds command state |

### A.2 Work logic
- **root**: RootManager.init/request/access/exec/copyFileToCache; RootFileAccess/RootPosixApi/RootCommands.
- **Shizuku**: ShizukuManager.init/refresh/ensurePermission/access/exec; ShizukuFsService.
- **Termux**: TermuxSession.refresh/run/runProject/install/createProject/openSsh/openTerminal; TermuxBridge.run/launch; TermuxLanguages/TermuxProjects.
- **Plugins**: Plugins.discover/textApps/signers/reply/ownsAuthority; PluginProtocol.interpret; PluginShare.stage/receive.
- **LAN**: LanFtpServer.start/stop; LanHttpServer.start/stop; LanGuards.SafeRoot/isLanPeer/LoginThrottle.
- **Termux UI**: TermuxSetupStep/RunOutputPanel/RunSheet/ProjectRunSheet/TerminalGate.

## B. Logic unique to the fork

### B.1 Added-file routing table
| Path | fork | Responsibility |
|---|---|---|
| `feature/termux/CommandRunnerScreen.kt` | 19084 | Command runner: cwd selection/command input/output/custom paths |

### B.2 Added/changed logic
- **RootManager**: the fork adds flavor/probeSuExec/detectFlavor/copyFileToCache.
- **ShizukuManager**: the fork adds `service`, `exec`, `IShizukuFileService.exec`.
- **ShizukuFsService**: the fork adds `exec`.
- **TermuxViewModel**: the fork adds `CommandRunState`/`runCommand`/`cancelCommand`/`clearCommandResult`/`writeLog`.
- **TermuxUi**: no structural additions, but depends on the fork's `CommandRunnerScreen`.
- **Plugins/sharing**: identical to 1.0.0.

## C. Logic unique to 1.0.0, removed or changed in the fork
- 1.0.0 has no `CommandRunnerScreen.kt`.
- 1.0.0 `RootManager` has no flavor/probeSuExec/copyFileToCache.
- 1.0.0 `ShizukuManager` has no exec/service.
- 1.0.0 `ShizukuFsService` has no exec.
- 1.0.0 `TermuxViewModel` only has `session`.