# Updating ELECTRO

1. Fetch current source, preserve local changes and create a feature branch.
2. Modify the owning source/data files and add regression coverage.
3. Increase `versionCode` and update `versionName` in `app/build.gradle.kts`; current development version is 2.1.0/code 13.
4. Update `CHANGELOG.md`, including migration implications.
5. Run Python validators and Android tests/lint/debug build. Inspect actual APK metadata and reports.
6. Regenerate packaging/checksums and commit complete directories, including the wrapper JAR/catalog.
7. Open/review a pull request and require successful CI before merge.
8. Configure the original release keystore securely; build/test/lint release and verify with `apksigner`.
9. Check application ID, increasing versionCode and original signing certificate. Test an upgrade over the prior release with representative saved data.
10. Tag the reviewed commit, publish only the verified signed APK, download it and install over the compatible existing app.

Data continuity requires `com.electrokit.app`, a compatible original signing certificate and stable datastore/journal keys. A version name alone does not establish compatibility. Debug keys generally cannot update release-signed installations. The original 1.0.0 debug certificate may also differ.

Do not uninstall a valued installation just to bypass a certificate mismatch: uninstalling clears private data. Retain secure backups of the original key. No original release key was supplied in this task, so production signing and real upgrade compatibility remain untested.
