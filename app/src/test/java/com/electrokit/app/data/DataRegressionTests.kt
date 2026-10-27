package com.electrokit.app.data

import java.io.File
import java.io.IOException
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

/**
 * Groups regression assertions for data regression tests; unexpected behaviour fails the test
 * visibly.
 */
class DataRegressionTests {
    /**
     * Verifies saved values modes and preferences round trip; unexpected behaviour fails the test
     * visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun savedValuesModesAndPreferencesRoundTrip() {
        val state =
            PreferencesState(
                light = true,
                alphabetical = true,
                favourites = setOf("c8", "p1"),
                modules = setOf("m1"),
                values = mapOf("c8" to mapOf("ratio" to "1e9", "unit_base" to "3")),
                modes = mapOf("c8" to 2),
            )
        assertEquals(state, PreferenceCodec.decode(PreferenceCodec.encode(state)))
    }

    /**
     * Verifies original state records remain compatible; unexpected behaviour fails the test
     * visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun originalStateRecordsRemainCompatible() {
        assertEquals(PreferencesState(), PreferenceCodec.decode(null))
        assertEquals(
            PreferencesState(light = true),
            PreferenceCodec.decode("""{"light":true,"futureField":10}"""),
        )
    }

    /**
     * Verifies damaged state is not silently replaced; unexpected behaviour fails the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test(expected = IOException::class)
    fun damagedStateIsNotSilentlyReplaced() {
        PreferenceCodec.decode("{damaged")
    }

    /**
     * Verifies bundled assets decode with shipping kotlin models; unexpected behaviour fails the
     * test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun bundledAssetsDecodeWithShippingKotlinModels() {
        val json = Json { ignoreUnknownKeys = true }
        val assets = File("src/main/assets")
        assertTrue("Unit-test working directory must be the app module", assets.isDirectory)
        val registry = json.decodeFromString<List<Entry>>(File(assets, "registry.json").readText())
        val pages = json.decodeFromString<Map<String, Page>>(File(assets, "pages.json").readText())
        val modules =
            json.decodeFromString<Map<String, List<Part>>>(File(assets, "modules.json").readText())
        assertEquals(110, registry.size)
        assertEquals(72, pages.size)
        assertEquals(setOf("m1", "m2", "m3"), modules.keys)
        assertTrue(modules.values.all { it.size >= 30 })
        assertTrue(modules.getValue("m1").all { "Description" !in it.numericKeys })
        assertTrue(modules.getValue("m3").all { "Instruction word bits" in it.numericKeys })
    }

    /**
     * Verifies numeric memory fields have specific datasheet references; unexpected behaviour fails
     * the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun numericMemoryFieldsHaveSpecificDatasheetReferences() {
        val json = Json { ignoreUnknownKeys = true }
        val modules =
            json.decodeFromString<Map<String, List<Part>>>(
                File("src/main/assets/modules.json").readText()
            )
        assertTrue(modules.getValue("m1").all { java.net.URI(it.source).path.length > 1 })
        val memory = modules.getValue("m3").filter { "Program memory words" in it.numericKeys }
        assertEquals(8, memory.size)
        assertTrue(
            memory.all {
                it.specs.getValue("Memory reference").startsWith("https://ww1.microchip.com/")
            }
        )
        assertEquals("256", memory.first { it.name == "PIC10F200" }.specs["Program memory words"])
        assertEquals("41", memory.first { it.name == "PIC12F509" }.specs["SRAM bytes"])
    }

    /**
     * Every technical page must expose its recorded provenance, including an explicit unverified
     * status.
     *
     * @return Unit; missing status text fails without treating citation URLs as proof of accuracy.
     */
    @Test
    fun referenceProvenanceIsExplicitOnEveryPage() {
        val pages =
            Json.decodeFromString<Map<String, Page>>(File("src/main/assets/pages.json").readText())
        assertEquals(72, pages.size)
        assertTrue(pages.values.all { it.provenance.isNotBlank() })
        assertTrue(
            pages.values
                .filter { it.sources.isEmpty() }
                .all { it.provenance.contains("not independently verified") }
        )
    }
}
