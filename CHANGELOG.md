# Changes

## 4.0.0 — engineering correctness and reliability (code 20)

- Promoted the audited source changes after 3.3.0 into the 4.0.0 release candidate; no new engineering specifications or manufacturer claims are introduced.
- Fixed numeric formatting, E-series tolerance, SMD codes, list parsing, reactance/resistance and calculation boundary cases, with regression tests.
- Strengthened engineering access lifecycle locking, reset/error feedback, text scaling and saved-input handling.
- Restored the validated dependency catalog after merged updates introduced duplicate TOML keys and broke Android CI.
- The signed-release workflow must pass unit tests, lint, APK signature/certificate verification and source packaging before publication; physical-device validation remains outstanding.
- See [the audit follow-up](docs/AUDIT_2026_10_28.md) for unresolved items.

## Audit changes originally prepared in 3.3.0 source

- Corrected formatter, resistor-code, list-input and numeric-boundary behavior; added regression coverage.
- Improved reference-gate lifecycle, error/reset accessibility and input-write scheduling.
- Build both variants in CI and generate verification/source evidence as artifacts.
- See [the audit follow-up](docs/AUDIT_2026_10_28.md) for remaining migrations and limitations.

## 3.1.0 — workbench UI redesign and verified release

- Refreshed the workbench presentation with a redesigned home/workbench surface, reusable ELECTRO panels, updated navigation labels and Profile presentation, while keeping the existing offline calculators, references, persistence and application identity connected.
- Added and updated Compose UI regression coverage for the redesigned workbench, navigation and design previews; the release workflow rebuilds and records verification evidence from the signed APK.
- Hardened release publication and CI diagnostics: reruns update an existing release safely, artifact-quota failures do not hide a successful build, and signing remains pinned to the authorized ELECTRO release certificate.
- Version 3.1.0, code 17. The signed release workflow completed successfully; physical-device validation remains unrun.

## 3.0.2 — reference-based UI release

- New signing identity explicitly authorized after loss of the previous credentials. Existing application ID retained; older-key installations cannot be upgraded in place.

- Applied the supplied ELECTRO reference through navy/cyan themes, lightning branding, compact bordered panels, illustrated home banner, six quick actions and grouped settings. Existing Compose routes, calculators and persistence remain connected.
- Release version 3.0.2/code 16 and pinned signing checks to the published 3.0.1 certificate. GitHub Actions signs with the published release identity and regenerates evidence before packaging.
- Validation: 121 debug/UI tests, 80 release tests, 10 Python tests, no lint errors. Physical-device validation remains unrun.

## 3.0.2 — drawing code dictionary alignment (source only, not yet built or signed)

- Aligned the engineering component dictionary with the supplied DMRC drawing code dictionary (146 rows). Technical records go from 245 to 325: 6 re-coded and 80 added.
- Re-coded: `K49` → `K99`, `E-BK` → `EBK`, `S-BK` → `SBK1` with `SBK2` added, `INV` → `NV` (VFD drive) with `INV` retained as the inverter-fault signal, `EK05`/`EK15` → `EL05`/`EL15`.
- Added the dictionary entries that were missing: `OL2`, `MT2`, `MPC`, `ICP`, `EXT`, `HM`, `LCD2`, `LCD3`, `MHP1`, `MHP2`, `MT1 PTC`, `MT2 PTC`, `DSS`, the lighting and socket group, the fan, speaker and metering group, the MCC/TMB/BMB plugs, and the signal/terminal designations.
- Added a `dmrc-codes` source record with the dictionary checksum, so every new entry carries its provenance.
- `X1–X10`, `Y5–Y6` and `Q13–Q14` are recorded as grouped ranges because the individual point functions are not supplied, following the existing `Q9–Q12` precedent.
- Switches `S11–S58`, `K03`, `OP`, `BIT0–BIT3`, `BM`, `RS485` and `INS` come from the photograph transcriptions and are retained; they are not part of the supplied dictionary.
- NOT changed: versionCode/versionName, signing, access-code design, Kotlin sources and the 125 exact fault rows.

## 3.0.1 — audit fixes

