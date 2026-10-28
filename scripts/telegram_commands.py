#!/usr/bin/env python3
"""Private ELECTRO Telegram command bot, polled by GitHub Actions.

Never print secrets or API URLs containing a Telegram bot token.
"""
from __future__ import annotations

import argparse
import json
import os
import re
import sys
import urllib.error
import urllib.parse
import urllib.request
import uuid
from typing import Any

REPO = "nandurpm/electro"
GITHUB_API = f"https://api.github.com/repos/{REPO}"
MAX_FILE_SIZE = 45 * 1024 * 1024
MAX_UPDATES_PER_RUN = 40
MAX_FILE_SENDS_PER_RUN = 3
COMMANDS = [
    {"command": "start", "description": "Open the ELECTRO bot menu"},
    {"command": "help", "description": "Show available commands"},
    {"command": "apk", "description": "Get the latest Android APK file"},
    {"command": "version", "description": "Check the latest app version"},
    {"command": "source", "description": "Get the source ZIP"},
    {"command": "releases", "description": "Show recent releases"},
    {"command": "status", "description": "Check the GitHub CI status"},
]
MENU = {
    "keyboard": [
        [{"text": "/apk"}, {"text": "/version"}],
        [{"text": "/source"}, {"text": "/releases"}],
        [{"text": "/status"}, {"text": "/help"}],
    ],
    "resize_keyboard": True,
    "is_persistent": True,
    "input_field_placeholder": "Choose an ELECTRO command",
}


class PublicDataError(RuntimeError):
    pass


class TelegramError(RuntimeError):
    pass


def api_get(url: str) -> Any:
    """Access only the ELECTRO GitHub public API."""
    if not url.startswith(GITHUB_API + "/"):
        raise PublicDataError("Untrusted GitHub endpoint")
    request = urllib.request.Request(
        url,
        headers={"User-Agent": "electro-telegram-bot/1.0", "Accept": "application/vnd.github+json"},
    )
    try:
        with urllib.request.urlopen(request, timeout=25) as response:
            return json.load(response)
    except (OSError, ValueError, urllib.error.HTTPError):
        raise PublicDataError("Unable to read GitHub release or status data") from None


def latest_release() -> dict[str, Any]:
    release = api_get(f"{GITHUB_API}/releases/latest")
    if not isinstance(release, dict) or release.get("draft") or not release.get("tag_name"):
        raise PublicDataError("Latest GitHub release is unavailable")
    return release


def release_asset(release: dict[str, Any], suffix: str) -> dict[str, Any]:
    for asset in release.get("assets") or []:
        name = str(asset.get("name", ""))
        url = str(asset.get("browser_download_url", ""))
        if name.lower().endswith(suffix) and url.startswith(
            f"https://github.com/{REPO}/releases/download/"
        ):
            return asset
    raise PublicDataError(f"No {suffix} file is attached to the release")


def telegram(method: str, token: str, data: bytes, *, content_type: str) -> Any:
    if not re.fullmatch(r"[A-Za-z]+", method):
        raise TelegramError("Invalid Telegram method")
    request = urllib.request.Request(
        f"https://api.telegram.org/bot{token}/{method}",
        data=data,
        headers={"Content-Type": content_type},
        method="POST",
    )
    try:
        with urllib.request.urlopen(request, timeout=120) as response:
            result = json.load(response)
    except (OSError, ValueError, urllib.error.HTTPError):
        # Exception details may contain the bot token; do not log them.
        raise TelegramError(f"Telegram {method} request failed") from None
    if not isinstance(result, dict) or not result.get("ok"):
        raise TelegramError(f"Telegram {method} did not confirm success")
    return result.get("result")


def tg_form(method: str, token: str, **fields: Any) -> Any:
    body = urllib.parse.urlencode(
        {k: json.dumps(v) if isinstance(v, (dict, list)) else str(v) for k, v in fields.items()}
    ).encode("utf-8")
    return telegram(method, token, body, content_type="application/x-www-form-urlencoded")


