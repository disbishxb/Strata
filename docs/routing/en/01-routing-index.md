# Routing Index

> Byte sizes are always in bytes. `—` means the file does not exist in that version. The full per-file tables are in each volume's A.1/B.1/C.1.
> 1.0.0 byte sizes are taken from `原仓库-list.md`; fork byte sizes are taken from actual runs.

## 1. Entry & Shell (see 10)
| Path | 1.0.0 | fork | Responsibility |
|---|---|---|---|
| `MainActivity.kt` | 6300 | 6317 | Sole Activity; LAUNCHER + VIEW |
| `StrataApp.kt` | 1800 | 2021 | Application; global initialization |
| `ui/AppShell.kt` | 29000 | 38559 | Shell: routing, drawer, dialog host, Screen dispatch |
| `ui/RouteStack.kt` | 1700 | 1739 | Navigation stack |
| `ui/drawer/StrataDrawer.kt` | 25700 | 27590 | Side drawer |
| `ui/ActionIcons.kt` | 6600 | 6925 | Action icons |
| `ui/Bidi.kt` | 1000 | 1066 | RTL |
| `ui/LocalUiDensity.kt` | 319 | 319 | Density/settings Local |
| `ui/SheetStates.kt` | 715 | 715 | Sheet state |
| `ui/TextClipboard.kt` | 1100 | 1121 | Clipboard |
| `ui/i18n/AppLocales.kt` | 3100 | 3263 | Languages |
| `ui/security/AppLock.kt` | 3500 | 3538 | App lock |
| `ui/security/DeviceAuth.kt` | 4600 | 4755 | Biometrics |
| `ui/security/LockScreen.kt` | 7000 | 7208 | Lock screen |
| `ui/security/VaultKeystore.kt` | 2500 | 2532 | Vault keystore |
| `ui/theme/KindColors.kt` | 1500 | 2345 | File-type colors |
| `ui/theme/Shape.kt` | 1400 | 1453 | Shapes |
| `ui/theme/Theme.kt` | 6900 | 7052 | Theme |
| `ui/theme/Type.kt` | 362 | 362 | Typography |
| `core/i18n/L10n.kt` | 1100 | 1126 | String source |
| `core/i18n/AppLanguage.kt` | 1000 | 1053 | Language enum |
| `core/data/SettingsRepository.kt` | 28200 | 35722 | Settings read/write |
| `core/data/SettingsBackup.kt` | 3800 | 3847 | Backup codec |
| `core/data/BackupLocations.kt` | 892 | 892 | Backup locations |
| `core/data/StartFolders.kt` | 655 | 935 | Start folders |
| `core/permission/StorageAccess.kt` | 2900 | 2990 | Storage permission |
| `core/open/Opener.kt` | 4700 | 4791 | Open-with |
| `core/open/OpenDefaults.kt` | 1100 | 1129 | Default open |
| `core/open/IncomingFiles.kt` | 2600 | 2654 | External Uri |
| `core/open/IncomingPaths.kt` | — | 3471 | Fork external paths |
| `core/util/IntentUtils.kt` | 2900 | 2970 | Intent/Provider |
| `core/root/RootManager.kt` | 3200 | 6860 | root |
| `core/vault/Vault.kt` | 1400 | 1437 | Vault entry |
| `core/sound/UiSounds.kt` | 2800 | 2864 | Sound policy |
| `core/sound/SoundPoolDevice.kt` | 4800 | 4866 | SoundPool |
| `core/text/StgxRegistry.kt` | — | 4982 | Fork STGX |
| `core/text/StgxParser.kt` | — | 3932 | Fork STGX parser |
| `core/text/StgxIcons.kt` | — | 3159 | Fork STGX icons |
| `core/text/StgxIconLoader.kt` | — | 1246 | Fork base64 icons |
| `core/dhizuku/DhizukuBridge.kt` | — | 5106 | Fork Dhizuku |
| `core/control/AppControlBridge.kt` | — | 10622 | Fork app control |
| `core/export/FileListExport.kt` | — | 12350 | Fork export |
| `core/model/DrawerBox.kt` | — | 1632 | Fork drawer box |
| `core/model/ActionMenu.kt` | — | 615 | Fork long-press menu |
| `core/util/AppIconLoader.kt` | — | 2793 | Fork app icons |
| `core/util/QrCode.kt` | — | 2403 | Fork QR code |

