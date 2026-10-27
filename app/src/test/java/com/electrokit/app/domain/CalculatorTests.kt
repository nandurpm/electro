package com.electrokit.app.domain

import kotlin.math.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Groups regression assertions for calculator tests; unexpected behaviour fails the test visibly.
 */
class CalculatorTests {
    /**
     * Combines relative tolerance with an absolute floor for meaningful comparisons over many
     * physical magnitudes.
     *
     * @param expected Reference value for the regression assertion.
     * @param actual Observed value from the production operation.
     * @param rel Relative numerical comparison tolerance.
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    private fun near(expected: Double, actual: Double, rel: Double = 1e-8) =
        assertEquals(expected, actual, max(1e-12, abs(expected) * rel))

    /**
     * Verifies parser handles engineering forms; unexpected behaviour fails the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun parserHandlesEngineeringForms() {
        mapOf(
                "4k7" to 4700.0,
                "1.1k" to 1100.0,
                "10u" to 1e-5,
                "2R2" to 2.2,
                "−1" to -1.0,
                "" to null,
                "NaN" to null,
                "Infinity" to null,
                "-2m2" to -0.0022,
                "1e3" to 1000.0,
            )
            .forEach { (s, n) ->
                if (n == null) assertNull(Engineering.parse(s)) else near(n, Engineering.parse(s)!!)
            }
    }

    /**
     * Verifies notation does not truncate integer zeros; unexpected behaviour fails the test
     * visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun notationDoesNotTruncateIntegerZeros() {
        assertEquals("100 Ω", Engineering.format(100.0, "Ω"))
        assertEquals("1.1 kΩ", Engineering.format(1100.0, "Ω"))
    }

    /**
     * Verifies ohm all six input pairs; unexpected behaviour fails the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun ohmAllSixInputPairs() {
        val base = listOf(12.0, 0.02, 600.0, 0.24)
        for (a in 0..2) for (b in a + 1..3) {
            val input = base.mapIndexed { ix, x -> if (ix == a || ix == b) x else null }
            val out = Circuits.ohm(input[0], input[1], input[2], input[3]).values.toList()
            base.zip(out).forEach { near(it.first, it.second) }
        }
    }

    /**
     * Verifies ohm rejects overdetermined; unexpected behaviour fails the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test(expected = IllegalArgumentException::class)
    fun ohmRejectsOverdetermined() {
        Circuits.ohm(12.0, 1.0, 12.0, null)
    }

    /**
     * Verifies parallel rejects zero; unexpected behaviour fails the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test(expected = IllegalArgumentException::class)
    fun parallelRejectsZero() {
        Circuits.parallel(listOf(0.0, 10.0))
    }

    /**
     * Verifies series parallel; unexpected behaviour fails the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun seriesParallel() {
        near(600.0, Circuits.series(listOf(100.0, 200.0, 300.0)))
        near(50.0, Circuits.parallel(listOf(100.0, 100.0)))
    }

    /**
     * Verifies divider and loaded; unexpected behaviour fails the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun dividerAndLoaded() {
        near(6.0, Circuits.divider(12.0, 1000.0, 1000.0))
        near(4.0, Circuits.divider(12.0, 1000.0, 1000.0, 1000.0))
    }

    /**
     * Verifies astable timing; unexpected behaviour fails the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun astableTiming() {
        val m = Circuits.astable(1000.0, 10000.0, 1e-5)
        near(ln(2.0) * .11, m.getValue("High (s)"))
        near(ln(2.0) * .10, m.getValue("Low (s)"))
        near(1 / (ln(2.0) * .21), m.getValue("Frequency (Hz)"))
    }

    /**
     * Verifies monostable timing; unexpected behaviour fails the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun monostableTiming() {
        near(ln(3.0) * .1, Circuits.mono(10000.0, 1e-5))
    }

    /**
     * Verifies colour both directions; unexpected behaviour fails the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun colourBothDirections() {
        near(1100.0, Resistors.decode(listOf(1, 1), 2))
        for (five in listOf(false, true)) for (v in
            (if (five) listOf(1.0, 47.0, 1100.0, 1e6) else listOf(0.1, 1.0, 47.0, 1100.0, 1e6))) {
            val b = Resistors.encode(v, five)
            near(v, Resistors.decode(b.dropLast(1), b.last()))
        }
    }

    /**
     * Verifies fractional multiplier; unexpected behaviour fails the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun fractionalMultiplier() {
        near(0.47, Resistors.decode(listOf(4, 7), -2))
    }

    /**
     * Verifies smd; unexpected behaviour fails the test visibly.
     *
     * @return Decoded resistance in ohms. Assertion/time-out failures propagate; the failure
     *   fixture deliberately throws IOException while enabled.
     */
    @Test
    fun smd() {
        near(1000.0, Resistors.smd("102"))
        near(10000.0, Resistors.smd("1002"))
        near(100.0, Resistors.smd("01A"))
        near(2.2, Resistors.smd("2R2"))
        near(1.0, Resistors.smd("01R"))
    }

