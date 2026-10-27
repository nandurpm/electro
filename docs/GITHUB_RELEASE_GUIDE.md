# Releases and signing

Android CI validates pull requests, main updates and manual runs, and uploads a **debug** APK plus diagnostic reports. These are development artifacts.

Release automation runs manually or on `v*` tags and requires repository Actions secrets:

| Name | Meaning |
|---|---|
| `ELECTROKIT_KEYSTORE_BASE64` | Base64 of the original private signing keystore |
| `ELECTROKIT_STORE_PASSWORD` | Original keystore password |
| `ELECTROKIT_KEY_ALIAS` | Signing alias |
| `ELECTROKIT_KEY_PASSWORD` | Private key password |

Enter values through GitHub Settings → Secrets and variables → Actions. The workflow decodes the key only into the runner's temporary directory, passes its path as `ELECTROKIT_KEYSTORE`, builds/tests/lints release and verifies the signed APK. Missing secrets fail explicitly. No unsigned APK is published as production-ready. GitHub's scoped `GITHUB_TOKEN` supplies release permission; no personal token belongs in source.

Manual runs upload a signed artifact without creating a public Release. A version tag can publish `app-release.apk` after successful validation. The tag must match `defaultConfig.versionName`. Review code, CI and update compatibility before pushing a release tag.

From the repository root with secure signing variables already configured:

```bash
./gradlew testReleaseUnitTest lintRelease assembleRelease
"$ANDROID_HOME/build-tools/35.0.0/apksigner" verify --verbose --print-certs app/build/outputs/apk/release/app-release.apk
"$ANDROID_HOME/build-tools/35.0.0/aapt" dump badging app/build/outputs/apk/release/app-release.apk
```

PowerShell uses `.\gradlew.bat` and `& "$env:ANDROID_HOME\build-tools\35.0.0\apksigner.bat" ...`. The underlying app can produce an unsigned release without credentials; do not distribute it as signed production output.

After approving the exact release commit:

```bash
git tag -a v2.1.0 -m 'ELECTRO 2.1.0'
git push origin v2.1.0
```

These Git commands also work in PowerShell. Tags trigger release automation and are not ordinary test steps. This task does not imply a tag or GitHub Release has been published.
