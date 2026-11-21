# 编写 .stgx 文件

`.stgx` 文件可以教会 Strata 认识一种新的文件类型：它的扩展名是什么、编辑器里怎么高亮、文件列表里显示什么图标。

`.stgx` 就是一个普通 JSON 文件。你不需要编译、也不需要读源码：写好、导入，就可以了。

---

## 1. 文件放在哪里

有两种方式添加 `.stgx` 文件：

- **从应用内导入。** 打开一个 `.stgx` 文件，Strata 会询问是否安装，安装后会复制到应用私有的 `stgx_extensions` 目录。
- **手动放入。** 把文件放进 Strata 私有数据目录下的 `stgx_extensions`（需要 root 或有访问 `/data/data/dev.cluvex.strata/files/` 的文件管理器）。

下次启动（或在“设置 → 语法”中点击导入 / 刷新）时，Strata 会读取这两个位置的所有 `.stgx` 文件。

如果某个文件加载失败，它会出现在“设置 → 语法”的加载失败列表中。修正 JSON 后重新导入即可。

同 `id` 的 `.stgx` 再次安装会替换已有版本；内置语法被用户版本覆盖后，删除用户版本即恢复内置。内置语法不能在应用内卸载，只能被用户版本覆盖。

`.stgx` 里可以写 `"highlight": false`，表示这个文件只提供图标和扩展名识别，不注册编辑器语法（不会出现在编辑器的“语法”菜单里）。设置 → 语法里仍然能看到它、删除它。

---

## 2. 最小的可用文件

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

这个文件已经足够让 Strata：

· 把 `.mylang` 和 `.ml` 文件识别为 My Language；
· 把 `if`、`else`、`while`、`return` 高亮为关键字；
· 在编辑器的“语法”菜单里提供 My Language。

`rules` 至少写一条。如果这个类型只需要图标、不需要高亮，写 `"highlight": false` 并保留一条占位规则，或者直接写一条不会匹配的规则（例如 `{ "scope": "comment.line", "match": "\\u0000" }`）。

---

## 3. 字段说明

### 顶层字段

| 字段 | 必填 | 类型 | 含义 |
| :--- | :--- | :--- | :--- |
| `id` | 是 | 字符串 | 唯一标识。只能用小写字母、数字、`_` 和 `-`。用于区分不同语法，也用于生成 scope `source.stgx.ext.<id>`。 |
| `name` | 是 | 字符串 | 在“语法”菜单里显示的名字。 |
| `extensions` | 是 | 字符串数组 | 文件扩展名，不带点：`["mylang", "ml"]`。 |
| `icon` | 否 | 字符串 | 内置图标名（见 §5）或 base64 图片（见 §6）。 |
| `iconTint` | 否 | 字符串 | 动态背景色跟随某个内置文件类型。取值见 §5。写了 `iconBackground` 时以 `iconBackground` 为准。 |
| `hue` | 否 | 整数 | 0–360。动态背景用这个色相生成，随主题深浅和“彩色图标”设置变化。`iconBackground` 和 `iconTint` 都没写时生效。 |
| `highlight` | 否 | 布尔 | 默认 `true`。设为 `false` 时该 `.stgx` 只提供图标和扩展名识别，不注册编辑器语法（不出现在“语法”菜单）。 |
| `iconBackground` | 否 | 字符串 | 自定义浅色模式图标背景色（Hex 格式，如 `#FDE293`）。写了它时，`iconTint` 和 `hue` 不生效。 |
| `iconBackgroundDark` | 否 | 字符串 | 自定义深色模式图标背景色（Hex 格式，如 `#564225`）。 |
| `iconForeground` | 否 | 字符串 | 自定义图标前景/文字颜色（Hex 格式，如 `#4D3F20`）。 |
| `comments` | 否 | 对象 | 行注释和块注释标记，用于编辑器的注释快捷键。见 §4。 |
| `rules` | 是 | 数组 | 高亮规则，至少一条。见 §4。 |

未知字段会被拒绝：像 `"extention"` 这种拼写错误会直接报错，而不是被悄悄忽略。`hue` 超出 0–360、`iconTint` 写了不认识的类型名，也会在加载时报错，文件会出现在“加载失败”列表里。

