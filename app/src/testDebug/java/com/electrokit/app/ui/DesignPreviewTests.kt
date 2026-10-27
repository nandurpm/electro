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

/**
 * Groups regression assertions for design preview tests; unexpected behaviour fails the test
 * visibly.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w412dp-h915dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DesignPreviewTests {
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
        ui.waitUntil(30000) {
            ui.onAllNodesWithText("Resistor Colour Code").fetchSemanticsNodes().isNotEmpty()
        }
    }

    /**
     * Waits for navigation animation then draws the actual decor view, avoiding unsupported
     * PixelCopy in Robolectric.
     *
     * @param name Asset filename or preview output name, as stated by the operation.
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    private fun save(name: String) {
        ui.mainClock.advanceTimeBy(1000)
        ui.waitForIdle()
        val folder = File("build/design-previews")
        folder.mkdirs()
        ui.runOnIdle {
            val view = ui.activity.window.decorView
            val bitmap =
                android.graphics.Bitmap.createBitmap(
                    view.width,
                    view.height,
                    android.graphics.Bitmap.Config.ARGB_8888,
                )
            view.draw(android.graphics.Canvas(bitmap))
            File(folder, name + ".png").outputStream().use {
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
            }
            bitmap.recycle()
        }
    }

    /**
     * Sets the requested palette only when necessary so prior saved test state cannot invert the
     * desired preview.
     *
     * @param light True selects the light palette.
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    private fun theme(light: Boolean) {
        ui.onNodeWithContentDescription("Menu").performClick()
        ui.onNodeWithText("Settings").performClick()
        val wanted =
            if (light) androidx.compose.ui.state.ToggleableState.On
            else androidx.compose.ui.state.ToggleableState.Off
        val toggle = ui.onNode(isToggleable())
        if (
            toggle
                .fetchSemanticsNode()
                .config[androidx.compose.ui.semantics.SemanticsProperties.ToggleableState] != wanted
        )
            toggle.performClick()
        ui.waitUntil(10000) {
            ui.onNode(isToggleable())
                .fetchSemanticsNode()
                .config[androidx.compose.ui.semantics.SemanticsProperties.ToggleableState] == wanted
        }
        ui.runOnIdle { ui.activity.onBackPressedDispatcher.onBackPressed() }
        ui.waitForIdle()
    }

    /**
     * Verifies home preview; unexpected behaviour fails the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun homePreview() {
        ready()
        theme(false)
        save("home")
        theme(true)
        save("light")
    }

    /**
     * Verifies connector preview; unexpected behaviour fails the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun connectorPreview() {
        ready()
        ui.onNodeWithText("Pin-outs").performClick()
        ui.onNodeWithText("USB port").performClick()
        save("usb")
        ui.onNode(hasSetTextAction() and hasText("Search this reference"))
            .performTextReplacement("USB 1")
        save("table")
    }

    /**
     * Captures the actual new connector families in both palettes to reveal clipping and contrast
     * issues.
     *
     * @return Unit; missing routes, inaccessible search fields and rendering failures fail the
     *   test.
     */
    @Test
    fun illustratedReferencePreviews() {
        ready()
        theme(true)
        ui.onNodeWithText("Pin-outs").performClick()
        for ((title, filename) in
            listOf(
                "Serial port" to "serial-illustration",
                "ATX Power connectors" to "power-illustration",
                "Arduino Uno board" to "uno-illustration",
                "RCA connector" to "rca-illustration",
                "VGA connector" to "vga-illustration",
                "Fibre optic cable colour" to "fibre-table",
            )) {
            ui.onNode(hasSetTextAction()).performTextReplacement(title)
            ui.onNode(hasText(title) and !hasSetTextAction(), useUnmergedTree = true).performClick()
            save(filename)
            if (filename == "serial-illustration") {
                ui.onAllNodesWithText("Enlarge diagram")[0].performScrollTo().performClick()
                ui.onNodeWithText("Close diagram").assertIsDisplayed()
                ui.onNodeWithText("Close diagram").performClick()
            }
            ui.runOnIdle { ui.activity.onBackPressedDispatcher.onBackPressed() }
            ui.waitForIdle()
        }
    }
}
