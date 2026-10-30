# Private ELECTRO webhook

Telegram delivers messages immediately to the `electro-telegram` Supabase Edge Function. GitHub Actions only verifies and registers the webhook after command/setup changes; it no longer polls on a schedule.

The existing GitHub bot secret is transported over HTTPS in Telegram's secret header, with the first colon replaced by an underscore to satisfy Telegram's allowed characters. The handler verifies the exact SHA-256 fingerprint before using that credential. The token is never committed, logged or stored in the database. Treat the header as a full bot credential; never enable header logging. Both the sender and private chat must match the owner ID. Requests with invalid credentials return 403 before processing data.

The endpoint checks commands against current public ELECTRO releases. APKs requested through /apk are uploaded as files; failed webhook uploads can fall back to release links. Automatic release announcements upload the APK itself as a Telegram document. Small engineering references are bundled. Network and large downloads can take longer than text replies.

## Validate

`node supabase/functions/electro-telegram/test.cjs`

`python3 -m unittest discover -s scripts -p 'test_telegram_commands.py'`

The setup workflow validates the deployed endpoint before calling setWebhook, preserves pending updates, checks getWebhookInfo and exercises a live owner-only menu command. GitHub Actions secrets remain the source for bot configuration. When rotating the token, regenerate the header fingerprint with `telegram_commands.py --webhook-fingerprint`, update and deploy the function, then rerun the setup workflow. The fingerprint is not a credential.

For a manual rollback, use the existing GitHub secrets in a trusted environment: deleteWebhook with drop_pending_updates=false, then run the legacy telegram_commands.py polling script. Do not poll while a webhook is active.
