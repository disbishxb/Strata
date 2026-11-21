# UI Development & Localization Guidelines

This document defines how UI, localization, and architecture should be handled in this fork.

---

## 0. Meta-rule: Updating these rules

This section governs changes to this document itself.

- **Rewrite-only refactors are allowed.** The document may be rewritten, reorganized, or restructured freely, as long as the information conveyed by the previous version is preserved: no rule may be dropped, weakened, or silently removed.
- **Reordering is allowed.** Rule order, section order, and numbering may be changed (for example moving the "allowed modifications to underlying layers" section to the top), as long as each rule's meaning and scope stay intact.
- **Wording may be tightened.** Redundant or verbose phrasing may be shortened or merged, but semantics must not change. Any ambiguity introduced during a rewrite must be resolved in favor of the previous, more specific wording.
- **Traceability.** Any non-trivial rewrite, reorder, or wording change to this document must be recorded in the commit message (what was moved or reworded, and why) so the intent can be reconstructed.
- **The information requirement supersedes the form.** When restructuring competes with completeness, completeness wins. A more convenient layout must never be achieved by discarding, merging away, or blurring any existing rule.

### 0.1 Commit messages
- Every commit message must state that the commit is addressed to the user (the recipient is the user, not a collaborator or upstream).

### 0.2 Comments
- Comments must be written in English.
- Comments must not be placed "nearby": do not attach comments to the lines adjacent to a change. (Exception: when demonstrating a modification location to the user.)
- When modifying files via scripts, do not add comments on the affected code lines.
- Once a comment is actually written into a file, it must be a sound, stable comment from the perspective of the whole file. It must not be a phase-specific note (i.e. not a note relative to the previous update, not a change-log-style annotation).
- Consequently: change history, rationale, and "what changed" belong in the commit message; comments in the file carry only content that remains valid for the complete current file (e.g. non-obvious behavior, boundary crossings), written in English, not attached next to the changed lines.

### 0.3 Files may be truncated by the client
- In clients such as the DeepSeek Android app, oversized files may not be delivered in full: on the AI side the content appears truncated, and on the user side the client reports "character limit exceeded, DeepSeek only read the first XX%".
- Regardless of whether the reader is DeepSeek, incomplete content may be caused by client-side truncation rather than by the file itself.
- Therefore: do not treat a truncated tail as the file's real end; before judging or modifying based on incomplete content, first confirm whether truncation occurred (e.g. ask the user to paste in chunks, or verify line/byte counts or the tail via commands).

## 1. Baseline rules

### 1.1 Layout placement
UI elements must be placed in their semantically appropriate and expected locations.
- Follow standard Android and Material Design layout conventions.
- Do not nest elements in confusing or non-standard ways.
- New UI belongs in the file that semantically hosts it, not in whichever file happens to be open at the time.

When unsure where a piece of UI semantically belongs, ask:
1. If this component were extracted for reuse, who would reference it? The referencing side is its semantic host.
2. Which ViewModel owns its state? State ownership usually implies UI ownership.
3. If this screen/component were deleted, should this UI disappear with it? If yes, it must not live elsewhere.

> Example: drawer UI goes in the drawer's own file (`StrataDrawer.kt`), not in an unrelated screen file such as `SettingsScreen.kt`, even if they are adjacent or tightly coupled.

### 1.2 Localization (i18n)
Never hardcode user-facing strings.
- Default English strings go in `app/src/main/res/values/strings*.xml`.
- All other locales (`values-zh-rCN`, `values-ru`, `values-fa`, …) must be updated in the same commit.
- Use `stringResource(R.string.xxx)` in Compose, `context.getString(R.string.xxx)` in non-Compose contexts.

Relevant files:
- Default (English): `app/src/main/res/values/strings.xml` (and `strings_*.xml`)
- Chinese: `app/src/main/res/values-zh-rCN/strings_*.xml`
- Russian: `app/src/main/res/values-ru/strings_*.xml`
- Persian: `app/src/main/res/values-fa/strings_*.xml`
- Japanese: `app/src/main/res/values-ja/strings_*.xml`

### 1.3 Style consistency (fork rules)
This project is a fork: new UI and features must align with the original project's visual style and interactions.
- Follow the established Expressive Material 3 style.
- Match spacing, shape (`RoundedCornerShape`), typography, and `MaterialTheme.colorScheme`.
- Do not introduce generic or non-standard UI components that break the design language.

Relevant files:
- Theme & colors: `app/src/main/java/dev/cluvex/strata/ui/theme/`
- Custom expressive components: `app/src/main/java/dev/cluvex/strata/ui/expressive/`

### 1.4 Architectural standardization
Keep separation of concerns and the standard project structure.
- **Frontend (UI):** Composables stay pure and focused on rendering; state management, data fetching and business logic belong in ViewModels.
- **Backend (Core):** filesystem operations, parsing, networking and heavy lifting live in `app/src/main/java/dev/cluvex/strata/core/`. No business logic in UI files.
- **Contract:** UI talks to Core strictly via ViewModels or defined Repositories.

Relevant files:
- State/logic: `app/src/main/java/dev/cluvex/strata/feature/*/*ViewModel.kt`
- Core backend: `app/src/main/java/dev/cluvex/strata/core/`

---

## 2. Modifying the underlying layer

