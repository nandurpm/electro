# ELECTRO

**Electrical • Electronics • Offline Reference**

Offline-first native Android electronics toolkit. Version **4.2.0**, code **23**, application ID **`com.electrokit.app`**, Android 7.0/API 24+.

## Features

- Material 3 dark-blue, light and system appearance; persistent header/search and visible Android system bars.
- Interactive multimeter training: seven exercises with diagrams, test steps, examples, common mistakes and safety precautions.
- In-app GitHub release checks: patch releases offer Update or Later; a higher feature version requires Update to Continue after a verified check. From 4.3.2, APKs download privately inside ELECTRO with progress and signature verification before Android’s install approval; no browser redirect. Users of 4.2.0 must install 4.3.0 manually, as older APKs have no update checker.
- INTERNET permission is used only for update checks and selected official APK downloads; REQUEST_INSTALL_PACKAGES is used for the user-initiated Android install screen (sideload distribution; this permission is restricted on Google Play); all learning, calculations and saved settings work locally.
- Home, Tools, Learn, Favorites and Profile, with recent public tools, bookmarks, saved calculation snapshots and retained raw inputs.
- All original **31 calculators, 55 pinout entries, 21 resources and three Parts/AVR/PIC databases** remain reachable.
- Seven additional utilities: ideal transformer, battery/UPS runtime, explicit-resistivity DC cable voltage-drop sizing, single-phase power factor, energy/cost, temperature and bounded scientific arithmetic.
- Eight-dimensional unit converter with explicit source/destination units.
- **98 image-backed component/diagram entries**, locally bundled with sampled off-thread decoding and an 8 MiB decoded cache. Generated artwork is illustrative, not manufacturer wiring authority.
- Multimeter, T568A/B, RS-485, RS-232, Modbus, CAN and interface lessons. The meter includes an interactive native drawing with mode/jack explanations.

Exact manufacturer ratings, terminal drawings and part-specific test procedures have not been supplied for every image. Pages identify those gaps. Cable estimates do not select code-compliant ampacity/protection. New external reference downloads were blocked by this environment's network policy; citations are provided for review and are not represented as freshly verified equipment documentation.

## Installation