def send_text(token: str, chat_id: str, text: str, *, with_menu: bool = False) -> Any:
    args: dict[str, Any] = {"chat_id": chat_id, "text": text, "disable_web_page_preview": "true"}
    if with_menu:
        args["reply_markup"] = MENU
    return tg_form("sendMessage", token, **args)


def send_document(token: str, chat_id: str, release: dict[str, Any], suffix: str) -> None:
    asset = release_asset(release, suffix)
    url = asset["browser_download_url"]
    expected_size = int(asset.get("size", 0))
    if expected_size < 1 or expected_size > MAX_FILE_SIZE:
        raise PublicDataError("This file is too large to send directly; use the GitHub link")
    try:
        request = urllib.request.Request(url, headers={"User-Agent": "electro-telegram-bot/1.0"})
        with urllib.request.urlopen(request, timeout=80) as response:
            file_bytes = response.read(MAX_FILE_SIZE + 1)
    except (OSError, urllib.error.HTTPError):
        raise PublicDataError("Unable to download the release file from GitHub") from None
    if not file_bytes or len(file_bytes) > MAX_FILE_SIZE:
        raise PublicDataError("Release file is empty or too large")
    boundary = "electro-" + uuid.uuid4().hex
    filename = str(asset["name"]).replace('"', "").replace("\r", "").replace("\n", "")
    file_type = "application/vnd.android.package-archive" if suffix == ".apk" else "application/zip"
    caption = f"ELECTRO {release['tag_name']} — {'Android APK' if suffix == '.apk' else 'Source ZIP'}"
    parts = []
    for key, value in (("chat_id", chat_id), ("caption", caption)):
        parts.append(
            f'--{boundary}\r\nContent-Disposition: form-data; name="{key}"\r\n\r\n{value}\r\n'.encode()
        )
    parts.append(
        f'--{boundary}\r\nContent-Disposition: form-data; name="document"; filename="{filename}"\r\n'
        f'Content-Type: {file_type}\r\n\r\n'.encode()
    )
    body = b"".join(parts) + file_bytes + f"\r\n--{boundary}--\r\n".encode()
    telegram("sendDocument", token, body, content_type=f"multipart/form-data; boundary={boundary}")


def format_releases() -> str:
    data = api_get(f"{GITHUB_API}/releases?per_page=15")
    if not isinstance(data, list):
        raise PublicDataError("Release listing is unavailable")
    releases = [
        x for x in data if not x.get("draft")
        and re.fullmatch(r"v\d+\.\d+\.\d+", x.get("tag_name", ""))
    ]
    releases.sort(key=lambda x: x.get("published_at") or "", reverse=True)
    releases = releases[:5]
    if not releases:
        return "No published ELECTRO releases found."
    return "Recent ELECTRO releases:\n\n" + "\n".join(
        f"{x['tag_name']}: {x['html_url']}" for x in releases
    )


def format_status() -> str:
    data = api_get(f"{GITHUB_API}/actions/runs?per_page=40")
    runs = data.get("workflow_runs", []) if isinstance(data, dict) else []
    latest_ci = next(
        (x for x in runs if x.get("path", "").endswith("android-ci.yml")
         and x.get("head_branch") == "main"), None
    )
    latest_release = next(
        (x for x in runs if x.get("path", "").endswith("android-release.yml")), None
    )
    text = "ELECTRO GitHub Actions status:\n"
    for label, entry in (("Android CI (main)", latest_ci), ("Android Release", latest_release)):
        if entry:
            state = entry.get("conclusion") or entry.get("status") or "unknown"
            text += f"\n{label}: {state}\n{entry.get('html_url', '')}\n"
        else:
            text += f"\n{label}: not found among recent runs\n"
    return text


