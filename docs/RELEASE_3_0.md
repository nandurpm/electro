# ELECTRO current build and signing status

Current source: ELECTRO 3.0.1, versionCode 15, application ID com.electrokit.app.

The reference-driven UI revision uses the existing Compose architecture and bundled art. Its current local release build is unsigned. The existing GitHub 3.0.1 APK predates this UI revision; do not present that APK as the redesigned app.

The active public certificate in `signing/release-certificate.sha256` is verified against the published `electro-3.0.1` APK:

`d9823b341c6951b6dc912230fd46f2d61d7b3c315de2ae31fbb112345d1a9070`

The original certificate remains in `signing/original-release-certificate.sha256` as a historical record. The locally generated replacement key also differs from the published APK. Neither is accepted by the release workflow. Compatible release signing requires the published APK's private key and an increased versionCode before the next publication.

Configure ELECTROKIT_KEYSTORE_BASE64, ELECTROKIT_STORE_PASSWORD, ELECTROKIT_KEY_ALIAS and ELECTROKIT_KEY_PASSWORD in GitHub Actions using that published key. Private credentials must remain outside source archives. The fingerprint cannot recreate a lost key.

Every file under verification is regenerated from the current local artifact and reports, including explicit unsigned status. Physical-device checks and external reference validation are not claimed. The historical docs/BUILD_REPORT.md describes the earlier baseline.
