# Testing

From the repository root, with the toolchain and Python requirements installed:

```bash
python scripts/check_comments.py
python -m unittest discover -s scripts -p 'test_*.py'
python scripts/validate_assets.py
python scripts/validate_engineering.py
python scripts/validate_library.py
python scripts/check_contrast.py
python scripts/package_project.py --check
./gradlew testDebugUnitTest lintDebug assembleDebug
```

PowerShell uses `.\gradlew.bat`. XML: `app/build/test-results/testDebugUnitTest/`. Test HTML: `app/build/reports/tests/testDebugUnitTest/`. Lint: `app/build/reports/lint-results-debug.html` and `.xml`. The current `verification/` snapshot is regenerated from actual APK/test/lint outputs. Run `python scripts/check_verification.py` to reject stale versions; use `--apk PATH --require-signed` before production publication.

Coverage includes original calculation/unit/formatting/fault contracts, all 107 legacy calculator/reference routes, database filters, persistence failures/recovery, new utility examples and invalid domains, SI/temperature conversion, PIN verification and UI session/search restrictions, component image/favorite navigation, large text and simulated screenshots. New suites are `WorkbenchToolsTests`, `UnitConversionsTests`, `LocalPinTests` and `WorkbenchUiTests`.

Robolectric is simulated Android API 34. `app/build/design-previews/` contains current simulated renders when the screenshot tests execute. These are not physical-device or API-24 emulator results.

## Unrun device checklist

- Install/launch on API 24 and current Android; test portrait/landscape, cutouts, gesture/three-button navigation and dark/light/system modes.
- Verify visible status bar, persistent header/search, TalkBack order and large-font/touch behavior.
- Open every destination; repeatedly navigate images and observe memory/performance.
- Try wrong/right PIN, manual lock, rotation, fresh Activity/process launch, recents previews and every protected route/favorite.
- Verify reference calculations, copy/save/share, restart and stored inputs/bookmarks/history.
- Test in actual airplane mode and inspect permissions.
- With the original key, upgrade from the previous APK and verify saved data.

Record device, Android version, commit/APK hash, steps and observed results. Keep passed, failed, skipped and unrun outcomes separate. Equipment-specific reference review remains separate from automated schema tests.

## Refresh all release evidence

After a successful build (preserve the command exit status), run:

```bash
python scripts/record_verification.py --apk app/build/outputs/apk/release/app-release-unsigned.apk --build-log /workspace/toolchains/electro-3.0-reviewed.log --variants debug release
python scripts/check_verification.py --apk app/build/outputs/apk/release/app-release-unsigned.apk
python scripts/package_project.py --archive /workspace/artifacts/ELECTRO-3.0-source.zip
```

Replace paths with actual current logs/APKs. Production uses `app-release.apk` and `--require-signed`; unsigned preparation never passes that gate. The recorder replaces stale XML/HTML/text and explicitly marks unrun external checks instead of carrying forward old success claims. Original certificate identity stays in `signing/original-release-certificate.sha256`.
