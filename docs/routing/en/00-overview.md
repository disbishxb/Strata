# Strata Routing & Work-Logic Docs · Overview

## 1. What this documentation set is
- A "navigation map" for AI / humans: when you see a file or feature, you immediately know which doc to look in and roughly how it works.
- Kotlin source is not copied; only responsibilities, data flow, and dependencies are written.

## 2. Baseline

> Timezone convention: all snapshot times/dates in this doc and under `docs/` are Beijing time (UTC+8).

- Upstream Strata 1.0.0 (snapshot 2026-11-13 17:58).
- Fork snapshot 2026-11-13 20:22.
- Source-level differences between the fork and 1.0.0 are listed in `99-fork-differences.md`.

## 3. Directories and what each file covers
| File | Covers |
|---|---|
| `00-overview.md` | This file: system, directories, meta-rules, query commands |
| `01-routing-index.md` | All files by module with responsibilities and byte sizes (fork / 1.0.0 columns) |
| `02-work-logic-index.md` | List and index of work-logic volumes by topic |
| `10-entry-and-shell.md` | MainActivity, StrataApp, AppShell, RouteStack, drawer, i18n, SettingsRepository, security, open, root/vault/stgx/sound/dhizuku/theme |
| `20-file-explorer.md` | All of feature/explorer + dependent core/fs, core/remote, bookmarks |
| `30-filesystem-and-remote.md` | core/fs + core/remote + feature/remote |
| `40-apk-and-control.md` | core/apk + core/control + core/dhizuku + feature/apk + feature/apps |
| `50-settings.md` | All of feature/settings |
| `60-archive-dex-and-pdf.md` | core/archive + core/dex + core/pdf + core/hex + feature/pdf |
| `70-root-termux-and-plugin-sharing.md` | core/root, core/shizuku, core/termux, core/plugin, core/share + feature/termux |
| `80-text-editor.md` | feature/editor + core/text |
| `90-vault-security-and-misc.md` | core/vault + ui/security + feature/vault + feature/trash + feature/share |
| `A0-native-and-build-resources.md` | native/, build.gradle.kts, libs.versions.toml, settings/gradle.properties, resource index |
| `99-fork-differences.md` | Summary of added/removed/significantly resized files, dependencies, Manifest differences |

## 4. Meta-rules
- M0 New files may be added, or doc categories changed.
- M1 After modifying any source file, update the byte sizes and responsibilities in the corresponding volume; sync additions/deletions/renames to `01` and `99`.
- M2 Byte sizes are always in bytes; verify with `stat -c '%s  %n' <path>` or `find ... -printf '%s\t%p\n'`.
- M3 Routing answers only "which file handles what"; logic answers only "roughly how it works"; do not paste Kotlin source.
- M4 `01`/`02` are entry points; volumes are the details; `99` is the difference summary.
- M5 fork and 1.0.0 are kept in strict separate volumes; each volume has A/B/C sections.

## 5. Skeleton inside each volume

> ps: "C. Logic unique to 1.0.0, removed or changed in the fork" means content of the fork relative to the upstream repo, not fork updates.

```markdown
# NN-ModuleName
> Meta-rule: after changing code, update this volume's byte sizes and entries; verify with `stat -c '%s %n'`.

## A. Logic identical in fork and 1.0.0
### A.1 File routing table (path | 1.0.0 byte | fork byte | responsibility)
### A.2 Work logic (topic-based)

## B. Logic unique to the fork
### B.1 Added-file routing table
### B.2 Added/changed logic

## C. Logic unique to 1.0.0, removed or changed in the fork
### C.1 Removed/replaced files and logic