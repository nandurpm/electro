# Phone-only development

GitHub Actions provides the build machine; Android Studio is not required on the phone.

1. Open the repository in the GitHub app/mobile browser. Create/select a feature branch from main.
2. Use GitHub's file editor or browser editor to change actual source files. Preserve directory structure; root documents alone cannot build Android.
3. Preview the diff, commit with a descriptive message and open a pull request.
4. Open Checks/Actions and select the run for the exact branch and commit. Inspect failed step logs; distinguish downloads/SDK failures from compiler and assertion errors.
5. After a successful Android CI run, download its debug artifact ZIP while signed in. Extract it in Files and open `app-debug.apk`.
6. Allow installation from the browser/Files app only as needed. Debug signing may not match your installed release; preserve valuable installed data.
7. For production, configure the original signing secrets, use the release workflow, then download the signed APK from Releases and verify its version/update compatibility.
8. Revert broken changes with a revert commit/pull request and rerun CI. Do not force-push main.

Manual builds: Actions → **Android CI** → **Run workflow**, choose the branch and run. The release workflow has a separate manual trigger and refuses unsigned publication.

Browser editing provides no local compiler, debugger or emulator. Large refactors, conflicts and accessibility review are harder on a phone. Use CI reports and physical-device checks. Never put keystore passwords into source, issues or chat; use repository Actions secrets.
