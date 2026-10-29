"""Offline unit tests for the ELECTRO Telegram command handler."""
import importlib.util
import unittest
from unittest import mock
from pathlib import Path

spec = importlib.util.spec_from_file_location("electro_bot", Path(__file__).with_name("telegram_commands.py"))
bot = importlib.util.module_from_spec(spec)
spec.loader.exec_module(bot)

RELEASE = {
    "tag_name": "v4.0.0",
    "html_url": "https://github.com/nandurpm/electro/releases/tag/v4.0.0",
    "assets": [
        {"name": "ELECTRO-4.0.0.apk", "size": 11249103,
         "browser_download_url": "https://github.com/nandurpm/electro/releases/download/v4.0.0/ELECTRO-4.0.0.apk"},
        {"name": "ELECTRO-4.0.0-source.zip", "size": 10106688,
         "browser_download_url": "https://github.com/nandurpm/electro/releases/download/v4.0.0/ELECTRO-4.0.0-source.zip"},
    ]
}


class TestBot(unittest.TestCase):
    def test_version_command(self):
        with mock.patch.object(bot, "latest_release", return_value=RELEASE), mock.patch.object(bot, "send_text") as send:
            self.assertFalse(bot.handle_command("secret", "allowed", "/version", downloads_left=3))
            self.assertIn("v4.0.0", send.call_args.args[2])

    def test_help_register_menu(self):
        with mock.patch.object(bot, "send_text") as send:
            bot.handle_command("secret", "allowed", "/help", downloads_left=3)
            self.assertTrue(send.call_args.kwargs["with_menu"])
            self.assertIn("/apk", send.call_args.args[2])

    def test_apk_and_source_are_files(self):
        with mock.patch.object(bot, "latest_release", return_value=RELEASE), mock.patch.object(bot, "send_document") as send:
            self.assertTrue(bot.handle_command("secret", "allowed", "/apk", downloads_left=2))
            self.assertEqual(send.call_args.args[-1], ".apk")
            self.assertTrue(bot.handle_command("secret", "allowed", "/source", downloads_left=1))
            self.assertEqual(send.call_args.args[-1], "-source.zip")
            self.assertEqual(send.call_count, 2)

    def test_file_quota_fallback_link(self):
        with mock.patch.object(bot, "latest_release", return_value=RELEASE), mock.patch.object(bot, "send_text") as send, mock.patch.object(bot, "send_document") as upload:
            self.assertFalse(bot.handle_command("secret", "allowed", "/apk", downloads_left=0))
            upload.assert_not_called()
            self.assertIn(".apk", send.call_args.args[2])

    def test_asset_validation(self):
        self.assertEqual(bot.release_asset(RELEASE, ".apk")["name"], "ELECTRO-4.0.0.apk")
        modified = {"assets": [{"name": "evil.apk", "size": 5,
                               "browser_download_url": "https://evil.example/virus.apk"}]}
        with self.assertRaises(bot.PublicDataError):
            bot.release_asset(modified, ".apk")

    def test_unauthorized_chat_is_ignored_and_acked(self):
        calls = []
        def fake_tg(method, token, **kwargs):
            calls.append((method, kwargs))
            if method == "getWebhookInfo":
                return {"url": ""}
            if method == "getUpdates" and "offset" not in kwargs and len(
                [x for x in calls if x[0] == "getUpdates" and "offset" not in x[1]]) == 1:
                return [{"update_id": 101, "message": {"text": "/apk", "chat": {"id": 999, "type": "private"}, "from": {"id": 999, "is_bot": False}}}]
            return []
        with mock.patch.object(bot, "tg_form", side_effect=fake_tg), mock.patch.object(bot, "send_document") as upload, mock.patch.object(bot, "send_text") as send:
            bot.poll("secret", "123")
            upload.assert_not_called()
            send.assert_not_called()
            self.assertTrue(any(k.get("offset") == 102 for m, k in calls if m == "getUpdates"))

    def test_authorized_chat_processed(self):
        calls = []
        def fake_tg(method, token, **kwargs):
            calls.append((method, kwargs))
            if method == "getWebhookInfo":
                return {"url": ""}
            if method == "getUpdates" and "offset" not in kwargs and len(
                [x for x in calls if x[0] == "getUpdates" and "offset" not in x[1]]) == 1:
                return [{"update_id": 200, "message": {"text": "/version", "chat": {"id": 123, "type": "private"}, "from": {"id": 123, "is_bot": False}}}]
            return []
        with mock.patch.object(bot, "tg_form", side_effect=fake_tg), mock.patch.object(bot, "latest_release", return_value=RELEASE), mock.patch.object(bot, "send_text") as send:
            bot.poll("secret", "123")
            send.assert_called_once()
            self.assertIn("v4.0.0", send.call_args.args[2])
            self.assertTrue(any(k.get("offset") == 201 for m, k in calls if m == "getUpdates"))


    def test_batch_of_two_commands_is_not_skipped(self):
        observed = []
        calls = []
        def fake_tg(method, token, **kwargs):
            calls.append((method, kwargs))
            if method == "getWebhookInfo":
                return {"url": ""}
            if method == "getUpdates":
                if "offset" not in kwargs:
                    return [
                        {"update_id": 210, "message": {"text": "/start", "chat": {"id": 123, "type": "private"}, "from": {"id": 123, "is_bot": False}}},
                        {"update_id": 211, "message": {"text": "/version", "chat": {"id": 123, "type": "private"}, "from": {"id": 123, "is_bot": False}}},
                    ]
                return []
            raise AssertionError(f"unexpected API method: {method}")
        with mock.patch.object(bot, "tg_form", side_effect=fake_tg), mock.patch.object(bot, "latest_release", return_value=RELEASE), mock.patch.object(bot, "send_text") as send:
            bot.poll("secret", "123")
            self.assertEqual(send.call_count, 2)
            self.assertIn("ELECTRO", send.call_args_list[0].args[2])
            self.assertIn("v4.0.0", send.call_args_list[1].args[2])
            updates = [opts for method, opts in calls if method == "getUpdates"]
            self.assertEqual(len(updates), 2)
            self.assertEqual(updates[1]["offset"], 212)


    def test_owner_message_requires_sender_and_private_chat(self):
        owner = {"chat": {"id": 123, "type": "private"},
                 "from": {"id": 123, "is_bot": False}, "text": "/apk"}
        self.assertTrue(bot.is_authorized_private_message(owner, "123"))
        for message in (
            {"chat": {"id": 123, "type": "private"}, "text": "/apk"},
            {"chat": {"id": 123, "type": "private"}, "from": {"id": 999}, "text": "/apk"},
            {"chat": {"id": 123, "type": "private"}, "from": {"id": 123, "is_bot": True}, "text": "/apk"},
            {"chat": {"id": 123, "type": "group"}, "from": {"id": 123}, "text": "/apk"},
            {"chat": {"id": -123, "type": "supergroup"}, "from": {"id": 123}, "text": "/apk"},
            {"chat": {"id": 999, "type": "private"}, "from": {"id": 123}, "text": "/apk"},
        ):
            with self.subTest(message=message):
                self.assertFalse(bot.is_authorized_private_message(message, "123"))

    def test_owner_only_command_scope(self):
        calls = []
        with mock.patch.object(bot, "tg_form", side_effect=lambda method, token, **kwargs: calls.append((method, kwargs)) or True):
            bot.configure_private_commands("secret", "123")
        command_scope = calls[0]
        self.assertEqual(command_scope[0], "setMyCommands")
        self.assertEqual(command_scope[1]["scope"], {"type": "chat", "chat_id": 123})
        self.assertEqual(command_scope[1]["commands"], bot.COMMANDS)
        removed_scopes = [args["scope"]["type"] for method, args in calls[1:] if method == "deleteMyCommands"]
        self.assertIn("default", removed_scopes)
        self.assertIn("all_private_chats", removed_scopes)
        self.assertIn("all_group_chats", removed_scopes)
        self.assertEqual(len(calls), 5)

    def test_reject_nonprivate_chat_id(self):
        for value in ("-100123456789", "@publicchat", "abc", "0", "", "+123"):
            with self.subTest(chat_id=value):
                with self.assertRaises(bot.TelegramError):
                    bot.private_chat_id(value)

    def test_spoofed_sender_is_ignored_in_poll(self):
        calls = []
        def fake_tg(method, token, **kwargs):
            calls.append((method, kwargs))
            if method == "getWebhookInfo":
                return {"url": ""}
            if method == "getUpdates" and "offset" not in kwargs:
                return [{
                    "update_id": 300,
                    "message": {"text": "/apk",
                                "chat": {"id": 123, "type": "private"},
                                "from": {"id": 999, "is_bot": False}},
                }]
            return []
        with mock.patch.object(bot, "tg_form", side_effect=fake_tg), mock.patch.object(bot, "send_document") as send:
            bot.poll("secret", "123")
            send.assert_not_called()
            self.assertTrue(any(options.get("offset") == 301 for method, options in calls if method == "getUpdates"))

    def test_webhook_does_not_get_deleted(self):
        with mock.patch.object(bot, "tg_form", return_value={"url": "https://example.com/webhook"}) as req:
            with self.assertRaises(bot.TelegramError):
                bot.poll("secret", "123")
            self.assertEqual(req.call_count, 1)

    def test_release_history_sorted(self):
        entries = [
            {"tag_name": "v3.3.0", "published_at": "2026-10-27T00:00:00Z", "html_url": "https://example.com/3"},
            {"tag_name": "v4.0.0", "published_at": "2026-10-28T00:00:00Z", "html_url": "https://example.com/4"},
            {"tag_name": "electro-3.3.0", "published_at": "2026-10-28T08:00:00Z", "html_url": "https://example.com/dup"},
        ]
        with mock.patch.object(bot, "api_get", return_value=entries):
            out = bot.format_releases()
            self.assertLess(out.index("v4.0.0"), out.index("v3.3.0"))
            self.assertNotIn("electro-3.3.0", out)

    def test_status_reads_main_ci(self):
        data = {"workflow_runs": [
            {"path": ".github/workflows/android-ci.yml", "head_branch": "main",
             "status": "completed", "conclusion": "success", "html_url": "https://github.com/run1"},
            {"path": ".github/workflows/android-release.yml", "head_branch": "v4.0.0",
             "status": "completed", "conclusion": "success", "html_url": "https://github.com/run2"},
        ]}
        with mock.patch.object(bot, "api_get", return_value=data):
            out = bot.format_status()
            self.assertIn("Android CI (main): success", out)
            self.assertIn("Android Release: success", out)


if __name__ == "__main__":
    unittest.main()
