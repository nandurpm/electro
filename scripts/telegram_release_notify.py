#!/usr/bin/env python3
"""Send a styled ELECTRO release announcement with Telegram inline buttons.

GitHub Actions supplies GH_TOKEN, TELEGRAM_BOT_TOKEN, TELEGRAM_CHAT_ID,
GITHUB_REPOSITORY and RELEASE_TAG. Credentials are never printed.
"""
import html
import json
import os
import re
import subprocess
import sys
import urllib.request


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


def announce():
    token = required("TELEGRAM_BOT_TOKEN")
    chat_id = required("TELEGRAM_CHAT_ID")
    repository = required("GITHUB_REPOSITORY")
    tag = required("RELEASE_TAG")
    release = published_release(repository, tag)
    release_url = safe_url(release.get("url"), repository)
    assets = release.get("assets") or []
    apk = asset_link(assets, ".apk")
    if not apk:
        raise ValueError("The published release does not contain an APK.")
    apk_url = safe_url(apk.get("url"), repository)
    source = asset_link(assets, "-source.zip")
    version = html.escape(tag.removeprefix("v"))
    bullets = "\n".join(f"  • {html.escape(line)}" for line in highlights(tag, release.get("body")))
    size = apk.get("size")
    size_label = f"{size / (1024 * 1024):.1f} MB" if isinstance(size, (int, float)) and size > 0 else "Ready"
    message = (
        "⚡ <b>E L E C T R O</b>\n"
        "━━━━━━━━━━━━━━━━━━\n"
        "🚀 <b>NEW RELEASE AVAILABLE</b>\n\n"
        f"📱 <b>Version {version}</b>\n"
        "Your offline electrical &amp; electronics workbench.\n\n"
        "🛠 <b>WHAT'S NEW</b>\n"
        f"{bullets}\n\n"
        f"📦 <b>APK size:</b> {size_label}\n"
        "✅ <b>Available on GitHub Releases</b>\n\n"
        "👇 <i>Tap below to download or view the details.</i>"
    )
    keyboard = [
        [{"text": "📲 DOWNLOAD APK", "url": apk_url}],
        [{"text": "📝 Release Notes", "url": release_url}],
    ]
    if source:
        keyboard[1].append({
            "text": "📦 Source ZIP",
            "url": safe_url(source.get("url"), repository),
        })
    keyboard.append([{
        "text": "💻 ELECTRO on GitHub",
        "url": f"https://github.com/{repository}",
    }])
    payload = {
        "chat_id": chat_id,
        "text": message,
        "parse_mode": "HTML",
        "link_preview_options": {"is_disabled": True},
        "reply_markup": {"inline_keyboard": keyboard},
    }
    request = urllib.request.Request(
        f"https://api.telegram.org/bot{token}/sendMessage",
        data=json.dumps(payload, ensure_ascii=False).encode("utf-8"),
        headers={"Content-Type": "application/json; charset=utf-8"},
        method="POST",
    )
    try:
        with urllib.request.urlopen(request, timeout=25) as response:
            answer = json.load(response)
    except Exception:
        raise ValueError("Telegram notification failed. Check bot permissions and secrets.") from None
    if not answer.get("ok"):
        raise ValueError("Telegram API did not confirm successful delivery.")
    print(f"Telegram confirmed the styled {tag} announcement was sent.")


if __name__ == "__main__":
    try:
        announce()
    except (ValueError, json.JSONDecodeError, OSError) as exc:
        print(f"::error::{exc}", file=sys.stderr)
        sys.exit(1)
