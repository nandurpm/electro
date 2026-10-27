package com.electrokit.app.domain

import org.junit.Assert.*
import org.junit.Test

/** Dimensional examples exercise both SI prefix factors and affine temperature conversions. */
class UnitConversionsTests {
    /** @return Unit; representative conversions preserve magnitude and exact unit definitions. */
    @Test fun independentConversions() {
        assertEquals(3300.0, UnitConversions.convert("Voltage", "V", "mV", 3.3), 1e-9)
        assertEquals(0.001, UnitConversions.convert("Current", "mA", "A", 1.0), 1e-12)
        assertEquals(100.0, UnitConversions.convert("Temperature", "°F", "°C", 212.0), 1e-9)
        assertEquals(273.15, UnitConversions.convert("Temperature", "°C", "K", 0.0), 1e-9)
        assertEquals(25.4, UnitConversions.convert("Length", "in", "mm", 1.0), 1e-9)
        assertEquals(1000.0, UnitConversions.convert("Capacitance", "µF", "nF", 1.0), 1e-9)
    }
    /** @return Unit; cross-dimension units, non-finite scalars and impossible temperatures fail. */
    @Test fun invalidDomainsRejected() {
        assertThrows(IllegalArgumentException::class.java) { UnitConversions.convert("Voltage", "V", "A", 1.0) }
        assertThrows(IllegalArgumentException::class.java) { UnitConversions.convert("Temperature", "K", "°C", -1.0) }
        assertThrows(IllegalArgumentException::class.java) { UnitConversions.convert("Voltage", "V", "mV", Double.POSITIVE_INFINITY) }
    }
}
