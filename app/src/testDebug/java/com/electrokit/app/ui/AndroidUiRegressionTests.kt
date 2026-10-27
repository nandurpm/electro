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

/**
 * Groups regression assertions for android ui regression tests; unexpected behaviour fails the test
 * visibly.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w360dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AndroidUiRegressionTests {
    /** Compose test rule owns Activity lifecycle and semantics synchronisation. */
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()

    /**
     * Waits for asynchronous registry loading before querying semantics, avoiding startup race
     * failures.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    private fun ready() {
        ui.waitUntil(30_000) {
            ui.onAllNodesWithText("Tools").fetchSemanticsNodes().isNotEmpty()
        }
        ui.onNodeWithText("Tools").performClick()
    }

    /**
     * Uses visible catalogue search rather than brittle row indices or scroll positions to open a
     * calculator.
     *
     * @param title Visible calculator title used to select the intended test route.
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    private fun openCalculator(title: String) {
        ready()
        ui.onNode(hasSetTextAction() and hasText("Search")).performTextReplacement(title)
        ui.onNode(hasText(title) and !hasSetTextAction()).performClick()
        ui.waitForIdle()
    }

    /**
     * Targets the editable node by its visible label so the test follows the actual user input
     * path.
     *
     * @param label Visible field/cell caption used for accessible presentation.
     * @param value Input scalar in the operation’s documented units.
     * @return A finite scalar in the expected field unit, or null for incompatible units/invalid
     *   input. Assertion/time-out failures propagate; the failure fixture deliberately throws
     *   IOException while enabled.
     */
    private fun input(label: String, value: String) {
        ui.onNode(hasSetTextAction() and hasText(label)).performTextReplacement(value)
    }

    /** A cached explicit result must not survive a mode change with different physical behavior.
     * @return Unit; at t=0 a 5 V capacitor model changes from 0 V charge to 5 V discharge.
     */
    @Test fun calculationModeChangeInvalidatesExplicitResult() {
        openCalculator("Capacitor charge")
        ui.onNode(hasText("Mode:", substring = true)).performClick()
        ui.onNodeWithText("Charge").performClick()
        input("Initial / supply voltage", "5")
        input("Elapsed time", "0")
        ui.onNodeWithText("Calculate").performScrollTo().performClick()
        ui.onNodeWithText("Voltage: 0 V", substring = true).assertExists()
        ui.onNode(hasText("Mode:", substring = true)).performScrollTo().performClick()
        ui.onNodeWithText("Discharge").performClick()
        ui.onNodeWithText("Voltage: 5 V", substring = true).assertExists()
    }

    /**
     * Verifies main tabs and reference navigation work; unexpected behaviour fails the test
     * visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun mainTabsAndReferenceNavigationWork() {
        ready()
        ui.onNodeWithText("Pin-outs").performClick()
        ui.onNodeWithText("USB port").performClick()
        ui.onNodeWithText("Universal Serial Bus").assertExists()
        ui.onNode(verticalList())
            .performScrollToNode(hasText("USB 3.x", substring = true))
        ui.onNodeWithText("USB 3.x Standard-A").assertIsDisplayed()
        ui.runOnIdle { ui.activity.onBackPressedDispatcher.onBackPressed() }
        ui.onNodeWithText("Reference").performClick()
        ui.onNodeWithText("Resistivity Table").performClick()
        ui.onAllNodesWithText("Resistivity Table").assertCountEquals(2)
    }

    /**
     * Verifies extreme ratio is an inline error instead of a crash; unexpected behaviour fails the
     * test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun extremeRatioIsAnInlineErrorInsteadOfACrash() {
        openCalculator("Resistor Ratio")
        input("Target R2/R1", "1e9")
        ui.onNodeWithText("Target ratio must be between", substring = true).assertExists()
        input("Target R2/R1", "1e-9")
        ui.onNodeWithText("Target ratio must be between", substring = true).assertExists()
        input("Target R2/R1", "2.5")
        ui.onNodeWithText("R1:", substring = true).assertExists()
    }

    /**
     * Verifies selected kilo unit does not multiply a suffixed value twice; unexpected behaviour
     * fails the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun selectedKiloUnitDoesNotMultiplyASuffixedValueTwice() {
        openCalculator("Ohm’s Law")
        // Resistance has the only unit selector initially labelled Ω.
        ui.onNodeWithText("Ω", substring = false).performClick()
        ui.onNodeWithText("kΩ", substring = false).performClick()
        input("Resistance", "10k")
        ui.onNodeWithText("Typed suffix / unit used; selector ignored").assertExists()
        ui.onNodeWithText("Resistance: 10 kΩ", substring = true).assertExists()
        ui.onNodeWithText("Current: 1.2 mA", substring = true).assertExists()
    }

    /**
     * Verifies fractional tolerance uses percent text; unexpected behaviour fails the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun fractionalToleranceUsesPercentText() {
        openCalculator("Resistor Colour Code")
        ui.onNodeWithContentDescription("Green tol ±0.5 percent").performScrollTo().performClick()
        ui.onNodeWithText("1.1 kΩ ±0.5%").assertExists()
        ui.onNodeWithText("500 m%", substring = true).assertDoesNotExist()
    }

    /** Reset restores both band renderers and the result used by Copy, without a prior cached tolerance.
     * @return Unit; resistor and inductor copies must contain default five-percent tolerance.
     */
    @Test fun bandResetRestoresDisplayAndCopiedResult() {
        for ((title, expected) in listOf("Resistor Colour Code" to "1.1 kΩ ±5%", "Inductor Colour Code" to "1.1 mH ±5%")) {
            openCalculator(title)
            ui.onNodeWithContentDescription("Calculator menu").performClick()
            ui.onNodeWithText("Reset").performClick()
            ui.onNodeWithContentDescription("Green tol ±0.5 percent").performScrollTo().performClick()
            ui.onNodeWithText("±0.5%", substring = true).assertExists()
            ui.onNodeWithContentDescription("Calculator menu").performClick()
            ui.onNodeWithText("Reset").performClick()
            ui.onNodeWithText(expected).assertExists()
            ui.onNodeWithContentDescription("Calculator menu").performClick()
            ui.onNodeWithText("Copy result").performClick()
            ui.runOnIdle {
                val clipboard = ui.activity.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val text = clipboard.primaryClip!!.getItemAt(0).coerceToText(ui.activity).toString()
                assertTrue(text, text.contains(expected))
                assertFalse(text, text.contains("±0.5%"))
            }
            ui.runOnIdle { ui.activity.onBackPressedDispatcher.onBackPressed() }
        }
    }

    /**
     * Verifies calculator input survives activity recreation; unexpected behaviour fails the test
     * visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun calculatorInputSurvivesActivityRecreation() {
        openCalculator("Voltage divider")
        input("Input voltage", "24")
        org.robolectric.RuntimeEnvironment.setQualifiers("w800dp-h360dp-land")
        ui.activityRule.scenario.recreate()
        ui.waitForIdle()
        ui.onNode(hasSetTextAction() and hasText("Input voltage")).assertTextContains("24")
        ui.onNodeWithText("Vout: 12 V", substring = true).assertExists()
    }

    /**
     * Verifies light theme persists across activity recreation; unexpected behaviour fails the test
     * visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun lightThemePersistsAcrossActivityRecreation() {
        ready()
        ui.onNodeWithContentDescription("Menu").performClick()
        ui.onNode(hasText("Settings") and hasClickAction() and !hasAnyAncestor(hasTestTag("main_navigation"))).performClick()
        // Establish a dark baseline: other preview tests deliberately persist a light preference.
        if (ui.onAllNodes(isOn()).fetchSemanticsNodes().isNotEmpty()) {
            ui.onNode(isToggleable()).performClick()
            ui.waitUntil(10_000) { ui.onAllNodes(isOff()).fetchSemanticsNodes().isNotEmpty() }
        }
        ui.onNode(isToggleable()).assertIsOff().performClick()
        ui.waitUntil(10_000) { ui.onAllNodes(isOn()).fetchSemanticsNodes().isNotEmpty() }
        ui.onNode(isToggleable()).assertIsOn()
        ui.activityRule.scenario.recreate()
        ui.waitForIdle()
        ui.onNode(isToggleable()).assertIsOn()
    }

    /**
     * Verifies every catalogue entry opens without crashing; unexpected behaviour fails the test
     * visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun everyCatalogueEntryOpensWithoutCrashing() {
        ready()
        val repository =
            com.electrokit.app.data.Repository(org.robolectric.RuntimeEnvironment.getApplication())
        kotlinx.coroutines.runBlocking { repository.loadAssets() }
        var opened = 0
        for (tab in listOf("CALCULATORS", "PIN-OUTS", "RESOURCES")) {
            ui.onNodeWithText(mapOf("CALCULATORS" to "Tools", "PIN-OUTS" to "Pin-outs", "RESOURCES" to "Reference").getValue(tab)).performClick()
            ui.waitForIdle()
            for (entry in repository.registry.filter { it.tab == tab }) {
                ui.onNode(hasSetTextAction() and hasText("Search"))
                    .performTextReplacement(entry.title)
                ui.onNode(hasText(entry.title) and !hasSetTextAction()).performClick()
                ui.onNodeWithContentDescription("Back").assertIsDisplayed()
                ui.onAllNodesWithText(entry.title).assertAny(hasText(entry.title))
                ui.runOnIdle { ui.activity.onBackPressedDispatcher.onBackPressed() }
                ui.waitForIdle()
                opened++
            }
        }
        assertEquals(107, opened)
    }

    /**
     * Verifies bundled databases open without enablement and retain working filters; unexpected
     * behaviour fails the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun bundledDatabasesAlwaysOpenAndSearch() {
        ready()
        val repository =
            com.electrokit.app.data.Repository(org.robolectric.RuntimeEnvironment.getApplication())
        kotlinx.coroutines.runBlocking {
            repository.loadAssets()
            repository.update { it.copy(modules = emptySet()) }
        }
        ui.onNodeWithText("34 calculators", substring = true).assertExists()
        ui.onAllNodesWithText("Data").onLast().performClick()
        for (entry in repository.registry.filter { it.tab == "PLUGINS" }) {
            ui.onNode(hasSetTextAction() and hasText("Search databases"))
                .performTextReplacement(entry.title)
            ui.onNodeWithText("ENABLE").assertDoesNotExist()
            ui.onNodeWithText("DISABLE").assertDoesNotExist()
            ui.onNodeWithText("OPEN DATABASE", substring = true).performClick()
            ui.onNodeWithText("Numeric parameter: Any").assertExists()
            val name = repository.parts.getValue(entry.id).first().name
            ui.onNode(hasSetTextAction() and hasText("Search")).performTextReplacement(name)
            ui.onNode(hasText(name) and !hasSetTextAction()).performClick()
            ui.onNodeWithText("Product reference:", substring = true).assertExists()
            if (entry.id == "m1") {
                ui.onNode(hasSetTextAction() and hasText("Search")).performTextClearance()
                ui.onNodeWithText("Numeric parameter: Any").performClick()
                ui.onNodeWithText("Output V").performClick()
                input("Minimum", "-12")
                input("Maximum", "0")
                ui.onNodeWithText("LM7905").assertExists()
                ui.onNodeWithText("LM7912").assertExists()
                ui.onNodeWithText("LM7805").assertDoesNotExist()
            }
            ui.runOnIdle { ui.activity.onBackPressedDispatcher.onBackPressed() }
            ui.waitForIdle()
        }
    }

    /**
     * Verifies large text keeps main actions reachable; unexpected behaviour fails the test
     * visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun largeTextKeepsMainActionsReachable() {
        org.robolectric.RuntimeEnvironment.setFontScale(2f)
        ui.activityRule.scenario.recreate()
        ready()
        assertEquals(2f, ui.activity.resources.configuration.fontScale, 0f)
        ui.onNodeWithContentDescription("Info").assertIsDisplayed().performClick()
        ui.onNodeWithText("WHAT’S INCLUDED").assertIsDisplayed().performClick()
        ui.onNodeWithText("Resistor Colour Code").assertExists()
    }

    /**
     * Ensures the catalogue uses the bottom viewport instead of reserving the old 56 dp footer.
     *
     * @return Unit; the measured viewport includes only ordinary system-navigation clearance.
     * @throws AssertionError if a persistent empty footer reduces the list viewport.
     */
    @Test
    fun catalogueHasNoReservedBottomFooter() {
        ready()
        val rootBounds = ui.onRoot().fetchSemanticsNode().boundsInRoot
        val listBounds =
            ui.onAllNodes(verticalList())
                .fetchSemanticsNodes()
                .first {
                    it.boundsInRoot.center.x > 0 && it.boundsInRoot.center.x < rootBounds.width
                }
                .boundsInRoot
        val navigation = ui.onNodeWithTag("main_navigation").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val density = ui.activity.resources.displayMetrics.density
        // Visible navigation is functional UI; separate blank strips remain regressions.
        val listGap = kotlin.math.abs(navigation.top - listBounds.bottom) / density
        val systemGap = (rootBounds.bottom - navigation.bottom) / density
        assertTrue("Empty strip before navigation: $listGap dp", listGap <= 1f)
        assertTrue("Unexpected empty footer: $systemGap dp", systemGap <= 48f)
    }
}