## 2. File Explorer (see 20)
| Path | 1.0.0 | fork | Responsibility |
|---|---|---|---|
| `feature/explorer/ExplorerScreen.kt` | 36300 | 41867 | Explorer screen |
| `feature/explorer/ExplorerViewModel.kt` | 64800 | 70115 | Explorer VM |
| `feature/explorer/ExplorerState.kt` | 6500 | 6606 | State |
| `components/FileRow.kt` | 17100 | 26926 | Row/grid |
| `components/PaneColumn.kt` | 28200 | 32610 | Single pane |
| `components/ItemActionSheet.kt` | 12300 | 14562 | Action sheet |
| `components/ExplorerBottomBar.kt` | 17200 | 17843 | Bottom bar |
| `components/ExplorerDialogs.kt` | 11000 | 11228 | Input/confirm/delete/sort |
| `components/ToolDialogs.kt` | 24100 | 31337 | Tool dialogs |
| `components/TransferDialog.kt` | 4100 | 4194 | Conflict |
| `components/ChecksumDialog.kt` | 3800 | 3908 | Checksum |
| `components/BatchRenameDialog.kt` | 13100 | 13521 | Batch rename |
| `components/BookmarksSheet.kt` | 12000 | 12280 | Bookmarks |
| `components/TaskOverlay.kt` | 1700 | 1727 | Task overlay |
| `dnd/FileDrag.kt` | 10800 | 11034 | Drag state |
| `dnd/DragLayer.kt` | 11900 | 12167 | Drag layer |
| `dnd/DropVisuals.kt` | 4900 | 5060 | Drop visuals |
| `core/bookmarks/Bookmarks.kt` | 4200 | 4259 | Bookmark model |
| `core/bookmarks/BookmarkRepository.kt` | 1000 | 1067 | Bookmark storage |

## 3. Filesystem & Remote (see 30)
| Path | 1.0.0 | fork | Responsibility |
|---|---|---|---|
| `core/fs/FileOperations.kt` | 10100 | 9778 | Copy/move/delete/link |
| `core/fs/DirectoryLister.kt` | 1600 | 1596 | Listing |
| `core/fs/FileAccess.kt` | 1600 | 2301 | Access abstraction |
| `core/fs/FsUtil.kt` | 5400 | 5532 | File utilities |
| `core/fs/TopLevel.kt` | 889 | 889 | Root detection |
| `core/fs/Mounts.kt` | 2000 | 2052 | Mounts |
| `core/fs/DropRules.kt` | 1800 | 1823 | Drop rules |
| `core/fs/SplitChannel.kt` | — | 2747 | Fork split volumes |
| `core/fs/RestrictedDirs.kt` | — | 1054 | Fork restricted dirs |
| `core/remote/CryptoProviders.kt` | 484 | 484 | BouncyCastle |
| `core/remote/FtpFileSystem.kt` | 6800 | 7008 | FTP/FTPS |
| `core/remote/RemoteConnectionRepository.kt` | 1300 | 1316 | Connection storage |
| `core/remote/RemoteConnections.kt` | 1200 | 1217 | Connection codec |
| `core/remote/RemoteFileSystem.kt` | 1900 | 1928 | Interface/factory |
| `core/remote/RemoteModel.kt` | 2800 | 2880 | Models/paths |
| `core/remote/RemoteSessions.kt` | 3100 | 3205 | Sessions |
| `core/remote/RemoteTransfers.kt` | 10500 | 10729 | Transfers |
| `core/remote/RemoteUri.kt` | 1600 | 1594 | remote:// |
| `core/remote/SafFileSystem.kt` | 8800 | 8988 | SAF |
| `core/remote/SftpFileSystem.kt` | 6800 | 6971 | SFTP |
| `core/remote/SmbFileSystem.kt` | 9600 | 9830 | SMB |
| `core/remote/WebDavFileSystem.kt` | 12600 | 12891 | WebDAV |
| `feature/remote/RemoteScreen.kt` | 29000 | 29659 | Remote screen |
| `feature/remote/RemoteViewModel.kt` | 12900 | 13198 | Remote VM |
| `feature/remote/RemoteDialogs.kt` | 4000 | 4101 | Remote dialogs |

