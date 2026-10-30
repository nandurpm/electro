package com.electrokit.app.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Offline image record; identifiers remain independent of translated display names.
 * @property id Stable filename-derived key.
 * @property name Human-readable family name.
 * @property category Supplied asset group.
 * @property image Relative path inside the Android asset directory.
 */
@Serializable
data class ComponentAsset(val id: String, val name: String, val category: String, val image: String)

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
    LearningPage("multimeter", "Digital multimeter • practical guide", listOf(
        "What you will learn" to "Identify the display, rotary function switch, range selection and input jacks. Perform isolated low-voltage tests for DC voltage, AC voltage, resistance, continuity, diodes, small DC current and capacitance if supported. The interactive meter above includes step-by-step exercises for each mode.",
        "Before any test — safety checklist" to "1. Read the actual instrument and lead ratings; CAT markings relate to measurement environment, not only voltage. 2. Check the case, fuse compartment, lead insulation, shrouds, connectors and battery warning. 3. Choose the right function and physical jack before connecting. 4. Use only isolated low-energy practice sources here. 5. Never touch probe tips or exposed conductors while measuring.",
        "Understanding the terminals" to "COM is the black common input. V/Ω is normally the red input for voltage, resistance, continuity and diode. Current may require a dedicated mA/µA jack or a higher-current A jack protected by its specified fuse. Capacitance jack positions vary. Moving the red lead to A changes the risk: never leave it there for a voltage test.",
        "Manual range and auto range" to "Auto-ranging meters select a suitable scale after the function is chosen. On a manual-range meter, start at a range above the expected value and reduce only within the instrument limits. OL/1 can mean overrange or open circuit depending on the function. AC and DC are different selectors and cannot be interchanged.",
        "Practice 1 — AA battery" to "Select V⎓ with black on COM and red on V/Ω; touch the probes across a loose 1.5 V AA cell. Red at +, black at − gives a positive reading; reverse the probes to observe a negative sign. Voltage depends on charge and load. Never connect the meter in current mode directly across a battery.",
        "Practice 2 — isolated low-voltage AC" to "Use a demonstrably isolated, low-voltage AC training output only. Select V~, choose the correct range and measure in parallel. Displayed RMS readings depend on waveform and frequency; true-RMS models and average-responding meters can disagree on non-sinusoidal waves. Household mains are NOT part of this exercise.",
        "Practice 3 — resistor value" to "Power down, isolate and discharge the circuit first. Remove or electrically isolate a resistor, then measure with Ω mode. A 1 kΩ component may read near 1000 Ω within tolerance. Parallel circuit paths can produce misleading in-circuit readings. Never use Ω on a powered circuit.",
        "Practice 4 — wire continuity" to "With the wire fully disconnected and verified de-energized, select the continuity/buzzer symbol and short the probe tips together to prove the leads work. Then check opposite ends of the wire. A beep means resistance below a manufacturer-defined threshold, not that a protective conductor is safe or an insulation test passed.",
        "Practice 5 — diode forward/reverse" to "With power removed, use diode mode on an isolated ordinary silicon diode. Red to anode and black to cathode often shows around 0.5–0.8 V; reverse bias often shows OL. LED readings and circuit paths differ. Confirm the exact diode's polarity marking and specification.",
        "Practice 6 — low-energy current" to "Measure current IN SERIES in a small, fused, battery-powered training circuit with a resistor limiting current. Read the meter's supported range and limits first. Disconnect power before opening the circuit, changing jacks or ranges; return red lead to V/Ω after the test. A current meter across a source may cause a dangerous short.",
        "Practice 7 — capacitance" to "Only if your meter supports it: power off, discharge by the manufacturer's method and verify absence of stored voltage before selecting capacitance. Isolate the component as directed. Compare the settled reading with the printed capacitance and tolerance. Large capacitors may retain charge for a long time.",
        "Reading symbols" to "V⎓ = DC volts; V~ = AC volts; Ω = resistance; )) = continuity/buzzer (exact icon varies); diode symbol = forward voltage; A⎓ = DC current; µF/nF = capacitance; Hz = frequency where supported. Consult the actual meter's manual for secondary mode buttons such as SELECT, HOLD, REL or MIN/MAX.",
        "Troubleshooting and common mistakes" to "Blank display: check the battery and power switch. OL in resistance: open component, bad lead or wrong range. Zero volts: source disconnected, wrong AC/DC function or bad contact. Unexpectedly low resistance: parallel load. Current mode reads zero: circuit open, wrong jack or blown internal fuse. Never defeat or bypass a fuse to restore a reading.",
        "Lead and fuse verification" to "Check leads with the meter's safe continuity mode using a disconnected probe pair. Examine fuses only according to the manufacturer's procedure with all sources removed. Replace with exactly specified type, voltage rating, breaking capacity and current rating; an ordinary glass fuse is not a substitute for a required high-energy protection fuse.",
        "Live electrical systems — restricted" to "Do not use these tutorials as permission to test building mains, control cabinets, motor drives or distribution equipment. Such work requires appropriate qualification, safe isolation or live-work authorization, rated PPE/instruments and an approved procedure. The wrong meter category or jack selection can cause severe injury.",
        "Practice checklist / self-test" to "Q: Voltage series or parallel? A: Parallel. Q: Current series or parallel? A: Series, with correct fused jack. Q: Ohms on energized circuit? A: No. Q: Can a continuity beep certify insulation? A: No. Q: After current testing, where should the red probe return? A: V/Ω. Q: What if the mode/rating is uncertain? A: Stop and consult the manufacturer's manual."
    ), listOf("https://www.fluke.com/en-us/learn/blog/digital-multimeters/how-to-use-a-multimeter",
        "https://www.fluke.com/en-us/learn/blog/safety/multimeter-safety")),
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
