package com.electrokit.app.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Family-level reference facts, explicitly separate from device-specific ratings.
 * @property category Library filter category.
 * @property aliases Related terminology and marking-search hints, not identification guarantees.
 * @property principle Operating explanation.
 * @property specifications Parameters to check in the exact datasheet, not assigned ratings.
 * @property testing Ordered safe test/limitation guidance.
 * @property identification Marking conventions and limitations.
 * @property application Educational application, assumptions and formula.
 * @property source Original reference URL.
 */
@Serializable
data class ComponentGuide(val category: String, val aliases: List<String>, val principle: String,
    val specifications: List<String>, val testing: List<String>, val identification: String,
    val application: String, val source: String)

/** Assigns existing artwork to a general family; never infers an exact part or terminal order.
 * @param asset Existing stable component record.
 * @return Family key or null when only the original reference is available.
 */
fun componentFamily(asset: ComponentAsset): String? {
    val id = asset.id
    return when {
        id == "breadboard" -> "breadboard"
        id == "crystal_oscillator" -> "crystal"
        id == "smps_power_supply" -> "supply"
        id in listOf("digital_multimeter", "clamp_meter", "insulation_tester") -> "instrument"
        id in listOf("can_bus_termination", "can_twisted_pair", "max485_uart_concept", "modbus_rtu_daisy_chain", "rs485_four_wire_concept", "rs485_termination_and_bias", "rs485_two_wire_bus") -> "network"
        id == "plc_controller" -> "module"
        listOf("module", "converter", "gateway", "isolator").any { it in id } || id == "ethernet_switch" -> "module"
        "display" in id || "screen" in id || "indicator_lamp" in id -> "display"
        id == "bridge_rectifier" -> "bridge"
        "potentiometer" in id -> "potentiometer"
        "resistor" in id -> "resistor"
        "capacitor" in id -> "capacitor"
        id == "led" -> "led"
        "diode" in id || "rectifier" in id -> "diode"
        "mosfet" in id -> "mosfet"
        "transistor" in id -> "bjt"
        "inductor" in id -> "inductor"
        "transformer" in id -> "transformer"
        ("relay" in id && "overload" !in id) || "contactor" in id -> "relay"
        "mcb" in id || "mccb" in id || "overload" in id -> "protection"
        "switch" in id || "button" in id || "encoder" in id || "station" in id -> "switch"
        "sensor" in id || "probe" in id -> "sensor"
        "motor" in id -> "motor"
        id == "dip_integrated_circuit" -> "ic"
        listOf("usb", "rj45", "db9", "db25", "terminal", "connector", "cable", "hdmi", "vga", "dupont").any { it in id } -> "connector"
        else -> null
    }
}

/** Reads the bundled guide dataset once; UI callers perform this on IO.
 * @param context Application assets context.
 * @return Validated immutable family records, with failures propagated rather than replaced by fake facts.
 */
fun loadComponentGuides(context: Context): Map<String, ComponentGuide> =
    context.assets.open("component_guides.json").bufferedReader().use { Json.decodeFromString(it.readText()) }