## 4. APK & Control (see 40)
| Path | 1.0.0 | fork | Responsibility |
|---|---|---|---|
| `core/apk/install/ApkInstaller.kt` | 6200 | 15998 | Install entry |
| `core/apk/install/InstallBackend.kt` | — | 4165 | Fork backend |
| `core/apk/install/InstallResult.kt` | 4200 | 4532 | Result |
| `core/apk/install/PmInstall.kt` | 2400 | 2457 | pm command |
| `core/apk/install/InstallResultReceiver.kt` | 2100 | 2186 | Broadcast |
| `core/apk/ApkCloner.kt` | 10200 | 10471 | Cloner |
| `core/apk/ApkExtractor.kt` | 2600 | 2625 | Extractor |
| `core/apk/ApkManifestInfo.kt` | 1700 | 1759 | Manifest |
| `core/apk/ApkSignature.kt` | 2300 | 2319 | Signature model |
| `core/apk/ApkSignatureInspector.kt` | 977 | 977 | Verify |
| `core/apk/ApkSigning.kt` | 2900 | 2939 | Signing |
| `core/apk/AppIntents.kt` | 1022 | 1022 | Launch/details/uninstall |
| `core/apk/CloneIcon.kt` | 1600 | 1646 | Icon model |
| `core/apk/CloneIconRenderer.kt` | 2700 | 2785 | Icon rendering |
| `core/apk/InstalledApps.kt` | 3100 | 3372 | Installed apps |
| `core/apk/InstalledSigners.kt` | 2400 | 2437 | Installed signers |
| `core/apk/JksFormat.kt` | 8200 | 8439 | JKS |
| `core/apk/KeyStoreFactory.kt` | 4000 | 4077 | Keystore |
| `core/apk/KeystoreFile.kt` | 7400 | 7610 | Keystore file |
| `core/apk/ProtectionDetector.kt` | 1400 | 1444 | Hardening detection |
| `core/apk/SavedKeys.kt` | 3900 | 3949 | Saved keys |
| `core/apk/SigningKeys.kt` | 1100 | 1148 | Default key cache |
| `core/apk/SplitBundle.kt` | 5700 | 5862 | Split bundle |
| `core/control/AppControlBridge.kt` | — | 10622 | Fork app control |
| `core/dhizuku/DhizukuBridge.kt` | — | 5106 | Fork Dhizuku |
| `feature/apk/ApkDialog.kt` | 27100 | 29901 | APK dialog |
| `feature/apk/ApkDialogViewModel.kt` | 10800 | 15596 | APK VM |
| `feature/apk/ApkInfoScreen.kt` | 11000 | 11290 | APK info |
| `feature/apk/SignApkDialog.kt` | 12500 | 12761 | Sign dialog |
| `feature/apk/SplitInstallDialog.kt` | 12800 | 13726 | Split install |
| `feature/apk/SplitInstallViewModel.kt` | 6400 | 7810 | Split VM |
| `feature/apps/AppControlSheet.kt` | — | 7667 | Fork control sheet |
| `feature/apps/AppsScreen.kt` | 22900 | 35834 | Apps screen |
| `feature/apps/AppsViewModel.kt` | 5900 | 22157 | Apps VM |

## 5. Settings (see 50)
| Path | 1.0.0 | fork | Responsibility |
|---|---|---|---|
| `feature/settings/SettingsScreen.kt` | 22100 | 23904 | Settings home/navigation |
| `feature/settings/SettingsPages.kt` | 36600 | 55798 | Pages |
| `feature/settings/AppearancePage.kt` | 18200 | 18684 | Appearance |
| `feature/settings/SecurityPages.kt` | 13200 | 13657 | Security/language |
| `feature/settings/ToolbarPage.kt` | 18900 | 19353 | Toolbar |
| `feature/settings/ZoneEditor.kt` | 22500 | 23032 | Zone editor |
| `feature/settings/SupportPage.kt` | 4600 | 8849 | Support |
| `feature/settings/DrawerBoxPage.kt` | — | 12877 | Fork drawer box |
| `feature/settings/StartLocationPage.kt` | — | 4505 | Fork start location |
| `feature/settings/SettingsComponents.kt` | 15200 | 15609 | Components |

