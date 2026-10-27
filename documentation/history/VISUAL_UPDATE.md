# ElectroKit 1.2.0 — reference illustrations

This update addresses recognisability of hardware inside reference pages. All artwork is original Compose Canvas vector work; none of the supplied screenshots or another application's drawings/photos are embedded.

## Changes

- Distinct illustrations for D-sub ports, Centronics, circular housings, modular connectors, power housings, RCA, TRRS jack, Uno R3 board identification, DIP14 ICs and TO-220 regulators.
- Metal gradients, recessed contacts, gold finishes, moulded insulation, mounting hardware and board components replace repeated outline boxes on 28 reference diagrams.
- New hardware illustrations have an Enlarge diagram action opening a larger, scrollable inspection view with a Close action.
- USB drawings gain metallic surface shading and recessed inserts. Remaining logical contact maps gain shaded surfaces and gold contacts; specialised circuit, switch and logic symbols remain schematic.
- Colour-name cells use the actual colour across the cell, with black/white text selected by relative luminance. Text labels remain so colour is not the only cue.
- Personal-use behaviour is retained: no reserved footer, all databases immediately available, no enable/disable switches, no INTERNET permission.

## Technical limits

Housing illustrations of circular/power connectors are intentionally unnumbered: their previous JSON coordinates were logical index grids, not verified physical contact positions. Captions explicitly distinguish these illustrations from wiring guides. Tables preserve the original data. D-sub/Centronics drawings state the male mating-face view. The Uno is a simplified identification illustration, not a manufacturer photo or an exact PCB manufacturing drawing. No new technical source claims are made.

Not every connector has a bespoke mechanical drawing yet: USB and the newly illustrated families have specialised renderers; other pages still use improved logical contact maps. Existing external-citation and real-device validation gaps remain. Robolectric screenshots are simulated Android rendering, not photographs or device validation.

## Final measured results

- Wrapper build and both lint tasks: passed, zero lint errors.
- Debug tests: 69 passed; release JVM tests: 50 passed; zero failed/errors/skipped.
- Python unittest quality checks: four passed; KDoc, asset/provenance and contrast gates passed.
- APK signature, app identity, no-INTERNET property and packaged JSON/source equality: verified.
- External citations: still 9/72. Real-device/emulator checks: not run. Dependencies remain pinned; intentional predictive-back compatibility warning retained.

See verification/summary.json, raw reports, REFERENCE_COVERAGE.md and DEVICE_VALIDATION.md for the exact limits.
