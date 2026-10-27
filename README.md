# Electro — Electrical & Electronics Engineering Toolkit

Electro 2.0.3 (version code 12) upgrades the existing ElectroKit application. Kotlin, Jetpack Compose, Material 3, single Activity, MVVM, Navigation Compose, Preferences DataStore and kotlinx.serialization are retained. Package/application ID remains **com.electrokit.app**, minSdk 24, target/compile SDK 35.

## Install / update

Install the separately supplied **Electro-2.0.3.apk**. It uses the same dedicated signing certificate as the ElectroKit release updates from 1.0.1 through 1.2.0, so it can update those installations while retaining their local data. The original 1.0.0 debug-key APK cannot be updated by this release certificate; uninstalling that original debug build would clear its data.

The launcher name, toolbar, Info/Changes text and original circuit/energy icon now use Electro. Internal Kotlin packages, the application ID, existing preference keys, journal filename and signing identity intentionally retain their old stable identifiers. There is no data-destructive migration.

## Contents

- All existing 31 calculators, 55 pin-out entries, 21 resource entries and 3 permanently available offline databases remain operational and keep their IDs/order.
- The new **ENGINEERING** tab and home card open **Elevator & Escalator Engineering**.
- Twelve requested engineering sections plus a dedicated CBM section provide 245 structured technical records, 125 original fault records, 14 additional source transcriptions and 15 new calculator definitions.
- Global engineering search includes records, component IDs, terminals, fault codes/messages, original transcriptions, new tools and existing toolkit titles.
- Fault Explorer has code/message search, original group and reset filters, a derived system/location index, bookmarks, details, copy/share, related references and related faults.
- The original cutaway has 15 selectable approximate component regions, accessible labelled legend buttons and an inspection dialog with pinch zoom, pan and explicit zoom/reset controls.
- Controller references show logical input/output selections. Missing I10/I11 functions and other unverified assignments are explicit. Q9–Q12 remain grouped; no individual inverter functions are invented.
- Original illustrations identify motors, brakes, sensors, limit switches, transformers, contactors and electronic assemblies. They are educational drawings, not OEM manufacturing or wiring drawings.
- CBM has clearly labelled fixed sample readings and selectable water/communication educational events linked to the supplied fault references. No machine is connected and there is no live interface.
- The existing footer-free personal-use layout and always-available databases remain. Existing theme, favourites, ordering, calculator fields, modes and unit settings persist locally. New bookmarks use the same preference record; new calculator inputs use a separate stable key namespace in the existing durable journal.

## Fault/source integrity

`app/src/main/assets/engineering.json` contains the exact supplied serial, string code, message, reset and group fields. Serial numbers **112, 113 and 114 are absent**; the 125 records retain serials 1–111 and 115–128. Codes `164`–`168` remain strings without an E prefix. Similar E/A alarm records are not merged.

The exact original field rows are independently retained in `app/src/test/resources/faults-original.tsv` and compared by Kotlin tests and `scripts/validate_engineering.py`. Added explanations, possible causes, derived location/system indexes and topic associations are separate fields. Those associations are **not verified wiring or trigger logic**. Auto/Manual classification does not grant permission to restart.

All fourteen photographs were visually inspected, with sideways pages rotated for inspection. Their readable technical transcriptions, SHA-256 hashes, cropped/uncertain readings and source IDs are in the asset and `documentation/PHOTO_TRANSCRIPTIONS.md`. Original user photographs are not embedded as app imagery. The app remains vector/Canvas-based. Original requirements are retained in `documentation/ENGINEERING_REQUIREMENTS.md`.

Read `TECHNICAL_VERIFICATION.md` before treating a source-note entry as equipment-specific advice. No OEM approval, certified safety performance, exact switching sequence, installation dimension or unspecified voltage/contact state is claimed.

## Calculators

The original 31 calculators remain unchanged except for the shared unit catalogue being extended for the new fields. Previously fixed `m/s`, `oz/ft²`, `°C/W`, `kW`, `mA`, Meg aliases and explicit-suffix precedence remain supported.

New tools cover single/three-phase power, motor current/efficiency/power factor/torque, synchronous speed/slip, frequency-to-speed with explicit slip, ideal passenger capacity, gear output speed and reverse ratio, step-chain speed from entered geometry, energy, inclined rise/projection and unit conversion.

Every new calculator starts with empty numerical fields. **Load example** explicitly supplies educational sample inputs; no physical parameter is silently assumed. Fields carry units, formulas and substitution steps. Assumptions stay alongside results. Inputs, copy/reset/share and process recreation are supported. Example geometry is hypothetical input for learning, not an OEM installation specification.

The motor example records 15 kW, 50 Hz, 415 V, 30 A, 975 rpm, PF 0.76, efficiency 91.2%, S1, frame 180, IP55 and insulation F. It also preserves the photographed delta connection and approximate 163 kg weight. The ambient-temperature value is absent and serial/efficiency-class lettering are uncertain. The app notes that rounded V/I/PF imply approximately 91.53% rather than exactly 91.2%. Six poles are an explicit inference/example, not a photographed nameplate fact.

## Build

Install JDK 17, Android SDK Platform 35 and Build Tools 35.0.0. Gradle wrapper 8.9, AGP 8.7.3 and Kotlin 2.0.31 remain pinned in the version catalogue. First-time development dependency downloads require internet; app execution does not.

