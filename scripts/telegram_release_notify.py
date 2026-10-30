#!/usr/bin/env python3
"""Send the official ELECTRO APK as a Telegram document with release details.

GitHub Actions supplies GH_TOKEN, TELEGRAM_BOT_TOKEN, TELEGRAM_CHAT_ID,
GITHUB_REPOSITORY and RELEASE_TAG. Credentials are never printed.
"""
import html
import json
import os
from pathlib import Path
import re
import subprocess
import sys
import tempfile
import urllib.request
import uuid


def required(name):
    value = os.environ.get(name, "").strip()
    if not value:
        raise ValueError(f"Missing required environment variable: {name}")
    return value


def published_release(repository, tag):
    result = subprocess.run(
        ["gh", "release", "view", tag, "--repo", repository,
         "--json", "name,url,body,assets,isDraft"],
        capture_output=True, text=True, check=False,
    )
    if result.returncode:
        raise ValueError("The requested GitHub release could not be found.")
    release = json.loads(result.stdout)
    if release.get("isDraft"):
        raise ValueError("Cannot announce a draft GitHub release.")
    return release


def asset_link(assets, suffix):
    matches = [a for a in assets if a.get("name", "").lower().endswith(suffix)]
    return matches[0] if matches else None


def safe_url(url, repository):
    prefix = f"https://github.com/{repository}/"
    if not isinstance(url, str) or not url.startswith(prefix):
        raise ValueError("Unexpected GitHub release asset URL.")
    return url


def highlights(tag, body):
    if tag == "v4.0.0":
        return [
            "More accurate calculations and edge-case handling",
            "Improved saved inputs and accessibility",
            "Stronger release checks and reliability",
        ]
    choices = []
    for line in (body or "").splitlines():
        match = re.match(r"^\s*[-*]\s+(.+)", line)
        if not match:
            continue
        detail = match.group(1).strip()
        if re.match(r"(?i)(bump|update dependency|dependabot)", detail):
            continue
        detail = re.sub(r"\s+by\s+@\S+.*$", "", detail)
        detail = re.sub(r"\s+in\s+https?://\S+", "", detail).strip()
        if detail and len(detail) <= 125:
            choices.append(detail)
        if len(choices) == 3:
            break
    return choices or ["See the release notes for the latest changes."]



MAX_APK_BYTES = 49 * 1024 * 1024


def download_apk(repository, tag, asset):
    """Retrieve only a matching official release APK and verify its size and ZIP magic."""
    name = str(asset.get("name", ""))
    size = asset.get("size")
    if (name != f"ELECTRO-{tag.removeprefix('v')}.apk" or
            not isinstance(size, int) or not 0 < size <= MAX_APK_BYTES):
        raise ValueError("The official APK name or size is not eligible for Telegram delivery.")
    with tempfile.TemporaryDirectory(prefix="electro-apk-") as folder:
        result = subprocess.run(
            ["gh", "release", "download", tag, "--repo", repository,
             "--pattern", name, "--dir", folder],
            capture_output=True, text=True, check=False,
        )
        if result.returncode:
            raise ValueError("Unable to retrieve the official release APK.")
        content = (Path(folder) / name).read_bytes()
    if len(content) != size or not content.startswith(b"PK\x03\x04"):
        raise ValueError("Official release APK download failed integrity checks.")
    return content


def send_apk_document(token, chat_id, filename, content, caption, keyboard):
    """Send actual APK bytes using Telegram sendDocument, not an inline download link."""
    if not str(chat_id).isdigit() or int(chat_id) <= 0:
        raise ValueError("Telegram destination must be a private numeric chat ID.")
    if not 0 < len(content) <= MAX_APK_BYTES:
        raise ValueError("APK exceeds Telegram's supported upload size.")
    boundary = "electro" + uuid.uuid4().hex
    fields = {
        "chat_id": str(chat_id), "caption": caption, "parse_mode": "HTML",
        "reply_markup": json.dumps({"inline_keyboard": keyboard}, ensure_ascii=False),
    }
    parts = []
    for key, value in fields.items():
        parts.append(
            (f'--{boundary}\r\nContent-Disposition: form-data; name="{key}"'
             f'\r\n\r\n{value}\r\n').encode("utf-8")
        )
    parts.append(
        (f'--{boundary}\r\nContent-Disposition: form-data; name="document";'
         f' filename="{filename}"\r\nContent-Type: application/vnd.android.package-archive'
         f'\r\n\r\n').encode("utf-8")
    )
    data = b"".join(parts) + content + f"\r\n--{boundary}--\r\n".encode("utf-8")
    request = urllib.request.Request(
        f"https://api.telegram.org/bot{token}/sendDocument",
        data=data,
        headers={"Content-Type": f"multipart/form-data; boundary={boundary}"},
        method="POST",
    )
    try:
        with urllib.request.urlopen(request, timeout=120) as response:
            answer = json.load(response)
    except Exception:
        # Do not print Telegram exceptions: they may contain a token-bearing API URL.
        raise ValueError("Telegram APK document upload failed.") from None
    if not answer.get("ok") or not answer.get("result", {}).get("document"):
        raise ValueError("Telegram did not confirm the APK document attachment.")
    return answer["result"]["document"].get("file_id")


def announce():
    token = required("TELEGRAM_BOT_TOKEN")
    chat_id = required("TELEGRAM_CHAT_ID")
    repo = required("GITHUB_REPOSITORY")
    tag = required("RELEASE_TAG")
    release = published_release(repo, tag)
    notes_url = safe_url(release.get("url"), repo)
    assets = release.get("assets") or []
    apk = asset_link(assets, ".apk")
    if not apk:
        raise ValueError("The published release does not contain an APK.")
    safe_url(apk.get("url"), repo)
    source = asset_link(assets, "-source.zip")
    notes = "\n".join(
        "• " + html.escape(s[:130]) for s in highlights(tag, release.get("body"))[:3]
    )
    caption = (
        "⚡ <b>ELECTRO — ANDROID APK</b>\n"
        f"📱 Version: <b>{html.escape(tag.removeprefix('v'))}</b>\n"
        f"📦 Size: {apk['size'] / (1024 * 1024):.1f} MB\n"
        "✅ Official signed ELECTRO release.\n\n"
        f"<b>What's new</b>\n{notes}\n\n"
        "Tap the attached APK file to download or install it."
    )
    keyboard = [[{"text": "📝 Release Notes", "url": notes_url}]]
    if source:
        keyboard[0].append({"text": "📦 Source ZIP", "url": safe_url(source.get("url"), repo)})
    keyboard.append([{"text": "💻 ELECTRO on GitHub", "url": f"https://github.com/{repo}"}])
    content = download_apk(repo, tag, apk)
    send_apk_document(token, chat_id, apk["name"], content, caption, keyboard)
    print(f"Telegram confirmed ELECTRO {tag} APK attachment delivery.")


if __name__ == "__main__":
    try:
        announce()
    except (ValueError, json.JSONDecodeError, OSError) as exc:
        print(f"::error::{exc}", file=sys.stderr)
        sys.exit(1)
