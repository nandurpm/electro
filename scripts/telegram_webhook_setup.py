#!/usr/bin/env python3
"""Verify and activate ELECTRO's HTTPS webhook without exposing credentials."""
import json
import os
import time
import urllib.error
import urllib.request
from telegram_commands import private_chat_id, tg_form, configure_private_commands, TelegramError

ENDPOINT = 'https://svqtisipjyymguswxteb.supabase.co/functions/v1/electro-telegram'

def endpoint(path, credential, payload):
    request = urllib.request.Request(ENDPOINT + path, data=json.dumps(payload).encode(),
        headers={'content-type': 'application/json', 'x-telegram-bot-api-secret-token': credential}, method='POST')
    try:
        with urllib.request.urlopen(request, timeout=90) as response:
            return json.load(response)
    except (OSError, ValueError, urllib.error.HTTPError):
        raise TelegramError('Webhook endpoint verification failed') from None

def main():
    token = os.environ.get('TELEGRAM_BOT_TOKEN', '')
    owner = os.environ.get('TELEGRAM_CHAT_ID', '')
    if not token:
        raise TelegramError('Bot configuration missing')
    chat_id = private_chat_id(owner)
    credential = token.replace(':', '_', 1)
    # The deployed endpoint must accept this exact credential before polling stops.
    verified = endpoint('/verify', credential, {})
    if verified.get('mode') != 'webhook' or not verified.get('ok'):
        raise TelegramError('Webhook endpoint is not ready')
    configure_private_commands(token, owner)
    tg_form('setWebhook', token, url=ENDPOINT, secret_token=credential,
        allowed_updates=['message', 'callback_query'], max_connections=1, drop_pending_updates=False)
    info = tg_form('getWebhookInfo', token)
    if info.get('url') != ENDPOINT:
        raise TelegramError('Telegram webhook registration did not persist')
    # Exercise the deployed owner-only route through HTTPS and Telegram sendMessage.
    started = time.monotonic()
    result = endpoint('', credential, {'message': {'chat': {'id': chat_id, 'type': 'private'},
        'from': {'id': chat_id, 'is_bot': False}, 'text': '/menu'}})
    if not result.get('ok'):
        raise TelegramError('Live webhook command test failed')
    print('ELECTRO webhook active; owner-only live command delivered in %.2f seconds.' % (time.monotonic()-started))
    print('Telegram pending updates: %s.' % info.get('pending_update_count', 0))
    print('Scheduled polling is disabled; pending Telegram updates were preserved.')

if __name__ == '__main__':
    try:
        main()
    except (TelegramError, ValueError) as error:
        # Never print request objects, headers, URLs with tokens or raw exceptions.
        print('Webhook activation failed: ' + str(error))
        raise SystemExit(1)
