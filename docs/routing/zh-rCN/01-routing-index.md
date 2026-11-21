# 总路由索引

> 字节数一律 byte。`—` 表示该版本无此文件。完整逐文件表在各分册 A.1/B.1/C.1。
> 1.0.0 字节取自 `原仓库-list.md`；fork 字节取自运行结果。

## 1. 入口与外壳（详见 10）
| 路径 | 1.0.0 | fork | 职责 |
|---|---|---|---|
| `MainActivity.kt` | 6300 | 6317 | 唯一 Activity；LAUNCHER + VIEW |
| `StrataApp.kt` | 1800 | 2021 | Application；全局初始化 |
| `ui/AppShell.kt` | 29000 | 38559 | 外壳：路由、抽屉、弹窗宿主、Screen 分发 |
| `ui/RouteStack.kt` | 1700 | 1739 | 导航栈 |
| `ui/drawer/StrataDrawer.kt` | 25700 | 27590 | 侧边抽屉 |
| `ui/ActionIcons.kt` | 6600 | 6925 | 动作图标 |
| `ui/Bidi.kt` | 1000 | 1066 | RTL |
| `ui/LocalUiDensity.kt` | 319 | 319 | 密度/设置 Local |
| `ui/SheetStates.kt` | 715 | 715 | sheet 状态 |
| `ui/TextClipboard.kt` | 1100 | 1121 | 剪贴板 |
| `ui/i18n/AppLocales.kt` | 3100 | 3263 | 语言 |
| `ui/security/AppLock.kt` | 3500 | 3538 | 应用锁 |
| `ui/security/DeviceAuth.kt` | 4600 | 4755 | 生物识别 |
| `ui/security/LockScreen.kt` | 7000 | 7208 | 锁屏 |
| `ui/security/VaultKeystore.kt` | 2500 | 2532 | 保险库密钥 |
| `ui/theme/KindColors.kt` | 1500 | 2345 | 文件类型色 |
| `ui/theme/Shape.kt` | 1400 | 1453 | 形状 |
| `ui/theme/Theme.kt` | 6900 | 7052 | 主题 |
| `ui/theme/Type.kt` | 362 | 362 | 字体 |
| `core/i18n/L10n.kt` | 1100 | 1126 | 字符串源 |
| `core/i18n/AppLanguage.kt` | 1000 | 1053 | 语言枚举 |
| `core/data/SettingsRepository.kt` | 28200 | 35722 | 设置读写 |
| `core/data/SettingsBackup.kt` | 3800 | 3847 | 备份编解码 |
| `core/data/BackupLocations.kt` | 892 | 892 | 备份位置 |
| `core/data/StartFolders.kt` | 655 | 935 | 启动目录 |
| `core/permission/StorageAccess.kt` | 2900 | 2990 | 存储权限 |
| `core/open/Opener.kt` | 4700 | 4791 | 打开方式 |
| `core/open/OpenDefaults.kt` | 1100 | 1129 | 默认打开 |
| `core/open/IncomingFiles.kt` | 2600 | 2654 | 外部 Uri |
| `core/open/IncomingPaths.kt` | — | 3471 | fork 外部路径 |
| `core/util/IntentUtils.kt` | 2900 | 2970 | Intent/Provider |
| `core/root/RootManager.kt` | 3200 | 6860 | root |
| `core/vault/Vault.kt` | 1400 | 1437 | 保险库入口 |
| `core/sound/UiSounds.kt` | 2800 | 2864 | 音效策略 |
| `core/sound/SoundPoolDevice.kt` | 4800 | 4866 | SoundPool |
| `core/text/StgxRegistry.kt` | — | 4982 | fork STGX |
| `core/text/StgxParser.kt` | — | 3932 | fork STGX 解析 |
| `core/text/StgxIcons.kt` | — | 3159 | fork STGX 图标 |
| `core/text/StgxIconLoader.kt` | — | 1246 | fork base64 图标 |
| `core/dhizuku/DhizukuBridge.kt` | — | 5106 | fork Dhizuku |
| `core/control/AppControlBridge.kt` | — | 10622 | fork 应用控制 |
| `core/export/FileListExport.kt` | — | 12350 | fork 导出 |
| `core/model/DrawerBox.kt` | — | 1632 | fork 抽屉盒 |
| `core/model/ActionMenu.kt` | — | 615 | fork 长按菜单 |
| `core/util/AppIconLoader.kt` | — | 2793 | fork 应用图标 |
| `core/util/QrCode.kt` | — | 2403 | fork 二维码 |

