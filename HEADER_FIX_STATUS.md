# Header and system bars correction — source patch

Applied to 2.0.3 source; not yet a built release.

- Removed scroll-driven toolbar hiding. Header remains in Scaffold topBar.
- Retained pinned catalogue search and scrolling introduction/banner.
- Removed immersive hide calls; clock/battery/system navigation remain visible.
- Status inset is painted navy to match the toolbar, with white system icons.
- System navigation inset is applied once at the root. Child navigation adds no second inset.
- Updated the scroll regression to require visible Info/header after swiping.

No APK was built or tested in this restored environment. Existing verification/ reports describe the earlier 2.0.3 release and are not evidence for this patch. Android SDK and release signing environment must be restored before delivering an installable update. Broader review remains pending.
