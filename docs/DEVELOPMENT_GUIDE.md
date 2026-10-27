# Editing ELECTRO

Kotlin paths below start at `app/src/main/java/com/electrokit/app/`.

| Change | Actual owner and procedure |
|---|---|
| App name | `res/values/strings.xml` (`app_name`) and the visible brand in `ui/screens/WorkbenchScreen.kt`; keep applicationId for compatible updates |
| Launcher icon | `res/drawable/ic_launcher.xml`, `ic_launcher_monochrome.xml`, and `res/mipmap-anydpi*/ic_launcher.xml` wrappers |
| Home/navigation | `ui/screens/WorkbenchScreen.kt`, `MainScreen`; retain original route ownership in `ui/screens/App.kt` |
| Theme | `ui/theme/Theme.kt`; run `python scripts/check_contrast.py --write` and review light/dark/large-text previews |
| Legacy calculator | `domain/Calculators.kt`, `assets/registry.json` and `ui/screens/CalculatorScreen.kt`; retain old IDs |
| New utility | Add metadata and model to `domain/WorkbenchTools.kt`; `UtilityScreen.kt` supplies input, calculate/reset/save/copy controls |
| Formula | Modify domain code, state units and assumptions, add independent numeric and invalid-domain tests |
| Component image | Add descriptive PNG to `app/src/main/assets/components/` and an `id/name/category/image` entry to `components.json`; reuse identical assets |
| Connection drawing | Extend `ui/components/` or original `assets/pages.json`; specify connector variant, contact-number view and original source |
| Learning content | `data/ComponentLibrary.kt`; add citations and explicit limits. Meter illustration: `ui/components/MultimeterLesson.kt` |
| Fault records | `assets/engineering.json`; preserve original serial/code/message/reset/group fields unless an authoritative correction is supplied |
| Language | Extract new workbench Kotlin text into `res/values/strings.xml`, then add translated resource directories. Only English is currently implemented |
| Version | `app/build.gradle.kts`, `versionCode`/`versionName`; increase code monotonically |

For example, a new transformer-like utility needs explicit fields/units, a formula and limitations in `WorkbenchTools.all`, a calculation branch in `calculate`, and independent tests in `app/src/test/java/com/electrokit/app/domain/`. Do not duplicate a new database or replace an original calculator merely to restyle it.

Document APIs with meaningful KDoc and run the syntax-tree gate. Add regression coverage before fixing a confirmed bug. Preserve datastore identity, filenames, preference keys and route IDs unless implementing a tested migration.

After intentional edits run `python scripts/package_project.py`, then `--check`. Git internals, virtual environments, generated builds and private keys must stay out of source listings. Binary image bytes are covered by hashes without Base64 duplication in the listing.

Use a feature branch and review `git diff --check`, tests and APK metadata before committing. Never replace the production signing key with a newly generated development key.
