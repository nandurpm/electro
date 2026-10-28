package com.electrokit.app.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.electrokit.app.MainActivity
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Actual Compose navigation/search/bookmark and drawing checks in simulated API-34 Android. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w412dp-h915dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class EngineeringUiTests {
    /** Activity lifecycle and Compose semantics rule. */
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()

    /** Checks all five destinations are visible without an undiscoverable horizontal tab scroll.
     * @return Unit; clipped or missing navigation labels fail the assertions.
     */
    @Test fun allHomeTabsVisible() {
        ui.waitUntil(30000) { ui.onAllNodesWithText("Tools").fetchSemanticsNodes().isNotEmpty() }
        listOf("Home", "Tools", "Learn", "Favorites", "Profile").forEach {
            ui.onAllNodesWithText(it).onLast().assertIsDisplayed()
        }
    }

    /** Exercises the engineering dashboard in persisted light mode and records actual simulated rendering.
     * @return Unit; theme persistence or module navigation failures fail this test.
     */
    @Test fun lightEngineeringPreview() {
        ui.waitUntil(30000) { ui.onAllNodesWithText("Tools").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithContentDescription("Menu").performClick()
        ui.onNode(hasText("Settings") and hasClickAction() and !hasAnyAncestor(hasTestTag("main_navigation"))).performClick()
        val toggle = ui.onNode(isToggleable())
        // Force a real save even when another test's in-memory DataStore already selected light.
        if (toggle.fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.ToggleableState] == androidx.compose.ui.state.ToggleableState.On) {
            toggle.performClick()
            ui.waitUntil(30000) { toggle.fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.ToggleableState] == androidx.compose.ui.state.ToggleableState.Off }
        }
        toggle.performClick()
        ui.waitUntil(30000) { com.electrokit.app.data.StartupTheme.light(ui.activity) }
        ui.runOnIdle { ui.activity.onBackPressedDispatcher.onBackPressed() }
        ui.activityRule.scenario.recreate()
        home()
        save("dashboard-light")
        ui.onNode(hasSetTextAction()).performTextReplacement("Motor shaft torque")
        ui.onNode(hasText("Motor shaft torque") and !hasSetTextAction()).performClick()
        ui.onNodeWithText("Load example").performClick()
        ui.onNode(verticalList()).performScrollToNode(hasText("Torque: 146.912 N·m", substring = true))
        save("torque-light")
        ui.runOnIdle { ui.activity.onBackPressedDispatcher.onBackPressed() }
        ui.onNode(hasSetTextAction()).performTextReplacement("USB port")
        ui.onNode(hasText("USB port") and !hasSetTextAction()).performClick()
        ui.onNodeWithText("Universal Serial Bus").assertExists()
        save("reference-light")
    }

    /** Restores global font configuration so accessibility fixtures cannot alter later tests.
     * @return Unit; framework errors fail the fixture.
     */
    @org.junit.After fun restoreFontScale() { org.robolectric.RuntimeEnvironment.setFontScale(1f) }

    /** Verifies that the mobile engineering dashboard does not expose source filenames or hashes.
     * @return Unit; provenance controls must remain hidden from the mobile UI.
     */
    @Test fun mobileUiHidesSourceReferences() {
        home()
        ui.onAllNodesWithText("Technical Reference Library").assertCountEquals(0)
        ui.onAllNodesWithText("Original source SHA-256").assertCountEquals(0)
        ui.onAllNodesWithText("4886(1).jpg").assertCountEquals(0)
    }

    /** Verifies full navigation labels and catalogue usability at 200 percent font size.
     * @return Unit; missing, clipped or inaccessible content fails the test.
     */
    @Test fun bottomNavigationSupportsLargeText() {
        org.robolectric.RuntimeEnvironment.setFontScale(2f)
        ui.activityRule.scenario.recreate()
        ui.waitUntil(30000) { ui.onAllNodesWithText("Tools").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithText("Tools").performClick()
        ui.onNodeWithText("Electrical Calculators").performClick()
        ui.onAllNodes(verticalList()).onLast().performScrollToNode(hasText("Resistor Colour Code"))
        ui.onNodeWithText("Resistor Colour Code").assertIsDisplayed()
        save("navigation-large")
        listOf("Home", "Tools", "Learn", "Favorites", "Profile").forEach { label ->
            val layouts = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
            ui.onNode(hasText(label) and hasAnyAncestor(hasTestTag("main_navigation")), useUnmergedTree = true)
                .assertIsDisplayed().performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult) { it(layouts) }
            org.junit.Assert.assertTrue("No text layout: " + label, layouts.isNotEmpty())
            org.junit.Assert.assertFalse("Clipped navigation label: " + label, layouts.any { it.hasVisualOverflow })
        }
    }

    /**
     * Opens the new module through the real legacy-home entry point.
     *
     * @return Unit; missing destinations or load errors fail with a bounded timeout.
     */
    private fun home() {
        ui.waitUntil(30000) { ui.onAllNodesWithText("Tools").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithText("Tools").performClick()
        ui.onNodeWithText("Escalator Engineering").performClick()
        ui.onNode(hasSetTextAction() and hasText("Access code")).performTextReplacement("789")
        ui.onNodeWithText("Unlock").performClick()
        ui.waitUntil(30000) { ui.onAllNodes(hasSetTextAction() and hasText("Search engineering topics and tools")).fetchSemanticsNodes().isNotEmpty() }
    }

    /**
     * Captures actual activity rendering, not a fabricated UI image or a physical-device
     * screenshot.
     *
     * @param name Preview basename without extension.
     * @return Unit; rendering or output failures fail the test.
     */
    private fun save(name: String) {
        ui.mainClock.advanceTimeBy(1000)
        ui.waitForIdle()
        ui.runOnIdle {
            val v = ui.activity.window.decorView
            val bitmap =
                android.graphics.Bitmap.createBitmap(
                    v.width,
                    v.height,
                    android.graphics.Bitmap.Config.ARGB_8888,
                )
            v.draw(android.graphics.Canvas(bitmap))
            val folder = File("build/design-previews")
            folder.mkdirs()
            File(folder, "electro-$name.png").outputStream().use {
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
            }
            bitmap.recycle()
        }
    }

    /**
     * Proves exact global fault lookup, detail navigation, bookmark persistence and related
     * component links.
     *
     * @return Unit; any missing route, source classification or bookmark fails.
     */
    @Test
    fun faultAndRelatedComponentNavigation() {
        home()
        save("dashboard")
        ui.onNode(hasSetTextAction()).performTextReplacement("E217")
        ui.onNodeWithText("E217 — Drive Chain Switch").performClick()
        ui.onNodeWithText("Reset classification is not restart permission").assertExists()
        save("fault")
        ui.onNodeWithContentDescription("Add favourite").performClick()
        ui.activityRule.scenario.recreate()
        ui.waitForIdle()
        ui.onAllNodesWithText("E217", substring = true).assertAny(hasText("E217", substring = true))
        ui.onNodeWithContentDescription("Remove favourite").assertExists()
        ui.onNode(verticalList())
            .performScrollToNode(hasText("S21A — Drive chain switch — suffix A not verified"))
        ui.onNodeWithText("S21A — Drive chain switch — suffix A not verified").performClick()
        ui.onNodeWithText("Content status").assertExists()
    }

    /**
     * Verifies the fault explorer's real search and group/reset filters with original numeric code
     * strings.
     *
     * @return Unit; failure to return the exact supplied maintenance code fails.
     */
    @Test
    fun explorerSearchAndFilters() {
        home()
        ui.onNode(verticalList())
            .performScrollToNode(hasText("Escalator Fault Code Explorer"))
        ui.onNodeWithText("Escalator Fault Code Explorer").performClick()
        ui.onNode(hasSetTextAction()).performTextReplacement("164")
        ui.onNode(hasText("164") and !hasSetTextAction()).assertExists()
        ui.onNodeWithText("Warning").performClick()
        ui.onNodeWithText("Auto").performClick()
        ui.onNodeWithText("Need maintenance").assertExists()
        save("explorer")
    }

    /**
     * Exercises controller-number selection and source-backed diagram inspection/close controls.
     *
     * @return Unit; missing I/O explanation or nonfunctional dialog controls fail.
     */
    @Test
    fun terminalAndCutawaySelection() {
        home()
        ui.onNode(verticalList()).performScrollToNode(hasText("Explore the escalator diagram"))
        ui.onNodeWithText("Explore the escalator diagram").performClick()
        ui.onNodeWithText("Inspect drawing").performScrollTo().performClick()
        ui.onNodeWithText("Zoom in").performClick()
        ui.onNodeWithText("Reset view").performClick()
        ui.onNodeWithText("Close drawing").performClick()
        ui.onNode(hasSetTextAction()).performTextReplacement("I13")
        ui.onNodeWithText("I13 — 0.45 / 0.65 Speed Selection Switch").performClick()
        ui.onNodeWithText("Content status").assertExists()
        save("terminal")
    }

    /**
     * Checks an actual new calculator example, live result update and saved-field recreation.
     *
     * @return Unit; mismatched independent torque, lost state or route failures fail.
     */
    @Test
    fun motorCalculatorPersists() {
        home()
        ui.onNode(hasSetTextAction()).performTextReplacement("Motor shaft torque")
        ui.onNode(hasText("Motor shaft torque") and !hasSetTextAction()).performClick()
        ui.onNodeWithText("Load example").performClick()
        ui.onNode(verticalList())
            .performScrollToNode(hasText("Torque: 146.912 N·m", substring = true))
        ui.onNodeWithText("Torque: 146.912 N·m", substring = true).assertExists()
        save("torque")
        ui.activityRule.scenario.recreate()
        ui.waitForIdle()
        ui.onNodeWithText("Torque: 146.912 N·m", substring = true).assertExists()
    }

    /**
     * Proves the CBM page is a labelled local simulation and its event opens the exact supplied
     * fault.
     *
     * @return Unit; missing simulation labels, event controls or reference links fail.
     */
    @Test
    fun cbmSampleLinksToReference() {
        home()
        ui.onNode(verticalList())
            .performScrollToNode(hasText("Condition-Based Monitoring"))
        ui.onNodeWithText("Condition-Based Monitoring").performClick()
        ui.onNode(verticalList()).performScrollToNode(hasText("Water-event example"))
        ui.onNodeWithText("Water-event example").performClick()
        ui.onNodeWithText("Wet pit — simulated event").assertExists()
        save("cbm")
        ui.onNodeWithText("Open related reference: E065").performClick()
        ui.onNodeWithText("E065").assertExists()
    }
    /** @return Unit; both brand toolbar and search remain visible after catalogue scrolling. */
    @Test fun catalogueKeepsHeaderAndSearchPinned() {
        ui.waitUntil(30000) { ui.onAllNodesWithText("Tools").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithText("Tools").performClick()
        ui.onNodeWithText("Electrical Calculators").performClick()
        ui.onAllNodes(verticalList()).onLast().performScrollToNode(hasText("Voltage divider"))
        ui.onAllNodes(verticalList()).onLast().performTouchInput { swipeUp() }
        ui.mainClock.advanceTimeBy(1000)
        ui.onNodeWithContentDescription("Info").assertIsDisplayed()
        ui.onNode(hasSetTextAction() and hasText("Search")).assertIsDisplayed()
        ui.onNodeWithText("Your electrical & electronics workbench").assertIsNotDisplayed()
        save("catalogue-scrolled")
    }

}
