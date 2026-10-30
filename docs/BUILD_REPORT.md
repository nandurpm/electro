# Historical 2.1.0 build and test report

This document retains the earlier 2.1.0 baseline. For current 3.0 evidence, use `verification/summary.json` and `docs/RELEASE_3_0.md`.

Date: 2026-10-27. Application-code checkpoint: `3f90b95` on `feature/electro-redesign`. Documentation/packaging commits may follow without changing the application binary.

## Actual results

| Check | Observed result |
|---|---|
| `./gradlew --no-daemon --max-workers=4 testDebugUnitTest assembleDebug lintDebug` | BUILD SUCCESSFUL |
| Android/JVM/Robolectric tests | 120 executed, 0 failures, 0 errors, 0 skipped |
| Python unittest | 4 passed |
| KDoc syntax-tree gate | 894 declarations documented; all five density thresholds passed |
| Original asset validation | 31 calculators, 55 pinouts, 21 resources and 3 databases passed |
| Engineering validation | 125 exact faults, 245 technical records, 14 source transcriptions and 15 tools passed |
| Component library | 114 linked unique PNGs; paths, dimensions and actual PNG integrity checked |
| Contrast | All 17 selected source-derived text/container pairs passed 4.5:1 |
| Debug lint | 0 errors, 14 warnings (unused resources/attribute) |
| Workflow configuration | Both YAML files parsed locally; hosted Actions execution not performed |
| Wrapper/setup | Gradle 8.9 and wrapper checksum verified; saved installation steps rerun successfully |
| Signature | APK verifies; Android Debug certificate, v2 scheme |

## APK

Actual build output: `/workspace/electro/app/build/outputs/apk/debug/app-debug.apk`.

Convenience copy: `/workspace/artifacts/ELECTRO-2.1.0-debug.apk`.

- Application ID: `com.electrokit.app`
- Version: `2.1.0` / code `13`
- Minimum SDK: 24; target/compile SDK: 35
- Size: 20,406,441 bytes
- SHA-256: `25166ab4653b6fb93ca8dcaecdadb3e50e7ed1d255bf4b0bb8d50d8aa430b605`
- No Internet permission. The AndroidX internal non-exported-receiver permission is present.

Toolchain: Temurin 17.0.17+10, SDK Platform 35, Build Tools 35.0.0, Gradle 8.9, AGP 8.7.3 and project Kotlin 2.0.21. Gradle's own embedded Kotlin version is separate from the app compiler plugin.

Current HTML/XML reports are under `app/build/reports/` and `app/build/test-results/testDebugUnitTest/`. Simulated UI renders are under `app/build/design-previews/`; `electro-home-redesign.png` was visually inspected. Historical root `verification/` files are not fresh results.

## Limits and remaining external work

No original release signing variables were present, so no production-signed APK was built. The debug certificate does not prove upgrade compatibility with installed release builds. No physical device, API-24 emulator, TalkBack speech, real airplane-mode or actual upgrade-installation test ran. Robolectric UI tests use simulated API 34; API-24 cryptographic compatibility uses the platform-supported PBKDF2-HMAC-SHA1 verifier, not the API-26-only SHA-256 factory name.

TI and Modbus technical-reference retrieval returned network-policy 403 responses. The draft adds `www.ti.com` and `www.modbus.org` for review; saving that draft does not activate access. Manufacturer-specific ratings/wiring, complete localization and standards-based cable/protection selection are not claimed.

The cloud Install script and Start skill were saved and rerun in the current instance; no newly restored cloud task was independently validated. Review/save environment settings and publish to retain the prepared environment. Local commits and source archives remain the concrete source handoff; GitHub publication is reported separately.
