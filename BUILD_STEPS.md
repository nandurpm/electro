# Implementation sequence

1. **Project and branding:** root Gradle Kotlin DSL, version catalog, Gradle 8.9 wrapper, API 24/35 manifest, dark/light Material theme, legacy and adaptive resistor/circuit icons.
2. **Registry and shell:** ordered JSON registry, serializable models, repository, DataStore state, AppViewModel, single Activity, Navigation Compose and four-tab pager. Lists include independent search/scroll state, favourites, sorting and footer spacing.
3. **Information and modules:** Info, What’s Included, Changes, Credits, Settings, module enable/disable cards and bundled searchable/filterable Parts, AVR and PIC datasets.
4. **Calculators:** engineering parser/formatter, pure calculation engine, field/mode templates, colour-band reference, value-to-band lookup, plots, NE555 timing circuit, star/delta drawing, result sharing/copy/reset and saved fields.
5. **Pin-outs:** generic data-driven pages, sticky and horizontally scrollable tables, original Canvas illustrations, USB reference, and the 55 named entries. Variant/coverage qualifications are explicit in the data and README.
6. **Resources:** 21 named entries, common-page aliases, generated AWG/ASCII/standard-value/PT100/dB tables, logic and schematic drawings.
7. **Tests and checks:** JUnit calculation tests, independent asset/link/schema validator and Android CI workflow.
8. **Documentation:** README, source register, validation evidence, full source listing and complete project tree.

Read `FULL_SOURCE.md` to inspect the full files implementing these steps without opening an IDE. The ZIP is the authoritative multi-file project; individual Kotlin and XML files remain separate and directly editable.

## Current maintenance scope

The 1.1.0 revision retains the visual refresh, original vector diagrams and unchanged release certificate. It adds documented calculator/persistence contracts, a syntax-tree KDoc gate, source-derived contrast evidence, resource-based UI prose, corrected numeric formatting/parsing, action-specific retries and focused regression tests. Historical releases are summarised in CHANGELOG.md; current checks and limits belong to VALIDATION.md and FIX_REPORT.md.

The 1.1.1 personal-use update removes reserved footer space and optional enablement; all three bundled databases open directly.
