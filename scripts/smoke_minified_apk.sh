#!/usr/bin/env bash
# CI-only runtime check of the R8 output. Uses an ephemeral test key, never the release key.
set -euo pipefail
apk=${1:?Path to unsigned minified APK required}
smoke_root=${RUNNER_TEMP:-/tmp}/electro-minified-smoke
mkdir -p "$smoke_root"
build_tools="$ANDROID_HOME/build-tools/35.0.0"
adb_timeout=15
adb_run() { timeout "$adb_timeout" adb "$@"; }
recover_launcher_anr() {
  local xml="$1"
  if grep -q "isn't responding\|Not responding" "$xml" 2>/dev/null; then
    echo 'Launcher ANR detected; restarting launcher and retrying the app.' >&2
    adb_run shell input keyevent 4 >/dev/null 2>&1 || true
    adb_run shell am force-stop com.google.android.apps.nexuslauncher >/dev/null 2>&1 || true
    adb_run shell am force-stop com.android.launcher3 >/dev/null 2>&1 || true
    adb_run shell am force-stop com.electrokit.app >/dev/null 2>&1 || true
    sleep 2
    return 0
  fi
  return 1
}
keytool -genkeypair -keystore "$smoke_root/test.jks" -storepass android -keypass android \
  -alias smoke -dname 'CN=CI smoke only' -keyalg RSA -keysize 2048 -validity 2 -noprompt
"$build_tools/zipalign" -p -f 4 "$apk" "$smoke_root/aligned.apk"
"$build_tools/apksigner" sign --ks "$smoke_root/test.jks" --ks-pass pass:android \
  --key-pass pass:android --out "$smoke_root/smoke.apk" "$smoke_root/aligned.apk"
adb_run install "$smoke_root/smoke.apk"
adb_run shell svc wifi disable
adb_run shell svc data disable
adb_run logcat -c
# Stabilize accessibility snapshots; production animations remain enabled in the APK.
for setting in window_animation_scale transition_animation_scale animator_duration_scale; do
  adb_run shell settings put global "$setting" 0
done
for configuration in compact large-font; do
  if [ "$configuration" = compact ]; then
    adb_run shell wm size 720x1280
    adb_run shell wm density 360
    adb_run shell settings put system font_scale 1.0
  else
    adb_run shell wm size 720x1280
    adb_run shell wm density 360
    adb_run shell settings put system font_scale 1.5
  fi
  adb_run shell am force-stop com.electrokit.app
  adb_run shell am start -W -n com.electrokit.app/.MainActivity > "$smoke_root/$configuration-start.txt"
  # Wait for the bundled offline content, not an arbitrary fixed startup delay.
  ready=false
  ready_deadline=$((SECONDS + 90))
  while [ "$SECONDS" -lt "$ready_deadline" ]; do
    if ! adb_run shell uiautomator dump /sdcard/electro-window.xml >/dev/null; then
      sleep 1
      continue
    fi
    if ! adb_run pull /sdcard/electro-window.xml "$smoke_root/$configuration.xml" >/dev/null; then
      sleep 1
      continue
    fi
    if recover_launcher_anr "$smoke_root/$configuration.xml"; then
      adb_run shell am start -W -n com.electrokit.app/.MainActivity > "$smoke_root/$configuration-start.txt"
      continue
    fi
    if python3 - "$smoke_root/$configuration.xml" <<'PY'
import sys, xml.etree.ElementTree as ET
nodes = ET.parse(sys.argv[1]).getroot().iter('node')
labels = {n.get('text', '') for n in nodes if n.get('package') == 'com.electrokit.app'}
sys.exit(0 if {'ELECTRO', 'Tools', 'Home'} <= labels else 1)
PY
    then ready=true; break; fi
    sleep 1
  done
  test "$ready" = true
  adb_run shell pidof com.electrokit.app
  timeout "$adb_timeout" adb exec-out screencap -p > "$smoke_root/$configuration.png"
done
adb_run logcat -d > "$smoke_root/logcat.txt"
if grep -E 'FATAL EXCEPTION|Process: com.electrokit.app.*PID:' "$smoke_root/logcat.txt"; then
  echo 'Minified APK crashed' >&2
  exit 1
fi
rm -f "$smoke_root/test.jks" "$smoke_root/aligned.apk" "$smoke_root/smoke.apk"
