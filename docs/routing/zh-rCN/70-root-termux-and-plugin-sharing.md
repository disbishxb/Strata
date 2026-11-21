# 70 root与Termux与插件分享

> 元规则：改代码后更新本册字节数与条目；用 `stat -c '%s %n'` 核对。字节单位 byte。

## A. fork 与 1.0.0 相同的逻辑

### A.1 文件路由表
| 路径 | 1.0.0 | fork | 职责 |
|---|---|---|---|
| `core/root/RootCommands.kt` | 1376 | 1376 | stat/chmod/chown/readlink/parseStat |
| `core/root/RootFileAccess.kt` | 2153 | 2153 | root FileAccess 实现 |
| `core/root/RootManager.kt` | 3259 | 6860 | 探测/绑定/执行/文件系统 |
| `core/root/RootPaths.kt` | 525 | 525 | 受限路径/是否需要 root |
| `core/root/RootPosixApi.kt` | 1407 | 1407 | POSIX 实现 |
| `core/root/StrataRootService.kt` | 320 | 320 | root 服务 |
| `core/shizuku/ShizukuFsService.kt` | 317 | 635 | Shizuku FS 服务 |
| `core/shizuku/ShizukuManager.kt` | 5389 | 5942 | Shizuku 状态/权限/绑定/执行 |
| `core/termux/RunCommands.kt` | 5899 | 5899 | 运行命令编解码/作用域/模板 |
| `core/termux/TermuxApi.kt` | 5519 | 5519 | 常量/命令/结果/问题 |
| `core/termux/TermuxBridge.kt` | 5717 | 5717 | 运行/接收/安装/启动 |
| `core/termux/TermuxLanguages.kt` | 11273 | 11273 | 语言表/模板/检测/安装 |
| `core/termux/TermuxProjects.kt` | 7053 | 7053 | 项目类型/模板/检测/运行 |
| `core/termux/TermuxSession.kt` | 13087 | 13087 | 会话/状态/运行/安装/创建 |
| `core/termux/TermuxSsh.kt` | 1316 | 1316 | SSH 命令/安装 |
| `core/plugin/PluginFiles.kt` | 1327 | 1327 | 复制/保存 |
| `core/plugin/PluginPins.kt` | 1518 | 1518 | pin 模型/信任 |
| `core/plugin/PluginProtocol.kt` | 3698 | 3698 | 协议/扩展/解释 |
| `core/plugin/PluginShare.kt` | 2175 | 2175 | 暂存/释放/接收 |
| `core/plugin/Plugins.kt` | 4742 | 4742 | 发现/签名/回复/authority |
| `core/share/LanFtpServer.kt` | 4403 | 4403 | FTP 服务 |
| `core/share/LanGuards.kt` | 2505 | 2505 | SafeRoot/名称/主机/局域网/节流 |
| `core/share/LanHttpServer.kt` | 10280 | 10280 | HTTP 服务/API |
| `feature/termux/LanguagesScreen.kt` | 6476 | 6476 | 语言屏 |
| `feature/termux/NewProjectSheet.kt` | 8286 | 8286 | 新项目 sheet |
| `feature/termux/TermuxUi.kt` | 29506 | 29506 | 安装步骤/输出/运行 sheet/门卫 |
| `feature/termux/TermuxViewModel.kt` | 396 | 4132 | 会话封装；fork 增命令状态 |

### A.2 工作逻辑
- **root**：RootManager.init/request/access/exec/copyFileToCache；RootFileAccess/RootPosixApi/RootCommands。
- **Shizuku**：ShizukuManager.init/refresh/ensurePermission/access/exec；ShizukuFsService。
- **Termux**：TermuxSession.refresh/run/runProject/install/createProject/openSsh/openTerminal；TermuxBridge.run/launch；TermuxLanguages/TermuxProjects。
- **插件**：Plugins.discover/textApps/signers/reply/ownsAuthority；PluginProtocol.interpret；PluginShare.stage/receive。
- **局域网**：LanFtpServer.start/stop；LanHttpServer.start/stop；LanGuards.SafeRoot/isLanPeer/LoginThrottle。
- **Termux UI**：TermuxSetupStep/RunOutputPanel/RunSheet/ProjectRunSheet/TerminalGate。

## B. fork 独有的逻辑

### B.1 新增文件路由表
| 路径 | fork | 职责 |
|---|---|---|
| `feature/termux/CommandRunnerScreen.kt` | 19084 | 命令运行器：cwd 选择/命令输入/输出/自定义路径 |

### B.2 新增/改动逻辑
- **RootManager**：fork 增 flavor/probeSuExec/detectFlavor/copyFileToCache。
- **ShizukuManager**：fork 增 `service`、`exec`、`IShizukuFileService.exec`。
- **ShizukuFsService**：fork 增 `exec`。
- **TermuxViewModel**：fork 增 `CommandRunState`/`runCommand`/`cancelCommand`/`clearCommandResult`/`writeLog`。
- **TermuxUi**：无结构性新增，但依赖 fork 的 `CommandRunnerScreen`。
- **插件/分享**：与 1.0.0 一致。

## C. 1.0.0 独有、fork 已删或改掉
- 1.0.0 无 `CommandRunnerScreen.kt`。
- 1.0.0 `RootManager` 无 flavor/probeSuExec/copyFileToCache。
- 1.0.0 `ShizukuManager` 无 exec/service。
- 1.0.0 `ShizukuFsService` 无 exec。
- 1.0.0 `TermuxViewModel` 仅 `session`。