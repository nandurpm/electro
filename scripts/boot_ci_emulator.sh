#!/usr/bin/env bash
# Boot the Android 15 CI device and fail promptly when its launcher exits.
set -euo pipefail
sdkmanager_path="$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager"
"$sdkmanager_path" emulator platform-tools 'system-images;android-35;google_apis;x86_64'
sudo chmod 666 /dev/kvm
export PATH="$ANDROID_HOME/platform-tools:$PATH"
adb start-server
echo no | "$ANDROID_HOME/cmdline-tools/latest/bin/avdmanager" create avd --force --name electro-smoke --package 'system-images;android-35;google_apis;x86_64'
"$ANDROID_HOME/emulator/emulator" -avd electro-smoke -no-window -no-audio -no-boot-anim -no-snapshot \
  -gpu swiftshader -feature -Vulkan -accel on -memory 2048 -cores 2 \
  > "$RUNNER_TEMP/electro-emulator.log" 2>&1 &
emulator_task_pid=$!
for attempt in $(seq 1 150); do
  if ! kill -0 "$emulator_task_pid" 2>/dev/null; then
    echo 'Emulator launcher exited before boot completion.' >&2
    exit 1
  fi
  if [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = 1 ]; then
    adb shell input keyevent 82
    exit 0
  fi
  sleep 2
done
adb devices -l > "$RUNNER_TEMP/electro-adb.txt" 2>&1
timeout 10 adb logcat -d -t 100 >> "$RUNNER_TEMP/electro-adb.txt" 2>&1 || true
exit 1
