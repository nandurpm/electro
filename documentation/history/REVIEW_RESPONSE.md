# Review response — 1.1.2

## Implemented

1. **Field units:** central UnitCatalog supplies parser units, declared-unit aliases and conversion factors. Compound m/s, oz/ft² and °C/W are supported. mA and kW already parsed via SI prefixes; explicit conversion tests now protect them. Metres were another ambiguity: 10m remains milli in bare engineering syntax, but means ten metres in a metre field. A regression iterates every numeric field in all 31 specifications. Already-scaled output labels also remain intact, preventing invalid stacked prefixes such as mmm or mmA.
2. **Pending input:** a small AtomicFile journal is written synchronously, fsynced and read back before acknowledging an input edit. DataStore commits immediately in the background; the deliberate 300 ms debounce is removed. Startup overlays/replays journal values and clears only snapshots whose main-record transaction completed. Newer edits cannot be cleared by an older acknowledgement. Corrupt backups surface an error. This trades a small synchronous private-file write per edit for stronger durability; storage failures or interruption before acknowledgement are not guaranteed.
3. **Provenance visibility:** all 72 pages now disclose their provenance/verification status before the table and include it in shared text. The asset gate rejects missing provenance. **External citation coverage remains 9/72.** The other 63 are explicitly marked unverified; no invented URLs or claim of standards verification was added. Full source attribution remains open.
4. **Lint:** tool, database-entry and part counts use Android plurals; unused developer credit string removed. Predictive-back opt-in is retained: it is intentional on recent Android versions, not a demonstrated API-24 crash. Android reference: https://developer.android.com/guide/navigation/custom-back/predictive-back-gesture. Dependency versions remain pinned to the tested API-35 toolchain; newer versions alone do not establish a product defect, and no untested dependency migration is claimed.

## Validation boundaries

No physical device/emulator was available in this environment, and no /dev/kvm acceleration is exposed. DEVICE_VALIDATION.md provides reproducible release-install/recovery checks with every device result marked Not run. TalkBack speech, actual airplane mode and on-device R8 behaviour remain unverified. Remote CI was not triggered here.

The project does not use pytest. Its Python quality tests run with `python3 -m unittest discover -s scripts -p 'test_*.py'`, after installing scripts/requirements-dev.txt. A missing Android SDK is a build-host prerequisite failure, not independent evidence of a Kotlin compilation defect.

The personal-use behaviour is retained: no empty footer, all three databases always available, no Enable/Disable controls, no INTERNET permission. Release signing is retained for an in-place update.

Final measured outcomes are recorded in VALIDATION.md and verification/summary.json after the fresh build completes.

## Final measured results

- Wrapper build and both lint tasks: passed, zero lint errors.
- Debug tests: 68 passed; release JVM tests: 50 passed; zero failed/errors/skipped.
- Python unittest quality checks: four passed; KDoc, asset/provenance and contrast gates passed.
- APK signature, app identity, no-INTERNET property and packaged JSON/source equality: verified.
- External citations: still 9/72. Real-device/emulator checks: not run. Dependencies remain pinned; intentional predictive-back compatibility warning retained.

See verification/summary.json, raw reports, REFERENCE_COVERAGE.md and DEVICE_VALIDATION.md for the exact limits.
