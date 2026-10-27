# ELECTRO 3.0 release preparation

Application name: **ELECTRO**. Version name: **3.0**. Version code: **14**. Application ID remains `com.electrokit.app`.

The local optimized, non-debuggable release build passed `testReleaseUnitTest lintRelease assembleRelease`: **80 tests, 0 failures/errors/skips**, and no lint errors. R8 code/resource shrinking completed. Android SDK metadata confirms the requested name/version and SDK 24/35 bounds. Current output is `app/build/outputs/apk/release/app-release-unsigned.apk`; it is **unsigned**, not an installable production release.

The user selected the original signing identity. No replacement key has been generated. The immutable original public certificate record in `signing/original-release-certificate.sha256` has SHA-256:

`68a284d34b73344e4797a09137c06d330797f33a2c9d288580ae2749aa58bbaa`

No original keystore or signing-password variables are configured in this workspace. GitHub denied access to repository signing-secret metadata. The release workflow can check/use original secrets in GitHub without disclosing the private key locally, and it now rejects any signed APK whose certificate differs from this original record.

Required repository Actions secrets: `ELECTROKIT_KEYSTORE_BASE64`, `ELECTROKIT_STORE_PASSWORD`, `ELECTROKIT_KEY_ALIAS`, `ELECTROKIT_KEY_PASSWORD`. Configure the **original** values securely under repository Settings → Secrets and variables → Actions. Never paste passwords into chat or commit a keystore. A certificate fingerprint or an old APK cannot recreate its private signing key.

The manual signing run [40477419087](https://github.com/nandurpm/electro/actions/runs/40477419087), against application-code commit `a571723`, confirmed that **all four secrets are missing**. Its early signing-prerequisite check failed, and signing/upload/publication steps were skipped. No signed 3.0 APK, final release tag or GitHub Release was created. After the original secrets are configured, rerun Android Release on `release/electro-3.0`; verify the original certificate match before publishing `v3.0`.

The previous hosted release run failed inside the third-party Android setup action before signing. Workflows now pin Ubuntu 24.04 and use its documented preinstalled SDK, with explicit required-package checks. Signing prerequisites are checked early to avoid an expensive build when credentials are absent. Hosted execution is reported separately from successful local compilation.

When signed verification succeeds, the workflow packages `ELECTRO-3.0.apk` and the matching `ELECTRO-3.0-source.zip`. Manual execution uploads artifacts; a matching `v3.0` tag publishes both. Do not create the final release tag until the original signing identity has been verified.

The earlier `docs/BUILD_REPORT.md` records the 2.1.0 baseline's 120 debug/JVM/Robolectric tests; these are historical results, not a new 3.0 debug run. Device installation, production upgrade behavior and TalkBack remain unrun. See the audit report for existing engineering-content limitations.

## Verification/reset review correction

Review confirmed that the old committed verification reports still described 2.0.3. All current verification files are regenerated for 3.0 after the reviewed build; unrun external references and unsigned signing status are explicit. New regression fixtures reject old summary/badging versions and unsigned publication. The original certificate fingerprint is independent of regenerated APK signatures.

Band-calculator Reset now clears the cached `bandResult` synchronously, before the new band effect runs. A UI regression covers both resistor and inductor reset/display/copy paths. Dependency and API-level lint warnings remain documented; dependency pins and application functionality were preserved.

## Reviewed 3.0 results

After the reset/evidence corrections, `testDebugUnitTest testReleaseUnitTest lintDebug lintRelease assembleRelease` passed: **121 debug/UI tests and 80 release tests**, with no failures/errors/skips and no lint errors. Eight Python tests passed. Every file under `verification/` was regenerated from the actual 3.0/code-14 unsigned release APK and current reports. The original certificate fingerprint was preserved in the separate signing identity file.

The unsigned artifact is explicitly marked `release_publishable: false`. No signed APK or final release is claimed while the original signing secrets remain missing. The version guard rejects the stale 2.0.3/12 evidence described in review, and `--require-signed` rejects the current unsigned state. Original dependency/API warnings remain warnings; no dependency upgrades were needed for these corrections.
