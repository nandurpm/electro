package com.electrokit.app.data

import android.content.Context

/**
 * Mirrors only the committed appearance preference for the pre-Compose window; DataStore remains
 * authoritative.
 */
object StartupTheme {
    /**
     * Reads a bounded synchronous launch hint before DataStore loads.
     *
     * @param context Application or Activity context; never retained.
     * @return Last mirrored light preference, defaulting to light; Android storage failures
     *   propagate.
     */
    fun light(context: Context): Boolean =
        context
            .getSharedPreferences("startup_appearance", Context.MODE_PRIVATE)
            .getBoolean("light", true)

    /**
     * Distinguishes an older installation without a launch hint from a deliberately saved dark
     * theme.
     *
     * @param context Application or Activity context; never retained.
     * @return True when a committed appearance has been mirrored; Android storage failures
     *   propagate.
     */
    fun available(context: Context): Boolean =
        context.getSharedPreferences("startup_appearance", Context.MODE_PRIVATE).contains("light")

    /**
     * Stores a launch hint after a successful DataStore transaction, avoiding a dark flash on later
     * light launches.
     *
     * @param context Application context used for local preferences.
     * @param light Committed theme value, not a pending UI toggle.
     * @param mode Stored appearance selection; system clears the platform per-app override.
     * @return Unit; this best-effort hint never replaces or mutates the authoritative DataStore
     *   record.
     */
    fun remember(context: Context, light: Boolean, mode: String = if (light) "light" else "dark") {
        val preferences = context.getSharedPreferences("startup_appearance", Context.MODE_PRIVATE)
        if (!preferences.contains("light") || preferences.getBoolean("light", true) != light) {
            preferences.edit().putBoolean("light", light).apply()
        }
        if (android.os.Build.VERSION.SDK_INT >= 31) {
            val platformMode =
                when (mode) {
                    "system" -> android.app.UiModeManager.MODE_NIGHT_AUTO
                    "light" -> android.app.UiModeManager.MODE_NIGHT_NO
                    else ->
                        if (light) android.app.UiModeManager.MODE_NIGHT_NO
                        else android.app.UiModeManager.MODE_NIGHT_YES
                }
            if (preferences.getInt("platform_mode", -1) != platformMode) {
                // Mirrored first: setApplicationNightMode may recreate the Activity.
                preferences.edit().putInt("platform_mode", platformMode).apply()
                context
                    .getSystemService(android.app.UiModeManager::class.java)
                    .setApplicationNightMode(platformMode)
            }
        }
    }
}