## 2. 文件浏览器（详见 20）
| 路径 | 1.0.0 | fork | 职责 |
|---|---|---|---|
| `feature/explorer/ExplorerScreen.kt` | 36300 | 41867 | 浏览器屏幕 |
| `feature/explorer/ExplorerViewModel.kt` | 64800 | 70115 | 浏览器 VM |
| `feature/explorer/ExplorerState.kt` | 6500 | 6606 | 状态 |
| `components/FileRow.kt` | 17100 | 26926 | 行/网格 |
| `components/PaneColumn.kt` | 28200 | 30583 | 单栏 |
| `components/ItemActionSheet.kt` | 12300 | 14562 | 动作面板 |
| `components/ExplorerBottomBar.kt` | 17200 | 17843 | 底栏 |
| `components/ExplorerDialogs.kt` | 11000 | 11228 | 输入/确认/删除/排序 |
| `components/ToolDialogs.kt` | 24100 | 31337 | 工具对话框 |
| `components/TransferDialog.kt` | 4100 | 4194 | 冲突 |
| `components/ChecksumDialog.kt` | 3800 | 3908 | 校验和 |
| `components/BatchRenameDialog.kt` | 13100 | 13521 | 批量重命名 |
| `components/BookmarksSheet.kt` | 12000 | 12280 | 书签 |
| `components/TaskOverlay.kt` | 1700 | 1727 | 任务遮罩 |
| `dnd/FileDrag.kt` | 10800 | 11034 | 拖拽状态 |
| `dnd/DragLayer.kt` | 11900 | 12167 | 拖影 |
| `dnd/DropVisuals.kt` | 4900 | 5060 | 落点视觉 |
| `core/bookmarks/Bookmarks.kt` | 4200 | 4259 | 书签模型 |
| `core/bookmarks/BookmarkRepository.kt` | 1000 | 1067 | 书签存储 |

## 3. 文件系统与远程（详见 30）
| 路径 | 1.0.0 | fork | 职责 |
|---|---|---|---|
| `core/fs/FileOperations.kt` | 10100 | 9778 | 复制/移动/删除/建链 |
| `core/fs/DirectoryLister.kt` | 1600 | 1596 | 列举 |
| `core/fs/FileAccess.kt` | 1600 | 2301 | 访问抽象 |
| `core/fs/FsUtil.kt` | 5400 | 5532 | 文件工具 |
| `core/fs/TopLevel.kt` | 889 | 889 | 根探测 |
| `core/fs/Mounts.kt` | 2000 | 2052 | 挂载 |
| `core/fs/DropRules.kt` | 1800 | 1823 | 拖放判定 |
| `core/fs/SplitChannel.kt` | — | 2747 | fork 分卷 |
| `core/fs/RestrictedDirs.kt` | — | 1054 | fork 受限目录 |
| `core/remote/CryptoProviders.kt` | 484 | 484 | BouncyCastle |
| `core/remote/FtpFileSystem.kt` | 6800 | 7008 | FTP/FTPS |
| `core/remote/RemoteConnectionRepository.kt` | 1300 | 1316 | 连接存储 |
| `core/remote/RemoteConnections.kt` | 1200 | 1217 | 连接编解码 |
| `core/remote/RemoteFileSystem.kt` | 1900 | 1928 | 接口/工厂 |
| `core/remote/RemoteModel.kt` | 2800 | 2880 | 模型/路径 |
| `core/remote/RemoteSessions.kt` | 3100 | 3205 | 会话 |
| `core/remote/RemoteTransfers.kt` | 10500 | 10729 | 传输 |
| `core/remote/RemoteUri.kt` | 1600 | 1594 | remote:// |
| `core/remote/SafFileSystem.kt` | 8800 | 8988 | SAF |
| `core/remote/SftpFileSystem.kt` | 6800 | 6971 | SFTP |
| `core/remote/SmbFileSystem.kt` | 9600 | 9830 | SMB |
| `core/remote/WebDavFileSystem.kt` | 12600 | 12891 | WebDAV |
| `feature/remote/RemoteScreen.kt` | 29000 | 29659 | 远程屏 |
| `feature/remote/RemoteViewModel.kt` | 12900 | 13198 | 远程 VM |
| `feature/remote/RemoteDialogs.kt` | 4000 | 4101 | 远程对话框 |