## 6. Archive/DEX/PDF/Hex (see 60)
| Path | 1.0.0 | fork | Responsibility |
|---|---|---|---|
| `core/archive/ArchiveCreator.kt` | 14200 | 16486 | Create archive |
| `core/archive/ArchiveManager.kt` | 16000 | 17409 | Extract/manage |
| `core/archive/ArchiveReader.kt` | 7600 | 9978 | List/read |
| `core/archive/RarArchive.kt` | 4600 | 4723 | RAR |
| `core/archive/SevenZHeaderCrypto.kt` | 5100 | 5261 | 7z header crypto |
| `core/archive/SevenZKeys.kt` | 2500 | 2566 | 7z keys |
| `core/archive/ZipEditor.kt` | 4000 | 4060 | zip editing |
| `core/dex/DexConvert.kt` | 10000 | 10197 | Conversion |
| `core/dex/DexIndex.kt` | 4300 | 4437 | Index |
| `core/dex/DexSearch.kt` | 11400 | 11689 | Search |
| `core/dex/DexTools.kt` | 8400 | 8610 | Tools |
| `core/dex/DexTree.kt` | 2800 | 2852 | Tree |
| `core/dex/JavaDecompiler.kt` | 4000 | 4084 | Decompiler |
| `core/dex/SmaliAssembler.kt` | 5300 | 5439 | Assembler |
| `core/dex/SmaliOutline.kt` | 3700 | 3819 | Outline |
| `core/dex/SmaliRefs.kt` | 5300 | 5407 | References |
| `core/dex/SmaliText.kt` | 5200 | 5276 | Text |
| `core/pdf/PdfFile.kt` | 21000 | 21487 | File |
| `core/pdf/PdfFilters.kt` | 6400 | 6593 | Filters |
| `core/pdf/PdfLexer.kt` | 10100 | 10384 | Lexer |
| `core/pdf/PdfMerger.kt` | 10300 | 10597 | Merger |
| `core/pdf/PdfObjects.kt` | 4400 | 4518 | Objects |
| `core/pdf/PdfSecurity.kt` | 10500 | 10758 | Security |
| `core/pdf/PdfWriter.kt` | 4600 | 4725 | Writer |
| `core/pdf/view/DocLayout.kt` | 5000 | 5109 | Layout |
| `core/pdf/view/PdfDocument.kt` | 8300 | 8540 | Rendering |
| `core/pdf/view/PdfSearch.kt` | 7000 | 7201 | Search |
| `core/pdf/view/PdfTextLayout.kt` | 21300 | 21773 | Text layout |
| `core/pdf/view/Pdfium.kt` | 1600 | 1674 | JNI |
| `core/pdf/view/ReaderMemory.kt` | 3000 | 3051 | Memory |
| `core/hex/ByteSource.kt` | 3600 | 3683 | Source |
| `core/hex/ChannelByteSource.kt` | 1800 | 1832 | Channel |
| `core/hex/ChunkedSearch.kt` | 2800 | 2844 | Chunked search |
| `core/hex/HexCodec.kt` | 10200 | 10413 | Codec |
| `core/hex/HexDocument.kt` | 5600 | 5726 | Document |
| `feature/pdf/PageImages.kt` | 6300 | 6458 | Images |
| `feature/pdf/PdfReaderScreen.kt` | 43600 | 44663 | Reader |
| `feature/pdf/PdfReaderViewModel.kt` | 12700 | 13029 | Reader VM |
| `feature/pdf/ReaderActions.kt` | 4500 | 4628 | Actions |
| `feature/pdf/ReaderSheets.kt` | 23800 | 24385 | Sheets |
| `feature/pdf/ReaderViewport.kt` | 17100 | 17536 | Viewport |

## 7. root/Termux/Plugin/Sharing (see 70)
| Path | 1.0.0 | fork | Responsibility |
|---|---|---|---|
| `core/root/RootCommands.kt` | 1300 | 1376 | Commands |
| `core/root/RootFileAccess.kt` | 2100 | 2153 | Access |
| `core/root/RootManager.kt` | 3200 | 6860 | Manager |
| `core/root/RootPaths.kt` | 525 | 525 | Paths |
| `core/root/RootPosixApi.kt` | 1400 | 1407 | POSIX |
| `core/root/StrataRootService.kt` | 320 | 320 | Service |
| `core/shizuku/ShizukuFsService.kt` | 317 | 635 | FS service |
| `core/shizuku/ShizukuManager.kt` | 5300 | 5942 | Manager |
| `core/termux/RunCommands.kt` | 5800 | 5899 | Run commands |
| `core/termux/TermuxApi.kt` | 5400 | 5519 | API |
| `core/termux/TermuxBridge.kt` | 5600 | 5717 | Bridge |
| `core/termux/TermuxLanguages.kt` | 11000 | 11273 | Languages |
| `core/termux/TermuxProjects.kt` | 6900 | 7053 | Projects |
| `core/termux/TermuxSession.kt` | 12800 | 13087 | Session |
| `core/termux/TermuxSsh.kt` | 1300 | 1316 | SSH |
| `core/plugin/PluginFiles.kt` | 1300 | 1327 | Files |
| `core/plugin/PluginPins.kt` | 1500 | 1518 | Pins |
| `core/plugin/PluginProtocol.kt` | 3600 | 3698 | Protocol |
| `core/plugin/PluginShare.kt` | 2100 | 2175 | Share |
| `core/plugin/Plugins.kt` | 4600 | 4742 | Discovery/trust |
| `core/share/LanFtpServer.kt` | 4300 | 4403 | FTP |
| `core/share/LanGuards.kt` | 2400 | 2505 | Guards |
| `core/share/LanHttpServer.kt` | 10000 | 10280 | HTTP |
| `feature/termux/CommandRunnerScreen.kt` | — | 19084 | Fork command runner |
| `feature/termux/LanguagesScreen.kt` | 6300 | 6476 | Languages |
| `feature/termux/NewProjectSheet.kt` | 8100 | 8286 | New project |
| `feature/termux/TermuxUi.kt` | 28800 | 29506 | Termux UI |
| `feature/termux/TermuxViewModel.kt` | 396 | 4132 | Termux VM |

