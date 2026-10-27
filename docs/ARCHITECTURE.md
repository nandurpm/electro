# Architecture

The existing stack remains: one `MainActivity`, Kotlin, Compose Material 3, Navigation Compose, DataStore Preferences and kotlinx.serialization. No image-loading framework, cloud service or additional database was introduced.

`MainActivity.kt` selects effective appearance, configures visible edge-to-edge system bars and composes `ElectroApp`. `ui/screens/App.kt` retains `main`, `entry/{id}`, `engineering/{route}`, `settings`, `plugins` and `info/{tab}` routes. `WorkbenchScreen.kt` supplies five primary destinations and saveable local component/lesson/utility selection.

Original calculation engines and IDs remain. `WorkbenchTools.kt` adds seven explicit educational models and a bounded arithmetic parser; it never evaluates executable code. `UnitConversions.kt` separates physical dimensions and temperature offsets.

`Repository.kt` retains the application-scoped `electrokit` DataStore and existing raw-input journal. New appearance mode, recent public IDs and bounded saved-result snapshots have default values for compatibility. Malformed saved state is surfaced rather than silently discarded.

`ComponentLibrary.kt` owns local metadata and generic lesson content. PNGs decode on Dispatchers.IO at display resolution and use a bounded 8 MiB cache. Failed assets have readable fallbacks. Artwork does not establish exact product identity or ratings.

`EngineeringScreen` and escalator image pages wrap `EngineeringGate` before composing protected data. The shared `EngineeringAccess` Activity ViewModel never persists an unlocked grant. Rotation retains that ViewModel; a fresh Activity/process starts locked. Public search/favorites omit protected snippets while locked. Secure-window protection applies while the gate is composed. APK assets remain extractable, so the gate is not encryption.

Application ID `com.electrokit.app`, minSdk 24, compile/target SDK 35 and release signing names remain. No Internet permission or deep-link filters are added. Only the launcher Activity is exported. Backups remain disabled. Real device/system-bar, TalkBack and upgrade checks remain distinct from Robolectric simulation.