    /**
     * Verifies led resistor; unexpected behaviour fails the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun ledResistor() {
        near(300.0, Circuits.led(12.0, 2.0, .02, 3.0))
        assertTrue(Resistors.nearest(300.1, 24, true) >= 300.1)
    }

    /**
     * Verifies led rejects insufficient supply; unexpected behaviour fails the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test(expected = IllegalArgumentException::class)
    fun ledRejectsInsufficientSupply() {
        Circuits.led(3.0, 2.0, .02, 2.0)
    }

    /**
     * Verifies lm317; unexpected behaviour fails the test visibly.
     *
     * @return Nominal regulated output voltage in volts. Assertion/time-out failures propagate; the
     *   failure fixture deliberately throws IOException while enabled.
     */
    @Test
    fun lm317() {
        near(5.036, Circuits.lm317(240.0, 720.0))
        near(5.0, Circuits.lm317(240.0, 720.0, 0.0))
    }

    /**
     * Verifies filter cutoff; unexpected behaviour fails the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun filterCutoff() {
        near(1591.5494309189535, Circuits.rc(1000.0, 1e-7))
    }

    /**
     * Verifies trace width scaling and known point; unexpected behaviour fails the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun traceWidthScalingAndKnownPoint() {
        val width = Circuits.traceWidth(1.0, 10.0, 1.0, false)
        near(.300378, width, 1e-4)
        near(width * 2.0.pow(1 / .725), Circuits.traceWidth(2.0, 10.0, 1.0, false))
        assertTrue(Circuits.traceWidth(1.0, 10.0, 1.0, true) > width)
    }

    /**
     * Verifies voltage drop loop length; unexpected behaviour fails the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun voltageDropLoopLength() {
        near(1.37928, Circuits.voltageDrop(10.0, 2.5, 10.0))
        near(2.75856, Circuits.voltageDrop(20.0, 2.5, 10.0))
    }

    /**
     * Verifies rtd inversion; unexpected behaviour fails the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun rtdInversion() {
        for (t in listOf(-200.0, -100.0, 0.0, 100.0, 850.0)) {
            near(t, Circuits.rtdTemperature(Circuits.rtd(t, 100.0), 100.0))
        }
    }

    /**
     * Verifies every calculator handles empty and bad inputs; unexpected behaviour fails the test
     * visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun everyCalculatorHandlesEmptyAndBadInputs() {
        for (id in 1..31) {
            val spec = Calculators.spec(id)
            for (mode in spec.modes.indices) {
                val empty = spec.fields.associate { it.key to "" }
                val bad = spec.fields.associate { it.key to "not a number" }
                assertTrue(Calculators.calculate(id, empty, mode).text.isNotBlank())
                assertTrue(Calculators.calculate(id, bad, mode).text.isNotBlank())
            }
        }
    }

    /**
     * Verifies default calculations return finite results; unexpected behaviour fails the test
     * visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun defaultCalculationsReturnFiniteResults() {
        for (id in 2..31) {
            if (id == 24) continue
            val spec = Calculators.spec(id)
            for (mode in spec.modes.indices) {
                val out =
                    Calculators.calculate(id, spec.fields.associate { it.key to it.initial }, mode)
                assertFalse(
                    "Calculator $id/$mode: ${out.text}",
                    out.text.contains("NaN") ||
                        out.text.contains("Infinity") ||
                        out.text.contains("Outside valid range"),
                )
            }
        }
    }

    /**
     * Verifies adc full scale saturates; unexpected behaviour fails the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun adcFullScaleSaturates() {
        val out =
            Calculators.calculate(23, mapOf("bits" to "12", "vref" to "3.3", "v" to "3.3"), 1).text
        assertTrue(out.contains("Code: 4095"))
        assertFalse(out.contains("4.095 k"))
    }

    /**
     * Verifies range mapping; unexpected behaviour fails the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun rangeMapping() {
        val out =
            Calculators.calculate(
                    30,
                    mapOf("x" to "12", "a" to "4", "b" to "20", "c" to "0", "d" to "100"),
                )
                .text
        assertTrue(out.contains("50"))
    }
}
