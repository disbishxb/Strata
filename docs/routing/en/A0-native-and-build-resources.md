# A0 native & Build Resources

> Meta-rule: after changing code, update this volume's byte sizes and entries; verify with `stat -c '%s %n'`. Byte unit: byte.

## A. Logic identical in fork and 1.0.0

### A.1 File routing table
| Path | 1.0.0 | fork | Responsibility |
|---|---|---|---|
| `native/src/crypt.rs` | 6000 | 6177 | Argon2/XChaCha20-Poly1305 |
| `native/src/hexio.rs` | 8200 | 8388 | Read/search/write |
| `native/src/lib.rs` | 14300 | 14677 | JNI table |
| `native/src/pdfium.rs` | 35500 | 36403 | pdfium bindings |
| `native/src/rar.rs` | 12200 | 12473 | RAR |
| `native/build.rs` | 498 | 498 | Build script |
| `native/Cargo.toml` | 682 | 682 | Dependencies |
| `app/build.gradle.kts` | 9300 | 10500 | App build |
| `gradle/libs.versions.toml` | 10200 | 10300 | Version catalog |
| `settings.gradle.kts` | 734 | 734 | Modules |
| `gradle.properties` | 304 | 396 | Properties |
| `local.properties` | 53 | 53 | SDK |
| `res/xml/backup_rules.xml` | 132 | 132 | Backup |
| `res/xml/data_extraction_rules.xml` | 276 | 276 | Data extraction |
| `res/xml/file_paths.xml` | 242 | 242 | FileProvider |
| `res/xml/locales_config.xml` | 266 | 299 | Locales |
| `res/xml/network_security_config.xml` | 253 | 253 | Network security |
| `res/xml/plugin_share_paths.xml` | 110 | 110 | Plugin share |
| `res/drawable/ic_launcher_foreground.xml` | 612 | 612 | Foreground |
| `res/drawable/ic_launcher_monochrome.xml` | 514 | 514 | Monochrome |
| `res/drawable/ic_stat_share.xml` | 741 | 741 | Notification icon |
| `res/mipmap-anydpi-v26/ic_launcher.xml` | 341 | 341 | Launcher icon |
| `res/mipmap-anydpi-v26/ic_launcher_round.xml` | 341 | 341 | Round icon |
| `res/values/colors.xml` | 226 | 226 | Colors |
| `res/values/strings.xml` | 129 | 1199 | Strings |
| `res/values/strings_*.xml` | see A.1 | see A.1 | String files |
| `res/values/themes.xml` | 719 | 719 | Themes |
| `res/values-night/colors.xml` | 123 | 123 | Night colors |
| `res/values-fa/*` | see A.1 | see A.1 | Persian |
| `res/values-ru/*` | see A.1 | see A.1 | Russian |
| `res/values-zh-rCN/*` | see A.1 | see A.1 | Chinese |

### A.2 Work logic
- **native**: lib.rs exports JNI; crypt/hexio/pdfium/rar provide implementations; build.rs links c++_static/c++abi on Android.
- **Build**: build.gradle.kts configures variants/signing/ABI split/cargoHostBuild/dependency alignment; libs.versions.toml version catalog; settings.gradle.kts modules; gradle.properties properties.
- **Resources**: values strings/colors/themes; values-* localization; xml config; drawable/mipmap icons; raw sounds; drawable-nodpi illustrations.

## B. Logic unique to the fork

### B.1 Added-file routing table
| Path | fork | Responsibility |
|---|---|---|
| `res/drawable/ic_python_logo.xml` | 2868 | Python icon |
| `res/values/colors_python.xml` | 77 | Python colors |
| `res/values-night/colors_python.xml` | 77 | Python night colors |
| `res/drawable-nodpi/art_unkown.webp` | see snapshot | Unknown illustration |
| `res/drawable-nodpi/art_warning.webp` | see snapshot | Warning illustration |
| `res/values-fa/strings_export.xml` | 1445 | Persian export strings |
| `res/values-ru/strings_export.xml` | 1513 | Russian export strings |
| `res/values-zh-rCN/strings_export.xml` | 1263 | Chinese export strings |
| `res/values/strings_export.xml` | 1243 | English export strings |
| `res/values/strings_grammar.xml` | 3784 | English grammar strings |
| `res/values-zh-rCN/strings_grammar.xml` | 3760 | Chinese grammar strings |
| `res/values-ru/strings_grammar.xml` | 4493 | Russian grammar strings |
| `res/values-fa/strings_grammar.xml` | 4244 | Persian grammar strings |

### B.2 Added/changed logic
- **Build**: app/build.gradle.kts adds Dhizuku/Ackpine dependencies, `strata.*` properties, `cargoHostBuild`, ABI split, resource excludes, `legacy29` variant targetSdk 29.
- **libs.versions.toml**: adds Dhizuku/Ackpine versions.
- **gradle.properties**: adds `strata.versionName/Code`.
- **Resources**: adds export strings, Python icon/colors, art_unkown/art_warning, STGX-related strings.
- **native**: identical to 1.0.0 (slightly different bytes, same functionality).

## C. Logic unique to 1.0.0, removed or changed in the fork
- 1.0.0 has no `ic_python_logo.xml`, `colors_python.xml`, `art_unkown.webp`, `art_warning.webp`, `strings_export.xml`.
- 1.0.0 `gradle.properties` has no `strata.*`.
- 1.0.0 `app/build.gradle.kts` has no Dhizuku/Ackpine/cargoHostBuild/ABI split.