## 4. APK 与控制（详见 40）
| 路径 | 1.0.0 | fork | 职责 |
|---|---|---|---|
| `core/apk/install/ApkInstaller.kt` | 6200 | 15998 | 安装入口 |
| `core/apk/install/InstallBackend.kt` | — | 4165 | fork 后端 |
| `core/apk/install/InstallResult.kt` | 4200 | 4532 | 结果 |
| `core/apk/install/PmInstall.kt` | 2400 | 2457 | pm 命令 |
| `core/apk/install/InstallResultReceiver.kt` | 2100 | 2186 | 广播 |
| `core/apk/ApkCloner.kt` | 10200 | 10471 | 克隆 |
| `core/apk/ApkExtractor.kt` | 2600 | 2625 | 提取 |
| `core/apk/ApkManifestInfo.kt` | 1700 | 1759 | Manifest |
| `core/apk/ApkSignature.kt` | 2300 | 2319 | 签名模型 |
| `core/apk/ApkSignatureInspector.kt` | 977 | 977 | 验签 |
| `core/apk/ApkSigning.kt` | 2900 | 2939 | 签名 |
| `core/apk/AppIntents.kt` | 1022 | 1022 | 启动/详情/卸载 |
| `core/apk/CloneIcon.kt` | 1600 | 1646 | 图标模型 |
| `core/apk/CloneIconRenderer.kt` | 2700 | 2785 | 图标渲染 |
| `core/apk/InstalledApps.kt` | 3100 | 3372 | 已安装应用 |
| `core/apk/InstalledSigners.kt` | 2400 | 2437 | 已安装签名 |
| `core/apk/JksFormat.kt` | 8200 | 8439 | JKS |
| `core/apk/KeyStoreFactory.kt` | 4000 | 4077 | 密钥库 |
| `core/apk/KeystoreFile.kt` | 7400 | 7610 | 密钥库文件 |
| `core/apk/ProtectionDetector.kt` | 1400 | 1444 | 加固检测 |
| `core/apk/SavedKeys.kt` | 3900 | 3949 | 保存密钥 |
| `core/apk/SigningKeys.kt` | 1100 | 1148 | 默认密钥缓存 |
| `core/apk/SplitBundle.kt` | 5700 | 5862 | split 包 |
| `core/control/AppControlBridge.kt` | — | 10622 | fork 应用控制 |
| `core/dhizuku/DhizukuBridge.kt` | — | 5106 | fork Dhizuku |
| `feature/apk/ApkDialog.kt` | 27100 | 29901 | APK 对话框 |
| `feature/apk/ApkDialogViewModel.kt` | 10800 | 15596 | APK VM |
| `feature/apk/ApkInfoScreen.kt` | 11000 | 11290 | APK 信息 |
| `feature/apk/SignApkDialog.kt` | 12500 | 12761 | 签名对话框 |
| `feature/apk/SplitInstallDialog.kt` | 12800 | 13726 | split 安装 |
| `feature/apk/SplitInstallViewModel.kt` | 6400 | 7810 | split VM |
| `feature/apps/AppControlSheet.kt` | — | 7667 | fork 控制 sheet |
| `feature/apps/AppsScreen.kt` | 22900 | 35834 | 应用屏 |
| `feature/apps/AppsViewModel.kt` | 5900 | 22157 | 应用 VM |

