package com.electrokit.app.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Offline image record; identifiers remain independent of translated display names.
 * @property id Stable filename-derived key.
 * @property name Human-readable family name.
 * @property category Supplied asset group.
 * @property image Relative path inside the Android asset directory.
 * @property advanced Whether the local advanced-reference display gate applies.
 */
@Serializable
data class ComponentAsset(val id: String, val name: String, val category: String, val image: String, val advanced: Boolean = false)

/** Loads only metadata; bitmaps are decoded separately at display resolution.
 * @param context Android context used only to read bundled metadata.
 * @return Decoded local component records; malformed metadata propagates a decoding failure.
 */
fun loadComponents(context: Context): List<ComponentAsset> =
    context.assets.open("components.json").bufferedReader().use { Json.decodeFromString(it.readText()) }

/** Authored learning reference, with explicit boundaries and source links stored offline.
 * @property id Stable route/bookmark key.
 * @property title Visible educational topic.
 * @property sections Ordered heading/body pairs.
 * @property sources Original technical reference URLs, stored as text.
 */
data class LearningPage(val id: String, val title: String, val sections: List<Pair<String, String>>, val sources: List<String>)

/** Generic educational references: never imply an exact controller's connector assignment. */
val learningPages = listOf(
    LearningPage("multimeter", "Digital multimeter guide", listOf(
        "Know the instrument" to "The display reports the selected quantity. The rotary selector chooses function/range. COM is the common input; the V/Ω jack is normally used for voltage and resistance. Separate current jacks have different fuse and time limits. Labels vary: read the actual meter manual before connecting probes.",
        "Low-voltage DC demonstration" to "Use an isolated small battery only. 1. Inspect leads and meter. 2. Black lead to COM, red to V/Ω. 3. Select DC volts and a suitable range. 4. Place probes across the battery, black on negative and red on positive. 5. Read voltage and disconnect. A negative reading means reversed polarity, not a defective battery.",
        "AC voltage" to "Select AC volts with leads in COM and the voltage jack. Voltage is measured in parallel. Mains work requires a competent person, a correctly rated meter/probes and an approved procedure; this lesson is not a live-mains exercise. AC accuracy depends on frequency, waveform and whether the instrument is true RMS.",
        "Resistance and continuity" to "De-energize, isolate, verify absence of voltage and safely discharge stored energy first. Use COM and V/Ω, then select Ω or continuity. Test the disconnected component where parallel paths would affect the result. A continuity beep indicates a threshold defined by that meter, not proof of a safe circuit.",
        "Current measurement" to "Never connect a current-mode meter across a voltage source. Current input is a low-impedance path. For a supervised isolated low-voltage demonstration, power off, choose the correct fused current jack and range, insert the meter in series, then power on within the instrument limits. Power off before changing connections; return the red lead to V/Ω afterward. Prefer a suitable clamp meter where appropriate.",
        "Symbols and checks" to "V⎓: DC voltage; V~: AC voltage; Ω: resistance; A: current; diode symbol: diode-test mode. Verify lead condition, jack selection, function and range every time. A blown current fuse does not necessarily stop voltage readings.",
        "Safety" to "Match CAT category and voltage ratings of both meter and probes to the measurement location; a voltage number alone is insufficient. Use specified replacement fuses. Do not measure resistance on energized circuits. Follow the manufacturer's limitations and local safe-work procedures. Stop if the setup is uncertain."
    ), listOf("https://www.fluke.com/en-us/learn/blog/digital-multimeters/how-to-use-a-multimeter", "https://www.fluke.com/en-us/learn/blog/safety/multimeter-safety")),
    LearningPage("ethernet", "RJ45 • T568A / T568B", listOf(
        "Connector and view" to "Ethernet commonly uses an 8P8C modular connector, often called RJ45. Identify numbered contacts in a specified plug mating view; a socket view can be mirrored. Do not infer contact numbers from a generated photograph.",
        "T568A — contacts 1 to 8" to "1 white/green\n2 green\n3 white/orange\n4 blue\n5 white/blue\n6 orange\n7 white/brown\n8 brown",
        "T568B — contacts 1 to 8" to "1 white/orange\n2 orange\n3 white/green\n4 blue\n5 white/blue\n6 green\n7 white/brown\n8 brown",
        "Straight and crossover" to "Straight-through: use the same scheme at both ends. Classic 10/100 crossover: T568A at one end and T568B at the other. Gigabit crossover and auto-MDI/MDIX have additional considerations; do not assume every device needs a crossover cable. Keep twisted pairs together and follow the connector and cable category installation instructions.",
        "Cable testing" to "Disconnect from equipment and PoE before using an ordinary continuity tester. Check contacts 1–8 end-to-end for opens, shorts and reversals. Basic continuity alone may miss split pairs and cannot certify CAT5e/CAT6 channel performance. Use an appropriate wire-map/certification tester for those tasks. Ethernet ports, breakouts and switches do not define a controller's RS-485 connector pinout."
    ), listOf("https://www.flukenetworks.com/blog/cabling-chronicles/what-s-difference-between-t568a-and-t568b")),
    LearningPage("rs485", "RS-485 • termination and bias", listOf(
        "Two-wire bus" to "Generic half-duplex concept:\nDevice 1 pair ⇄ trunk pair ⇄ Device 2 pair ⇄ Device 3 pair\nBoth directions share a differential pair; only one transmitter drives it at a time. Use the device manual for reference/common and shield arrangements. RS-485 specifies electrical behavior, not a universal connector or application protocol.",
        "A/B and D+/D−" to "A/B naming conventions differ between vendors. Never infer polarity solely from the letters or from a photograph. Cross-check both devices' manuals and signal definitions. RS-485 on an RJ45, DB9 or terminal block has no universal contact assignment.",
        "Four-wire concept" to "A full-duplex arrangement uses separate transmit and receive differential pairs. A transmitter pair connects to the receiving pair of the peer(s) according to supported topology. It is not simply a two-wire bus with an extra ground. Consult each device's supported mode.",
        "Termination and biasing" to "Prefer a trunk/daisy-chain topology with short stubs. Terminate the two physical ends of the active transmission line according to the cable impedance and transceiver guidance; 120 Ω is common, not universal. Fail-safe bias must be engineered for loading, termination and the transceiver's built-in fail-safe behavior. Do not add a resistor at every node or copy resistor values blindly.",
        "Converters and MAX485" to "A USB adapter may require isolation, a common reference, driver control and matching serial settings. A MAX485 module is not a USB or RS-232 interface; its UART side has logic-level supply and I/O constraints. Check the exact chip/module datasheet before connecting a 3.3 V or 5 V host."
    ), listOf("https://www.ti.com/lit/an/slla272d/slla272d.pdf", "https://www.analog.com/media/en/technical-documentation/data-sheets/MAX1487-MAX491.pdf")),
    LearningPage("modbus", "Modbus RTU / TCP", listOf(
        "RTU" to "Modbus RTU uses binary frames with a CRC and timing rules, commonly over RS-485. Match baud rate, parity, data/stop bits and address. A differential bus alone does not establish Modbus compatibility. Conventional serial unicast server addresses are 1–247; address 0 is broadcast and not a normal response address.",
        "TCP" to "Modbus TCP carries application requests over TCP using an MBAP header, conventionally port 502. It does not use the RTU serial CRC. A serial/Ethernet gateway must map serial settings and unit IDs correctly. Do not connect controller ports based only on an Ethernet-shaped connector.",
        "Troubleshooting" to "Verify physical interface and device manuals first, then addresses, framing, timeouts and documented register maps. Register numbering conventions differ; never write an unknown register to discover its function. Use read-only diagnostics on authorized equipment."
    ), listOf("https://www.modbus.org/specs.php")),
    LearningPage("rs232", "RS-232 • DB9 DTE", listOf(
        "Numbered DE-9 DTE contacts" to "Typical PC-style DTE assignment: 1 DCD; 2 RXD; 3 TXD; 4 DTR; 5 signal ground; 6 DSR; 7 RTS; 8 CTS; 9 RI. Use stamped contact numbers and a defined mating view. DCE assignments and null-modem wiring differ; gender alone does not establish DTE/DCE.",
        "Electrical interface" to "RS-232 is single-ended and uses bipolar signaling. It is not TTL UART, USB or RS-485. Use a suitable transceiver/adapter, confirm handshake requirements and follow the equipment documentation."
    ), listOf("https://www.ti.com/lit/an/slla037a/slla037a.pdf")),
    LearningPage("can", "CAN bus fundamentals", listOf(
        "Differential network" to "High-speed CAN normally uses CAN_H and CAN_L on a linear bus. Termination at the two physical ends must match the intended network and transceiver specification. 120 Ω per end is common for high-speed CAN; other CAN physical layers differ. Connector assignments are device-specific.",
        "Diagnosis" to "Use the documented bit rate, topology, common-mode and grounding limits. A USB or Ethernet-shaped connector does not identify CAN pins. Power down and isolate before resistance checks. Do not transmit on operational equipment without authorization and the protocol definition."
    ), listOf("https://www.ti.com/lit/an/slla270/slla270.pdf")),
    LearningPage("interfaces", "USB, HDMI and sensor connectors", listOf(
        "USB-A, USB-B and USB-C" to "Connector shape does not guarantee data rate, alternate modes or available current. USB-C uses orientation and configuration-channel detection; never infer contacts from a single photograph. Use the existing numbered USB reference pages and original USB specifications for the exact variant.",
        "HDMI, DB25, JST and M8/M12" to "HDMI variants have distinct contact layouts. DB25 usage depends on the interface. JST denotes a family of connectors, not one universal pinout. M8/M12 coding, contact count, orientation and device purpose affect assignments. Check the exact part number and manufacturer drawing.",
        "Arduino, ESP32 and PLC I/O" to "Board revisions and modules vary. Confirm logic voltage, allowable input current, isolation and whether an I/O is sourcing or sinking. Do not connect 24 V PLC outputs to microcontroller pins directly. A pin label is not proof of electrical compatibility.",
        "Three-wire sensors" to "Some industrial DC sensors use brown for supply, blue for reference and black for output, but this is not a universal wiring instruction. PNP sources current; NPN sinks current. Verify the device diagram, load type, supply and interface rating before any connection."
    ), listOf("https://www.usb.org/documents", "https://www.espressif.com/en/support/documents/technical-documents"))
)
