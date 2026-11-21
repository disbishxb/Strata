# 40 APK & Control

> Meta-rule: after changing code, update this volume's byte sizes and entries; verify with `stat -c '%s %n'`. Byte unit: byte.

## A. Logic identical in fork and 1.0.0

### A.1 File routing table
| Path | 1.0.0 | fork | Responsibility |
|---|---|---|---|
| `core/apk/install/ApkInstaller.kt` | 6301 | 15998 | Install entry; writeRootSession/installWithRoot/installWithSystem |
| `core/apk/install/InstallResult.kt` | 4334 | 4532 | InstallFailure/InstallResult/InstallStatus |
| `core/apk/install/PmInstall.kt` | 2424 | 2457 | pm command assembly/stage/create/write/commit/abandon |
| `core/apk/install/InstallResultReceiver.kt` | 2186 | 2186 | Install-result broadcast/InstallEvents |
| `core/apk/ApkCloner.kt` | 10471 | 10471 | Clone: package/components/authority/permissions/icon |
| `core/apk/ApkExtractor.kt` | 2625 | 2625 | Extract APK/split bundle to .apks |
| `core/apk/ApkManifestInfo.kt` | 1759 | 1759 | Manifest reading |
| `core/apk/ApkSignature.kt` | 2319 | 2319 | Signature model/fingerprint |
| `core/apk/ApkSignatureInspector.kt` | 977 | 977 | Signature verification |
| `core/apk/ApkSigning.kt` | 2939 | 2939 | apksig signing/alignment/verification |
| `core/apk/AppIntents.kt` | 1022 | 1022 | Launch/details/uninstall |
| `core/apk/CloneIcon.kt` | 1646 | 1646 | Icon model/edge color |
| `core/apk/CloneIconRenderer.kt` | 2785 | 2785 | Icon rendering |
| `core/apk/InstalledApps.kt` | 3135 | 3372 | Installed-app list |
| `core/apk/InstalledSigners.kt` | 2437 | 2437 | Installed signatures/labels/hashes |
| `core/apk/JksFormat.kt` | 8439 | 8439 | JKS read/write |
| `core/apk/KeyStoreFactory.kt` | 4077 | 4077 | Key generation/loading |
| `core/apk/KeystoreFile.kt` | 7610 | 7610 | Keystore detect/open/export/pk8 |
| `core/apk/ProtectionDetector.kt` | 1444 | 1444 | Hardening detection |
| `core/apk/SavedKeys.kt` | 3949 | 3949 | Saved keys |
| `core/apk/SigningKeys.kt` | 1148 | 1148 | Default key cache |
| `core/apk/SplitBundle.kt` | 5862 | 5862 | Split bundle read/select/obb |
| `feature/apk/ApkDialog.kt` | 27729 | 29901 | APK dialog |
| `feature/apk/ApkDialogViewModel.kt` | 11031 | 15596 | APK VM |
| `feature/apk/ApkInfoScreen.kt` | 11290 | 11290 | APK info screen |
| `feature/apk/SignApkDialog.kt` | 12761 | 12761 | Sign dialog |
| `feature/apk/SplitInstallDialog.kt` | 13145 | 13726 | Split install dialog |
| `feature/apk/SplitInstallViewModel.kt` | 6600 | 7810 | Split VM |
| `feature/apps/AppsScreen.kt` | 23440 | 35834 | Apps screen |
| `feature/apps/AppsViewModel.kt` | 5993 | 22157 | Apps VM |

### A.2 Work logic
- **Parsing**: ApkExtractor/ApkManifestInfo/SplitBundle; SplitBundles.kindOf/select/readBaseInfo.
- **Signing**: ApkSignatureInspector.inspect → ApkSignature; ApkSigning.sign; KeyStoreFactory/KeystoreFile/JksFormat.
- **Cloning**: ApkCloner.clone (package/components/authority/permissions/icon); CloneIconRenderer.
- **Installing**: ApkInstaller.installWithRoot/installWithSystem; PmInstall; InstallResultReceiver.
- **App list**: InstalledApps.list/find; InstalledSigners.of/label/shortHash/sha256/isDebugKey.
- **APK UI**: ApkDialog details/sign/clone/install; SplitInstallDialog select split/install; AppsScreen list/filter/sort/extract.
- **APK VM**: ApkDialogViewModel.load/install/cloneAndInstall/extractIcon; SplitInstallViewModel.load/install.

## B. Logic unique to the fork

### B.1 Added-file routing table
| Path | fork | Responsibility |
|---|---|---|
| `core/apk/install/InstallBackend.kt` | 4165 | InstallBackend/PRIVILEGED_BACKENDS/InstallPolicy/InstallAccess/ThirdPartyInstaller |
| `core/control/AppControlBridge.kt` | 10622 | App enable/disable/hide/suspend; AUTO backend; autoLiftLimit |
| `core/dhizuku/DhizukuBridge.kt` | 5106 | Dhizuku init/permission/delegation/suspend/hide |
| `feature/apps/AppControlSheet.kt` | 7667 | App control sheet |

### B.2 Added/changed logic
- **ApkInstaller**: the fork adds `installByPolicy`, `installWithShizuku`, `installWithDhizuku`, `installWithThirdParty`, `installBundleWithThirdParty`, `writePrivilegedSession`; `installWithRoot` is kept.
- **InstallBackend**: `InstallBackend` enum; `InstallPolicy.resolve(availability)`; `InstallAccess.probe/policyOf/thirdPartyOf`.
- **AppControlBridge**: `Backend` AUTO/ROOT/SHIZUKU/DHIZUKU/PRIVILEGED; `Capability`; `forceStop/setEnabled/setHidden/setSuspended`; `autoLiftLimit`; `hiddenPackages/suspendedPackages`.
- **DhizukuBridge**: `init/refresh/ensurePermission/ensurePackageDelegation/setPackagesSuspended/setApplicationHidden/isApplicationHidden/isSuspended`.
- **ApkDialog**: the fork adds `onSign(readable, original)`, `InstallPolicy` backend selection, third-party installer, `apk_installing_shizuku/dhizuku`.
- **ApkDialogViewModel**: the fork adds `materializedFile` (restricted-dir materialization), `install(policy)` via `InstallAccess.policyOf`, `cloneAndInstall` writes back to restricted dir.
- **SplitInstallViewModel**: the fork adds third-party installer branch, `InstallAccess` backend selection.
- **AppsScreen/AppsViewModel**: the fork adds permission checks, Dhizuku suspension, control sheet, filter/sort/size cache/signature cache.

## C. Logic unique to 1.0.0, removed or changed in the fork
- 1.0.0 has no `InstallBackend.kt`, `AppControlBridge.kt`, `DhizukuBridge.kt`, `AppControlSheet.kt`.
- 1.0.0 `ApkInstaller` only has `installWithRoot/installWithSystem`; the fork has multiple backends.
- 1.0.0 `ApkDialog`'s `onSign` has no readable/original params.
- 1.0.0 `ApkDialogViewModel` has no materialization, no policy, no third-party installer.
- 1.0.0 `SplitInstallViewModel` has no third-party installer/backend selection.
- 1.0.0 `AppsScreen/AppsViewModel` has no permission checks, Dhizuku, control sheet.