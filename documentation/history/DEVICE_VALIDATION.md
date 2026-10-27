# Release-device validation — not executed here

This environment has no attached Android device, installed emulator/system image, or `/dev/kvm` acceleration. Robolectric is not counted as device evidence. The table below is an execution checklist, not a passing test report.

Use a disposable API 24 emulator/device and a recent Android device/emulator. Install the **signed, shrunk release APK** using the same certificate as the existing installation:

```sh
adb install -r ElectroKit-1.1.2.apk
adb shell am force-stop com.electrokit.app
adb shell monkey -p com.electrokit.app 1
```

Record device model, API, APK SHA-256, date and outcome for each check:

| Check | Required observation | Actual result |
| --- | --- | --- |
| Launch/relaunch | No release/R8 crash | Not run |
| Insets/rotation/keyboard | Controls remain visible above gesture and keyboard areas | Not run |
| Large font and TalkBack | Controls remain reachable; useful spoken labels and traversal | Not run |
| Airplane mode | All calculator/reference/database routes work without connectivity | Not run |
| Clipboard/share chooser | Copied text and shared units/results are correct | Not run |
| Journal recovery | Type a new value, force-stop immediately after the edit appears, relaunch; latest acknowledged edit remains | Not run |
| Compound units | Paste 10m/s, 20°C/W, 2oz/ft², 20mA, 2kW in matching fields | Not run |
| Upgrade | Theme, favourites and saved values survive install -r | Not run |

Do not clear app data to perform the upgrade/recovery checks. Device evidence should be added only after execution; successful JVM tests do not fill this table.
