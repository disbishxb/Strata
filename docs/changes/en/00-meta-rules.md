# Meta Rules for the changes Directory

## 0. General Rule

> Timezone convention: all snapshot times/dates in this directory and under `docs/` are Beijing time (UTC+8).
This directory is not a real-time log of every specific change made during development.
It is a staging and summarization area used before a release, so that formal
user-facing release notes can be written from it. Do not record every change
as it happens; summarize after a piece of work is complete.

## 1. Scope
This directory records changes made by this fork relative to the original
repository (baseline Strata 1.0.0), for use when summarizing a release.

## 2. File Responsibilities
- `01-user-facing-changes.md`: only updates users can perceive (features,
  behavior, UI, copy, permission prompts, install/open method changes, etc.).
- `02-developer-changes.md`: all changes that are not "user-visible" (API,
  architecture, dependencies, build, Manifest, data formats, internal behavior,
  performance, tests, etc.).
- `03-changelog.md`: per-version summary of "this version vs. the previous
  version"; used as the source when publishing.
- This file: the rules.

## 3. Update Rules

> ps: AI should not write entries for every change as it happens; write after
> that part of the work is done, to avoid over-recording developer changes
> while user-visible updates are missing.

- R1 Classify every change first: user-visible -> `01`; otherwise -> `02`.
- R2 On each release, aggregate the new/modified entries of this version into
  the corresponding version section of `03-changelog.md`, noting the source file.
- R3 `01` and `02` only describe changes "relative to the baseline or the
  previous version"; do not repeat long-term unchanged content. They are
  summarized change digests, not a real-time log of the development process.
- R4 Entries must be traceable: they should map to a file/module; include paths
  when necessary.
- R5 User-visible entries must be written in user-facing language; developer
  entries may use technical language.
- R6 After modifying `01`/`02`/`03`, also update the "Current Status" section
- R7 Two cases for source changes (see `docs/rule/ai-rule.md` §4.1):
  - Case A (cross-version change): a change relative to the previous release. Write it to `01` or `02` and state that it is a cross-version change.
  - Case B (single unreleased change): bugs found and fixed within the same version, or unreleased intermediate edits, are not recorded separately. Only revise content that remains inaccurate after the fix, or merge content into a clearer account.

  of this file.

## 4. Current Status
- Baseline: Strata 1.0.0.
- Fork snapshot: 2026-11-15 (includes Japanese localization).
- `01`/`02` have been organized per the snapshot; `03` awaits the first
  release version.