### comments（可选）

```json
"comments": { "line": "//", "block": ["/*", "*/"] }
```

| 字段 | 类型 | 含义 |
| :--- | :--- | :--- |
| `line` | 字符串 | 行注释标记，例如 `//` 或 `#`。 |
| `block` | `[字符串, 字符串]` | 块注释的开始和结束标记，例如 `["/*", "*/"]`。 |

两项都可省略。它们只影响编辑器的“切换注释”功能，不会自动高亮任何内容（高亮需要写规则）。

---

## 4. 规则

每条规则是下面两种形式之一：

### A. 单行规则（match）

```json
{ "scope": "constant.numeric", "match": "\\b-?\\d+(\\.\\d+)?\\b" }
```

· `match` 是一个正则表达式（Java / Oniguruma 语法），匹配同一行内的文本。
· 匹配到的文本会应用指定的 `scope`。

### B. 块规则（begin + end）

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

· `begin` 匹配开始标记，`end` 匹配结束标记。
· 两者之间的全部内容（包括跨行内容）都会应用规则的 `scope`。
· `patterns` 可选，用来定义块内部的附加规则（例如字符串里的转义字符）。
· `patterns` 只能出现在块规则里；出现在 `match` 规则里会报错。

每条规则必须恰好包含一种写法：要么 `match`，要么 `begin + end`。同时写、或只写 `begin`/`end` 之一，都会报错。`rules` 至少写一条。

### scope

`scope` 是 TextMate 的作用域名。编辑器会根据当前主题给不同作用域上色。下面这些名字大多数主题都认识，用它们能获得最稳定的效果。

| Scope | 常见用途 |
| :--- | :--- |
| `keyword.control` | `if`、`else`、`for`、`return` |
| `keyword.operator` | `+`、`-`、`==`、`&&` |
| `storage.type` | `int`、`var`、`const`、`class` |
| `variable.other` | 变量名 |
| `variable.parameter` | 函数参数 |
| `entity.name.function` | 函数名 |
| `entity.name.type` | 类型 / 类名 |
| `constant.numeric` | 数字 |
| `constant.language` | `true`、`false`、`null`、`nil` |
| `string.quoted` | 引号字符串 |
| `constant.character.escape` | `\n`、`\"`、`\\` |
| `comment.line` | 行注释 |
| `comment.block` | 块注释 |
| `support.function` | 内置函数 |
| `punctuation.separator` | `,`、`;` |

你也可以自创作用域（`"scope": "keyword.mykeyword"`），只是并非所有主题都会给它上色。

---

## 5. 内置图标与动态背景

如果 `icon` 是下面之一，Strata 会使用对应的内置图标：

| 名称 | 对应系统类型/图标 | 默认动态背景色相 |
| :--- | :--- | :--- |
| `code` | 通用代码 (`<>`) | 紫色（265°） |
| `terminal`、`shell` | 终端 (`>_`) | 深蓝色（220°） |
| `data_object`、`json` | 花括号 (`{}`) | 紫色（265°） |
| `description`、`markdown`、`text` | 文档 (`📄`) | 青色（200°） |
| `folder_zip`、`archive`、`zip` | 压缩包 (`📁🔗`) | 黄色（85°） |
| `image` | 图片 (`🖼️`) | 粉/红色（340°） |
| `video` | 视频 (`🎬`) | 橙色（27°） |
| `audio` | 音频 (`🎵`) | 黄/棕色（60°） |
| `pdf` | PDF (`📕`) | 红色（12°） |
| `font` | 字体 (`A`) | 紫色（300°） |
| `apk` | 安卓安装包 (`🤖`) | 绿色（140°） |
| `python` | Python 官方标志 (`🐍`) | 主题色，不随类型变化 |

如果 `icon` 缺失或无法识别，Strata 显示通用代码图标（`code`）。

`python` 是特殊情况：它的图标用图片资源，前景固定用主题的 `onPrimaryContainer`，背景保持主题色，`iconBackground` / `iconBackgroundDark` / `iconForeground` / `iconTint` / `hue` 对它都不生效。

### 动态背景

图标的背景色可以动态生成，随主题深浅和设置里的“彩色图标”变化。优先级从高到低：

