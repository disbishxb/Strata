# Version History

> Per-version summary of "this version vs. the previous version"; used as the
> source when publishing. The first release is relative to baseline 1.0.0.

## Pending Release (fork relative to 1.0.0)
### User-Visible
- See all entries in `01-user-facing-changes.md`.

### Developer
- See all entries in `02-developer-changes.md`.

### Known Limitations
- Third-party installer results are determined by the external app; Strata only
  hands over the APK.
- Dhizuku requires device owner/authorization; falls back to other backends
  when unavailable.
- STGX only affects highlighting/icons/colors; it does not change file content.
- Split-volume merging relies on `.001/.002/...` naming.