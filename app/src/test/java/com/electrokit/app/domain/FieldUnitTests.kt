package com.electrokit.app.domain

import org.junit.Assert.*
import org.junit.Test

/** Guards field-unit/parser agreement without duplicating the calculator's unit list. */
class FieldUnitTests {
    /**
     * Every numeric field must accept a pasted value bearing its declared unit.
     *
     * @return Unit; assertion failure identifies a schema/parser drift.
     */
    @Test
    fun everyDeclaredNumericFieldAcceptsItsUnit() {
        for (id in 1..31) for (field in Calculators.spec(id).fields.filter { !it.text }) {
            assertTrue("c$id ${field.key}: ${field.unit}", field.unit in UnitCatalog.fieldUnits)
            val actual = Engineering.input("1${field.unit}", 0, field.unit)
            assertNotNull("c$id ${field.key}: ${field.unit}", actual)
            assertEquals(1.0, actual!!, 1e-12)
        }
    }

    /**
     * Compound dimensions must be recognised before interpreting a leading SI letter.
     *
     * @return Unit; failures expose rejected or mis-scaled physical quantities.
     */
    @Test
    fun compoundQuantitiesAndConversions() {
        assertEquals(10.0, Engineering.input("10m/s", 3, "m/s")!!, 0.0)
        assertEquals(20.0, Engineering.input("20°C/W", 0, "°C/W")!!, 0.0)
        assertEquals(2.0, Engineering.input("2oz/ft²", 0, "oz/ft²")!!, 0.0)
        assertEquals(20.0, Engineering.input("20mA", 0, "mA")!!, 1e-12)
        assertEquals(20.0, Engineering.input("0.02A", 0, "mA")!!, 1e-12)
        assertEquals(2.0, Engineering.input("2000W", 0, "kW")!!, 0.0)
        assertNull(Engineering.input("20°C/W", 0, "m/s"))
    }

    /**
     * The same m token is contextual: metre in a metre field, milli in bare engineering notation.
     *
     * @return Unit; assertions prevent either interpretation from regressing.
     */
    @Test
    fun metresDoNotBreakMilliNotation() {
        assertEquals(0.01, Engineering.parse("10m")!!, 0.0)
        assertEquals(10.0, Engineering.input("10m", 3, "m")!!, 0.0)
        assertEquals(1000.0, Engineering.input("1km", 0, "m")!!, 0.0)
    }

    /**
     * Already-scaled field/result labels must not be prefixed again.
     *
     * @return Unit; malformed units such as mmm and mmA fail the regression.
     */
    @Test
    fun alreadyScaledUnitsKeepTheirDeclaredLabel() {
        assertEquals("0.2 mm", Engineering.format(0.2, "mm"))
        assertEquals("0.01 mm²", Engineering.format(0.01, "mm²"))
        assertEquals("0.1 mA", Engineering.format(0.1, "mA"))
        assertEquals("1000 mAh", Engineering.format(1000.0, "mAh"))
        assertEquals("0.5 °C", Engineering.format(0.5, "°C"))
        assertEquals("1 kΩ", Engineering.format(1000.0, "Ω"))
    }
}
