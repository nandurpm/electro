package com.electrokit.app.domain

import org.junit.Assert.*
import org.junit.Test

/** Independent numeric examples and invalid domains exercise the new public utility models. */
class WorkbenchToolsTests {
    /** @return Unit; ideal transformer ratio must agree with independently calculated voltage. */
    @Test fun transformerRatio() { assertEquals("24 V", WorkbenchTools.calculate("transformer", listOf("240", "1000", "100"))) }
    /** @return Unit; runtime uses explicit discharge and efficiency factors rather than hidden defaults. */
    @Test fun upsAccountsForBothLossFactors() { assertEquals("4.8 h", WorkbenchTools.calculate("ups", listOf("24", "100", "50", "80", "200"))) }
    /** @return Unit; DC round-trip cable resistance must produce the independently derived area. */
    @Test fun cableSizingIncludesReturnPath() { assertEquals("3.5 mm²", WorkbenchTools.calculate("cable", listOf("10", "10", "0.0175", "1"))) }
    /** @return Unit; invalid power factor cannot be reported as a valid engineering result. */
    @Test fun invalidPowerFactorRejected() { assertTrue(WorkbenchTools.calculate("pf", listOf("500", "100", "2")).contains("cannot exceed")) }
    /** @return Unit; absolute zero is valid but lower thermodynamic temperature is rejected. */
    @Test fun temperatureBoundary() { assertTrue(WorkbenchTools.calculate("temperature", listOf("-273.15")).contains("0 K")); assertTrue(WorkbenchTools.calculate("temperature", listOf("-274")).contains("below absolute")) }
    /** @return Unit; parser precedence, radians and right-associative powers follow documented grammar. */
    @Test fun scientificGrammar() {
        assertEquals("19", WorkbenchTools.calculate("scientific", listOf("2+3^2+sqrt(64)")))
        assertEquals("512", WorkbenchTools.calculate("scientific", listOf("2^3^2")))
        assertEquals("-4", WorkbenchTools.calculate("scientific", listOf("-2^2")))
        assertEquals("1", WorkbenchTools.calculate("scientific", listOf("sin(pi/2)")))
    }
    /** @return Unit; malformed expressions, numerical overflow and zero divisors never escape as results. */
    @Test fun scientificInvalidInputs() {
        for (expression in listOf("1/0", "sqrt(-1)", "ln(0)", "1e999", "eval(1)", "1;2", "(".repeat(300))) {
            val output = WorkbenchTools.calculate("scientific", listOf(expression))
            assertFalse(output, output in listOf("NaN", "Infinity", "-Infinity"))
            assertTrue(output, output.any(Char::isLetter))
        }
    }
    /** @return Unit; percentages cannot silently exceed the stated model's physical range. */
    @Test fun invalidPercentagesRejected() { assertTrue(WorkbenchTools.calculate("ups", listOf("24", "100", "101", "80", "200")).contains("at most 100")) }
    @Test fun typedUtilityResultsSeparateFailuresFromValues() {
        assertTrue(WorkbenchTools.evaluate("transformer", listOf("240", "1000", "100")) is Calculation.Value)
        assertEquals("24 V", WorkbenchTools.evaluate("transformer", listOf("240", "1000", "100")).text)
        for (input in listOf(listOf("", "", ""), listOf("240", "0", "100"))) {
            assertTrue(WorkbenchTools.evaluate("transformer", input) is Calculation.Error)
        }
        assertTrue(WorkbenchTools.evaluate("missing", emptyList()) is Calculation.Error)
        assertTrue(WorkbenchTools.evaluate("scientific", listOf("sqrt(-1)")) is Calculation.Error)
    }
    @Test fun radixConversionValidatesItsBaseAndDigits() {
        assertTrue(WorkbenchTools.calculate("radix", listOf("FF", "16")).contains("Decimal: 255"))
        assertTrue(WorkbenchTools.calculate("radix", listOf("65", "10")).contains("ASCII: A"))
        assertTrue(WorkbenchTools.evaluate("radix", listOf("2", "2")) is Calculation.Error)
        assertTrue(WorkbenchTools.evaluate("radix", listOf("10", "8")) is Calculation.Error)
    }
}