## 5. 设置（详见 50）
| 路径 | 1.0.0 | fork | 职责 |
|---|---|---|---|
| `feature/settings/SettingsScreen.kt` | 22100 | 23904 | 设置首页/导航 |
| `feature/settings/SettingsPages.kt` | 36600 | 55798 | 各页 |
| `feature/settings/AppearancePage.kt` | 18200 | 18684 | 外观 |
| `feature/settings/SecurityPages.kt` | 13200 | 13657 | 安全/语言 |
| `feature/settings/ToolbarPage.kt` | 18900 | 19353 | 工具栏 |
| `feature/settings/ZoneEditor.kt` | 22500 | 23032 | 分区编辑 |
| `feature/settings/SupportPage.kt` | 4600 | 8849 | 支持 |
| `feature/settings/DrawerBoxPage.kt` | — | 12877 | fork 抽屉盒 |
| `feature/settings/StartLocationPage.kt` | — | 4505 | fork 启动位置 |
| `feature/settings/SettingsComponents.kt` | 15200 | 15609 | 组件 |

## 6. 归档/DEX/PDF/Hex（详见 60）
| 路径 | 1.0.0 | fork | 职责 |
|---|---|---|---|
| `core/archive/ArchiveCreator.kt` | 14200 | 16486 | 创建归档 |
| `core/archive/ArchiveManager.kt` | 16000 | 17409 | 解压/管理 |
| `core/archive/ArchiveReader.kt` | 7600 | 9978 | 列表/读取 |
| `core/archive/RarArchive.kt` | 4600 | 4723 | RAR |
| `core/archive/SevenZHeaderCrypto.kt` | 5100 | 5261 | 7z 头加密 |
| `core/archive/SevenZKeys.kt` | 2500 | 2566 | 7z 密钥 |
| `core/archive/ZipEditor.kt` | 4000 | 4060 | zip 编辑 |
| `core/dex/DexConvert.kt` | 10000 | 10197 | 转换 |
| `core/dex/DexIndex.kt` | 4300 | 4437 | 索引 |
| `core/dex/DexSearch.kt` | 11400 | 11689 | 搜索 |
| `core/dex/DexTools.kt` | 8400 | 8610 | 工具 |
| `core/dex/DexTree.kt` | 2800 | 2852 | 树 |
| `core/dex/JavaDecompiler.kt` | 4000 | 4084 | 反编译 |
| `core/dex/SmaliAssembler.kt` | 5300 | 5439 | 汇编 |
| `core/dex/SmaliOutline.kt` | 3700 | 3819 | 大纲 |
| `core/dex/SmaliRefs.kt` | 5300 | 5407 | 引用 |
| `core/dex/SmaliText.kt` | 5200 | 5276 | 文本 |
| `core/pdf/PdfFile.kt` | 21000 | 21487 | 文件 |
| `core/pdf/PdfFilters.kt` | 6400 | 6593 | 滤镜 |
| `core/pdf/PdfLexer.kt` | 10100 | 10384 | 词法 |
| `core/pdf/PdfMerger.kt` | 10300 | 10597 | 合并 |
| `core/pdf/PdfObjects.kt` | 4400 | 4518 | 对象 |
| `core/pdf/PdfSecurity.kt` | 10500 | 10758 | 安全 |
| `core/pdf/PdfWriter.kt` | 4600 | 4725 | 写 |
| `core/pdf/view/DocLayout.kt` | 5000 | 5109 | 布局 |
| `core/pdf/view/PdfDocument.kt` | 8300 | 8540 | 渲染 |
| `core/pdf/view/PdfSearch.kt` | 7000 | 7201 | 搜索 |
| `core/pdf/view/PdfTextLayout.kt` | 21300 | 21773 | 文本布局 |
| `core/pdf/view/Pdfium.kt` | 1600 | 1674 | JNI |
| `core/pdf/view/ReaderMemory.kt` | 3000 | 3051 | 记忆 |
| `core/hex/ByteSource.kt` | 3600 | 3683 | 源 |
| `core/hex/ChannelByteSource.kt` | 1800 | 1832 | 通道 |
| `core/hex/ChunkedSearch.kt` | 2800 | 2844 | 分块搜索 |
| `core/hex/HexCodec.kt` | 10200 | 10413 | 编解码 |
| `core/hex/HexDocument.kt` | 5600 | 5726 | 文档 |
| `feature/pdf/PageImages.kt` | 6300 | 6458 | 图片 |
| `feature/pdf/PdfReaderScreen.kt` | 43600 | 44663 | 阅读器 |
| `feature/pdf/PdfReaderViewModel.kt` | 12700 | 13029 | 阅读器 VM |
| `feature/pdf/ReaderActions.kt` | 4500 | 4628 | 动作 |
| `feature/pdf/ReaderSheets.kt` | 23800 | 24385 | sheet |
| `feature/pdf/ReaderViewport.kt` | 17100 | 17536 | 视口 |