## 8. Text Editor (see 80)
| Path | 1.0.0 | fork | Responsibility |
|---|---|---|---|
| `feature/editor/CodeEditorView.kt` | 1300 | 1294 | View |
| `feature/editor/EditorColors.kt` | 4300 | 4410 | Colors |
| `feature/editor/EditorController.kt` | 9300 | 9521 | Controller |
| `feature/editor/TextEditorScreen.kt` | 43800 | 45846 | Editor screen |
| `feature/editor/TextEditorViewModel.kt` | 20000 | 21515 | Editor VM |
| `feature/editor/TextMateEngine.kt` | 3900 | 7928 | Grammar/theme |
| `feature/grammar/GrammarScreen.kt` | — | 30791 | Grammar management page |
| `core/text/EditTracker.kt` | 1400 | 1403 | Edit tracking |
| `core/text/GrammarCatalog.kt` | 4300 | 4612 | Grammar catalog |
| `core/text/TextEncoding.kt` | 5400 | 8989 | Encoding |
| `core/text/TextPager.kt` | 5500 | 5594 | Paging |
| `core/text/Stgx*.kt` | — | see 80 | Fork STGX |

## 9. Vault & Security & Misc (see 90)
| Path | 1.0.0 | fork | Responsibility |
|---|---|---|---|
| `core/vault/Vault.kt` | 1400 | 1437 | Entry |
| `core/vault/VaultCipher.kt` | 6800 | 6971 | Encryption/decryption |
| `core/vault/VaultFileSystem.kt` | 1400 | 1442 | FS |
| `core/vault/VaultModel.kt` | 1700 | 1727 | Model |
| `core/vault/VaultStore.kt` | 18900 | 19305 | Storage |
| `core/vault/VaultTransfers.kt` | 8300 | 8501 | Transfers |
| `ui/security/LockScreen.kt` | 7000 | 7208 | Lock screen |
| `feature/vault/VaultAccess.kt` | 5700 | 5828 | Access |
| `feature/vault/VaultDialogs.kt` | 15500 | 15853 | Dialogs |
| `feature/trash/RecycleBinScreen.kt` | 11900 | 12146 | Recycle bin screen |
| `feature/trash/RecycleBinViewModel.kt` | 4300 | 4406 | Recycle bin VM |
| `feature/share/ShareScreen.kt` | 12500 | 12791 | Share screen |
| `feature/share/ShareService.kt` | 8900 | 9137 | Share service |

## 10. native & Build Resources (see A0)
| Path | 1.0.0 | fork | Responsibility |
|---|---|---|---|
| `native/src/crypt.rs` | 6000 | 6177 | Crypto |
| `native/src/hexio.rs` | 8200 | 8388 | Hex I/O |
| `native/src/lib.rs` | 14300 | 14677 | JNI table |
| `native/src/pdfium.rs` | 35500 | 36403 | pdfium |
| `native/src/rar.rs` | 12200 | 12473 | RAR |
| `native/build.rs` | 498 | 498 | Build |
| `native/Cargo.toml` | 682 | 682 | Dependencies |
| `app/build.gradle.kts` | 9300 | 10500 | App build |
| `gradle/libs.versions.toml` | 10200 | 10300 | Version catalog |
| `settings.gradle.kts` | 734 | 734 | Modules |
| `gradle.properties` | 304 | 396 | Properties |
| `local.properties` | 53 | 53 | SDK |
| `res/values/*` | see A0 | see A0 | Resources |
| `res/values-zh-rCN/*` | see A0 | see A0 | Resources |
| `res/values-fa/*`, `values-ru/*`, `values-night/*` | see A0 | see A0 | Resources |
| `res/xml/*` | 132/276/242/266/253/110 | same | Config |
| `res/drawable/*` | 612/514/—/741 | 612/514/2868/741 | Icons |
| `res/mipmap-anydpi-v26/*` | 341/341 | 341/341 | Launcher icons |
| `res/drawable-nodpi/*` | 3 webp | 5 webp | Illustrations |
| `res/raw/*` | 11 ogg | 11 ogg | Sounds |