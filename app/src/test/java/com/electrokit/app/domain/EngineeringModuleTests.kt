package com.electrokit.app.domain

import com.electrokit.app.data.*
import java.io.File
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

/**
 * Independently specified engineering, source-integrity and search regression checks; no Android
 * runtime.
 */
class EngineeringModuleTests {
    /** Complete immutable bundled corpus used by integrity and filter assertions. */
    private val data =
        Json.decodeFromString<EngineeringContent>(
            File("src/main/assets/engineering.json").readText()
        )
    /** New-tool definitions allow systematic field/edge-case coverage. */
    private val tools =
        Json.decodeFromString<List<EngineeringTool>>(
            File("src/main/assets/engineering_tools.json").readText()
        )

    /**
     * Requires the exact supplied serial/code/message/reset/group columns, including gaps and
     * strings.
     *
     * @return Unit; any altered original field fails the assertion.
     */
    @Test
    fun exact125RowImport() {
        val rows =
            javaClass.getResourceAsStream("/faults-original.tsv")!!.bufferedReader().readLines()
        assertEquals(125, data.faults.size)
        assertEquals(
            rows,
            data.faults.map { "${it.serial}|${it.code}|${it.message}|${it.reset}|${it.group}" },
        )
        assertEquals((1..128).filterNot { it in 112..114 }, data.faults.map { it.serial })
        assertEquals("Need maintenance", data.faults.single { it.code == "164" }.message)
        assertEquals(125, data.faults.map { it.code }.toSet().size)
    }

    /**
     * Proves every relation, section, source and interactive marker resolves without invented
     * targets.
     *
     * @return Unit; broken references or lost photographed sources fail.
     */
    @Test
    fun graphAndSourceIntegrity() {
        val ids = data.records.map { it.id }.toSet()
        val sources = data.sources.map { it.id }.toSet()
        assertEquals(data.records.size, ids.size)
        assertEquals(14, data.sources.count { it.type == "Photograph transcription" })
        assertEquals(13, data.sections.size)
        data.records.forEach { r ->
            assertTrue(sources.containsAll(r.sources))
            assertTrue(ids.containsAll(r.related))
            assertTrue(data.sections.any { it.id == r.section })
        }
        data.faults.forEach {
            assertTrue(ids.containsAll(it.related))
            assertTrue(sources.containsAll(it.sources))
        }
        data.markers.forEach {
            assertTrue(it.record in ids)
            assertTrue(it.source in sources)
            assertTrue(it.x in 0f..1000f && it.y in 0f..560f)
        }
        assertTrue(data.records.single { it.id == "output-Q9–Q12" }.summary.contains("grouped"))
        assertFalse(
            data.records.any {
                it.id in listOf("output-Q9", "output-Q10", "output-Q11", "output-Q12")
            }
        )
        assertTrue(data.records.single { it.id == "input-I10" }.title.contains("Not Verified"))
    }

    /**
     * Exercises code/message matching and intersected group/reset/bookmark filters.
     *
     * @return Unit; mismatched classifications or a lost exact code fail.
     */
    @Test
    fun searchAndFilters() {
        assertEquals(listOf("E217"), EngineeringSearch.faults(data.faults, "e217").map { it.code })
        assertEquals(
            listOf("164"),
            EngineeringSearch.faults(data.faults, " Need maintenance ").map { it.code },
        )
        assertTrue(
            EngineeringSearch.faults(data.faults, "", "CBM Alarm", "Auto").all {
                it.group == "CBM Alarm" && it.reset == "Auto"
            }
        )
        assertEquals(
            listOf("E001"),
            EngineeringSearch.faults(data.faults, "", bookmarks = setOf("E001")).map { it.code },
        )
        assertTrue(EngineeringSearch.faults(data.faults, "abc-not-a-fault").isEmpty())
        assertEquals(125, EngineeringSearch.faults(data.faults, "").size)
    }

    /**
     * Checks a new tool result using explicit scalar inputs and an independently specified result
     * fragment.
     *
     * @param id Calculator identifier.
     * @param values Finite physical field values.
     * @param expected Independent reference text/value after six-digit display rounding.
     * @return Unit; domain errors or incorrect results fail.
     */
    private fun check(id: String, values: Map<String, String>, expected: String) {
        val result = EngineeringTools.calculate(id, values)
        assertTrue(result.text, result is Calculation.Value)
        assertTrue(result.text, result.text.contains(expected))
    }

    /** @return Unit; validates √3×400×10×0.8 against 5.54256 kW independently. */
    @Test
    fun balancedThreePhase() {
        check("three-phase", mapOf("v" to "400", "i" to "10", "pf" to "0.8"), "5.54256 kW")
    }

    /** @return Unit; validates 230×10×0.9 = 2.07 kW independently. */
    @Test
    fun singlePhase() {
        check("single-phase", mapOf("v" to "230", "i" to "10", "pf" to "0.9"), "2.07 kW")
    }

    /** @return Unit; validates 15 kW shaft-current estimate with explicit PF and efficiency. */
    @Test
    fun motorCurrent() {
        check(
            "motor-current",
            mapOf("p" to "15", "v" to "415", "pf" to "0.76", "eta" to "91.2"),
            "30.1075 A",
        )
    }