def help_message() -> str:
    return (
        "ELECTRO — Engineering App Bot\n\n"
        "/apk — Download latest signed Android APK\n"
        "/version — View latest version and release\n"
        "/source — Download matching source ZIP\n"
        "/releases — Show recent versions\n"
        "/status — Check GitHub build/release status\n"
        "/help — Show this menu\n\n"
        "Responses are checked about every 5 minutes, so replies may be delayed."
    )


def handle_command(token: str, chat_id: str, user_text: str, *, downloads_left: int) -> bool:
    """Return True only when a file transfer consumes the run quota."""
    command = user_text.strip().split(maxsplit=1)[0].split("@", 1)[0].lower() if user_text.strip() else ""
    if command in ("/start", "/help"):
        send_text(token, chat_id, help_message(), with_menu=True)
    elif command == "/version":
        release = latest_release()
        send_text(token, chat_id, f"Latest ELECTRO version: {release['tag_name']}\n{release['html_url']}")
    elif command in ("/apk", "/source"):
        release = latest_release()
        suffix = ".apk" if command == "/apk" else "-source.zip"
        asset = release_asset(release, suffix)
        if downloads_left <= 0:
            send_text(
                token, chat_id, "File transfer limit reached for this check. "
                f"Download it here:\n{asset['browser_download_url']}"
            )
        else:
            send_document(token, chat_id, release, suffix)
            return True
    elif command == "/releases":
        send_text(token, chat_id, format_releases())
    elif command == "/status":
        send_text(token, chat_id, format_status())
    else:
        send_text(token, chat_id, "Unknown command. Use /help to see ELECTRO bot commands.", with_menu=True)
    return False


def poll(token: str, chat_id: str) -> None:
    webhook = tg_form("getWebhookInfo", token)
    if webhook.get("url"):
        raise TelegramError("A Telegram webhook is active. Polling cannot run while webhook is configured")
    handled = 0
    docs_sent = 0
    while handled < MAX_UPDATES_PER_RUN:
        updates = tg_form(
            "getUpdates", token, limit=min(40, MAX_UPDATES_PER_RUN - handled),
            timeout=0, allowed_updates=["message"]
        )
        if not updates:
            break
        for update in updates:
            if handled >= MAX_UPDATES_PER_RUN:
                break
            message = update.get("message") or {}
            chat = message.get("chat") or {}
            if str(chat.get("id", "")) == chat_id and chat.get("type") == "private":
                user_text = message.get("text", "")
                if isinstance(user_text, str) and user_text:
                    try:
                        was_file = handle_command(
                            token, chat_id, user_text,
                            downloads_left=MAX_FILE_SENDS_PER_RUN - docs_sent
                        )
                        docs_sent += int(was_file)
                    except PublicDataError:
                        send_text(
                            token, chat_id,
                            "GitHub data or file is unavailable right now. Try again or use "
                            "https://github.com/nandurpm/electro/releases"
                        )
            # Acknowledge only after sending; failures remain pending to retry.
            tg_form(
                "getUpdates", token, offset=int(update["update_id"]) + 1,
                limit=1, timeout=0, allowed_updates=["message"]
            )
            handled += 1
    print(f"Telegram poll completed; processed {handled} updates (authorized chat only).")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--setup", action="store_true", help="Register commands and announce activation")
    args = parser.parse_args()
    token = os.environ.get("TELEGRAM_BOT_TOKEN", "")
    chat_id = os.environ.get("TELEGRAM_CHAT_ID", "")
    if not token or not chat_id:
        print("Missing TELEGRAM_BOT_TOKEN or TELEGRAM_CHAT_ID GitHub Actions secret", file=sys.stderr)
        return 1
    try:
        if args.setup:
            tg_form("setMyCommands", token, commands=COMMANDS)
            send_text(
                token, chat_id,
                "ELECTRO bot commands are enabled. Send /help or use the buttons below.\n\n"
                "Commands are checked approximately every 5 minutes.",
                with_menu=True
            )
            print("Telegram command menu registered and setup message sent.")
        poll(token, chat_id)
    except (TelegramError, PublicDataError) as error:
        print(f"ELECTRO bot: {error}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
