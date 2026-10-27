# Editing Electro 2.0.3

This guide explains which source owns each behaviour and how to extend it. Read the KDoc above a declaration for its inputs, result and failure contract. Inline comments explain formulas, unit conventions, state ownership and tricky branches. Comments are intentionally attached to meaningful blocks rather than repeating every Kotlin statement. JSON does not support comments; its schema and provenance fields carry explanations instead.

## Open and build

1. Extract the complete archive and open the `Electro` directory in Android Studio.
2. Install JDK 17, Android SDK Platform 35 and Build Tools 35.0.0.
3. Let Android Studio create your own `local.properties`. Do not copy another developer's SDK path.
4. Run `./gradlew assembleDebug testDebugUnitTest testReleaseUnitTest lintDebug lintRelease` (Windows: `gradlew.bat`). Debug installation is for development and uses a different certificate from the personal release.
5. Use the existing private signing backup and documented environment variables for an upgrade-compatible release. Never put passwords in source, Git, screenshots or this guide.
6. Run all scripts listed in `.github/workflows/android.yml`, then `python3 scripts/package_project.py` and `python3 scripts/package_project.py --check` after final edits.

## Follow an input through the app

`CalculatorScreen` owns editable field presentation → `AppViewModel.values` immediately publishes the new raw string → a single worker stages the latest snapshot with `InputJournal` on an IO dispatcher → DataStore commits the preference record → only matching pending snapshots are removed. New typing during a write remains pending for the next iteration. The UI thread owns the pending map; the IO worker receives an immutable snapshot.

The calculator domain consumes raw fields and selected units, returning `Calculation.Value` or `Calculation.Error`. It does not open Android UI or files. A blank field is a valid editing state, not a crash. A suffixed number takes precedence over a dropdown scale, preventing double multiplication.

Asynchronous saving keeps typing responsive. An abrupt process kill before the first disk commit can lose the latest edit. AtomicFile prevents partially replaced journal files; it cannot guarantee completion during power loss. Corrupt JSON is preserved in a `.corrupt-<timestamp>` file, a visible error is raised, and Retry can continue. Ordinary filesystem errors are not treated as corrupt content.

## File map

| File or directory | Responsibility | Change here when… |
|---|---|---|
| `MainActivity.kt` | Single Activity, theme startup, immersive system bars | Changing full-screen behaviour; keep edge gestures available |
| `ui/screens/App.kt` | NavHost routes, toolbar, catalogue pager, sticky search, bottom navigation, share action | Adding a screen or changing list layout |
| `ui/screens/CalculatorScreen.kt` | Generic input controls, unit selectors, result cards, colour-code UI | Changing calculator presentation |
| `ui/screens/EngineeringScreens.kt` | Engineering dashboard, fault explorer, source library, technical details | Changing Engineering navigation and cards |
| `ui/theme/Theme.kt` | Named semantic colour tokens, Material theme | Changing palette; run contrast checker afterwards |
| `ui/components/` | Canvas diagrams, tables and original vector-style drawings | Changing technical illustrations; preserve orientation and pin numbers |
| `viewmodel/AppViewModel.kt` | Preference loading, input write queue, retry ownership | Changing persistence; never put blocking disk work in `values` |
| `viewmodel/EngineeringViewModel.kt` | Engineering asset parsing and relationship validation | Adding new content types; keep unique IDs and valid links |
| `data/Repository.kt` | Serializable registry/page models and repository boundary | Changing asset schema or preference storage |
| `data/InputJournal.kt` | Atomic pending-input backup | Changing recovery; retain compare-before-clear behaviour |
| `data/PreferenceCodec.kt` | Saved-state encoding/decoding | Changing saved schema; preserve old installations |
| `domain/Engineering.kt` | Number parsing and formatting | Changing grammar or display precision |
| `domain/UnitCatalog.kt` | Shared units, suffixes and explicit conversion rules | Adding field units; keep parser and dropdown scales consistent |
| `domain/EngineeringSearch.kt` | Matching and engineering search helpers | Changing lookup behaviour |
| `domain/NumericFilter.kt` | Typed numeric component filters | Adding parametric filters |
| `data/EngineeringContent.kt` | Serializable engineering records, source links and tool fields | Extending the engineering JSON schema |
| `data/StartupTheme.kt` | Early startup palette before DataStore loading | Preventing a wrong-colour launch frame |
| `domain/Calculators.kt` | Original calculator specifications and formulas | Adding calculator behaviour |
| `domain/EngineeringTools.kt` | Engineering formulas and validation | Adding motor, drive or mechanical tools |
| `domain/Messages.kt` | Domain result/error labels | Changing domain wording |
| `app/src/main/assets/` | Offline JSON content | Adding references, components, faults and tool definitions |
| `app/src/main/res/values/strings.xml` | Android UI strings | Changing labels; use `stringResource` in Compose |
| `app/src/test/` | Pure Kotlin tests for both variants | Testing formulas and formatters |
| `app/src/testDebug/` | Robolectric UI/persistence tests | Testing screens, rotation, search and save failures |
| `scripts/` | Content, documentation, contrast and packaging checks | Extending developer validation |
| `.github/workflows/android.yml` | Reproducible SDK setup and automated checks | Changing CI; preserve pinned actions and read-only permissions |

