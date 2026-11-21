# A0 native 与构建资源

> 元规则：改代码后更新本册字节数与条目；用 `stat -c '%s %n'` 核对。字节单位 byte。

## A. fork 与 1.0.0 相同的逻辑

### A.1 文件路由表
| 路径 | 1.0.0 | fork | 职责 |
|---|---|---|---|
| `native/src/crypt.rs` | 6000 | 6177 | Argon2/XChaCha20-Poly1305 |
| `native/src/hexio.rs` | 8200 | 8388 | 读/搜索/写 |
| `native/src/lib.rs` | 14300 | 14677 | JNI 总表 |
| `native/src/pdfium.rs` | 35500 | 36403 | pdfium 绑定 |
| `native/src/rar.rs` | 12200 | 12473 | RAR |
| `native/build.rs` | 498 | 498 | 构建脚本 |
| `native/Cargo.toml` | 682 | 682 | 依赖 |
| `app/build.gradle.kts` | 9300 | 10500 | 应用构建 |
| `gradle/libs.versions.toml` | 10200 | 10300 | 版本目录 |
| `settings.gradle.kts` | 734 | 734 | 模块 |
| `gradle.properties` | 304 | 396 | 属性 |
| `local.properties` | 53 | 53 | SDK |
| `res/xml/backup_rules.xml` | 132 | 132 | 备份 |
| `res/xml/data_extraction_rules.xml` | 276 | 276 | 数据提取 |
| `res/xml/file_paths.xml` | 242 | 242 | FileProvider |
| `res/xml/locales_config.xml` | 266 | 299 | 语言 |
| `res/xml/network_security_config.xml` | 253 | 253 | 网络安全 |
| `res/xml/plugin_share_paths.xml` | 110 | 110 | 插件分享 |
| `res/drawable/ic_launcher_foreground.xml` | 612 | 612 | 前景 |
| `res/drawable/ic_launcher_monochrome.xml` | 514 | 514 | 单色 |
| `res/drawable/ic_stat_share.xml` | 741 | 741 | 通知图标 |
| `res/mipmap-anydpi-v26/ic_launcher.xml` | 341 | 341 | 启动图标 |
| `res/mipmap-anydpi-v26/ic_launcher_round.xml` | 341 | 341 | 圆形 |
| `res/values/colors.xml` | 226 | 226 | 颜色 |
| `res/values/strings.xml` | 129 | 1199 | 字符串 |
| `res/values/strings_*.xml` | 见 A.1 | 见 A.1 | 字符串分文件 |
| `res/values/themes.xml` | 719 | 719 | 主题 |
| `res/values-night/colors.xml` | 123 | 123 | 夜间色 |
| `res/values-fa/*` | 见 A.1 | 见 A.1 | 波斯语 |
| `res/values-ru/*` | 见 A.1 | 见 A.1 | 俄语 |
| `res/values-zh-rCN/*` | 见 A.1 | 见 A.1 | 中文 |

### A.2 工作逻辑
- **native**：lib.rs 导出 JNI；crypt/hexio/pdfium/rar 提供实现；build.rs 在 Android 链接 c++_static/c++abi。
- **构建**：build.gradle.kts 配置变体/签名/ABI split/cargoHostBuild/依赖强制对齐；libs.versions.toml 版本目录；settings.gradle.kts 模块；gradle.properties 属性。
- **资源**：values 字符串/颜色/主题；values-* 本地化；xml 配置；drawable/mipmap 图标；raw 音效；drawable-nodpi 插画。

## B. fork 独有的逻辑

### B.1 新增文件路由表
| 路径 | fork | 职责 |
|---|---|---|
| `res/drawable/ic_python_logo.xml` | 2868 | Python 图标 |
| `res/values/colors_python.xml` | 77 | Python 色 |
| `res/values-night/colors_python.xml` | 77 | Python 夜间色 |
| `res/drawable-nodpi/art_unkown.webp` | 见快照 | 未知插画 |
| `res/drawable-nodpi/art_warning.webp` | 见快照 | 警告插画 |
| `res/values-fa/strings_export.xml` | 1445 | 波斯语导出 |
| `res/values-ru/strings_export.xml` | 1513 | 俄语导出 |
| `res/values-zh-rCN/strings_export.xml` | 1263 | 中文导出 |
| `res/values/strings_export.xml` | 1243 | 英文导出 |
| `res/values/strings_grammar.xml` | 3784 | 英文语法 |
| `res/values-zh-rCN/strings_grammar.xml` | 3760 | 中文语法 |
| `res/values-ru/strings_grammar.xml` | 4493 | 俄文语法 |
| `res/values-fa/strings_grammar.xml` | 4244 | 波斯文语法 |

### B.2 新增/改动逻辑
- **构建**：app/build.gradle.kts 新增 Dhizuku/Ackpine 依赖、`strata.*` 属性、`cargoHostBuild`、ABI split、资源排除、`legacy29` 变体 targetSdk 29。
- **libs.versions.toml**：新增 Dhizuku/Ackpine 版本。
- **gradle.properties**：新增 `strata.versionName/Code`。
- **资源**：新增 export 字符串、Python 图标/色、art_unkown/art_warning、STGX 相关字符串。
- **native**：与 1.0.0 一致（字节略有差异，功能相同）。

## C. 1.0.0 独有、fork 已删或改掉
- 1.0.0 无 `ic_python_logo.xml`、`colors_python.xml`、`art_unkown.webp`、`art_warning.webp`、`strings_export.xml`。
- 1.0.0 `gradle.properties` 无 `strata.*`。
- 1.0.0 `app/build.gradle.kts` 无 Dhizuku/Ackpine/cargoHostBuild/ABI split。