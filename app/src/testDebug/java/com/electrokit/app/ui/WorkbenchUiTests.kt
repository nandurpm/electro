package com.electrokit.app.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import com.electrokit.app.MainActivity
import com.electrokit.app.viewmodel.EngineeringAccess
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Exercises redesigned native navigation, privacy boundary and bundled images in simulated Android. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w412dp-h915dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class WorkbenchUiTests {
    /** Owns the Activity and Compose synchronization for each test. */
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()

    /** @return Unit; waits for actual repository loading rather than treating a process as readiness. */
    private fun ready() { ui.waitUntil(30000) { ui.onAllNodesWithText("Tools").fetchSemanticsNodes().isNotEmpty() } }

    /** @return Unit; opens the guarded destination using public navigation. */
    private fun engineering() { ready(); ui.onNodeWithText("Tools").performClick(); ui.onNodeWithText("Engineering").performClick() }

    /** @return Unit; incorrect input cannot reveal snippets, and manual locking removes composed data. */
    @Test fun protectedContentRequiresCodeAndRelocks() {
        engineering()
        ui.onNodeWithText("Engineering access").assertIsDisplayed()
        ui.onNodeWithText("E217", substring = true).assertDoesNotExist()
        ui.onNode(hasSetTextAction() and hasText("Access code")).performTextReplacement("000")
        ui.onNodeWithText("Unlock").performClick()
        ui.onNodeWithText("Access code not accepted.").assertIsDisplayed()
        ui.onNode(hasSetTextAction() and hasText("Access code")).performTextReplacement("789")
        ui.onNodeWithText("Unlock").performClick()
        ui.onNode(hasSetTextAction() and hasText("Search all engineering references and tools")).assertExists()
        ui.activityRule.scenario.recreate()
        ui.waitForIdle()
        ui.onNodeWithText("Lock engineering").performClick()
        ui.onNodeWithText("Engineering access").assertIsDisplayed()
        ui.onNode(hasText("Search all engineering references and tools")).assertDoesNotExist()
    }

    /** @return Unit; activity-local state never becomes a persisted grant for a fresh session. */
    @Test fun accessIsNotSavedAndAttemptsAreRateLimited() {
        val access = ViewModelProvider(ui.activity)[EngineeringAccess::class.java]
        assertFalse(access.unlocked)
        assertNull(access.unlock("789")); assertTrue(access.unlocked)
        access.lock(); assertFalse(access.unlocked)
        repeat(5) { assertNotNull(access.unlock("000")) }
        assertTrue(access.unlock("789")!!.contains("Wait"))
        assertFalse(EngineeringAccess().unlocked)
    }

    /** @return Unit; protected fault text cannot leak through the public dashboard search. */
    @Test fun publicSearchExcludesFaultSnippets() {
        ready()
        ui.onNode(hasSetTextAction() and hasText("Search")).performTextReplacement("E217")
        ui.onNodeWithText("Drive Chain Switch", substring = true).assertDoesNotExist()
        ui.onNodeWithText("E217 — Drive Chain Switch").assertDoesNotExist()
    }

    /** @return Unit; a new public model calculates through its native inputs and Android Back returns to Tools. */
    @Test fun utilityCalculationAndAndroidBack() {
        ready(); ui.onNodeWithText("Tools").performClick()
        ui.onNode(hasScrollToIndexAction() and SemanticsMatcher.keyIsDefined(androidx.compose.ui.semantics.SemanticsProperties.HorizontalScrollAxisRange))
            .performScrollToNode(hasText("Utilities"))
        ui.onNodeWithText("Utilities").performClick()
        ui.onNodeWithText("Ideal transformer").performClick()
        ui.onNode(hasSetTextAction() and hasText("Primary voltage (V)")).performTextReplacement("240")
        ui.onNode(hasSetTextAction() and hasText("Primary turns")).performTextReplacement("1000")
        ui.onNode(hasSetTextAction() and hasText("Secondary turns")).performTextReplacement("100")
        ui.onNodeWithText("Calculate").performScrollTo().performClick()
        ui.onNodeWithText("24 V").assertExists()
        ui.runOnIdle { ui.activity.onBackPressedDispatcher.onBackPressed() }
        ui.onNodeWithText("Engineering unit converter").assertExists()
    }

    /** @return Unit; selected units govern the actual converter input and output without duplicate prefix scaling. */
    @Test fun unitConverterNativeWorkflow() {
        ready(); ui.onNodeWithText("Tools").performClick()
        ui.onNode(hasScrollToIndexAction() and SemanticsMatcher.keyIsDefined(androidx.compose.ui.semantics.SemanticsProperties.HorizontalScrollAxisRange))
            .performScrollToNode(hasText("Utilities"))
        ui.onNodeWithText("Utilities").performClick()
        ui.onNodeWithText("Engineering unit converter").performClick()
        ui.onNode(hasSetTextAction() and hasText("Value")).performTextReplacement("3.3")
        ui.onNodeWithText("3300 mV").assertExists()
        ui.onNodeWithText("Quantity: Voltage").performClick()
        ui.onNodeWithText("Temperature").performClick()
        ui.onNode(hasSetTextAction() and hasText("Value")).performTextReplacement("100")
        ui.onNodeWithText("212 °F").assertExists()
    }

    /** @return Unit; an actual bundled image renders and a component favorite is navigable. */
    @Test fun componentImageAndFavoriteRoundTrip() {
        ready(); ui.onNodeWithText("Learn").performClick()
        ui.onNode(hasSetTextAction() and hasText("Search")).performTextReplacement("Axial Resistor")
        ui.waitUntil(10000) { ui.onAllNodes(hasText("Axial Resistor") and !hasSetTextAction()).fetchSemanticsNodes().isNotEmpty() }
        ui.onNode(hasText("Axial Resistor") and !hasSetTextAction()).performClick()
        ui.waitUntil(10000) { ui.onAllNodesWithContentDescription("Axial Resistor").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithContentDescription("Axial Resistor").assertExists()
        ui.onNodeWithContentDescription("Favorite component").performClick()
        ui.onNodeWithContentDescription("Back").performClick()
        ui.onNodeWithText("Favorites").performClick()
        ui.onNodeWithText("Axial Resistor").assertExists()
    }

    /** @return Unit; the native home render is exported from the simulated Activity for visual review. */
    @Test fun homeScreenshotAndSystemBarFlags() {
        ready()
        ui.onNodeWithText("Settings").performClick()
        ui.onNodeWithText("Dark").performClick()
        ui.waitUntil(10000) { !com.electrokit.app.data.StartupTheme.light(ui.activity) }
        ui.onNodeWithText("Home").performClick()
        ui.mainClock.advanceTimeBy(1000)
        ui.runOnIdle {
            assertEquals(0, ui.activity.window.attributes.flags and android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN)
            val view = ui.activity.window.decorView
            val bitmap = android.graphics.Bitmap.createBitmap(view.width, view.height, android.graphics.Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(bitmap))
            val file = java.io.File("build/design-previews/electro-home-redesign.png")
            file.parentFile!!.mkdirs()
            file.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }
}
