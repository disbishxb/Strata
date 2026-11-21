# Strata 路由与工作逻辑文档 · 总说明

## 1. 这套文档是什么
- 给 AI / 人看的“导航地图”：看到某个文件或功能，立刻知道去哪个文件找、大致怎么运作。
- 不复制 Kotlin 源码；只写职责、数据流、依赖。

## 2. 基线

> 时区约定：本文档及 `docs/` 下所有快照时间/日期均为北京时间（UTC+8）。

- 原仓库 Strata 1.0.0（快照 2026-11-13 17:58）。
- fork 快照 2026-11-13 20:22。
- fork 相对 1.0.0 的源码级差异见 `99-fork-differences.md`。

## 3. 目录与各文件讲什么
| 文件 | 讲什么 |
|---|---|
| `00-overview.md` | 本文件：体系、目录、元规则、查询命令 |
| `01-routing-index.md` | 按模块列出所有文件的职责与字节数（fork / 1.0.0 两列） |
| `02-work-logic-index.md` | 按主题列出工作逻辑分册的清单与索引 |
| `10-entry-and-shell.md` | MainActivity、StrataApp、AppShell、RouteStack、drawer、i18n、SettingsRepository、安全、open、root/vault/stgx/sound/dhizuku/theme |
| `20-file-explorer.md` | feature/explorer 全部 + 依赖的 core/fs、core/remote、bookmarks |
| `30-filesystem-and-remote.md` | core/fs + core/remote + feature/remote |
| `40-apk-and-control.md` | core/apk + core/control + core/dhizuku + feature/apk + feature/apps |
| `50-settings.md` | feature/settings 全部 |
| `60-archive-dex-and-pdf.md` | core/archive + core/dex + core/pdf + core/hex + feature/pdf |
| `70-root-termux-and-plugin-sharing.md` | core/root、core/shizuku、core/termux、core/plugin、core/share + feature/termux |
| `80-text-editor.md` | feature/editor + core/text |
| `90-vault-security-and-misc.md` | core/vault + ui/security + feature/vault + feature/trash + feature/share |
| `A0-native-and-build-resources.md` | native/、build.gradle.kts、libs.versions.toml、settings/gradle.properties、资源索引 |
| `99-fork-differences.md` | 新增/删除/体积显著变化/依赖/Manifest 差异汇总 |

## 4. 元规则
- M0 允许添加新文件或更改说明文档分类
- M1 修改任一源文件后，必须更新对应分册的字节数与职责；新增/删除/重命名同步到 `01` 与 `99`。
- M2 字节数一律用 byte；可用 `stat -c '%s  %n' <path>` 或 `find ... -printf '%s\t%p\n'` 核对。
- M3 路由只答“哪个文件管什么”，逻辑只答“大致怎样运作”，不贴 Kotlin 源码。
- M4 `01`/`02` 是入口；分册是细节；`99` 是差异总表。
- M5 fork 与 1.0.0 严格分册；每册内 A/B/C 三段。

## 5. 每册内部骨架

>ps:"C. 1.0.0 独有、fork 已删或改掉的逻辑"是 fork 相对原仓库的内容不是 fork 更新内容

```markdown
# NN-模块名
> 元规则：改代码后更新本册字节数与条目；用 `stat -c '%s %n'` 核对。

## A. fork 与 1.0.0 相同的逻辑
### A.1 文件路由表（路径 | 1.0.0 byte | fork byte | 职责）
### A.2 工作逻辑（主题式）

## B. fork 独有的逻辑
### B.1 新增文件路由表
### B.2 新增/改动逻辑

## C. 1.0.0 独有、fork 已删或改掉的逻辑
### C.1 删除/替换的文件与逻辑