    /** @return Unit; independently checks 15/16.5 output/input efficiency. */
    @Test
    fun efficiency() {
        check("efficiency", mapOf("out" to "15", "in" to "16.5"), "90.91%")
    }

    /** @return Unit; checks unitful kVA input and dimensionless 0.8 power factor. */
    @Test
    fun powerFactor() {
        check("power-factor", mapOf("p" to "8kW", "s" to "10kVA"), "0.8")
    }

    /** @return Unit; validates 15 kW at 975 rpm against the exact torque relationship. */
    @Test
    fun torque() {
        check("torque", mapOf("p" to "15", "n" to "975"), "146.912 N·m")
    }

    /** @return Unit; checks explicit six poles → 1000 rpm and 2.5% slip. */
    @Test
    fun slip() {
        check("slip", mapOf("f" to "50", "poles" to "6", "n" to "975"), "2.5%")
    }

    /** @return Unit; checks explicit slip yields 975 rpm. */
    @Test
    fun frequencySpeed() {
        check("frequency-speed", mapOf("f" to "50", "poles" to "6", "slip" to "2.5"), "975 rpm")
    }

    /** @return Unit; ideal 0.5 m/s, 0.4 m pitch, one person/step gives 4500 persons/h. */
    @Test
    fun theoreticalCapacity() {
        check(
            "capacity",
            mapOf("v" to "0.5m/s", "pitch" to "0.4m", "occupancy" to "1"),
            "4500 persons/h",
        )
    }

    /** @return Unit; tests explicitly supplied gear reduction and chain geometry. */
    @Test
    fun gearAndStepSpeed() {
        check("gear-ratio", mapOf("input" to "975", "output" to "32.5"), "Reduction ratio: 30")
        check("gear", mapOf("n" to "975", "ratio" to "30"), "32.5 rpm")
        check(
            "step-speed",
            mapOf("n" to "975", "ratio" to "30", "teeth" to "20", "pitch" to "0.05"),
            "0.541667 m/s",
        )
    }

    /** @return Unit; validates 5 kW×8 h = 40 kWh and 10 m at 30° gives 5 m rise. */
    @Test
    fun energyAndGeometry() {
        check("energy", mapOf("p" to "5", "h" to "8h"), "40 kWh")
        check("geometry", mapOf("length" to "10", "angle" to "30°"), "5 m")
    }

    /** @return Unit; verifies exact conversion factors and rejects dimensionally wrong values. */
    @Test
    fun unitConversions() {
        check("unit-converter", mapOf("conversion" to "mA → A", "x" to "20mA"), "0.02 A")
        check("unit-converter", mapOf("conversion" to "kWh → J", "x" to "1kWh"), "3.6E+6 J")
        assertTrue(
            EngineeringTools.calculate(
                "unit-converter",
                mapOf("conversion" to "W → kW", "x" to "10V"),
            ) is Calculation.Error
        )
        assertTrue(
            EngineeringTools.calculate(
                "three-phase",
                mapOf("v" to "400", "i" to "10", "pf" to "0.8V"),
            ) is Calculation.Error
        )
    }

    /**
     * @return Unit; rejects missing fields and impossible poles, efficiencies and power factors.
     */
    @Test
    fun physicalRanges() {
        tools.forEach {
            assertTrue(EngineeringTools.calculate(it.id, emptyMap()) is Calculation.Error)
        }
        assertTrue(
            EngineeringTools.calculate("slip", mapOf("f" to "50", "poles" to "3", "n" to "975"))
                is Calculation.Error
        )
        assertTrue(
            EngineeringTools.calculate("efficiency", mapOf("out" to "20", "in" to "10"))
                is Calculation.Error
        )
        assertTrue(
            EngineeringTools.calculate(
                "motor-current",
                mapOf("p" to "15", "v" to "415", "pf" to "1.1", "eta" to "91.2"),
            ) is Calculation.Error
        )
    }

    /**
     * Probes every scalar independently while retaining the other explicit example parameters.
     *
     * @return Unit; any exception, generic requirement message or unhandled range error fails.
     */
    @Test
    fun everyNewToolExtremeInputGrid() {
        val probes = listOf("", "0", "-1", "1e-300", "1e300", "NaN", "abc")
        tools.forEach { tool ->
            val baseline = tool.fields.associate { it.key to it.sample }
            assertTrue(
                tool.title,
                EngineeringTools.calculate(tool.id, baseline) is Calculation.Value,
            )
            tool.fields
                .filter { it.options.isEmpty() }
                .forEach { field ->
                    probes.forEach { probe ->
                        val result =
                            EngineeringTools.calculate(tool.id, baseline + (field.key to probe))
                        assertFalse(result.text.contains("Failed requirement."))
                        assertTrue(result.text.isNotBlank())
                    }
                }
        }
    }
    /** @return Unit; verifies generating speed gives negative slip and converts back consistently. */
    @Test
    fun regenerativeSlip() {
        check("slip", mapOf("f" to "50", "poles" to "6", "n" to "1025"), "-2.5%")
        check("frequency-speed", mapOf("f" to "50", "poles" to "6", "slip" to "-2.5"), "1025 rpm")
    }

}
