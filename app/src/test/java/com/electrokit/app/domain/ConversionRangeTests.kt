package com.electrokit.app.domain

import org.junit.Assert.*
import org.junit.Test

/** Identity conversions must preserve representable values at both floating-point extremes. */
class ConversionRangeTests {
    @Test
    fun sameUnitDoesNotOverflowOrUnderflow() {
        assertEquals(1e308, UnitConversions.convert("Voltage", "kV", "kV", 1e308), 0.0)
        assertEquals(1e-308, UnitConversions.convert("Capacitance", "pF", "pF", 1e-308), 0.0)
    }
}
