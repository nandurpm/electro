package com.electrokit.app.domain

import org.junit.Assert.*
import org.junit.Test

/**
 * Groups regression assertions for formatting regression tests; unexpected behaviour fails the test
 * visibly.
 */
class FormattingRegressionTests {
    /**
     * Verifies percentages are rounded without changing band tolerances; unexpected behaviour fails
     * the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun percentagesAreRoundedWithoutChangingBandTolerances() {
        assertEquals("52.38%", Engineering.percent(52.38095238095238))
        assertEquals("9.421%", Engineering.percent(9.421333333333333))
        assertEquals("0.6061%", Engineering.percent(0.60606060606061))
        for (value in listOf(0.5, 0.25, 0.1, 0.05)) assertEquals(
            "$value%",
            Engineering.percent(value),
        )
    }

    /**
     * Verifies default rtd temperature is exactly positive zero; unexpected behaviour fails the
     * test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun defaultRtdTemperatureIsExactlyPositiveZero() {
        assertEquals(0.0, Circuits.rtdTemperature(100.0, 100.0), 0.0)
        assertEquals(0L, Circuits.rtdTemperature(100.0, 100.0).toBits())
        val spec = Calculators.spec(26)
        assertTrue(
            Calculators.calculate(26, spec.fields.associate { it.key to it.initial })
                .text
                .contains("Temperature: 0 °C")
        )
    }

    /**
     * Verifies unitless values never use engineering prefixes; unexpected behaviour fails the test
     * visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun unitlessValuesNeverUseEngineeringPrefixes() {
        assertEquals("1E+7", Engineering.format(1e7))
        assertEquals("1E-7", Engineering.format(1e-7))
        assertEquals("12.3457 V/V", Engineering.format(12.3456789, "V/V"))
        assertEquals("0", Engineering.format(-0.0))
    }

    /**
     * Verifies smd r notation uses plain decimal digits; unexpected behaviour fails the test
     * visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun smdRNotationUsesPlainDecimalDigits() {
        assertTrue(Resistors.smdCodes(0.0001).contains("0R0001"))
        assertTrue(Resistors.smdCodes(4.7).contains("4R7"))
        assertFalse(Resistors.smdCodes(0.0001).contains("1R0E"))
    }

    /**
     * Verifies mega aliases and micro values are exact; unexpected behaviour fails the test
     * visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun megaAliasesAndMicroValuesAreExact() {
        assertEquals(1e6, Engineering.parse("1Meg")!!, 0.0)
        assertEquals(1e6, Engineering.parse("1MEG")!!, 0.0)
        assertEquals(1e-5, Engineering.parse("10u")!!, 0.0)
        assertEquals(1e-5, Engineering.parse("10uF")!!, 0.0)
    }
}
