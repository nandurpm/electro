package com.electrokit.app.domain

import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

/** Reproduces reported numeric boundaries against the actual shipping implementations. */
class Audit2026RegressionTests {
    @Test fun plainUnitsNeverAcquireExtraPrefixes() {
        for ((v, unit, expected) in listOf(Triple(0.5,"h","0.5 h"), Triple(0.5,"kWh","0.5 kWh"), Triple(2000.0,"°F","2000 °F"), Triple(1500.0,"K","1500 K"), Triple(1700.0,"hours","1700 hours"))) {
            assertEquals(expected, Engineering.format(v,unit))
        }
        assertEquals("1500000",Engineering.format(1500000.0))
        assertEquals("1234567.89",Engineering.format(1234567.89))
    }
    @Test fun standardRoundUpIncludesExactValuesButStillRoundsUp() {
        for (v in listOf(510.0,820.0,510000.0,820000.0)) assertEquals(v, Resistors.nearest(v,24,true),v*1e-12)
        assertEquals(560.0,Resistors.nearest(510.01,24,true),1e-9)
    }
    @Test fun trailingRMeansDecimalResistance() {
        assertEquals(47.0,Resistors.smd("47R"),0.0)
        assertEquals(10.0,Resistors.smd("10R"),0.0)
        assertEquals(99.0,Resistors.smd("99R"),0.0)
        assertEquals(100.0,Resistors.smd("01A"),0.0)
    }
    @Test fun listInputAllowsTrailingSeparators() {
        assertTrue(Calculators.calculate(9,mapOf("values" to "1k,2k,\n")) is Calculation.Value)
        assertTrue(Calculators.calculate(9,mapOf("values" to ",;\n")) is Calculation.Error)
    }
    @Test fun resonanceDoesNotRequireLossResistance() {
        val input=mapOf("c" to "0.000001","l" to "0.001","freq" to "1000")
        assertTrue(Calculators.calculate(5,input) is Calculation.Value)
        assertTrue(Calculators.calculate(5,input+ ("r" to "0")) is Calculation.Value)
        assertTrue(Calculators.calculate(5,input+ ("r" to "-1")) is Calculation.Error)
    }
    @Test fun voltageDropRejectsInvalidTemperature() {
        val input=mapOf("length" to "10","area" to "1","i" to "1","v" to "230")
        for (temp in listOf("-300","-250")) assertTrue(Calculators.calculate(19,input+ ("temp" to temp)) is Calculation.Error)
    }
    @Test fun floatingPointUnityPowerFactorIsAccepted() {
        assertEquals("PF = 1",WorkbenchTools.calculate("pf",listOf("0.07","0.1","0.7")))
        assertTrue(WorkbenchTools.calculate("pf",listOf("1","0.1","0.7")).contains("cannot exceed"))
        assertEquals("Unknown utility.",WorkbenchTools.calculate("missing",emptyList()))
    }
    @Test fun printedCodesUseAsciiEvenInArabicLocale() {
        val previous=Locale.getDefault()
        try { Locale.setDefault(Locale.forLanguageTag("ar")); assertTrue(Resistors.smdCodes(100.0).contains("01A")) }
        finally { Locale.setDefault(previous) }
    }
}
