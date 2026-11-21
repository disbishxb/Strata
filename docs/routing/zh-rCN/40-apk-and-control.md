# 40 APK 与控制

> 元规则：改代码后更新本册字节数与条目；用 `stat -c '%s %n'` 核对。字节单位 byte。

## A. fork 与 1.0.0 相同的逻辑

### A.1 文件路由表
| 路径 | 1.0.0 | fork | 职责 |
|---|---|---|---|
| `core/apk/install/ApkInstaller.kt` | 6301 | 15998 | 安装入口；writeRootSession/installWithRoot/installWithSystem |
| `core/apk/install/InstallResult.kt` | 4334 | 4532 | InstallFailure/InstallResult/InstallStatus |
| `core/apk/install/PmInstall.kt` | 2424 | 2457 | pm 命令拼装/stage/create/write/commit/abandon |
| `core/apk/install/InstallResultReceiver.kt` | 2186 | 2186 | 安装结果广播/InstallEvents |
| `core/apk/ApkCloner.kt` | 10471 | 10471 | 克隆：改包名/组件/authority/权限/图标 |
| `core/apk/ApkExtractor.kt` | 2625 | 2625 | 提取 APK/拆分包为 .apks |
| `core/apk/ApkManifestInfo.kt` | 1759 | 1759 | Manifest 读取 |
| `core/apk/ApkSignature.kt` | 2319 | 2319 | 签名模型/指纹 |
| `core/apk/ApkSignatureInspector.kt` | 977 | 977 | 验签 |
| `core/apk/ApkSigning.kt` | 2939 | 2939 | apksig 签名/对齐/校验 |
| `core/apk/AppIntents.kt` | 1022 | 1022 | 启动/详情/卸载 |
| `core/apk/CloneIcon.kt` | 1646 | 1646 | 图标模型/边色 |
| `core/apk/CloneIconRenderer.kt` | 2785 | 2785 | 图标渲染 |
| `core/apk/InstalledApps.kt` | 3135 | 3372 | 已安装应用列表 |
| `core/apk/InstalledSigners.kt` | 2437 | 2437 | 已安装签名/标签/哈希 |
| `core/apk/JksFormat.kt` | 8439 | 8439 | JKS 读写 |
| `core/apk/KeyStoreFactory.kt` | 4077 | 4077 | 密钥生成/加载 |
| `core/apk/KeystoreFile.kt` | 7610 | 7610 | 密钥库检测/打开/导出/pk8 |
| `core/apk/ProtectionDetector.kt` | 1444 | 1444 | 加固检测 |
| `core/apk/SavedKeys.kt` | 3949 | 3949 | 保存密钥 |
| `core/apk/SigningKeys.kt` | 1148 | 1148 | 默认密钥缓存 |
| `core/apk/SplitBundle.kt` | 5862 | 5862 | split 包读取/选择/obb |
| `feature/apk/ApkDialog.kt` | 27729 | 29901 | APK 对话框 |
| `feature/apk/ApkDialogViewModel.kt` | 11031 | 15596 | APK VM |
| `feature/apk/ApkInfoScreen.kt` | 11290 | 11290 | APK 信息屏 |
| `feature/apk/SignApkDialog.kt` | 12761 | 12761 | 签名对话框 |
| `feature/apk/SplitInstallDialog.kt` | 13145 | 13726 | split 安装对话框 |
| `feature/apk/SplitInstallViewModel.kt` | 6600 | 7810 | split VM |
| `feature/apps/AppsScreen.kt` | 23440 | 35834 | 应用屏 |
| `feature/apps/AppsViewModel.kt` | 5993 | 22157 | 应用 VM |

### A.2 工作逻辑
- **解析**：ApkExtractor/ApkManifestInfo/SplitBundle；SplitBundles.kindOf/select/readBaseInfo。
- **签名**：ApkSignatureInspector.inspect → ApkSignature；ApkSigning.sign；KeyStoreFactory/KeystoreFile/JksFormat。
- **克隆**：ApkCloner.clone（改包名/组件/authority/权限/图标）；CloneIconRenderer。
- **安装**：ApkInstaller.installWithRoot/installWithSystem；PmInstall；InstallResultReceiver。
- **应用列表**：InstalledApps.list/find；InstalledSigners.of/label/shortHash/sha256/isDebugKey。
- **APK UI**：ApkDialog 详情/签名/克隆/安装；SplitInstallDialog 选择 split/安装；AppsScreen 列表/过滤/排序/提取。
- **APK VM**：ApkDialogViewModel.load/install/cloneAndInstall/extractIcon；SplitInstallViewModel.load/install。

## B. fork 独有的逻辑

### B.1 新增文件路由表
| 路径 | fork | 职责 |
|---|---|---|
| `core/apk/install/InstallBackend.kt` | 4165 | InstallBackend/PRIVILEGED_BACKENDS/InstallPolicy/InstallAccess/ThirdPartyInstaller |
| `core/control/AppControlBridge.kt` | 10622 | 应用启用/停用/隐藏/挂起；AUTO 后端；autoLiftLimit |
| `core/dhizuku/DhizukuBridge.kt` | 5106 | Dhizuku 初始化/权限/委托/挂起/隐藏 |
| `feature/apps/AppControlSheet.kt` | 7667 | 应用控制 sheet |

### B.2 新增/改动逻辑
- **ApkInstaller**：fork 增 `installByPolicy`、`installWithShizuku`、`installWithDhizuku`、`installWithThirdParty`、`installBundleWithThirdParty`、`writePrivilegedSession`；`installWithRoot` 保留。
- **InstallBackend**：`InstallBackend` 枚举；`InstallPolicy.resolve(availability)`；`InstallAccess.probe/policyOf/thirdPartyOf`。
- **AppControlBridge**：`Backend` AUTO/ROOT/SHIZUKU/DHIZUKU/PRIVILEGED；`Capability`；`forceStop/setEnabled/setHidden/setSuspended`；`autoLiftLimit`；`hiddenPackages/suspendedPackages`。
- **DhizukuBridge**：`init/refresh/ensurePermission/ensurePackageDelegation/setPackagesSuspended/setApplicationHidden/isApplicationHidden/isSuspended`。
- **ApkDialog**：fork 增 `onSign(readable, original)`、`InstallPolicy` 后端选择、第三方安装器、`apk_installing_shizuku/dhizuku`。
- **ApkDialogViewModel**：fork 增 `materializedFile`（受限目录物化）、`install(policy)` 走 `InstallAccess.policyOf`、`cloneAndInstall` 写回受限目录。
- **SplitInstallViewModel**：fork 增第三方安装器分支、`InstallAccess` 后端选择。
- **AppsScreen/AppsViewModel**：fork 增权限检查、Dhizuku 挂起、控制 sheet、过滤/排序/大小缓存/签名缓存。

## C. 1.0.0 独有、fork 已删或改掉
- 1.0.0 无 `InstallBackend.kt`、`AppControlBridge.kt`、`DhizukuBridge.kt`、`AppControlSheet.kt`。
- 1.0.0 `ApkInstaller` 只有 `installWithRoot/installWithSystem`；fork 多后端。
- 1.0.0 `ApkDialog` 的 `onSign` 无 readable/original 参数。
- 1.0.0 `ApkDialogViewModel` 无物化、无 policy、无第三方安装器。
- 1.0.0 `SplitInstallViewModel` 无第三方安装器/后端选择。
- 1.0.0 `AppsScreen/AppsViewModel` 无权限检查、Dhizuku、控制 sheet。