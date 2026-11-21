# 80 文本编辑器

> 元规则：改代码后更新本册字节数与条目；用 `stat -c '%s %n'` 核对。字节单位 byte。

## A. fork 与 1.0.0 相同的逻辑

### A.1 文件路由表
| 路径 | 1.0.0 | fork | 职责 |
|---|---|---|---|
| `feature/editor/CodeEditorView.kt` | 1294 | 1294 | AndroidView 包裹 sora CodeEditor |
| `feature/editor/EditorColors.kt` | 4410 | 4410 | EditorChrome/StrataColorScheme/autoThemeId |
| `feature/editor/EditorController.kt` | 9521 | 9521 | 编辑器控制器：文本/选择/搜索/替换/语言/配色 |
| `feature/editor/TextEditorScreen.kt` | 44876 | 45846 | 编辑屏：顶栏/查找/符号栏/分页/对话框 |
| `feature/editor/TextEditorViewModel.kt` | 20456 | 21515 | 编辑 VM：加载/分页/编码/保存/外部变更 |
| `feature/editor/TextMateEngine.kt` | 3956 | 7928 | 语法/主题加载；fork 增 STGX 动态语法/预览 |
| `core/text/EditTracker.kt` | 1403 | 1403 | 编辑跟踪/分页脏标记 |
| `core/text/GrammarCatalog.kt` | 4382 | 4612 | 语法目录/主题/包含闭包；fork 增 extensionsOf |
| `core/text/TextEncoding.kt` | 5502 | 8989 | 编码探测/BOM/行尾/转换；fork 增 windows-1252/GB 探测 |
| `core/text/TextPager.kt` | 5594 | 5594 | 分页索引/读写/拼接 |

### A.2 工作逻辑
- **加载**：TextEditorViewModel.load → 读取样本 → shouldPage → 整文件或分页 → 解码/二进制资源 → 状态。
- **分页**：TextPager.index/readPage/decode/assemble；goToPage/goToLine。
- **编辑**：EditorController.attach/setText/insert/selection/replace/search/undo/redo。
- **高亮**：TextMateEngine.init/language/activateTheme；GrammarCatalog.closure。
- **保存**：save/savePaged；编码/行尾/BOM；外部变更检测。
- **UI**：TextEditorScreen 顶栏/查找栏/符号栏/分页栏/对话框；ProcessText。

## B. fork 独有的逻辑

### B.1 新增文件路由表
| 路径 | fork | 职责 |
|---|---|---|
| `core/text/StgxRegistry.kt` | 4982 | STGX 注册表：加载/覆盖/扩展名/图标/颜色 |
| `core/text/StgxParser.kt` | 3932 | STGX 解析/校验/编译 |
| `core/text/StgxIcons.kt` | 3159 | STGX 图标（vector/drawable/bitmap） |
| `core/text/StgxIconLoader.kt` | 1246 | base64 图标加载/缓存 |
| `feature/grammar/GrammarScreen.kt` | 30791 | 语法管理页 |

### B.2 新增/改动逻辑
- **TextMateEngine**：fork 增 `StgxRegistry.load` 后的动态语法加载、`loadDynamicGrammar`、`previewColorsFor`、`themes` 解析。
- **GrammarCatalog**：fork 增 `extensionsOf`、`includedScopes` 支持 STGX。
- **TextEncoding**：fork 增 `choices` 带 label、`labelOf`、windows-1252 启发式、GB 严格解码、`detectSample`。
- **TextEditorViewModel**：fork 增 `charsetLabel`、`resolveLanguage` 走 STGX 兜底、`setSaveCharset(choice)`。
- **TextEditorScreen**：fork 增 STGX 语法选择、颜色选择、ProcessText 对话框、`textApps` 参数。
- **TextEditorScreen**：fork 增退出编辑器时自动收起输入法。
- **EditorController**：fork 增 `colorUnderCursor/replaceColor/insertText/replaceSelection/select/lineText`。
- **StgxRegistry**：`load/reload/entryForExtension/entryForFile/iconForExtension/tintOfExtension/hueOfExtension/colorsOfExtension`。
- **语法管理页**：内置文件语法列表新增 STGX viewer（`app.intent.stgx`）；语法字符串独立到 `strings_grammar.xml`。

## C. 1.0.0 独有、fork 已删或改掉
- 1.0.0 无 `Stgx*.kt`。
- 1.0.0 `TextMateEngine` 无 STGX 动态语法/预览。
- 1.0.0 `GrammarCatalog` 无 `extensionsOf`。
- 1.0.0 `TextEncoding` 无 label/windows-1252/GB 探测。
- 1.0.0 `TextEditorViewModel` 无 charsetLabel/STGX 兜底。
- 1.0.0 `TextEditorScreen` 无 STGX 语法/颜色/ProcessText。
- 1.0.0 `EditorController` 无颜色/选择辅助。