package com.electrokit.app.domain

import kotlin.math.pow
import org.junit.Assert.*
import org.junit.Test

/**
 * Groups regression assertions for audit regression tests; unexpected behaviour fails the test
 * visibly.
 */
class AuditRegressionTests {
    /**
     * Derives fixtures from the shipping specification instead of duplicating a stale default-input
     * table.
     *
     * @param id Stable calculator or registry identifier; independent of the visible title.
     * @return Default field values derived from the shipping calculator specification.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    private fun defaults(id: Int) = Calculators.spec(id).fields.associate { it.key to it.initial }

    /**
     * Verifies extreme ratios return errors without throwing; unexpected behaviour fails the test
     * visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun extremeRatiosReturnErrorsWithoutThrowing() {
        for (ratio in listOf("1e-309", "1e-9", "1e-8", "1e8", "1e9", "1e308")) {
            for (mode in 0..2) {
                val result = Calculators.calculate(8, defaults(8) + ("ratio" to ratio), mode)
                assertTrue("$ratio/$mode: ${result.text}", result is Calculation.Error)
            }
        }
    }

    /**
     * Verifies supported ratio boundaries find pairs; unexpected behaviour fails the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun supportedRatioBoundariesFindPairs() {
        for (ratio in listOf("1e-7", "1", "1e7")) {
            val result = Calculators.calculate(8, defaults(8) + ("ratio" to ratio), 2)
            assertTrue(result.text, result is Calculation.Value)
        }
    }

    /**
     * Verifies explicit suffix overrides every selector; unexpected behaviour fails the test
     * visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun explicitSuffixOverridesEverySelector() {
        for (exp in listOf(-12, -9, -6, -3, 0, 3, 6, 9)) {
            assertEquals(10000.0, Engineering.input("10k", exp)!!, 0.0)
            assertEquals(4700.0, Engineering.input("4k7", exp)!!, 0.0)
            assertEquals(12.0, Engineering.input("12V", exp)!!, 0.0)
            assertEquals(10.0 * 10.0.pow(exp), Engineering.input("10", exp)!!, 0.0)
        }
    }

    /**
     * Verifies explicit units are converted to the field unit; unexpected behaviour fails the test
     * visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun explicitUnitsAreConvertedToTheFieldUnit() {
        assertEquals(100.0, Engineering.input("100mA", 0, "mA")!!, 1e-10)
        assertEquals(2.0, Engineering.input("2000W", 0, "kW")!!, 0.0)
        assertNull(Engineering.input("12V", 0, "A"))
    }

    /**
     * Verifies tolerance labels use ordinary percentages; unexpected behaviour fails the test
     * visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun toleranceLabelsUseOrdinaryPercentages() {
        val expected = listOf("1%", "2%", "0.5%", "0.25%", "0.1%", "0.05%", "5%", "10%")
        assertEquals(expected, Resistors.tolerance.values.map(Engineering::percent))
        for (x in Resistors.tolerance.values) assertEquals(
            Engineering.percent(x),
            Engineering.format(x, "%"),
        )
    }

    /**
     * Verifies pasted units and minus are accepted without java suffixes; unexpected behaviour
     * fails the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun pastedUnitsAndMinusAreAcceptedWithoutJavaSuffixes() {
        mapOf(
                "−5" to -5.0,
                "12V" to 12.0,
                "50Hz" to 50.0,
                "10 W" to 10.0,
                "10uF" to 1e-5,
                "2R2 Ω" to 2.2,
                "1Ω" to 1.0,
                "1e-3 A" to .001,
            )
            .forEach { (input, value) ->
                assertEquals(input, value, Engineering.parse(input)!!, 1e-12)
            }
        for (input in
            listOf(
                "10f",
                "1d",
                "500 mW class axial Zener",
                "10garbage",
                "1k2Vextra",
                "NaN",
                "Infinity",
                "1e999",
            )) assertNull(input, Engineering.parse(input))
    }

    /**
     * Verifies invalid persisted modes are clamped; unexpected behaviour fails the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun invalidPersistedModesAreClamped() {
        for (id in 1..31) for (mode in listOf(Int.MIN_VALUE, -1, 100, Int.MAX_VALUE)) {
            val value = Calculators.calculate(id, defaults(id), mode)
            assertTrue(value.text.isNotBlank())
            assertFalse(value.text.contains("IndexOutOfBounds"))
        }
    }

    /**
     * Verifies inactive op amp inputs can be empty; unexpected behaviour fails the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun inactiveOpAmpInputsCanBeEmpty() {
        for (mode in 0..1) assertTrue(
            Calculators.calculate(12, defaults(12) + mapOf("v2" to "", "r3" to ""), mode)
                is Calculation.Value
        )
        assertTrue(Calculators.calculate(12, defaults(12) + ("r3" to ""), 3) is Calculation.Value)
        assertTrue(Calculators.calculate(12, defaults(12) + ("r3" to ""), 2) is Calculation.Error)
    }

    /**
     * Verifies rounding normalises engineering prefixes; unexpected behaviour fails the test
     * visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun roundingNormalisesEngineeringPrefixes() {
        assertEquals("1 MΩ", Engineering.format(999999.9, "Ω"))
        assertEquals("1 V", Engineering.format(.9999999, "V"))
        assertFalse(Engineering.format(1e-18, "F").contains(" p"))
        assertEquals("4095", Engineering.decimal(4095.0))
    }

    /**
     * Verifies negative module values filter correctly; unexpected behaviour fails the test
     * visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun negativeModuleValuesFilterCorrectly() {
        assertTrue(NumericFilter.matches("−5", "-12", "0"))
        assertFalse(NumericFilter.matches("5", "-12", "0"))
        assertFalse(NumericFilter.matches("−12", "-10", "0"))
        assertFalse(NumericFilter.matches("5", "bad", ""))
        assertNotNull(NumericFilter.validation("10", "-10"))
        assertNotNull(NumericFilter.validation("", "bad"))
    }

    /**
     * Verifies extreme input grid never crashes or leaks requirement text; unexpected behaviour
     * fails the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun extremeInputGridNeverCrashesOrLeaksRequirementText() {
        val samples = listOf("", "garbage", "abc", "NaN", "-1", "0", "1e-300", "1e300")
        var exercised = 0
        for (id in 1..31) {
            val spec = Calculators.spec(id)
            for (mode in spec.modes.indices) for (field in spec.fields) for (input in samples) {
                exercised++
                val result = Calculators.calculate(id, defaults(id) + (field.key to input), mode)
                assertTrue("$id/${field.key}/$input", result.text.isNotBlank())
                assertFalse(
                    "$id/$mode/${field.key}/$input: ${result.text}",
                    result.text.contains("Failed requirement.") ||
                        result.text == "Check the input values and selected mode." ||
                        result.text.contains("NaN") ||
                        result.text.contains("Infinity"),
                )
            }
        }
        println("Extreme per-field cases exercised: $exercised")
    }

    /**
     * Verifies field errors name their inputs; unexpected behaviour fails the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun fieldErrorsNameTheirInputs() {
        val negativeTime = Calculators.calculate(11, defaults(11) + ("t" to "-1"))
        assertTrue(negativeTime is Calculation.Error)
        assertTrue(negativeTime.text.contains("Elapsed time"))
        val invalidCode = Calculators.calculate(2, defaults(2) + ("code" to "00A"))
        assertTrue(invalidCode.text.contains("01 to 96"))
    }
}
