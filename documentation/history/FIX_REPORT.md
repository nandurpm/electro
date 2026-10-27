# ElectroKit 1.1.1 — personal workbench update

This report separates implementation from executed evidence. `verification/summary.json` and the retained reports contain the final measured outcomes. An Android framework simulation is not a device run.

| ID | Change | Files touched |
| --- | --- | --- |
| A1 | KDoc purpose, named parameters, return and error behaviour for types/functions, including private/local helpers and composables. Member/file properties and constructor fields are documented. | Every Kotlin file under app/src, including tests; domain/Messages.kt; data/Repository.kt |
| A2 | Formula and unit comments immediately above calculator-ID dispatch cases, including screen-only/formula-browser cases. Nested mode branches are not mislabelled as calculator IDs. | domain/Calculators.kt |
| A3 | Explains numeric grammar, suffix precedence, rounding promotion, negative-scale division, CVD coefficients, IPC constants, debounce/pending ownership and corruption preservation. | domain/Engineering.kt; viewmodel/AppViewModel.kt; data/PreferenceCodec.kt |
| A4 | Field meanings and colour roles; rationale for build/CI sections and Python functions. | data/Repository.kt; ui/theme/Theme.kt; build.gradle.kts; settings.gradle.kts; app/build.gradle.kts; .github/workflows/android.yml; scripts/*.py |
| A5 | Five requested files meet at least one comment line per eight nonblank code lines. The coverage report records measured counts, not a hand-maintained estimate. | Calculators.kt; Engineering.kt; App.kt; CalculatorScreen.kt; AppViewModel.kt; verification/comments.txt |
| A6 | Syntax-tree KDoc gate, parameter/return-tag checks, syntax-error failure, density gate and negative/positive fixtures; runs in CI. Temporary local vals and lambda parameters are outside the API-declaration gate. | scripts/check_comments.py; scripts/requirements-dev.txt; scripts/test_quality_checks.py; android.yml |
| B1 | Four significant digits for decimal/percent output, trailing-zero trimming and exact fractional tolerances; integer ADC codes retain an unrounded path. | Engineering.kt; Calculators.kt; FormattingRegressionTests.kt |
| B2 | Quantises inverse RTD result to 1e-6 °C and removes signed zero. | Engineering.kt; FormattingRegressionTests.kt |
| B3 | Unitless and V/V results use six-significant-digit decimal/scientific notation, with no SI prefix letter. | Engineering.kt; FormattingRegressionTests.kt |
| B4 | BigDecimal plain R notation, explicit practical-range message, and R0 for integer alternatives. | Engineering.kt; Messages.kt; FormattingRegressionTests.kt |
| B5 | Retains the failed action for retry; settings and input retries are distinguished by ViewModel tests with an injected failing repository. | AppViewModel.kt; Repository.kt; App.kt; RetryRegressionTests.kt |
| B6 | Case-insensitive Meg alias restored without weakening the remaining suffix/unit grammar. | Engineering.kt; FormattingRegressionTests.kt |
| B7 | Negative suffix powers use division, with exact assertions for 10u and 10uF against 1e-5. | Engineering.kt; FormattingRegressionTests.kt |
| C1 | UI controls/hints/accessibility and storage errors use Android resources. Domain English prose/labels/formulas are consolidated in Messages. Non-composable owners resolve Context resources. | strings.xml; App.kt; CalculatorScreen.kt; Drawings.kt; AppViewModel.kt; domain/Messages.kt; domain calculators/parser/filter |
| C2 | Changes describes product improvements rather than an audit. | strings.xml; App.kt |
| C3 | Info, Changes and Credits prose moved out of the composable body into resources. | strings.xml; App.kt |
| D1 | Contrast reads actual Theme.kt palette/toolbar hex values, enforces thresholds and rejects stale committed evidence. | Theme.kt; App.kt; scripts/check_contrast.py; verification/contrast.txt; test_quality_checks.py |
| D2 | One current-version README description and a short historical CHANGELOG. | README.md; CHANGELOG.md |
| D3 | Replaces overbroad audit claims; regenerates listing/tree/checksums after source and verification updates. | AUDIT_RESOLUTION.md; FULL_SOURCE.md; PROJECT_TREE.txt; SHA256SUMS.txt; scripts/package_project.py |
| E | Retains existing tests; adds formatting/parser/RTD/SMD/retry regressions and quality-gate tests. Extends the existing per-field extreme grid with abc/NaN without removing its original samples. | FormattingRegressionTests.kt; RetryRegressionTests.kt; AuditRegressionTests.kt; test_quality_checks.py |

API-declaration coverage does not imply that every temporary variable has KDoc, nor can a parser grade comment quality. English technical symbols and bundled reference/module datasets are not a complete localisation system.

The per-field grid evaluates every declared calculator field in every supported mode with empty text, garbage, abc, NaN, -1, 0, 1e-300 and 1e300. Its measured case total is printed in the JUnit XML. Calculator IDs with no scalar fields remain covered by the existing default-calculation and catalogue tests.

Unchanged limitations: 9/72 shared reference pages have source URLs; full standards traceability and remaining PIC metadata are incomplete. No physical-device/emulator run, TalkBack speech or actual airplane-mode test was performed. Release JVM tests do not execute R8 output on a device.

Command outcomes are appended from the final verification run below; old build reports are not accepted as proof of this revision.



## Command results

| Command/task | Measured outcome |
| --- | --- |
| Project wrapper: assembleDebug, assembleRelease, testDebugUnitTest, testReleaseUnitTest, lintDebug, lintRelease | BUILD SUCCESSFUL; current wrapper run retained |
| testDebugUnitTest | 59 passed; zero failed/errors/skipped |
| testReleaseUnitTest | 45 passed; zero failed/errors/skipped |
| Extreme per-field grid inside both test variants | 1,984 cases; no rejected exception/generic text |
| python3 scripts/check_comments.py | PASS: 606 declarations; all five density floors met |
| python3 -m unittest discover -s scripts -p 'test_*.py' | 4 passed |
| python3 scripts/validate_assets.py | PASS: 31 / 55 / 21 / 3 |
| python3 scripts/check_contrast.py | PASS; source-derived and committed numbers match |
| apksigner verify --verbose --print-certs | PASS: v2/v3; existing release certificate retained |
| aapt package/manifest/permission checks | PASS: code 6, API 24/35, non-debuggable, no INTERNET permission |
| Packaged JSON comparison | PASS: every bundled JSON file matches source bytes |
| python3 scripts/package_project.py, then --check | PASS; complete source/tree/checksums regenerated |

No device/emulator run or remote CI execution is claimed. Detailed lint warning counts are in VALIDATION.md and raw reports. Changes by every requested ID and touched files are listed above.

## Complete touched-file list

- `.github/workflows/android.yml`
- `AUDIT_RESOLUTION.md`
- `BUILD_STEPS.md`
- `CHANGELOG.md`
- `FIX_REPORT.md`
- `FULL_SOURCE.md`
- `PROJECT_TREE.txt`
- `README.md`
- `SHA256SUMS.txt`
- `VALIDATION.md`
- `app/build.gradle.kts`
- `app/src/main/java/com/electrokit/app/MainActivity.kt`
- `app/src/main/java/com/electrokit/app/data/PreferenceCodec.kt`
- `app/src/main/java/com/electrokit/app/data/Repository.kt`
- `app/src/main/java/com/electrokit/app/domain/Calculators.kt`
- `app/src/main/java/com/electrokit/app/domain/Engineering.kt`
- `app/src/main/java/com/electrokit/app/domain/Messages.kt`
- `app/src/main/java/com/electrokit/app/domain/NumericFilter.kt`
- `app/src/main/java/com/electrokit/app/ui/components/Drawings.kt`
- `app/src/main/java/com/electrokit/app/ui/components/ToolIcon.kt`
- `app/src/main/java/com/electrokit/app/ui/screens/App.kt`
- `app/src/main/java/com/electrokit/app/ui/screens/CalculatorScreen.kt`
- `app/src/main/java/com/electrokit/app/ui/theme/Theme.kt`
- `app/src/main/java/com/electrokit/app/viewmodel/AppViewModel.kt`
- `app/src/main/res/values/strings.xml`
- `app/src/test/java/com/electrokit/app/data/DataRegressionTests.kt`
- `app/src/test/java/com/electrokit/app/domain/AuditRegressionTests.kt`
- `app/src/test/java/com/electrokit/app/domain/CalculatorTests.kt`
- `app/src/test/java/com/electrokit/app/domain/FormattingRegressionTests.kt`
- `app/src/testDebug/java/com/electrokit/app/ui/AndroidUiRegressionTests.kt`
- `app/src/testDebug/java/com/electrokit/app/ui/DesignPreviewTests.kt`
- `app/src/testDebug/java/com/electrokit/app/viewmodel/RetryRegressionTests.kt`
- `build.gradle.kts`
- `scripts/check_comments.py`
- `scripts/check_contrast.py`
- `scripts/check_reference_links.py`
- `scripts/package_project.py`
- `scripts/requirements-dev.txt`
- `scripts/test_quality_checks.py`
- `scripts/validate_assets.py`
- `settings.gradle.kts`
- `verification/apk-badging.txt`
- `verification/apk-manifest.txt`
- `verification/build-output.txt`
- `verification/clean-build-output.txt`
- `verification/comment-only-proof.json`
- `verification/comments.txt`
- `verification/contrast.txt`
- `verification/lint-results-debug.html`
- `verification/lint-results-debug.txt`
- `verification/lint-results-debug.xml`
- `verification/lint-results-release.html`
- `verification/lint-results-release.txt`
- `verification/lint-results-release.xml`
- `verification/python-quality-tests.txt`
- `verification/summary.json`
- `verification/tests-debug/TEST-com.electrokit.app.data.DataRegressionTests.xml`
- `verification/tests-debug/TEST-com.electrokit.app.domain.AuditRegressionTests.xml`
- `verification/tests-debug/TEST-com.electrokit.app.domain.CalculatorTests.xml`
- `verification/tests-debug/TEST-com.electrokit.app.domain.FormattingRegressionTests.xml`
- `verification/tests-debug/TEST-com.electrokit.app.ui.AndroidUiRegressionTests.xml`
- `verification/tests-debug/TEST-com.electrokit.app.ui.DesignPreviewTests.xml`
- `verification/tests-debug/TEST-com.electrokit.app.viewmodel.RetryRegressionTests.xml`
- `verification/tests-release/TEST-com.electrokit.app.data.DataRegressionTests.xml`
- `verification/tests-release/TEST-com.electrokit.app.domain.AuditRegressionTests.xml`
- `verification/tests-release/TEST-com.electrokit.app.domain.CalculatorTests.xml`
- `verification/tests-release/TEST-com.electrokit.app.domain.FormattingRegressionTests.xml`

## Personal-use changes

- Removed the two explicit 56 dp bottom bars from MainScreen and the databases action screen. The Android gesture/navigation inset remains.
- Renamed the visible PLUGINS tab/action to DATABASES. All three datasets are available without changing any saved setting.
- Removed Enable/Disable UI and its ViewModel mutator. Legacy `modules` values remain readable but no longer control availability.
- Kept database rows in Calculators and added unconditional direct Open actions to database cards.
- Updated the retained module UI test to start from an empty legacy selection and verify direct access. Added a measured bottom-viewport regression.
- Existing app ID, DataStore record and signing certificate are preserved for an in-place update.

The final command results in VALIDATION.md and verification/summary.json are authoritative for 1.1.1.

Current 1.1.1 outcome: build, both lint tasks and 59 debug/45 release tests passed. The additional test measures the catalogue viewport after footer removal. No executable source edits followed this build.

## 1.1.2 follow-up

See REVIEW_RESPONSE.md for the current issue-by-issue response. The current implementation replaces the old debounce with an atomic input journal, fixes declared compound-unit parsing and stacked display prefixes, uses plural resources, and discloses provenance on every reference. Source-link coverage and device validation remain open. The final current test totals in VALIDATION.md supersede the previous-version totals above.
