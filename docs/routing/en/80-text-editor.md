# 80 Text Editor

> Meta-rule: after changing code, update this volume's byte sizes and entries; verify with `stat -c '%s %n'`. Byte unit: byte.

## A. Logic identical in fork and 1.0.0

### A.1 File routing table
| Path | 1.0.0 | fork | Responsibility |
|---|---|---|---|
| `feature/editor/CodeEditorView.kt` | 1294 | 1294 | AndroidView wrapping sora CodeEditor |
| `feature/editor/EditorColors.kt` | 4410 | 4410 | EditorChrome/StrataColorScheme/autoThemeId |
| `feature/editor/EditorController.kt` | 9521 | 9521 | Editor controller: text/selection/search/replace/language/colors |
| `feature/editor/TextEditorScreen.kt` | 44876 | 45846 | Editor screen: top bar/find/symbol bar/paging/dialogs |
| `feature/editor/TextEditorViewModel.kt` | 20456 | 21515 | Editor VM: load/paging/encoding/save/external changes |
| `feature/editor/TextMateEngine.kt` | 3956 | 7928 | Grammar/theme loading; fork adds STGX dynamic grammar/preview |
| `core/text/EditTracker.kt` | 1403 | 1403 | Edit tracking/paging dirty flags |
| `core/text/GrammarCatalog.kt` | 4382 | 4612 | Grammar catalog/themes/include closure; fork adds extensionsOf |
| `core/text/TextEncoding.kt` | 5502 | 8989 | Encoding detection/BOM/line endings/conversion; fork adds windows-1252/GB detection |
| `core/text/TextPager.kt` | 5594 | 5594 | Paging index/read-write/assembly |

### A.2 Work logic
- **Load**: TextEditorViewModel.load → read sample → shouldPage → whole file or paged → decode/binary resource → state.
- **Paging**: TextPager.index/readPage/decode/assemble; goToPage/goToLine.
- **Editing**: EditorController.attach/setText/insert/selection/replace/search/undo/redo.
- **Highlighting**: TextMateEngine.init/language/activateTheme; GrammarCatalog.closure.
- **Save**: save/savePaged; encoding/line endings/BOM; external-change detection.
- **UI**: TextEditorScreen top bar/find bar/symbol bar/paging bar/dialogs; ProcessText.

## B. Logic unique to the fork

### B.1 Added-file routing table
| Path | fork | Responsibility |
|---|---|---|
| `core/text/StgxRegistry.kt` | 4982 | STGX registry: load/override/extension/icon/color |
| `core/text/StgxParser.kt` | 3932 | STGX parse/validate/compile |
| `core/text/StgxIcons.kt` | 3159 | STGX icons (vector/drawable/bitmap) |
| `core/text/StgxIconLoader.kt` | 1246 | base64 icon load/cache |
| `feature/grammar/GrammarScreen.kt` | 30791 | Grammar management page |

### B.2 Added/changed logic
- **TextMateEngine**: the fork adds dynamic grammar loading after `StgxRegistry.load`, `loadDynamicGrammar`, `previewColorsFor`, `themes` parsing.
- **GrammarCatalog**: the fork adds `extensionsOf`, `includedScopes` STGX support.
- **TextEncoding**: the fork adds `choices` with labels, `labelOf`, windows-1252 heuristic, strict GB decoding, `detectSample`.
- **TextEditorViewModel**: the fork adds `charsetLabel`, `resolveLanguage` STGX fallback, `setSaveCharset(choice)`.
- **TextEditorScreen**: the fork adds STGX grammar selection, color selection, ProcessText dialog, `textApps` param.
- **TextEditorScreen**: the fork dismisses the keyboard automatically when leaving the editor.
- **EditorController**: the fork adds `colorUnderCursor/replaceColor/insertText/replaceSelection/select/lineText`.
- **StgxRegistry**: `load/reload/entryForExtension/entryForFile/iconForExtension/tintOfExtension/hueOfExtension/colorsOfExtension`.

## C. Logic unique to 1.0.0, removed or changed in the fork
- 1.0.0 has no `Stgx*.kt`.
- 1.0.0 `TextMateEngine` has no STGX dynamic grammar/preview.
- 1.0.0 `GrammarCatalog` has no `extensionsOf`.
- 1.0.0 `TextEncoding` has no label/windows-1252/GB detection.
- 1.0.0 `TextEditorViewModel` has no charsetLabel/STGX fallback.
- 1.0.0 `TextEditorScreen` has no STGX grammar/color/ProcessText.
- 1.0.0 `EditorController` has no color/selection helpers.