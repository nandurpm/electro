#!/usr/bin/env bash
# Boot the Android 15 CI device and fail promptly when its launcher exits.
set -euo pipefail
# Recent command-line tools and emulator versions resolve default AVD homes differently.
export ANDROID_USER_HOME="$RUNNER_TEMP/electro-android-user"
export ANDROID_EMULATOR_HOME="$ANDROID_USER_HOME"
export ANDROID_AVD_HOME="$ANDROID_USER_HOME/avd"
mkdir -p "$ANDROID_AVD_HOME"
sdkmanager_path="$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager"
"$sdkmanager_path" emulator platform-tools 'system-images;android-35;google_apis;x86_64'
sudo chmod 666 /dev/kvm
export PATH="$ANDROID_HOME/platform-tools:$PATH"
adb start-server
echo no | "$ANDROID_HOME/cmdline-tools/latest/bin/avdmanager" create avd --force --name electro-smoke --path "$ANDROID_AVD_HOME/electro-smoke.avd" --package 'system-images;android-35;google_apis;x86_64'
test -f "$ANDROID_AVD_HOME/electro-smoke.ini"
"$ANDROID_HOME/emulator/emulator" -avd electro-smoke -no-window -no-audio -no-boot-anim -no-snapshot \
  -gpu swiftshader -feature -Vulkan -accel on -memory 2048 -cores 2 \
  > "$RUNNER_TEMP/electro-emulator.log" 2>&1 &
emulator_task_pid=$!
boot_deadline=$((SECONDS + 300))
while [ "$SECONDS" -lt "$boot_deadline" ]; do
  if ! kill -0 "$emulator_task_pid" 2>/dev/null; then
    echo 'Emulator launcher exited before boot completion.' >&2
    exit 1
  fi
  if [ "$(timeout 5 adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = 1 ]; then
    # Pixel Launcher can show a transient ANR immediately after boot and block
    # the first activity launch. Stop both common launcher package names; the
    # app under test can then start directly without a launcher dialog.
    timeout 10 adb shell am force-stop com.google.android.apps.nexuslauncher 2>/dev/null || true
    timeout 10 adb shell am force-stop com.android.launcher3 2>/dev/null || true
    timeout 10 adb shell input keyevent 4 2>/dev/null || true
    timeout 10 adb shell input keyevent 82 2>/dev/null || true
    timeout 10 adb shell input keyevent 82
    sleep 5
    exit 0
  fi
  sleep 2
done
adb devices -l > "$RUNNER_TEMP/electro-adb.txt" 2>&1
timeout 10 adb logcat -d -t 100 >> "$RUNNER_TEMP/electro-adb.txt" 2>&1 || true
exit 1
