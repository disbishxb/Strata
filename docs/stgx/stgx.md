# Writing a `.stgx` file

`.stgx` files teach Strata a new file type: which extensions belong to it, how its text is highlighted in the editor, and which icon the file list shows.

A `.stgx` file is plain JSON. You do not need to build anything or read the source: write the file, import it, done.

---

## 1. Where the file goes

You can add a `.stgx` file in two ways:

- **Import from the app.** Open a `.stgx` file; Strata offers to install it. It is copied into the app's private `stgx_extensions` folder.
- **Drop it in by hand.** Put the file in `stgx_extensions` inside Strata's private data folder (root or a file manager with access to `/data/data/dev.cluvex.strata/files/`).

On the next start (or when you tap Import / Refresh in Settings → Syntax), Strata reads every `.stgx` it finds, in both places.

If a file fails to load, it appears under **Failed to load** in Settings → Syntax. Fix the JSON and import it again.

Installing a `.stgx` with an `id` that already exists replaces the existing one. A built-in grammar can be overridden by a user version; deleting the user version restores the built-in. Built-in grammars cannot be uninstalled from inside the app.

A `.stgx` can set `"highlight": false` to provide only an icon and extension recognition, without registering an editor grammar (it will not appear in the editor's Syntax menu). It still appears in Settings → Syntax, where you can delete it.

---

## 2. The smallest working file

```json
{
  "id": "mylang",
  "name": "My Language",
  "extensions": ["mylang", "ml"],
  "rules": [
    { "scope": "keyword.control", "match": "\\b(if|else|while|return)\\b" }
  ]
}
```

That is enough for Strata to:

· treat `.mylang` and `.ml` files as My Language,
· highlight `if`, `else`, `while`, `return` as keywords,
· offer My Language in the editor's Syntax menu.

`rules` needs at least one entry. If a type only needs an icon and no highlighting, set `"highlight": false` and keep a placeholder rule, or write a rule that never matches (e.g. `{ "scope": "comment.line", "match": "\\u0000" }`).

---

## 3. Fields

### Top level

| Field | Required | Type | Meaning |
| :--- | :--- | :--- | :--- |
| `id` | yes | string | Unique identifier. Lowercase letters, digits, `_` and `-`. Used to tell grammars apart and to build the scope `source.stgx.ext.<id>`. |
| `name` | yes | string | What the user sees in the Syntax menu. |
| `extensions` | yes | string[] | File extensions without the dot: `["mylang", "ml"]`. |
| `icon` | no | string | An icon name (see §5) or a base64 image (see §6). |
| `iconTint` | no | string | Make the dynamic background follow a built-in file type. Values in §5. `iconBackground` takes precedence when present. |
| `hue` | no | integer | 0–360. Generate the dynamic background from this hue; follows theme darkness and the "Colorful icons" setting. Used when neither `iconBackground` nor `iconTint` is set. |
| `highlight` | no | boolean | Default `true`. Set to `false` to provide only an icon and extension recognition, without registering an editor grammar (it will not appear in the Syntax menu). |
| `iconBackground` | no | string | Custom light-mode icon background color (Hex, e.g. `#FDE293`). When set, `iconTint` and `hue` are ignored. |
| `iconBackgroundDark` | no | string | Custom dark-mode icon background color (Hex, e.g. `#564225`). |
| `iconForeground` | no | string | Custom icon foreground color (Hex, e.g. `#4D3F20`). |
| `comments` | no | object | Line and block comment markers, for the editor's comment shortcut. See §4. |
| `rules` | yes | array | The highlighting rules. At least one. See §4. |

Unknown fields are rejected on purpose: a typo like `"extention"` fails loudly instead of being silently ignored. A `hue` outside 0–360, or an `iconTint` naming an unknown file type, also fails at load time; the file then appears under Failed to load.

### comments (optional)

```json
"comments": { "line": "//", "block": ["/*", "*/"] }
```

| Field | Type | Meaning |
| :--- | :--- | :--- |
| `line` | string | Line comment marker, e.g. `//` or `#`. |
| `block` | `[string, string]` | Opening and closing markers, e.g. `["/*", "*/"]`. |

Both are optional. They only affect the editor's comment toggle; they do not highlight anything (do that with a rule).

---

## 4. Rules

Every rule is one of two shapes:

### A. Single-line rule (match)

```json
{ "scope": "constant.numeric", "match": "\\b-?\\d+(\\.\\d+)?\\b" }
```

· `match` is a regular expression (Java/Oniguruma syntax) that matches text on one line.
· The matched text gets the given `scope`.

### B. Block rule (begin + end)

```json
{
  "scope": "string.quoted",
  "begin": "\"",
  "end": "\"",
  "patterns": [
    { "scope": "constant.character.escape", "match": "\\\\." }
  ]
}
```

· `begin` matches the opening marker, `end` matches the closing marker.
· Everything between them (including multi-line content) gets the rule's `scope`.
· `patterns` is optional and adds rules inside the block (for example, escapes inside a string).
· `patterns` is only allowed on block rules. On a `match` rule it is an error.

A rule must have exactly one of `match` or `begin+end`. Having both, or only one of `begin`/`end`, is an error. `rules` needs at least one entry.

### scope

`scope` is a TextMate scope name. The editor colours text by matching the scope against the active theme. The names below are the ones most themes recognise; using them gives the best result across Strata's built-in themes.

| Scope | Typical use |
| :--- | :--- |
| `keyword.control` | `if`, `else`, `for`, `return` |
| `keyword.operator` | `+`, `-`, `==`, `&&` |
| `storage.type` | `int`, `var`, `const`, `class` |
| `variable.other` | variable names |
| `variable.parameter` | function arguments |
| `entity.name.function` | function names |
| `entity.name.type` | type / class names |
| `constant.numeric` | numbers |
| `constant.language` | `true`, `false`, `null`, `nil` |
| `string.quoted` | quoted strings |
| `constant.character.escape` | `\n`, `\"`, `\\` |
| `comment.line` | line comments |
| `comment.block` | block comments |
| `support.function` | built-in functions |
| `punctuation.separator` | `,`, `;` |

You can invent your own scope (`"scope": "keyword.mykeyword"`); it just may not be coloured by every theme.

---

## 5. Built-in icons and dynamic backgrounds

If `icon` is one of these names, Strata uses its built-in icon:

| Name | System Type / Icon | Default dynamic hue |
| :--- | :--- | :--- |
| `code` | Generic Code (`<>`) | Purple (265°) |
| `terminal`, `shell` | Terminal (`>_`) | Dark Blue (220°) |
| `data_object`, `json` | Curly Braces (`{}`) | Purple (265°) |
| `description`, `markdown`, `text` | Document (`📄`) | Cyan (200°) |
| `folder_zip`, `archive`, `zip` | Archive (`📁🔗`) | Yellow (85°) |
| `image` | Image (`🖼️`) | Pink / Red (340°) |
| `video` | Video (`🎬`) | Orange (27°) |
| `audio` | Audio (`🎵`) | Brown (60°) |
| `pdf` | PDF (`📕`) | Red (12°) |
| `font` | Font (`A`) | Purple (300°) |
| `apk` | APK (`🤖`) | Green (140°) |
| `python` | Python logo (`🐍`) | Theme color, not a kind hue |

If `icon` is missing or unknown, Strata shows the generic code icon (`code`).

`python` is a special case: its icon is a picture resource, its foreground is the theme's `onPrimaryContainer`, and its background stays the theme color. `iconBackground` / `iconBackgroundDark` / `iconForeground` / `iconTint` / `hue` have no effect on it.

### Dynamic backgrounds

Icon backgrounds can be generated dynamically, following the theme's light/dark mode and the "Colorful icons" setting. Priority, highest first:

1. `iconBackground` (light) or `iconBackgroundDark` (dark) is set: use your static Hex.
2. `iconTint` is set: follow that built-in file type's hue. Values:
   `folder`, `image`, `video`, `audio`, `archive`, `apk`, `code`, `shell`, `python`, `text`, `json`, `pdf`, `font`, `document`, `encrypted`, `binary`.
3. `hue` is set: generate from that hue, 0–360.
4. None of the above: follow the file type of `icon`; if `icon` is missing or unknown, use `code`.

`iconForeground` is used when set; otherwise a foreground matching the dynamic background is generated.

Examples:

```json
{
  "id": "mylang",
  "name": "My Language",
  "extensions": ["mylang"],
  "icon": "code",
  "iconTint": "image"
}
```

This grammar's icon background follows the image kind (pink/red, 340°). Changing it to:

```json
"hue": 120
```

makes it green and dynamic. Adding `"iconBackground": "#FFEB3B"` pins it to yellow and ignores `iconTint` / `hue`.

The long-press menu and the "Open with" menu use the menu's own background color for the icon, and ignore `iconTint` / `hue`.

---

## 6. Base64 icons

You can ship your own picture instead of a built-in icon. Put it in `icon` as a base64 data URI:

```json
"icon": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAADKSURBVFhH7ZTRDYAgDEQ7gI7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7g=="
```

Or, as a shorthand, just the base64 payload (longer than 64 characters):

```json
"icon": "iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0..."
```

Rules:

· Supported formats: PNG, WebP, JPEG. SVG is not supported yet.
· After decoding, the image must be 1 MB or smaller; larger pictures are rejected.
· There is no automatic light/dark inversion. Provide a picture that works on both, or make two `.stgx` files if you really need two icons.
· If the picture fails to decode, Strata logs a warning and shows the built-in code icon. The grammar still works; only the icon is missing.

Base64 icons also support dynamic backgrounds: `iconTint` / `hue` / `iconBackground` apply to the container around the picture; the picture itself is not tinted.

Tip: to keep the file readable, generate the data URI from your picture with any online "image to base64" tool, then paste it as one long line.

---

## 7. A complete example

A small language with keywords, numbers, two comment styles, strings with escapes, and a dynamic background:

```json
{
  "id": "tiny",
  "name": "Tiny",
  "extensions": ["tiny"],
  "icon": "code",
  "iconTint": "code",
  "comments": {
    "line": "//",
    "block": ["/*", "*/"]
  },
  "rules": [
    { "scope": "comment.line",   "match": "//.*" },
    { "scope": "comment.block",  "begin": "/\\*", "end": "\\*/" },
    { "scope": "string.quoted",  "begin": "\"", "end": "\"",
      "patterns": [
        { "scope": "constant.character.escape", "match": "\\\\." }
      ]
    },
    { "scope": "constant.numeric", "match": "\\b\\d+\\b" },
    { "scope": "constant.language", "match": "\\b(true|false|null)\\b" },
    { "scope": "keyword.control", "match": "\\b(if|else|while|for|return)\\b" },
    { "scope": "entity.name.function", "match": "\\b[a-zA-Z_]\\w*(?=\\s*\\()" }
  ]
}
```

Note the order: rules are tried top to bottom, so put comments and strings before keywords. Otherwise a `//` inside a string, or the word `if` inside a comment, would be coloured incorrectly.

---

## 8. Troubleshooting

| Symptom | Likely cause |
| :--- | :--- |
| File appears under Failed to load | Invalid JSON, a missing required field, a duplicate `id`, a `hue` outside 0–360, or an `iconTint` naming an unknown type. |
| Icon is always the generic code icon | `icon` is misspelled, or the base64 data does not decode to PNG/WebP/JPEG, or the picture is larger than 1 MB. |
| Extensions do not appear in the file list | Another grammar already claims the same extension. The first one loaded wins; rename or remove the other. |
| Highlighting is wrong inside strings or comments | Rule order. Move the string/comment rules above the keyword rules. |
| Highlighting looks different in another theme | Some themes do not define every scope. Stick to the scopes listed in §4 for the most consistent result. |
| `.stgx` does not appear in the editor's Syntax menu | The file sets `"highlight": false`, so it only provides an icon and extension recognition. This is expected. |
| Background color is not the Hex I wrote | Both `iconBackground` and `iconTint` / `hue` are set, and `iconBackground` wins; or `iconBackground` is missing and `iconTint` / `hue` is in effect. |
| The long-press menu and "Open with" backgrounds differ from the file list | The menus use their own background color and ignore `iconTint` / `hue`. This is expected. |

To see errors, open Settings → Syntax. The failed files and their names are listed there; the reason is in the system log under the `StgxRegistry` tag.