## 7. root/Termux/插件/分享（详见 70）
| 路径 | 1.0.0 | fork | 职责 |
|---|---|---|---|
| `core/root/RootCommands.kt` | 1300 | 1376 | 命令 |
| `core/root/RootFileAccess.kt` | 2100 | 2153 | 访问 |
| `core/root/RootManager.kt` | 3200 | 6860 | 管理 |
| `core/root/RootPaths.kt` | 525 | 525 | 路径 |
| `core/root/RootPosixApi.kt` | 1400 | 1407 | POSIX |
| `core/root/StrataRootService.kt` | 320 | 320 | 服务 |
| `core/shizuku/ShizukuFsService.kt` | 317 | 635 | FS 服务 |
| `core/shizuku/ShizukuManager.kt` | 5300 | 5942 | 管理 |
| `core/termux/RunCommands.kt` | 5800 | 5899 | 运行命令 |
| `core/termux/TermuxApi.kt` | 5400 | 5519 | API |
| `core/termux/TermuxBridge.kt` | 5600 | 5717 | 桥 |
| `core/termux/TermuxLanguages.kt` | 11000 | 11273 | 语言 |
| `core/termux/TermuxProjects.kt` | 6900 | 7053 | 项目 |
| `core/termux/TermuxSession.kt` | 12800 | 13087 | 会话 |
| `core/termux/TermuxSsh.kt` | 1300 | 1316 | SSH |
| `core/plugin/PluginFiles.kt` | 1300 | 1327 | 文件 |
| `core/plugin/PluginPins.kt` | 1500 | 1518 | pin |
| `core/plugin/PluginProtocol.kt` | 3600 | 3698 | 协议 |
| `core/plugin/PluginShare.kt` | 2100 | 2175 | 分享 |
| `core/plugin/Plugins.kt` | 4600 | 4742 | 发现/信任 |
| `core/share/LanFtpServer.kt` | 4300 | 4403 | FTP |
| `core/share/LanGuards.kt` | 2400 | 2505 | 守卫 |
| `core/share/LanHttpServer.kt` | 10000 | 10280 | HTTP |
| `feature/termux/CommandRunnerScreen.kt` | — | 19084 | fork 命令运行器 |
| `feature/termux/LanguagesScreen.kt` | 6300 | 6476 | 语言 |
| `feature/termux/NewProjectSheet.kt` | 8100 | 8286 | 新项目 |
| `feature/termux/TermuxUi.kt` | 28800 | 29506 | Termux UI |
| `feature/termux/TermuxViewModel.kt` | 396 | 4132 | Termux VM |

