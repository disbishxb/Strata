# 60 Archive, DEX & PDF

> Meta-rule: after changing code, update this volume's byte sizes and entries; verify with `stat -c '%s %n'`. Byte unit: byte.

## A. Logic identical in fork and 1.0.0

### A.1 File routing table
| Path | 1.0.0 | fork | Responsibility |
|---|---|---|---|
| `core/archive/ArchiveCreator.kt` | 14556 | 16486 | Create zip/7z/tar; encryption/level/split |
| `core/archive/ArchiveManager.kt` | 16367 | 17409 | Extract zip/7z/rar/tar/single file; ArchiveSelection/PasswordRequired/WrongPassword |
| `core/archive/ArchiveReader.kt` | 7822 | 9978 | List/read; ArchiveEntry/ArchiveTree; split volumes |
| `core/archive/RarArchive.kt` | 4723 | 4723 | RAR header/volume/failure mapping |
| `core/archive/SevenZHeaderCrypto.kt` | 5261 | 5261 | 7z header encryption |
| `core/archive/SevenZKeys.kt` | 2566 | 2566 | 7z key derivation/cache |
| `core/archive/ZipEditor.kt` | 4060 | 4060 | In-place zip edit/delete/rename/add |
| `core/dex/DexConvert.kt` | 10197 | 10197 | smali→dex; jar→dex (D8) |
| `core/dex/DexIndex.kt` | 4437 | 4437 | Load/class/method/field parsing/smali disassembly |
| `core/dex/DexSearch.kt` | 11689 | 11689 | Search modes/matching/concurrency/results |
| `core/dex/DexTools.kt` | 8610 | 8610 | dex→jar; disassemble; replace class; entry |
| `core/dex/DexTree.kt` | 2852 | 2852 | Package/class tree |
| `core/dex/JavaDecompiler.kt` | 4084 | 4084 | jadx decompile/members |
| `core/dex/SmaliAssembler.kt` | 5439 | 5439 | smali assembly/API probing/errors |
| `core/dex/SmaliOutline.kt` | 3819 | 3819 | Member outline |
| `core/dex/SmaliRefs.kt` | 5407 | 5407 | Reference spans/types/methods/fields |
| `core/dex/SmaliText.kt` | 5276 | 5276 | Readability/escape/literals/comments |
| `core/pdf/PdfFile.kt` | 21487 | 21487 | PDF parsing/xref/repair/pages/streams/encryption |
| `core/pdf/PdfFilters.kt` | 6593 | 6593 | Flate/AHx/A85/predict |
| `core/pdf/PdfLexer.kt` | 10384 | 10384 | Lexer/Token/objects |
| `core/pdf/PdfMerger.kt` | 10597 | 10597 | Merge PDF/bookmarks |
| `core/pdf/PdfObjects.kt` | 4518 | 4518 | Objects/sources |
| `core/pdf/PdfSecurity.kt` | 10758 | 10758 | Standard encryption/RC4/AES |
| `core/pdf/PdfWriter.kt` | 4725 | 4725 | Write objects/streams/xref |
| `core/pdf/view/DocLayout.kt` | 5109 | 5109 | Page layout/zoom/visibility |
| `core/pdf/view/PdfDocument.kt` | 8540 | 8540 | Open/render/chars/links/outline/metadata |
| `core/pdf/view/PdfSearch.kt` | 7201 | 7201 | Collapse/find/fragments |
| `core/pdf/view/PdfTextLayout.kt` | 21773 | 21773 | Text layout/lines/cells/reflow |
| `core/pdf/view/Pdfium.kt` | 1674 | 1674 | JNI table |
| `core/pdf/view/ReaderMemory.kt` | 3051 | 3051 | Reading position/bookmark codec |
| `core/hex/ByteSource.kt` | 3683 | 3683 | Source interface/file/native |
| `core/hex/ChannelByteSource.kt` | 1832 | 1832 | Channel source |
| `core/hex/ChunkedSearch.kt` | 2844 | 2844 | Chunked search |
| `core/hex/HexCodec.kt` | 10413 | 10413 | Codec/inspector/copy/paste |
| `core/hex/HexDocument.kt` | 5726 | 5726 | Document/overwrite/undo/save/find |
| `feature/pdf/PageImages.kt` | 6458 | 6458 | Image cache/rendering/tiles |
| `feature/pdf/PdfReaderScreen.kt` | 44663 | 44663 | Reader screen/gestures/selection/search/menu |
| `feature/pdf/PdfReaderViewModel.kt` | 13029 | 13029 | Reader VM/text/links/search/save |
| `feature/pdf/ReaderActions.kt` | 4628 | 4628 | Share/save/print |
| `feature/pdf/ReaderSheets.kt` | 24385 | 24385 | Password/goto/info/thumbnails/search sheets |
| `feature/pdf/ReaderViewport.kt` | 17536 | 17536 | Viewport/gestures/drawing/highlight |

### A.2 Work logic
- **Archive creation**: ArchiveCreator.create/createEach → zip/7z/tar; encryption/split.
- **Archive extraction**: ArchiveManager.extract → zip/7z/rar/tar/single; password/selection/conflict.
- **Archive reading**: ArchiveReader.list → ArchiveEntry; ArchiveTree builds the tree.
- **zip editing**: ZipEditor.put/delete/rename/add.
- **7z encrypted header**: SevenZHeaderCrypto.encryptHeader; SevenZKeys.key/derive.
- **RAR**: RarArchive.list/readable/failure; native rar.
- **DEX conversion**: DexConvert.smaliToDex/jarToDex; SmaliAssembler.
- **DEX index/search**: DexIndex.load/smali/resolve; DexSearch.run/classHits.
- **DEX tools**: DexTools.dexToJar/disassemble/replaceClassSmali/replaceClassInApk.
- **DEX tree**: DexTree.children/crumbs.
- **Java decompilation**: JavaDecompiler.decompileToDir/decompile.
- **smali text**: SmaliText.readable/unescape/literals/commentStart.
- **PDF parsing**: PdfFile.open/pages/getObject/stream/decoded; PdfFilters/PdfLexer/PdfSecurity/PdfWriter.
- **PDF merging**: PdfMerger.merge.
- **PDF rendering/search/layout**: PdfDocument/PdfTextLayout/PdfSearch/Pdfium.
- **Hex**: ByteSource/ChannelByteSource/ChunkedSearch/HexCodec/HexDocument.
- **PDF UI**: PdfReaderScreen/ViewModel/ReaderActions/ReaderSheets/ReaderViewport/PageImages.

## B. Logic unique to the fork
- This module's source is identical to 1.0.0; no added files or structural changes.
- The only differences are dependencies (e.g. `Stgx` affects the editor, not this module).

## C. Logic unique to 1.0.0, removed or changed in the fork
- None.