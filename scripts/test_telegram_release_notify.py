"""Unit tests for delivery of an actual ELECTRO APK attachment in Telegram."""
import importlib.util
import io
import json
from pathlib import Path
from unittest import TestCase, main, mock

spec = importlib.util.spec_from_file_location("electro_notify", Path(__file__).with_name("telegram_release_notify.py"))
notify = importlib.util.module_from_spec(spec)
spec.loader.exec_module(notify)
APK = b"PK\x03\x04abcd"
RELEASE = {
    "url": "https://github.com/nandurpm/electro/releases/tag/v4.2.0",
    "body": "- Remove restricted equipment pages\n- Keep generic calculators",
    "assets": [
        {"name": "ELECTRO-4.2.0.apk", "size": len(APK), "url": "https://github.com/nandurpm/electro/releases/download/v4.2.0/ELECTRO-4.2.0.apk"},
        {"name": "ELECTRO-4.2.0-source.zip", "size": 22, "url": "https://github.com/nandurpm/electro/releases/download/v4.2.0/ELECTRO-4.2.0-source.zip"}
    ]
}


class Response:
    def __init__(self, result):
        self.data = io.BytesIO(json.dumps(result).encode())
    def __enter__(self):
        return self.data
    def __exit__(self, *_):
        return False


class DirectAttachmentTests(TestCase):
    def test_download_verifies_official_filename_size_and_archive_format(self):
        def gh(command, **_):
            Path(command[command.index("--dir") + 1], "ELECTRO-4.2.0.apk").write_bytes(APK)
            return mock.Mock(returncode=0)
        with mock.patch.object(notify.subprocess, "run", side_effect=gh):
            self.assertEqual(notify.download_apk("nandurpm/electro", "v4.2.0", RELEASE["assets"][0]), APK)
        with self.assertRaises(ValueError):
            notify.download_apk("nandurpm/electro", "v4.2.0", dict(RELEASE["assets"][0], name="../evil.apk"))
        with self.assertRaises(ValueError):
            notify.download_apk("nandurpm/electro", "v4.2.0", dict(RELEASE["assets"][0], size=notify.MAX_APK_BYTES+1))

    def test_send_document_uploads_binary_not_download_url(self):
        requests = []
        def inspect(req, **_):
            requests.append(req)
            return Response({"ok": True, "result": {"document": {"file_id": "file-42"}}})
        with mock.patch.object(notify.urllib.request, "urlopen", side_effect=inspect):
            fid = notify.send_apk_document("fake-token", "12345", "ELECTRO-4.2.0.apk", APK, "Release", [[{"text": "Notes", "url": RELEASE["url"]}]])
        self.assertEqual(fid, "file-42")
        req = requests[0]
        self.assertTrue(req.full_url.endswith("/sendDocument"))
        self.assertIn(b'filename="ELECTRO-4.2.0.apk"', req.data)
        self.assertIn(APK, req.data)
        self.assertIn(b"inline_keyboard", req.data)
        self.assertNotIn(b"DOWNLOAD APK", req.data)

    def test_announce_sends_one_file_with_release_notes(self):
        env = {"TELEGRAM_BOT_TOKEN":"fake-token","TELEGRAM_CHAT_ID":"12345","GITHUB_REPOSITORY":"nandurpm/electro","RELEASE_TAG":"v4.2.0"}
        with mock.patch.dict(notify.os.environ, env):
            with mock.patch.object(notify,"published_release",return_value=RELEASE):
                with mock.patch.object(notify,"download_apk",return_value=APK):
                    with mock.patch.object(notify,"send_apk_document",return_value="fid") as send:
                        notify.announce()
        send.assert_called_once()
        args = send.call_args.args
        self.assertEqual(args[:4],("fake-token","12345","ELECTRO-4.2.0.apk",APK))
        self.assertIn("Version: <b>4.2.0</b>",args[4])
        self.assertTrue(all("DOWNLOAD APK" not in b["text"] for row in args[5] for b in row))

    def test_telegram_must_confirm_document(self):
        with mock.patch.object(notify.urllib.request,"urlopen",return_value=Response({"ok": True,"result":{"message_id":1}})):
            with self.assertRaises(ValueError):
                notify.send_apk_document("token","12345","ELECTRO-4.2.0.apk",APK,"caption",[])
        with self.assertRaises(ValueError):
            notify.send_apk_document("token","-7","ELECTRO-4.2.0.apk",APK,"caption",[])


if __name__=="__main__":
    main()