## 8. 文本编辑器（详见 80）
| 路径 | 1.0.0 | fork | 职责 |
|---|---|---|---|
| `feature/editor/CodeEditorView.kt` | 1300 | 1294 | 视图 |
| `feature/editor/EditorColors.kt` | 4300 | 4410 | 配色 |
| `feature/editor/EditorController.kt` | 9300 | 9521 | 控制器 |
| `feature/editor/TextEditorScreen.kt` | 43800 | 45846 | 编辑屏 |
| `feature/editor/TextEditorViewModel.kt` | 20000 | 21515 | 编辑 VM |
| `feature/editor/TextMateEngine.kt` | 3900 | 7928 | 语法/主题 |
| `feature/grammar/GrammarScreen.kt` | — | 30791 | 语法管理页 |
| `core/text/EditTracker.kt` | 1400 | 1403 | 编辑跟踪 |
| `core/text/GrammarCatalog.kt` | 4300 | 4612 | 语法目录 |
| `core/text/TextEncoding.kt` | 5400 | 8989 | 编码 |
| `core/text/TextPager.kt` | 5500 | 5594 | 分页 |
| `core/text/Stgx*.kt` | — | 见 80 | fork STGX |

## 9. 保险库与安全与其他（详见 90）
| 路径 | 1.0.0 | fork | 职责 |
|---|---|---|---|
| `core/vault/Vault.kt` | 1400 | 1437 | 入口 |
| `core/vault/VaultCipher.kt` | 6800 | 6971 | 加解密 |
| `core/vault/VaultFileSystem.kt` | 1400 | 1442 | FS |
| `core/vault/VaultModel.kt` | 1700 | 1727 | 模型 |
| `core/vault/VaultStore.kt` | 18900 | 19305 | 存储 |
| `core/vault/VaultTransfers.kt` | 8300 | 8501 | 传输 |
| `ui/security/LockScreen.kt` | 7000 | 7208 | 锁屏 |
| `feature/vault/VaultAccess.kt` | 5700 | 5828 | 访问 |
| `feature/vault/VaultDialogs.kt` | 15500 | 15853 | 对话框 |
| `feature/trash/RecycleBinScreen.kt` | 11900 | 12146 | 回收站屏 |
| `feature/trash/RecycleBinViewModel.kt` | 4300 | 4406 | 回收站 VM |
| `feature/share/ShareScreen.kt` | 12500 | 12791 | 分享屏 |
| `feature/share/ShareService.kt` | 8900 | 9137 | 分享服务 |

## 10. native 与构建资源（详见 A0）
| 路径 | 1.0.0 | fork | 职责 |
|---|---|---|---|
| `native/src/crypt.rs` | 6000 | 6177 | 加解密 |
| `native/src/hexio.rs` | 8200 | 8388 | 十六进制 I/O |
| `native/src/lib.rs` | 14300 | 14677 | JNI 总表 |
| `native/src/pdfium.rs` | 35500 | 36403 | pdfium |
| `native/src/rar.rs` | 12200 | 12473 | RAR |
| `native/build.rs` | 498 | 498 | 构建 |
| `native/Cargo.toml` | 682 | 682 | 依赖 |
| `app/build.gradle.kts` | 9300 | 10500 | 应用构建 |
| `gradle/libs.versions.toml` | 10200 | 10300 | 版本目录 |
| `settings.gradle.kts` | 734 | 734 | 模块 |
| `gradle.properties` | 304 | 396 | 属性 |
| `local.properties` | 53 | 53 | SDK |
| `res/values/*` | 见 A0 | 见 A0 | 资源 |
| `res/values-zh-rCN/*` | 见 A0 | 见 A0 | 资源 |
| `res/values-fa/*`、`values-ru/*`、`values-night/*` | 见 A0 | 见 A0 | 资源 |
| `res/xml/*` | 132/276/242/266/253/110 | 同左 | 配置 |
| `res/drawable/*` | 612/514/—/741 | 612/514/2868/741 | 图标 |
| `res/mipmap-anydpi-v26/*` | 341/341 | 341/341 | 启动图标 |
| `res/drawable-nodpi/*` | 3 个 webp | 5 个 webp | 插画 |
| `res/raw/*` | 11 个 ogg | 11 个 ogg | 音效 |