Paths in the first part of the table are relative to `app/src/main/java/com/electrokit/app/`.

## Change the home layout

In `MainScreen`, each catalogue is one `LazyColumn`. The introductory heading/banner is an ordinary `item`; search is a `stickyHeader`. Put future promotional or explanatory cards in ordinary items so they scroll away. Do not add a fixed-height footer or a second navigation-bar inset. The root handles display cutouts; immersive system bars appear transiently on an edge swipe. Navigation height adapts to font scale. Test both normal and 200% fonts before reducing it.

Toolbar visibility observes vertical nested-scroll gestures. It hides when browsing down and returns on an upward gesture. Search remains reachable after the introductory content scrolls away. The Engineering dashboard puts Faults, Calculations and Sources first, then groups the remaining systems; the schematic is optional and expandable.

## Add an original calculator

1. Choose a new stable ID that does not collide with an existing calculator or module. Never rename an existing ID to change its display title.
2. Add a specification in `Calculators.kt`: field keys, labels, SI units, defaults and modes. Follow a neighbouring calculator's exact model constructor.
3. Add the calculation branch and a formula comment with units and assumptions. Use the shared parser and explicit finite/range validation. Return a specific editing error for missing/invalid input.
4. Add its registry entry with `id`, `title`, `icon`, `tab`, `screenType` and any existing required fields. Use the schema declared in `Repository.kt`, not guessed JSON keys.
5. Add a vector icon or reuse the relevant original component renderer with a deliberate icon key.
6. Add known-answer, boundary and blank-input tests. Extend the extreme-input grid and validator expected counts intentionally; a count change must correspond to actual new content.
7. Confirm its row opens, values survive rotation, reset works, and share/copy contain the displayed result.

## Add an Engineering tool or section

Tool fields live in `engineering_tools.json`; executable calculations live in `EngineeringTools.calculate`. Add a stable tool ID to both, use the existing field schema and include formula/assumptions. Add tests with independent expected values. A new section needs a unique ID in `engineering.json`, records that refer to that section, and a real navigation route in `EngineeringScreen`. Supply a specific dashboard description in strings.xml. Ensure search reaches the new record and links resolve.

## Add or correct a fault record

Keep the original serial, code, message, reset type and group exactly as supplied. Append new verified records with unique codes; runtime validation no longer assumes exactly 125. The validator deliberately checks the delivered source dataset, so update its fixture only when new authentic source rows are available. Include source IDs, certainty and related-record IDs. Do not infer a reset procedure, PLC logic, terminal number or safe bypass from an error label. Shared generic checks are labelled reference context, not an OEM diagnosis. Distinct diagnostic instructions require the applicable manufacturer's documentation.

## Add a reference page, diagram or table

Use an existing asset entry of the same type as a schema example. Every row must have the declared number of cells. Keep connector view direction and male/female captions explicit. Number contacts from the authoritative drawing. Supply source/provenance and assumptions. Colour is redundant information: always retain the printed colour name and signal label. Reference aliases should point to the same `contentId`, avoiding duplicated pages. Run asset validation and view the result in both themes and large text.

## Change comments and documentation

Every Kotlin type, function, composable, member and constructor property is checked by `scripts/check_comments.py`; function KDoc must name each parameter and describe its return/error contract. Temporary local values are explained through surrounding block comments, not an individual KDoc for every variable. Add a comment before non-obvious unit conversion, rounding, parser regex, formula, asynchronous state change or error recovery. Python scripts use module docstrings and function comments. Gradle and workflow comments describe each build phase.

Do not write comments that merely restate an identifier. For example, explain why a newer pending input must not be cleared after an older write, rather than saying “remove input.” Update the comment when changing the behaviour. JSON cannot contain `//` comments; document schema changes here and in the data models.

## Release review and remaining validation

Check dependencies quarterly and before changing target SDK. Upgrade Android/Compose/Kotlin together in a separate branch, run the complete regression suite, then check the shrunk APK on a device. Pinned versions currently provide a reproducible baseline; lint freshness warnings are not suppressed.

Before relying on a new release, test API 24 and a recent Android version: install/update with the same certificate, full-screen gestures, light/dark theme, 200% text, rotation, keyboard, copy/share, process recreation, TalkBack and airplane mode. JVM/Robolectric tests do not certify the R8-shrunk APK on a phone. See AUDIT_RESOLUTION_2_0_3.md for exact executed checks and unresolved evidence gaps.