### 2.1 Any underlying layer may be modified
Because this is a fork, any underlying layer may be changed when it is necessary to implement, fix, polish or refactor a feature. This includes, but is not limited to:
- The Core backend: `app/src/main/java/dev/cluvex/strata/core/` (filesystem, parsing, networking, APK, archive, DEX, PDF, crypto, root/Shizuku, Termux, …).
- Shared UI infrastructure and reusable components (e.g. `ZoneEditor.kt`, `SettingsComponents.kt`, `SettingsRepository.kt`, theme and expressive primitives).
- Repositories, data sources, serialization models, persisted preference schemas.
- Native code under `native/` and any other source set, module, or build configuration.

### 2.2 Constraints when modifying the underlying layer
While doing so:
- Preserve backward compatibility of public APIs and persisted settings where reasonably possible. If a persisted format must change, provide a migration or a safe fallback.
- Keep existing architectural boundaries intact: heavy I/O, parsing and networking stay in `core/`; UI stays in `feature/` and `ui/`.
- Do not bypass essential validation, security checks, or signature verification.
- Document non-obvious behavior of the current code with clear comments (content that stays valid for the whole file, not a change log), and update related UI rules, strings and tests where applicable. Change history and rationale belong in the commit message.

### 2.3 Flexibility for related rules
When a change is made under §2.1, the baseline rules in §1 (especially 1.2, 1.3 and 1.4) may be applied flexibly according to the actual scope of the change:

- **1.2 (i18n):** If the change introduces no new user-facing strings, no string-resource work is required. If it modifies or removes existing user-facing text, update all affected locales (en / zh-rCN / ru / fa / ja) consistently. Never add hardcoded strings just to “satisfy” the rule — only add strings when the change actually surfaces new UI.
- **1.3 (style):** When refactoring shared UI infrastructure (e.g. `ZoneEditor.kt`, `SettingsComponents.kt`), prioritize functional correctness and interaction consistency. Visible style deviations must be justified by a concrete functional requirement.
- **1.4 (architecture):** When modifying core or shared components, adjusting boundaries or introducing direct calls is acceptable if a strict ViewModel/Repository indirection would block a necessary fix or refactor. Still prefer the existing architecture where practical, and document boundary crossings in comments.
- **Traceability:** Non-trivial deviations from §1 caused by a §2.1 change must be recorded in the commit message so the intent is traceable.
- **Documentation sync:** After a §2.1 change, maintain the docs per §4.

---

## 3. Scripted source modification (optional methodology)

### 3.1 When it applies
When the user requests an app optimization and the known information is complete enough and the intended change is clear enough, scripts may be used to modify the source code on the user's behalf.

Any kind of script qualifies — Python, shell, or any other scripting language — chosen as needed for the task. There is no restriction on the scripting language or tooling.

### 3.2 Operational guidance
Scripts of any language (Python, shell, etc.) are acceptable; pick whatever fits the job. Regardless of language, keep the following in mind:
1. **Backups:** Place backups uniformly under `~/build`, or in a directory adjacent to the project.
2. **Escaping:** Pay attention to escaping issues in shell/regex/sed scenarios so special characters are not misinterpreted.
3. **Greediness:** When using regex or batch replacement, watch out for greedy matching that could affect content beyond the intended target.
4. **Import completeness:** After changes, check whether any imports were missed (especially when introducing new types, Composables, or utility class references).

### 3.3 Information acquisition (optional)
When the information needed for a scripted change is incomplete, it may be obtained by asking the user to attach relevant files or to run commands and report the output.
- This is an optional means of gathering information, not a mandatory requirement.
- It does not override §3.4: when the change is still unclear, confirm with the user before acting.

### 3.4 Status
The above are **optional, non-mandatory requirements** (methodology-level hints). How to actually help the user may be handled flexibly.

### 3.5 Relationship to existing rules
- Does not override the hard constraints of §1.2 (i18n), §1.3 (style), or §1.4 (architecture); if a scripted change touches user-facing strings, shared UI, or core boundaries, the original rules still apply.
- Serves as one operational means under §2.1 ("any underlying layer may be modified"). If a scripted change causes a substantive deviation from §1, record it in the commit message per the "Traceability" requirement in §2.3.
- The choice of scripting language is free; no language is mandated. Backup, escaping, greediness, and import checks are operational safety suggestions, not thresholds that must be satisfied item by item. When information is incomplete or the change is unclear, confirm with the user before acting.

---

## 4. Documentation sync after code changes

After modifying source code, maintain the documentation per the existing meta-rule documents; do not restate or redefine their rules here:

- Routing docs: follow `docs/routing/en/00-overview.md`; after modifying source code, update the byte counts and responsibilities referenced by the corresponding volume.
- Change records: follow `docs/changes/en/00-meta-rules.md`.
- Those two documents are authoritative; this section is guidance only, and they win on conflict.

### 4.1 Two cases for changes
- **Case A (cross-version change):** A user-visible or developer change relative to the previous release. File it into `01` or `02` per `00-meta-rules.md`, and state that it is a cross-version change.
- **Case B (single unreleased change):** Bugs found and fixed within the same version, or unreleased intermediate edits, are not recorded separately. Only revise content that remains inaccurate after the fix, or merge content that can be combined into a clearer account.