- Engineering code verification now runs off the UI thread; Unlock is disabled while checking.
- Unknown or damaged `entry/{id}` routes return to the previous screen instead of a blank screen or a crash.
- DC cable calculator rejects a computed drop that is not below the supply voltage.
- Removed the published access code from README. Stale 2.0.3 status files corrected.
- Version name 3.0.1, version code 15. Access-code design, status-bar colour and string extraction remain unchanged.

## 3.0 — release preparation (2026-10-27)

- Set the application name to ELECTRO, versionName to 3.0 and versionCode to 14.
- Retain the validated native workbench, original application ID, local data and engineering records.
- Release build/signature verification is recorded separately in docs/RELEASE_3_0.md.

## 2.1.0 — development build (2026-10-27)

- Restored the original complete Android project from checksum-verified embedded source.
- Added five primary destinations, dark-blue/light/system appearance, fixed header/search and visible system bars.
- Integrated 114 local component images, generic component details, bookmarks and bounded off-thread image loading.
- Added communication/multimeter lessons, an interactive meter illustration, seven public utilities and an eight-dimensional converter.
- Added session-only engineering access, manual locking, basic attempt throttling and protected-window handling.
- Preserved original calculator/reference/database routes, exact engineering/fault records and existing local input/preferences.
- Added recent tools and bounded saved calculation history; invalidated explicit results after input/mode changes.
- Added Android regression coverage, source/asset validators, debug/release workflows and maintenance guides.
- Production signing, real-device upgrade/accessibility checks and independent external reference verification remain unrun; see docs/AUDIT_REPORT.md.

## 2.0.3

- Full-screen workbench with compact navigation, scrolling introductions and pinned catalogue search.
- Engineering dashboard organised around fault lookup, calculations, source references and system topics.
- Responsive background input saves, preserved damaged-journal recovery and operation-specific errors.
- Regenerative motor slip support and extensible fault-count validation.
- Expanded developer guide and explicit Android SDK installation in CI.

# 2.0.2

Preserved the navy/teal redesign and bottom navigation; restored technical-source access; corrected large-text catalogue/navigation layout; retained calculator behaviour and exact fault data.

# 2.0.1

Coherent green semantic theme colours; visible navigation tabs; consistent launcher branding; clearer fault-reference guidance; improved wire-colour cells, diagram cards and markers; persisted appearance during startup; refreshed previews and audit evidence.

# Changelog

## 2.0.0 — Electro

- Renamed the displayed app and original icon while retaining package, signing and stored data.
- Added additional technical references, 245 records and 14 photograph transcriptions.
- Added source-traceable global search, fault filtering/bookmarks and bidirectional topic navigation.
- Added selectable cutaway with zoom/pan, logical controller references and original industrial illustrations.
- Added 15 engineering calculators and explicitly simulated CBM examples.
- Preserved all existing tools and footer-free personal-use behaviour.


## 1.2.0

- Added detailed original hardware illustrations and an enlarged inspection view.
- Improved USB/contact shading and full-cell wire-colour tables.
- Preserved always-available databases, local data and the footer-free personal-use layout.


## 1.1.2 — Input reliability

- Shared field-unit grammar and explicit compound-unit support.
- Atomic local input journal and immediate main-record writes.
- Visible provenance status on every reference and truthful citation-coverage register.
- Proper quantity plurals and removal of an unused credit string.


## 1.1.1 — Personal workbench

- Removed both unused 56 dp bottom spacers; lists extend to normal system-navigation clearance.
- All three offline databases are always available, including when an older saved record disabled them.
- Replaced module management with direct Open buttons and renamed the section Databases.
- Existing saved values, favourites and theme remain compatible.


## 1.1.0 revision 2

- Rounded percentages, clean unitless notation and zero-normalised RTD results.
- Restored Meg/MEG aliases and corrected micro-unit parsing for the reported default.
- Plain-decimal SMD R notation and retries that repeat the failed operation.
- Expanded Kotlin, formula, build and script comments, with a CI documentation gate.
- Resource-based UI/help text and contrast evidence generated from the shipping palette.

## 1.1.0

Rounded tool cards, original circuit motifs, forest/mint palettes, clearer input/result cards, connector panels and easier-to-read reference tables.

## 1.0.2

Expanded component references and Android framework simulations covering catalogue routes, modules, rotation, theme and large text.

## 1.0.1

Ratio validation, input scaling, fractional tolerance labels, explicit numeric filters and optimised release packaging.
