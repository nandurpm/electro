package com.electrokit.app.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.electrokit.app.MainActivity
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Exercises redesigned native navigation, general navigation and bundled images in simulated Android. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w412dp-h915dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class WorkbenchUiTests {
    /** Owns the Activity and Compose synchronization for each test. */
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()

    /** @return Unit; waits for actual repository loading rather than treating a process as readiness. */
    private fun ready() { ui.waitUntil(30000) { ui.onAllNodesWithText("Tools").fetchSemanticsNodes().isNotEmpty() } }

    /** @return Unit; a new public model calculates through its native inputs and Android Back returns to Tools. */
    @Test fun utilityCalculationAndAndroidBack() {
        ready(); ui.onNodeWithText("Tools").performClick()
        ui.onNodeWithText("Extra Utilities").performClick()
        ui.onNodeWithText("Ideal transformer").performClick()
        ui.onNode(hasSetTextAction() and hasText("Primary voltage (V)")).performTextReplacement("240")
        ui.onNode(hasSetTextAction() and hasText("Primary turns")).performTextReplacement("1000")
        ui.onNode(hasSetTextAction() and hasText("Secondary turns")).performTextReplacement("100")
        ui.onNodeWithText("Calculate").performScrollTo().performClick()
        ui.onNodeWithText("24 V").assertExists()
        ui.runOnIdle { ui.activity.onBackPressedDispatcher.onBackPressed() }
        ui.onNodeWithText("Unit converter").assertExists()
    }

    /** @return Unit; selected units govern the actual converter input and output without duplicate prefix scaling. */
    @Test fun unitConverterNativeWorkflow() {
        ready(); ui.onNodeWithText("Tools").performClick()
        ui.onNodeWithText("Extra Utilities").performClick()
        ui.onNodeWithText("Unit converter").performClick()
        ui.onNode(hasSetTextAction() and hasText("Value")).performTextReplacement("3.3")
        ui.onNodeWithText("3300 mV").assertExists()
        capture44("converter-4-4")
        ui.onNodeWithText("Quantity: Voltage").performClick()
        ui.onNodeWithText("Temperature").performClick()
        ui.onNode(hasSetTextAction() and hasText("Value")).performTextReplacement("100")
        ui.onNodeWithText("212 °F").assertExists()
        ui.onNodeWithText("Save conversion").performScrollTo().performClick()
        ui.waitUntil(10000) { ui.onAllNodesWithText("100 °C = 212 °F").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithText("Reset").performScrollTo().performClick()
        ui.onNodeWithText("Copy conversion").assertIsNotEnabled()
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
        ui.onNodeWithText("Profile").performClick()
        ui.onNodeWithText("Appearance").performClick()
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
    @Test fun invalidUtilityCannotBeSavedAsCalculation() {
        ready()
        ui.onNodeWithText("Tools").performClick()
        ui.onNodeWithText("Extra Utilities").performClick()
        ui.onNodeWithText("Ideal transformer").performClick()
        ui.onNodeWithText("Reset").performScrollTo().performClick()
        ui.onNodeWithText("Calculate").performScrollTo().performClick()
        ui.onNodeWithText("Check your input").assertExists()
        ui.onNodeWithText("Save calculation").assertDoesNotExist()
        ui.onNodeWithText("Copy result").assertDoesNotExist()
        ui.onNode(hasSetTextAction() and hasText("Primary voltage (V)")).performScrollTo().performTextReplacement("240")
        ui.onNode(hasSetTextAction() and hasText("Primary turns")).performScrollTo().performTextReplacement("1000")
        ui.onNode(hasSetTextAction() and hasText("Secondary turns")).performScrollTo().performTextReplacement("100")
        ui.onNodeWithText("Calculate").performScrollTo().performClick()
        ui.onNodeWithText("24 V").assertExists()
        ui.onNodeWithText("Save calculation").assertExists()
        ui.onNode(hasSetTextAction() and hasText("Primary turns")).performScrollTo().performTextReplacement("0")
        ui.onNodeWithText("Save calculation").assertDoesNotExist()
    }

    @Test fun converterSwapAndInvalidCopyState() {
        ready()
        ui.onNodeWithText("Tools").performClick()
        ui.onNodeWithText("Converters").performClick()
        ui.onNodeWithText("Copy conversion").assertIsNotEnabled()
        ui.onNode(hasSetTextAction() and hasText("Value")).performTextReplacement("2")
        ui.onNodeWithText("2000 mV").assertExists()
        ui.onNodeWithText("Swap units").performClick()
        ui.onNodeWithText("0.002 V").assertExists()
        ui.onNode(hasSetTextAction() and hasText("Value")).performTextReplacement("NaN")
        ui.onNodeWithText("Check your input").assertExists()
        ui.onNodeWithText("Copy conversion").assertIsNotEnabled()
    }

    @Test fun dashboardAndToolsSearchFindReferencesAndUtilities() {
        ready()
        ui.onNode(hasSetTextAction() and hasText("Search")).performTextReplacement("USB port")
        ui.onNode(hasText("USB port") and !hasSetTextAction()).assertExists()
        ui.onNodeWithText("Tools").performClick()
        ui.onNode(hasSetTextAction() and hasText("Search")).performTextReplacement("Ideal transformer")
        ui.onNode(hasText("Ideal transformer") and !hasSetTextAction()).performClick()
        ui.onNode(hasSetTextAction() and hasText("Primary turns")).assertExists()
        ui.runOnIdle { ui.activity.onBackPressedDispatcher.onBackPressed() }
        ui.onNode(hasSetTextAction() and hasText("Search")).performTextReplacement("zzzz-no-match")
        ui.onNodeWithText("No matches yet").assertExists()
    }
    @Test
    @Config(sdk = [34], qualifiers = "w320dp-h700dp")
    fun compactLargeTextLaunchersRemainReachable() {
        org.robolectric.RuntimeEnvironment.setFontScale(2f)
        ui.activityRule.scenario.recreate()
        ready()
        ui.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Converters"))
        ui.onNodeWithText("Converters").performClick()
        ui.onNode(hasSetTextAction() and hasText("Value")).assertExists()
        ui.runOnIdle { ui.activity.onBackPressedDispatcher.onBackPressed() }
        ui.onNodeWithText("Tools").performClick()
        ui.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Extra Utilities"))
        ui.onNodeWithText("Extra Utilities").performClick()
        ui.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Ideal transformer"))
        ui.onNodeWithText("Ideal transformer").assertExists()
    }
    @Test fun learnLanguageChangesProseAndSurvivesRecreation() {
        ready()
        ui.onNodeWithText("Learn").performClick()
        ui.onNodeWithText("Lessons").performClick()
        ui.onNodeWithText("தமிழ்").performClick()
        ui.onNodeWithText("Digital multimeter • practical guide").performClick()
        ui.onNodeWithText("தமிழ்").assertIsSelected()
        ui.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("What you will learn"))
        ui.onNodeWithText("What you will learn").assertExists()
        ui.onNodeWithText("digital multimeter", substring = true).assertExists()
        ui.activityRule.scenario.recreate()
        ui.waitForIdle()
        ui.onNode(hasScrollToIndexAction()).performScrollToIndex(0)
        ui.onNodeWithText("தமிழ்").assertIsSelected()
        ui.onNodeWithText("English").performClick()
        ui.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("What you will learn"))
        ui.onNodeWithText("Identify the display", substring = true).assertExists()
    }
    @Test fun freshInstallUsesLightAndSavedDarkSurvivesRestart() {
        kotlinx.coroutines.runBlocking { com.electrokit.app.data.Repository(ui.activity).resetPreferences() }
        ui.activity.getSharedPreferences("startup_appearance", android.content.Context.MODE_PRIVATE).edit().clear().commit()
        ui.activityRule.scenario.recreate()
        ready()
        ui.onNodeWithText("Profile").performClick()
        ui.onNodeWithText("Appearance").performClick()
        ui.onNodeWithText("Light").assertIsSelected()
        ui.onNodeWithText("Dark").performClick()
        ui.waitUntil(10000) { !com.electrokit.app.data.StartupTheme.light(ui.activity) }
        ui.activityRule.scenario.recreate()
        ui.waitForIdle()
        ui.onNodeWithText("Dark").assertIsSelected()
    }
    @Test fun databaseComparisonUsesBundledSpecifications() {
        ready()
        ui.onNodeWithText("Tools").performClick()
        ui.onNodeWithText("Electronics").performClick()
        ui.onNodeWithText("Parts Finder").performClick()
        ui.onAllNodes(isToggleable())[0].performClick()
        ui.onAllNodes(isToggleable())[1].performClick()
        ui.onNodeWithText("Compare selected (2/3)").assertIsEnabled().performClick()
        ui.onNodeWithText("Compare components").assertExists()
        ui.onNodeWithText("Specification").assertExists()
        capture44("comparison-4-4")
        ui.onNodeWithText("Close").performClick()
    }
    private fun capture44(name: String) {
        ui.mainClock.advanceTimeBy(700)
        ui.waitForIdle()
        ui.runOnIdle {
            val view = ui.activity.window.decorView
            val bitmap = android.graphics.Bitmap.createBitmap(view.width, view.height, android.graphics.Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(bitmap))
            val file = java.io.File("build/design-previews/$name.png")
            file.parentFile!!.mkdirs()
            file.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }
    @Test fun componentMarkingSearchAndCapacitorDecoder() {
        ready(); ui.onNodeWithText("Learn").performClick()
        ui.onNode(hasSetTextAction() and hasText("Search")).performTextReplacement("104")
        ui.waitUntil(10000) { ui.onAllNodesWithText("Ceramic Capacitor").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithText("Ceramic Capacitor").performClick()
        ui.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Identification & markings", substring = true))
        ui.onNodeWithText("Identification & markings", substring = true).performClick()
        ui.onNode(hasSetTextAction() and hasText("Three-digit capacitor code")).performTextReplacement("104")
        ui.onNodeWithText("100 nF").assertExists()
        capture44("Component-capacitor-guide")
    }

    @Test fun onlySelectedVerifiedPackageShowsNumberedPins() {
        ready(); ui.onNodeWithText("Learn").performClick()
        ui.onNode(hasSetTextAction() and hasText("Search")).performTextReplacement("DIP Integrated Circuit")
        ui.onNode(hasText("DIP Integrated Circuit") and !hasSetTextAction()).performClick()
        ui.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Pin configuration", substring = true))
        ui.onNodeWithText("Pin configuration", substring = true).performClick()
        ui.onNodeWithText("TI NE555 • PDIP-8 • top view").performClick()
        ui.onNodeWithText("8 VCC").performScrollTo().performClick()
        ui.onNodeWithText("Pin 8: VCC — Supply").assertExists()
        capture44("Component-verified-pinout")
    }
    @Test fun componentFiltersAndSavedListPosition() {
        ready(); ui.onNodeWithText("Learn").performClick()
        ui.waitUntil(10000) { ui.onAllNodesWithText("ELECTRO — Components").fetchSemanticsNodes().isNotEmpty() }
        capture44("Components-library-dark")
        ui.onNode(hasText("Passive") and SemanticsMatcher.keyIsDefined(androidx.compose.ui.semantics.SemanticsProperties.Selected)).performClick()
        ui.onNode(hasScrollToIndexAction() and !hasAnyAncestor(hasScrollToIndexAction())).performScrollToNode(hasText("Axial Resistor"))
        ui.onNodeWithText("Axial Resistor").performClick()
        ui.onNodeWithContentDescription("Back").performClick()
        ui.onNode(hasScrollToIndexAction() and !hasAnyAncestor(hasScrollToIndexAction())).performScrollToIndex(0)
        ui.onNode(hasText("Passive") and SemanticsMatcher.keyIsDefined(androidx.compose.ui.semantics.SemanticsProperties.Selected)).assertIsSelected()
        ui.onNodeWithText("Home").performClick()
        ui.onNodeWithText("Profile").performClick()
        ui.onNodeWithText("Appearance").performClick()
        ui.onNodeWithText("Light").performClick()
        ui.onNodeWithText("Learn").performClick()
        capture44("Components-library-light")
    }

}
