package com.electrokit.app.data

import org.junit.Assert.*
import org.junit.Test
import kotlinx.serialization.json.Json
import java.io.File

/** Validates offline component integrity and conservative guide classification. */
class ComponentGuideTests {
    @Test fun allOriginalAssetsAndStructuredGuidesRemainValid() {
        val root = File("src/main/assets")
        val assets = Json.decodeFromString<List<ComponentAsset>>(File(root, "components.json").readText())
        val guides = Json.decodeFromString<Map<String, ComponentGuide>>(File(root, "component_guides.json").readText())
        assertEquals(98, assets.size)
        assertEquals(assets.size, assets.map { it.id }.toSet().size)
        assets.forEach { assertTrue(it.image, File(root, it.image).isFile()) }
        guides.forEach { (name, guide) ->
            assertTrue(name, guide.principle.length > 80)
            assertTrue(name, guide.testing.size >= 3)
            assertTrue(name, guide.specifications.isNotEmpty())
            assertTrue(name, guide.source.startsWith("https://"))
        }
        fun family(id: String) = componentFamily(assets.first { it.id == id })
        assertEquals("module", family("ethernet_switch"))
        assertEquals("module", family("usb_rs232_converter"))
        assertEquals("crystal", family("crystal_oscillator"))
        assertEquals("display", family("red_indicator_lamp"))
        assertEquals("bridge", family("bridge_rectifier"))
        assertEquals("protection", family("thermal_overload_relay_a"))
    }
}
