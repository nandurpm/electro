"""Credential migration must fail before touching Telegram when configuration is unsafe."""
import os
import io
from contextlib import redirect_stdout
import io
from contextlib import redirect_stdout
import unittest
from unittest.mock import patch
import telegram_webhook_setup as setup


class WebhookSetupTests(unittest.TestCase):
    def test_missing_independent_secret_does_not_change_the_webhook(self):
        with patch.dict(os.environ, {'TELEGRAM_BOT_TOKEN': '12345:' + 'x' * 40,
                                     'TELEGRAM_CHAT_ID': '12345'}, clear=True):
            with patch.object(setup, 'endpoint') as endpoint, patch.object(setup, 'tg_form') as telegram:
                with self.assertRaises(setup.TelegramError):
                    setup.main()
                endpoint.assert_not_called()
                telegram.assert_not_called()

    def test_token_derived_secret_is_rejected(self):
        token = '12345:' + 'x' * 40
        with patch.dict(os.environ, {'TELEGRAM_BOT_TOKEN': token, 'TELEGRAM_CHAT_ID': '12345',
                                     'TELEGRAM_WEBHOOK_SECRET': token.replace(':', '_', 1)}, clear=True):
            with patch.object(setup, 'endpoint') as endpoint:
                with self.assertRaises(setup.TelegramError):
                    setup.main()
                endpoint.assert_not_called()

    def test_registration_uses_only_the_independent_secret_header(self):
        token, secret = '12345:' + 'x' * 40, 'independent_random_webhook_secret_123456789'
        env = {'TELEGRAM_BOT_TOKEN': token, 'TELEGRAM_CHAT_ID': '12345', 'TELEGRAM_WEBHOOK_SECRET': secret}
        with patch.dict(os.environ, env, clear=True), redirect_stdout(io.StringIO()):
            with patch.object(setup, 'endpoint', side_effect=[{'ok': True, 'mode': 'webhook'}, {'ok': True}]) as endpoint:
                with patch.object(setup, 'configure_private_commands'), patch.object(setup, 'tg_form',
                    return_value={'url': setup.ENDPOINT}) as telegram:
                    setup.main()
        self.assertTrue(all(call.args[1] == secret for call in endpoint.call_args_list))
        self.assertEqual(telegram.call_args_list[0].kwargs['secret_token'], secret)
        self.assertEqual(telegram.call_args_list[0].args[1], token)
