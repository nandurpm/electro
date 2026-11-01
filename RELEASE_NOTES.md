# ELECTRO 4.5.4

- Keep every failed settings change in order for Retry; input/history limits no longer erase storage errors.
- Remember Later for the exact optional patch across restarts; explicit Check now can show it again. Major/feature updates remain required.
- Use lifecycle-owned update state and background preference IO; improve release-note formatting, translations, rate-limit feedback and dialog actions.
- Match the status inset to the navy toolbar, remove deprecated window color setters, and add theme-aware startup colors with Android 12+ per-app night-mode support.
- Improve button/copy/expansion accessibility, resource UI labels and define the complete text scale.
- Smoke test the minified APK offline on Android 15 in CI at compact width and enlarged text, using an ephemeral test key only.
- Use a timing-safe webhook-secret digest comparison. Deployment remains a separate coordinated administrator action.
- Preserve package name, release-signing configuration and browser-only update downloads. No native installer is added.