For the production release, download `ELECTRO-4.0.0.apk` from [GitHub Releases](https://github.com/nandurpm/electro/releases) after the signed Android Release workflow succeeds. For development, build `app/build/outputs/apk/debug/app-debug.apk` or extract the debug artifact from GitHub Actions. Android may ask you to allow installation from your browser or Files app.

A debug certificate generally cannot update a release-signed installation. Preserve the same application ID, original release key and increasing versionCode for data-preserving updates. Do not uninstall a valued installation merely to bypass a certificate mismatch.

## Windows development — PowerShell

Install JDK 17 and Android SDK command-line tools or Android Studio's SDK Manager. Set `JAVA_HOME` to your actual JDK root and `ANDROID_HOME` to your actual SDK root.

```powershell
git clone https://github.com/nandurpm/electro.git
Set-Location electro
git switch -c feature/my-change
java -version
& "$env:ANDROID_HOME\cmdline-tools\latest\bin\sdkmanager.bat" --licenses
& "$env:ANDROID_HOME\cmdline-tools\latest\bin\sdkmanager.bat" 'platforms;android-35' 'build-tools;35.0.0' 'platform-tools'
& "$env:ANDROID_HOME\cmdline-tools\latest\bin\sdkmanager.bat" --list_installed
.\gradlew.bat --version
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
Get-Item .\app\build\outputs\apk\debug\app-debug.apk
```

Replace `latest` with your actual versioned command-line tools directory when needed. For SDK discovery, use `ANDROID_HOME` or an untracked `local.properties` with `sdk.dir=C:/Users/YOUR_NAME/AppData/Local/Android/Sdk`; never commit machine paths.

## Linux/macOS and cloud

```bash
cd electro
./gradlew --version
./gradlew --no-daemon --max-workers=4 testDebugUnitTest lintDebug assembleDebug
ls -lh app/build/outputs/apk/debug/app-debug.apk
```

In this cloud workspace first run `source /workspace/toolchains/activate-electro.sh`. The saved environment Install script prepares JDK/SDK and writable caches. Gradle **8.9**, AGP **8.7.3**, Kotlin **2.0.21**, JDK **17**, SDK **35** and Build Tools **35.0.0** are the pinned toolchain. Kotlin needs no separate installation. First-time dependency downloads require network access; the app has no Internet permission. The wrapper distribution SHA-256 is pinned; TLS remains enabled.

## Developer checks

```bash
python3 -m venv .venv
. .venv/bin/activate
python -m pip install -r scripts/requirements-dev.txt
python scripts/check_comments.py
python -m unittest discover -s scripts -p 'test_*.py'
python scripts/validate_assets.py
python scripts/validate_library.py
python scripts/check_contrast.py
python scripts/package_project.py --check
python scripts/check_verification.py
```

PowerShell venv activation: `.\.venv\Scripts\Activate.ps1`. After intentional edits, regenerate source listings/hashes with `python scripts/package_project.py`; after palette changes also run `python scripts/check_contrast.py --write`.

## Actual source paths

All Kotlin paths below are under `app/src/main/java/com/electrokit/app/`.

| Area | Path |
|---|---|
| Version, SDK, application ID, signing | `app/build.gradle.kts` (repository root) |
| Dependency pins | `gradle/libs.versions.toml` (repository root) |
| Activity and system bars | `MainActivity.kt` |
| Dashboard/navigation/library/settings | `ui/screens/WorkbenchScreen.kt` |
| Original route host, references and databases | `ui/screens/App.kt` |
| Design tokens | `ui/theme/Theme.kt` |
| Original calculator UI / engine | `ui/screens/CalculatorScreen.kt`, `domain/Calculators.kt` |
| Public utility UI / models | `ui/screens/UtilityScreen.kt`, `domain/WorkbenchTools.kt` |
| Converter UI / models | `ui/screens/UnitConverterScreen.kt`, `domain/UnitConversions.kt` |
| Learning text / meter drawing | `data/ComponentLibrary.kt`, `ui/components/MultimeterLesson.kt` |
| Preference schema / mutation | `data/Repository.kt`, `viewmodel/AppViewModel.kt` |

Images/metadata: `app/src/main/assets/components/` and `components.json`. General references: `registry.json`, `pages.json` and `modules.json`. Android strings/icons: `app/src/main/res/`.

The missing nested project files were restored from the existing `FULL_SOURCE.md` and verified byte-for-byte against the original SHA-256 manifest, including its Base64 wrapper JAR. Verification files are generated from each current APK, tests and lint reports and published as build artifacts; they are not committed. `python scripts/check_verification.py` rejects stale versions; signed publication additionally requires the active, user-authorized release certificate. See [release status](docs/RELEASE_3_0.md). The older 2.1.0 build report is explicitly historical.

## Maintenance guides

- [Architecture](docs/ARCHITECTURE.md)
- [Editing and development](docs/DEVELOPMENT_GUIDE.md)
- [PowerShell/Bash commands](docs/COMMANDS.md)
- [Updating and compatibility](docs/UPDATE_GUIDE.md)
- [Releases and signing](docs/GITHUB_RELEASE_GUIDE.md)
- [Phone-only workflow](docs/ANDROID_PHONE_BUILD_GUIDE.md)
- [Testing](docs/TESTING_GUIDE.md)
- [Audit and limitations](docs/AUDIT_REPORT.md)

Release signing retains `ELECTROKIT_KEYSTORE`, `ELECTROKIT_STORE_PASSWORD`, `ELECTROKIT_KEY_ALIAS` and `ELECTROKIT_KEY_PASSWORD`. Keep the new 3.0.2 private key and all values outside the repository. A release is signed only when valid credentials are supplied.

ELECTRO 3.0.2 uses a newly authorized signing key. It cannot update older-key installations; uninstalling those installations removes their local data. Keep the separate private signing backup for future updates.

Repository maintenance: runtime artwork lives only in `app/src/main/assets/components/`. Duplicate photo packs, unused catalogue previews and obsolete historical reports were removed; Git history retains earlier copies. See [artwork provenance](docs/ARTWORK_PROVENANCE.md).

Build evidence, the combined source listing and SHA-256 manifest are generated for CI/release archives and are no longer committed. CI builds and tests both debug and release before generating fresh verification. Run `python scripts/package_project.py` locally to regenerate the source listing, or use `--archive PATH.zip` to package it.
