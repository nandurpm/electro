# Security administration

## Protected signing: required before merging the release-workflow migration

The companion infrastructure migration (not active in the app-fix PR) adds a preflight that refuses signing if reviewer/ref restrictions are missing or their metadata cannot be read. This does not replace removing repository-scoped signing secrets.

That infrastructure migration binds stable signing to the `release-signing` GitHub Environment and review signing to `release-review`. This binding alone is not protection if the keystore also remains in repository/organization secrets accessible to arbitrary workflows.

An administrator must:

1. Create `release-signing` in repository Settings → Environments. Require a trusted reviewer and restrict deployment refs to `main` and version tags (`v*`). Configure reviewer/self-review rules for the actual maintainer team so a one-person repository is not accidentally locked out.
2. Move `ELECTROKIT_KEYSTORE_BASE64`, `ELECTROKIT_STORE_PASSWORD`, `ELECTROKIT_KEY_ALIAS` and `ELECTROKIT_KEY_PASSWORD` into that Environment. Use the existing values from the maintainer's secure backup; never paste them into a PR, issue or log. Remove their repository-level copies and restrict any organization-level copies to the minimum necessary access. The current integration cannot enumerate these secrets (HTTP 403), so this migration is not claimed as completed.
3. If signed review artifacts are needed, configure `release-review` with the same reviewer requirements, selected review branch restrictions and separately authorized signing access. Do not leave a permissive review Environment that bypasses stable protection. Otherwise do not run signed review dispatches.
4. Protect `main`, release workflow changes and `v*` tags with appropriate rulesets/branch protection. Require successful CI and trusted review; avoid blanket administrator bypass for untrusted writers. Repository rules depend on account permissions and should be checked in Settings.
5. Test a review signing run, then a tagged release. Verify the installed signing identity and published source commit. Required Environment approval intentionally pauses unattended publication until the maintainer approves the concrete run.

Stable dispatches require an explicit existing `vX.Y.Z` tag. The checkout, app version and trusted main history must agree before any signing-secret step runs. Existing release assets cannot be replaced by a rerun; issue a new version for corrections.

## Independent Telegram webhook secret: coordinated migration

The new function requires two distinct Supabase secrets: `TELEGRAM_BOT_TOKEN` and `TELEGRAM_WEBHOOK_SECRET`. The webhook secret must be an independent cryptographically random value, at least 32 characters from `A-Z a-z 0-9 _ -`. Do not derive it from the bot token. Store the same webhook secret in the GitHub Environment/repository secret used by the webhook setup workflow; the token remains separate.

Do not deploy the new function without its secrets or activate a new header against the old function. During a planned maintenance window:

1. Configure both Supabase secrets using an authorized local secret manager/CLI. Configure GitHub's `TELEGRAM_WEBHOOK_SECRET` for setup without logging it.
2. Deploy the updated Supabase function. It fails closed with a generic 503 when secrets are missing and never reconstructs a token from request headers.
3. Run the existing webhook-activation workflow. It checks `/verify` with the new independent secret before changing Telegram registration; pending updates are preserved. Its existing live `/menu` check sends a message to the configured owner, so run this intentionally as part of activation.
4. Confirm owner-only authorization and delivery. Telegram may retry during the transition. Do not delete pending updates to hide a migration failure.
5. Retire the old token-derived header credential. Rotate the bot token separately if an actual leak is suspected; mere presence of the old design is not proof of a leak.

The application changes and local mocked webhook tests do not deploy Supabase or submit Telegram messages. Administrator credentials for the deployed service are not available to this task; the live migration is not claimed as completed.

## Required app updates

Major/feature updates remain required after valid official metadata is observed. Failed checks or offline operation do not silently dismiss the requirement. A user can refresh metadata; if the publisher withdraws a higher version and serves a lower valid stable release, the obsolete requirement is cleared/replaced. The maintainer must withdraw an unavailable/broken release before this recovery can work. There is no automatic bypass for unrelated signing keys: preserve the pinned certificate and provide a supported migration without telling users to uninstall valuable data.

## Play Protect and privacy

Stable releases must not request `REQUEST_INSTALL_PACKAGES`, expose an APK FileProvider or implement a native installer flow. The capability guards remain enabled. APK scans and valid signatures do not establish Google's malware clearance. Update checking sends a network request to GitHub (including the connection IP); successful automatic checks are cached and manual refresh is explicit. Calculation data is not attached to update requests.

Private signing/key files and local environment files must stay outside Git and source archives. Public certificate fingerprints and `.env.example` may be deliberately tracked; no real credentials belong in examples.
