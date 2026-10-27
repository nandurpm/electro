# Verification status

Version 3.0 evidence in verification/ was generated from an UNSIGNED build (apk_sha256 5da3aa39…). The separately delivered signed APK (sha256 1371076a…) is signed with certificate D9823B34…1A9070, which is NOT the recorded original identity 68A284D3…58BBAA. Treat 3.0.1 source changes as unbuilt and untested until `./gradlew testDebugUnitTest lintDebug` and a release build with the original keystore pass.
