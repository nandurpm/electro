# Electro 2.0.3 — screenshot and two-audit resolution

Version code 12; application ID remains `com.electrokit.app`. This report responds to both attached audits and screenshot 5082.png. The supplied technical data and original fault fields remain unchanged. Status is specific to each finding; this is not a claim that every audit concern is closed.

## User interface and documentation

| Request | Change | Files |
|---|---|---|
| Bottom blank area | Removed the independent root navigation inset and shortened normal-size navigation to 64dp. No reserved advertising/footer area. Large text retains an expanded bar. | MainActivity.kt, App.kt |
| Full screen / top strip | Immersive system bars with transient edge-swipe access. Removed the hard-coded green status spacer. Display cutout protection is retained. | MainActivity.kt, App.kt |
| Only search should remain fixed | Catalogue title and banner are lazy-list items. Search is a sticky header. Main toolbar hides on downward scrolling and returns upward. | App.kt |
| Engineering hard to understand | Prioritised fault codes, calculations and source references; added specific section descriptions; moved the dense schematic behind an expandable control; outlined cards with clear navigation markers. | EngineeringScreens.kt, strings.xml |
| Explain code and modification | KDoc declaration coverage retained and extended; persistence, layout, regenerative-slip and recovery comments updated; new developer guide covers file responsibilities, execution flow, adding options/calculators/pages/faults and verification. JSON remains valid JSON, so schema explanations live in models and the guide. This does not claim every individual line has a comment. | DEVELOPER_GUIDE.md, changed Kotlin, Gradle/workflow and scripts |

## First audit: Electro 2.0.2 Audit

| Finding | Resolution |
|---|---|
| 1: Disk I/O on main thread | Fixed the per-keystroke path: UI immediately updates in-memory state; a serialized background worker stages and commits snapshots using the IO dispatcher. No keystroke cancels an in-progress write. Newer edits survive older commits. The journal still verifies durable writes off-main. Malformed JSON is preserved in a timestamped quarantine and Retry can recover. Trade-off: a kill before the worker finishes can lose the latest edit; no zero-loss claim. |
| 2: Release UI never device-tested | Remains open. Signed, shrunk release assembly and JVM tests are rerun, but neither substitutes for running that APK on an API-24 and recent physical/emulated device. This environment exposes neither emulator binary nor /dev/kvm. |
| 3: Fault guidance mostly templates | Content limitation retained explicitly. The 125 exact fault rows do not include verified detailed OEM diagnosis. Inventing unique causes, wiring or reset instructions would be inappropriate. Source links and authority labels remain accessible; richer diagnosis needs verified manufacturer documentation. |
| 4: Super-synchronous slip rejected | Fixed. Nonnegative rotor speed above synchronous speed returns negative slip; inverse frequency/speed calculation accepts negative slip. Added regression for 50Hz, six poles, 1025rpm → −2.5%. |
| 5: Exactly 125 faults required at runtime | Fixed. Runtime requires nonempty fault data and unique codes, not a particular count. Dataset-integrity tests intentionally retain the count of the currently supplied 125 rows. |
| 6: Inconsistent exception handling | Engineering tools now catch unexpected exceptions as a safe numerical error and rethrow cancellation. Calculator input failures use deliberate argument exceptions; unexpected failures no longer expose raw exception text. Settings/input successes clear only their own error category. Share handles a missing Android activity with a readable message. |
| 7: Backup/migration | Unchanged: automatic cloud backup and device-transfer remain disabled for the personal offline dataset. Migration/export has not been added. |
| 7: Citation coverage | Remains 9/72 legacy pages; no unverified source URLs added. |
| 7: English only | Retained; localisation was not requested. UI labels are resource-backed; domain messages remain centralised. |
| 7: Dependency age | Pinned versions retained for compatibility; quarterly review and full upgrade validation are documented. No claim of latest dependencies. |
| 7: FULL_SOURCE duplication | Retained because the original requirement explicitly asks for every file/full source; checksum generation guards drift. |
| 7: Unsigned release without credentials | Existing behaviour remains documented: local/CI builds without private signing variables are unsigned. The separately delivered APK is signed and independently checked with apksigner. Credentials never enter the ZIP. |

## Second audit: ELECTRO_AUDIT_REPORT

| ID | Resolution |
|---|---|
| A1 Android build unavailable to auditor | Fresh local SDK-backed assembly, tests and lint are recorded below. SDK setup and prerequisites are documented. The audit sandbox's missing SDK is not an application defect. Remote CI is not claimed. |
| A2 APK/source identity | APK supplied separately, with a release manifest containing APK SHA-256, source ZIP SHA-256 and certificate SHA-256. Signature is verified with SDK apksigner; packaged JSON assets are byte-compared to source. The APK hash is not reproducibility proof by itself. |
| A3 CI SDK installation | Added explicit sdkmanager installation of Platform 35, Build Tools 35.0.0 and platform-tools, license handling and early tool presence checks. |
| A4 Dependency warnings | Open maintenance item with documented review cadence; compatibility baseline retained and warnings reported. |
| A5 Device/accessibility testing | Open: physical phone, standalone emulator, API24, real TalkBack speech, actual airplane mode, update installation and R8-on-device behaviour remain untested. Robolectric rendering is labelled simulation. |

## Commands and observed results

{
  "version": "2.0.3",
  "version_code": 12,
  "tests": {
    "debug": {
      "tests": 100,
      "failures": 0,
      "errors": 0,
      "skipped": 0
    },
    "release": {
      "tests": 68,
      "failures": 0,
      "errors": 0,
      "skipped": 0
    }
  },
  "lint": {
    "debug": {
      "errors": 0,
      "warnings": 10,
      "warning_ids": {
        "UnusedAttribute": 1,
        "AndroidGradlePluginVersion": 2,
        "GradleDependency": 7
      }
    },
    "release": {
      "errors": 0,
      "warnings": 5,
      "warning_ids": {
        "UnusedAttribute": 1,
        "GradleDependency": 4
      }
    }
  },
  "physical_device_tested": false,
  "emulator_tested": false,
  "remote_ci_executed": false,
  "apk_sha256": "4b73cf6fdda7b9b988f270383780bba7c991cb12cb4bc277c2b3d846348621c4",
  "apk_bytes": 1751157,
  "assets_match_source": true,
  "internet_permission": false,
  "faults_unchanged_from_2_0_1": true,
  "source_records_unchanged": true,
  "domain_changes": [
    "Engineering.kt",
    "Calculators.kt",
    "EngineeringTools.kt"
  ],
  "photo_hashes_match": true
}

Python gates: `check_comments.py`, unittest discovery, `validate_assets.py`, `validate_engineering.py`, `check_contrast.py`, `package_project.py --check`. Android gates: `assembleDebug assembleRelease testDebugUnitTest testReleaseUnitTest lintDebug lintRelease` through the committed wrapper, JDK17 and SDK35. Raw results are under `verification/`.

## Implementation references

- Android immersive bars: https://developer.android.com/develop/ui/views/layout/immersive
- Kotlin IO dispatcher: https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-dispatchers/-i-o.html

These describe implementation behaviour, not validation of the engineering reference data.