1. 写了 `iconBackground`（浅色）或 `iconBackgroundDark`（深色）：用你的静态 Hex。
2. 写了 `iconTint`：跟随那个内置文件类型的色相。取值：
   `folder`、`image`、`video`、`audio`、`archive`、`apk`、`code`、`shell`、`python`、`text`、`json`、`pdf`、`font`、`document`、`encrypted`、`binary`。
3. 写了 `hue`：用这个色相生成，0–360。
4. 都没写：跟随 `icon` 对应的文件类型；`icon` 也没写或无法识别时用 `code`。

`iconForeground` 写了就用；没写时由动态背景自动生成与背景匹配的前景色。

示例：

```json
{
  "id": "mylang",
  "name": "My Language",
  "extensions": ["mylang"],
  "icon": "code",
  "iconTint": "image"
}
```

这个语法的图标背景会跟图片类型（340° 粉/红）动态变化。改成：

```json
"hue": 120
```

则跟绿色动态变化。再写 `"iconBackground": "#FFEB3B"`，则固定用黄色，`iconTint` / `hue` 被忽略。

长按菜单和“打开方式”菜单里的图标背景固定用菜单底色，不受 `iconTint` / `hue` 影响。

---

## 6. base64 图标

除了内置图标，你也可以用自己的图片。把图片以 base64 data URI 的形式写进 `icon`：

```json
"icon": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAADKSURBVFhH7ZTRDYAgDEQ7gI7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7gAA7g=="
```

或者更简短，只写 base64 主体（长度需大于 64 字符）：

```json
"icon": "iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0..."
```

规则：

· 支持格式：PNG、WebP、JPEG。暂不支持 SVG。
· 解码后图片必须 ≤ 1 MB，更大的图片会被拒绝。
· 不做明暗自动反转。请提供在浅色和深色主题下都能看的图片；确实需要两张图时，可以做两个 `.stgx` 文件。
· 图片解码失败时，Strata 会记一条警告日志并显示内置代码图标。语法本身仍然有效，只是图标缺失。

base64 图标同样支持动态背景：`iconTint` / `hue` / `iconBackground` 对 base64 图片的容器生效，图片本身不染色。

小技巧：为了保持文件可读，可以用任意“图片转 base64”的在线工具生成 data URI，再作为一整行粘贴进来。

---

## 7. 完整示例

一个小语言，带关键字、数字、两种注释、带转义的字符串，以及动态背景：

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

注意顺序：规则从上到下匹配，所以注释和字符串要放在关键字前面。否则字符串里的 `//`、或注释里的 `if`，会被错误地着色。

---

## 8. 疑难排查

| 现象 | 可能原因 |
| :--- | :--- |
| 文件出现在“加载失败”列表 | JSON 无效、缺少必填字段、`id` 重复，或 `hue` 超出 0–360、`iconTint` 写了不认识的类型名。 |
| 图标始终是通用代码图标 | `icon` 拼写错误，或 base64 数据解码后不是 PNG/WebP/JPEG，或图片超过 1 MB。 |
| 文件列表中扩展名不出现 | 已有另一个语法占用了同一扩展名。先加载的生效；请重命名或删除另一个。 |
| 字符串或注释里的高亮不对 | 规则顺序问题。把字符串 / 注释规则移到关键字规则上方。 |
| 换个主题后高亮看起来不一样 | 有些主题没有定义全部作用域。尽量使用 §4 中列出的作用域，结果最稳定。 |
| `.stgx` 不出现在编辑器“语法”菜单 | 该文件写了 `"highlight": false`，只提供图标和扩展名识别。这是预期行为。 |
| 背景色不是我写的 Hex | 同时写了 `iconBackground` 和 `iconTint` / `hue`，以 `iconBackground` 为准；或者没写 `iconBackground`，`iconTint` / `hue` 在生效。 |
| 长按菜单和“打开方式”背景和列表不一样 | 菜单固定用菜单底色，不受 `iconTint` / `hue` 影响。这是预期行为。 |

要查看错误，打开“设置 → 语法”，加载失败的文件会列在那里；具体原因在系统日志的 `StgxRegistry` 标签下。