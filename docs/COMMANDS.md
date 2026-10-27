# Command reference

Unless stated otherwise, run from the repository root `electro`. Requirements: Git, JDK 17, SDK Platform 35/Build Tools 35.0.0/platform-tools, and network access for first dependency downloads. Kotlin/Gradle/AGP come from committed pins. Replace placeholder repository/branch names deliberately.

| Purpose | Bash | PowerShell | Expected result / common error |
|---|---|---|---|
| Clone (run in parent directory) | `git clone https://github.com/nandurpm/electro.git` | Same | New `electro/`; access errors require checking existing GitHub access |
| Enter checkout | `cd electro` | `Set-Location electro` | Commands now address project root |
| Check changes | `git status --short` | Same | Lists changed/untracked files; preserve these before pulling |
| Fetch | `git fetch origin` | Same | Remote refs refreshed, working files preserved |
| Update clean main | `git switch main` then `git pull --ff-only origin main` | Same | Fast-forward only; divergence requires review, not forced reset |
| Feature branch | `git switch -c feature/my-change` | Same | New development branch |
| Check Java | `java -version` | Same | JDK 17; wrong version: correct `JAVA_HOME`/PATH |
| Check SDK (tools on PATH) | `sdkmanager --list_installed` | `sdkmanager.bat --list_installed` | Lists Android 35/35.0.0; executable missing: add actual command-line tools `bin` to PATH |
| Install SDK | `sdkmanager 'platforms;android-35' 'build-tools;35.0.0' 'platform-tools'` | `sdkmanager.bat 'platforms;android-35' 'build-tools;35.0.0' 'platform-tools'` | Packages installed; accept licenses using `--licenses` first |
| Wrapper version | `./gradlew --version` | `.\gradlew.bat --version` | Gradle 8.9; missing wrapper JAR means incomplete source checkout |
| Available tasks | `./gradlew tasks --all` | `.\gradlew.bat tasks --all` | Task listing; SDK discovery/configuration must succeed |
| Debug APK | `./gradlew assembleDebug` | `.\gradlew.bat assembleDebug` | `app/build/outputs/apk/debug/app-debug.apk` |
| Debug tests | `./gradlew testDebugUnitTest` | `.\gradlew.bat testDebugUnitTest` | Executed test report; failed assertions are not successful validation |
| Debug lint | `./gradlew lintDebug` | `.\gradlew.bat lintDebug` | `app/build/reports/lint-results-debug.html` |
| Release tests/lint/build | `./gradlew testReleaseUnitTest lintRelease assembleRelease` | `.\gradlew.bat testReleaseUnitTest lintRelease assembleRelease` | Signed `app-release.apk` only with valid original credentials; otherwise unsigned output is not production-ready |
| Clean generated outputs | `./gradlew clean` | `.\gradlew.bat clean` | Deletes build outputs; next build must regenerate them |
| Find APKs | `find app/build/outputs/apk -name '*.apk' -print` | `Get-ChildItem app\build\outputs\apk -Recurse -Filter *.apk` | Actual artifact paths; no output means no APK exists |
| Check debug package | `"$ANDROID_HOME/build-tools/35.0.0/aapt" dump badging app/build/outputs/apk/debug/app-debug.apk` | `& "$env:ANDROID_HOME\build-tools\35.0.0\aapt.exe" dump badging .\app\build\outputs\apk\debug\app-debug.apk` | `com.electrokit.app`, version 3.0/code 14 |
| Verify debug signature | `"$ANDROID_HOME/build-tools/35.0.0/apksigner" verify --verbose --print-certs app/build/outputs/apk/debug/app-debug.apk` | `& "$env:ANDROID_HOME\build-tools\35.0.0\apksigner.bat" verify --verbose --print-certs .\app\build\outputs\apk\debug\app-debug.apk` | Valid development certificate; compare original certificate separately for release updates |
| Check whitespace | `git diff --check` | Same | Empty output on success |
| Stage intentional files | `git add app docs scripts .github README.md CHANGELOG.md` | Same | Index contains reviewed changes; inspect `git diff --cached` |
| Commit | `git commit -m 'Describe the verified change'` | Same | New local commit; does not push or release |
| Push branch | `git push -u origin feature/my-change` | Same | Remote feature branch; requires write permission |
| Open PR (GitHub CLI authenticated) | `gh pr create --base main --head feature/my-change --title 'Describe change' --body-file /tmp/electro-pr.md` | `gh pr create --base main --head feature/my-change --title 'Describe change' --body-file "$env:TEMP\electro-pr.md"` | PR link; first write the reviewed description file. Git authentication alone does not establish GitHub API access |
| Release tag (after review) | `git tag -a v3.0 -m 'ELECTRO 3.0'` then `git push origin v3.0` | Same | Triggers signed-release workflow; do not run as an ordinary test |
| Publish verified signed file manually | `gh release create v3.0 app/build/outputs/apk/release/app-release.apk --verify-tag --title 'ELECTRO 3.0' --generate-notes` | Same paths may use backslashes | Public release, requiring reviewed tag, verified original signature and GitHub API write access |

Version editing is a source change: update `versionCode` and `versionName` in `app/build.gradle.kts`, review the diff, update changelog and rebuild. Do not blindly replace every version string across dependencies/history.

## Python tooling

```bash
python3 -m venv .venv
source .venv/bin/activate
python -m pip install -r scripts/requirements-dev.txt
python scripts/check_comments.py
python -m unittest discover -s scripts -p 'test_*.py'
python scripts/validate_assets.py
python scripts/validate_engineering.py
python scripts/validate_library.py
python scripts/check_contrast.py
python scripts/package_project.py --check
```

PowerShell: `python -m venv .venv`, then `.\.venv\Scripts\Activate.ps1`; other Python commands are unchanged. Each validator prints PASS only after checking its actual dataset. Intentional source changes require `python scripts/package_project.py` before `--check`; intentional palette changes require `python scripts/check_contrast.py --write`. Do not regenerate evidence to conceal a failing assertion or downloaded-artifact mismatch.

## Cloud-specific troubleshooting

Use `source /workspace/toolchains/activate-electro.sh` before Gradle and prepend `/workspace/toolchains/python-dev/bin` to PATH for Python tools. `JAVA_HOME`, `ANDROID_HOME`, `ANDROID_SDK_ROOT`, `ANDROID_USER_HOME`, `GRADLE_USER_HOME` and Java's `user.home` target writable workspace directories. The latter prevents Robolectric's download lock from targeting read-only `/home/agent`. Java uses the existing HTTPS proxy and system Java trust store; verification remains enabled.

An SDK cache manifest failure can be a writable-directory problem, not a missing package. A proxy 403 is access-policy evidence; do not disable TLS or substitute unverified downloads. Preserve underlying exit codes when logging (`> file 2>&1`, or Bash `set -o pipefail` before `tee`). Local setup helpers are outside the Git checkout. No worktree is needed for an already isolated cloud task.