Windows PowerShell:

```powershell
Set-Location 'C:\Projects\Electro'
.\gradlew.bat assembleDebug testDebugUnitTest
```

Linux/macOS:

```sh
chmod +x gradlew
./gradlew assembleDebug assembleRelease testDebugUnitTest testReleaseUnitTest lintDebug lintRelease
```

Android Studio can create local.properties with the installed SDK path. A missing SDK is an environment prerequisite, not proof of a Kotlin compile failure.

For a signed release, retain the existing private signing backup and set `ELECTROKIT_KEYSTORE`, `ELECTROKIT_STORE_PASSWORD`, `ELECTROKIT_KEY_ALIAS` and `ELECTROKIT_KEY_PASSWORD` outside the source tree. Signing variable names intentionally remain stable. The private key/passwords are never included in the source ZIP. Release builds are code/resource shrunk with R8 and non-debuggable; without those external credentials the release artifact is unsigned.

## Developer checks

```sh
python3 -m pip install -r scripts/requirements-dev.txt
python3 scripts/check_comments.py
python3 -m unittest discover -s scripts -p 'test_*.py'
python3 scripts/validate_assets.py
python3 scripts/validate_engineering.py
python3 scripts/check_contrast.py
python3 scripts/package_project.py --check
```

Python checks use unittest, not pytest. CI runs these checks and both Android test/lint variants; it was not remotely executed for this delivery. After intentional edits, regenerate source listings/tree/checksums with `python3 scripts/package_project.py`. After a palette change, use `python3 scripts/check_contrast.py --write`.

## Architecture and data formats

- `domain/`: original pure electronics calculators/parser and new EngineeringTools/EngineeringSearch.
- `data/`: original repository/DataStore/input journal; serializable engineering content models.
- `viewmodel/`: existing persistent app state and EngineeringViewModel's validated immutable asset loading.
- `ui/screens/`: existing screens plus engineering search, faults, sources, terminal references, calculators and CBM samples.
- `ui/components/`: original connector/schematic renderers plus industrial artwork and the interactive escalator cutaway.
- `assets/registry.json`, `pages.json`, `modules.json`: legacy content and stable routes.
- `assets/engineering.json`: sections, technical records, exact faults, source records and approximate drawing markers.
- `assets/engineering_tools.json`: new calculator IDs, fields/options/units, explicit examples and assumptions.

To add a reference: give it a unique stable ID, an existing section ID, a title/summary/body, source IDs, an explicit status and valid related IDs. Add a fault association only with its authority/uncertainty stated; never edit the five original fields to make an explanation look nicer. To add a tool, define its schema and explicit physical model in EngineeringTools, then add independent numeric and invalid-input tests. The data validators catch broken links and duplicate IDs.

## Offline behaviour and limits

No INTERNET permission, ads, login, analytics, billing or external service. Reference URLs are stored text for copy/share. Android share recipients may independently use a network. Cloud backup/device-to-device extraction remain disabled as before.

Only nine of the 72 legacy shared reference pages have external citations; this update does not claim to close that older gap. New user-note records identify their photograph or supplied-text authority, and generic motor/planning references identify reviewed manufacturer publications without suggesting approval of the supplied controller information.

`AUDIT_RESOLUTION_2_0_3.md` and `verification/` record executed results. Robolectric API-34 UI checks are simulated Android rendering. Physical-device/API-24 emulator, TalkBack speech, actual airplane mode and release-APK operation on a phone remain untested. `documentation/history/DEVICE_VALIDATION.md` provides the unexecuted checklist. No broad claim of zero defects or OEM certification is made.

`FULL_SOURCE.md`, `PROJECT_TREE.txt` and `SHA256SUMS.txt` include the complete distributable source and evidence. Product history is in `CHANGELOG.md`; new implementation and uncertainty details are in `TECHNICAL_VERIFICATION.md` and `documentation/history/IMPLEMENTATION_REPORT.md`.

## Visual audit update

See AUDIT_RESOLUTION_2_0_3.md for the current issue-by-issue status and verification limits. Older resolution reports are archived under documentation/history and describe their named historical releases. The source ZIP contains source and scrubbed evidence only; the APK is supplied separately. Raw source photographs accompany the archive as evidence, not app assets.

## Interface and editing guide

The app uses immersive full screen. Swipe from a screen edge to reveal Android system controls. The catalogue heading/banner scroll away, search remains pinned, and the main toolbar hides while scrolling down. Bottom navigation is compact at normal text size and expands for accessibility. Engineering starts with fault codes, calculations and source references, followed by system topics and an optional expandable diagram.

Read **DEVELOPER_GUIDE.md** for file responsibilities, the input-save pipeline, and step-by-step instructions for adding options, calculators, fault rows, reference tables and sections. Kotlin declarations carry checked KDoc and key algorithms carry explanatory comments; this is not a claim that every source line is individually commented.

Input snapshots are staged and committed on an IO worker to keep typing responsive. A sudden kill before the worker commits may lose the latest edit. Completed journal writes survive recreation; malformed JSON is preserved in a quarantine file and Retry can recover. No zero-loss guarantee is made.

Audit closure, executed command results and outstanding verification gaps are recorded in AUDIT_RESOLUTION_2_0_3.md. Dependency review is due quarterly and before SDK upgrades; upgrade pinned dependencies separately with full tests and device validation.
