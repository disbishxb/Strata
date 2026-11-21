# 60 归档DEX与PDF

> 元规则：改代码后更新本册字节数与条目；用 `stat -c '%s %n'` 核对。字节单位 byte。

## A. fork 与 1.0.0 相同的逻辑

### A.1 文件路由表
| 路径 | 1.0.0 | fork | 职责 |
|---|---|---|---|
| `core/archive/ArchiveCreator.kt` | 14556 | 16486 | 创建 zip/7z/tar；加密/等级/分卷 |
| `core/archive/ArchiveManager.kt` | 16367 | 17409 | 解压 zip/7z/rar/tar/单文件；ArchiveSelection/PasswordRequired/WrongPassword |
| `core/archive/ArchiveReader.kt` | 7822 | 9978 | 列表/读取；ArchiveEntry/ArchiveTree；分卷 |
| `core/archive/RarArchive.kt` | 4723 | 4723 | RAR 头/卷/失败映射 |
| `core/archive/SevenZHeaderCrypto.kt` | 5261 | 5261 | 7z 头加密 |
| `core/archive/SevenZKeys.kt` | 2566 | 2566 | 7z 密钥派生/缓存 |
| `core/archive/ZipEditor.kt` | 4060 | 4060 | zip 原地编辑/删除/重命名/添加 |
| `core/dex/DexConvert.kt` | 10197 | 10197 | smali→dex；jar→dex（D8） |
| `core/dex/DexIndex.kt` | 4437 | 4437 | 加载/类/方法/字段解析/smali 反汇编 |
| `core/dex/DexSearch.kt` | 11689 | 11689 | 搜索模式/匹配/并发/结果 |
| `core/dex/DexTools.kt` | 8610 | 8610 | dex→jar；反汇编；替换类；入口 |
| `core/dex/DexTree.kt` | 2852 | 2852 | 包/类树 |
| `core/dex/JavaDecompiler.kt` | 4084 | 4084 | jadx 反编译/成员 |
| `core/dex/SmaliAssembler.kt` | 5439 | 5439 | smali 汇编/API 探测/错误 |
| `core/dex/SmaliOutline.kt` | 3819 | 3819 | 成员大纲 |
| `core/dex/SmaliRefs.kt` | 5407 | 5407 | 引用跨度/类型/方法/字段 |
| `core/dex/SmaliText.kt` | 5276 | 5276 | 可读化/转义/字面量/注释 |
| `core/pdf/PdfFile.kt` | 21487 | 21487 | PDF 解析/xref/修复/页面/流/加密 |
| `core/pdf/PdfFilters.kt` | 6593 | 6593 | Flate/AHx/A85/predict |
| `core/pdf/PdfLexer.kt` | 10384 | 10384 | 词法/Token/对象 |
| `core/pdf/PdfMerger.kt` | 10597 | 10597 | 合并 PDF/书签 |
| `core/pdf/PdfObjects.kt` | 4518 | 4518 | 对象/源 |
| `core/pdf/PdfSecurity.kt` | 10758 | 10758 | 标准加密/RC4/AES |
| `core/pdf/PdfWriter.kt` | 4725 | 4725 | 写对象/流/xref |
| `core/pdf/view/DocLayout.kt` | 5109 | 5109 | 页面布局/缩放/可见 |
| `core/pdf/view/PdfDocument.kt` | 8540 | 8540 | 打开/渲染/字符/链接/大纲/元数据 |
| `core/pdf/view/PdfSearch.kt` | 7201 | 7201 | 折叠/查找/片段 |
| `core/pdf/view/PdfTextLayout.kt` | 21773 | 21773 | 文本布局/行/单元/重排 |
| `core/pdf/view/Pdfium.kt` | 1674 | 1674 | JNI 表 |
| `core/pdf/view/ReaderMemory.kt` | 3051 | 3051 | 阅读位置/书签编解码 |
| `core/hex/ByteSource.kt` | 3683 | 3683 | 源接口/文件/原生 |
| `core/hex/ChannelByteSource.kt` | 1832 | 1832 | 通道源 |
| `core/hex/ChunkedSearch.kt` | 2844 | 2844 | 分块搜索 |
| `core/hex/HexCodec.kt` | 10413 | 10413 | 编解码/检查器/复制/粘贴 |
| `core/hex/HexDocument.kt` | 5726 | 5726 | 文档/覆盖/撤销/保存/查找 |
| `feature/pdf/PageImages.kt` | 6458 | 6458 | 图片缓存/渲染/瓦片 |
| `feature/pdf/PdfReaderScreen.kt` | 44663 | 44663 | 阅读器屏/手势/选择/搜索/菜单 |
| `feature/pdf/PdfReaderViewModel.kt` | 13029 | 13029 | 阅读器 VM/文本/链接/搜索/保存 |
| `feature/pdf/ReaderActions.kt` | 4628 | 4628 | 分享/保存/打印 |
| `feature/pdf/ReaderSheets.kt` | 24385 | 24385 | 密码/跳页/信息/缩略图/搜索 sheet |
| `feature/pdf/ReaderViewport.kt` | 17536 | 17536 | 视口/手势/绘制/高亮 |

### A.2 工作逻辑
- **归档创建**：ArchiveCreator.create/createEach → zip/7z/tar；加密/分卷。
- **归档解压**：ArchiveManager.extract → zip/7z/rar/tar/single；密码/选择/冲突。
- **归档读取**：ArchiveReader.list → ArchiveEntry；ArchiveTree 建树。
- **zip 编辑**：ZipEditor.put/delete/rename/add。
- **7z 加密头**：SevenZHeaderCrypto.encryptHeader；SevenZKeys.key/derive。
- **RAR**：RarArchive.list/readable/failure；native rar。
- **DEX 转换**：DexConvert.smaliToDex/jarToDex；SmaliAssembler。
- **DEX 索引/搜索**：DexIndex.load/smali/resolve；DexSearch.run/classHits。
- **DEX 工具**：DexTools.dexToJar/disassemble/replaceClassSmali/replaceClassInApk。
- **DEX 树**：DexTree.children/crumbs。
- **Java 反编译**：JavaDecompiler.decompileToDir/decompile。
- **smali 文本**：SmaliText.readable/unescape/literals/commentStart。
- **PDF 解析**：PdfFile.open/pages/getObject/stream/decoded；PdfFilters/PdfLexer/PdfSecurity/PdfWriter。
- **PDF 合并**：PdfMerger.merge。
- **PDF 渲染/搜索/布局**：PdfDocument/PdfTextLayout/PdfSearch/Pdfium。
- **Hex**：ByteSource/ChannelByteSource/ChunkedSearch/HexCodec/HexDocument。
- **PDF UI**：PdfReaderScreen/ViewModel/ReaderActions/ReaderSheets/ReaderViewport/PageImages。

## B. fork 独有的逻辑
- 本模块源码与 1.0.0 一致；无新增文件或结构性改动。
- 差异仅在依赖（如 `Stgx` 影响编辑器，不影响本模块）。

## C. 1.0.0 独有、fork 已删或改掉
- 无。