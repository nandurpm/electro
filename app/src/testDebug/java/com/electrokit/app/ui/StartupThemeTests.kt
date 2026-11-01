package com.electrokit.app.ui

import android.app.UiModeManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.electrokit.app.R
import com.electrokit.app.data.StartupTheme
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** Verifies the platform launch-theme hint without claiming a rendered physical splash test. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StartupThemeTests {
    /** @return Unit; manual dark sets a per-app override and System removes that override. */
    @Test
    fun committedAppearanceUpdatesPlatformSplashMode() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = context.getSystemService(UiModeManager::class.java)
        StartupTheme.remember(context, false, "dark")
        assertFalse(StartupTheme.light(context))
        assertEquals(UiModeManager.MODE_NIGHT_YES, shadowOf(manager).applicationNightMode)
        StartupTheme.remember(context, true, "light")
        assertTrue(StartupTheme.light(context))
        assertEquals(UiModeManager.MODE_NIGHT_NO, shadowOf(manager).applicationNightMode)
        StartupTheme.remember(context, true, "system")
        assertEquals(UiModeManager.MODE_NIGHT_AUTO, shadowOf(manager).applicationNightMode)
    }

    /** @return Unit; Android's night resource uses the same navy as the app toolbar. */
    @Test
    @Config(qualifiers = "night")
    fun nightStartupBackgroundMatchesBrand() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals(
            context.getColor(R.color.brand_navy),
            context.getColor(R.color.startup_background),
        )
    }
}
