#!/usr/bin/env bash
# CI-only runtime check of the R8 output. Uses an ephemeral test key, never the release key.
set -euo pipefail
apk=${1:?Path to unsigned minified APK required}
smoke_root=${RUNNER_TEMP:-/tmp}/electro-minified-smoke
mkdir -p "$smoke_root"
build_tools="$ANDROID_HOME/build-tools/35.0.0"
keytool -genkeypair -keystore "$smoke_root/test.jks" -storepass android -keypass android \
  -alias smoke -dname 'CN=CI smoke only' -keyalg RSA -keysize 2048 -validity 2 -noprompt
"$build_tools/zipalign" -p -f 4 "$apk" "$smoke_root/aligned.apk"
"$build_tools/apksigner" sign --ks "$smoke_root/test.jks" --ks-pass pass:android \
  --key-pass pass:android --out "$smoke_root/smoke.apk" "$smoke_root/aligned.apk"
adb install "$smoke_root/smoke.apk"
adb shell svc wifi disable
adb shell svc data disable
adb logcat -c
for configuration in compact large-font; do
  if [ "$configuration" = compact ]; then
    adb shell wm size 720x1280
    adb shell wm density 360
    adb shell settings put system font_scale 1.0
  else
    adb shell wm size 720x1280
    adb shell wm density 360
    adb shell settings put system font_scale 1.5
  fi
  adb shell am force-stop com.electrokit.app
  adb shell am start -W -n com.electrokit.app/.MainActivity > "$smoke_root/$configuration-start.txt"
  # Wait for the bundled offline content, not an arbitrary fixed startup delay.
  ready=false
  for attempt in $(seq 1 30); do
    adb shell uiautomator dump /sdcard/electro-window.xml >/dev/null
    adb pull /sdcard/electro-window.xml "$smoke_root/$configuration.xml" >/dev/null
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
  adb shell pidof com.electrokit.app
  adb exec-out screencap -p > "$smoke_root/$configuration.png"
done
adb logcat -d > "$smoke_root/logcat.txt"
if grep -E 'FATAL EXCEPTION|Process: com.electrokit.app.*PID:' "$smoke_root/logcat.txt"; then
  echo 'Minified APK crashed' >&2
  exit 1
fi
rm -f "$smoke_root/test.jks" "$smoke_root/aligned.apk" "$smoke_root/smoke.apk"
