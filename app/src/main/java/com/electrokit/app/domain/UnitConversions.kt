package com.electrokit.app.domain

/** Named linear unit groups use exact SI scale factors; temperature has explicit affine offsets. */
object UnitConversions {
    /** Canonical scale to each group's SI base unit, without mixing physical dimensions. */
    val groups = linkedMapOf(
        "Voltage" to linkedMapOf("V" to 1.0, "mV" to 1e-3, "kV" to 1e3, "µV" to 1e-6),
        "Current" to linkedMapOf("A" to 1.0, "mA" to 1e-3, "µA" to 1e-6, "kA" to 1e3),
        "Power" to linkedMapOf("W" to 1.0, "kW" to 1e3, "MW" to 1e6, "mW" to 1e-3),
        "Resistance" to linkedMapOf("Ω" to 1.0, "kΩ" to 1e3, "MΩ" to 1e6, "mΩ" to 1e-3),
        "Capacitance" to linkedMapOf("F" to 1.0, "µF" to 1e-6, "nF" to 1e-9, "pF" to 1e-12, "mF" to 1e-3),
        "Frequency" to linkedMapOf("Hz" to 1.0, "kHz" to 1e3, "MHz" to 1e6, "GHz" to 1e9),
        "Length" to linkedMapOf("m" to 1.0, "mm" to 1e-3, "cm" to 1e-2, "in" to 0.0254, "ft" to 0.3048, "km" to 1000.0, "mil" to 0.0000254),
        "Temperature" to linkedMapOf("°C" to 1.0, "°F" to 1.0, "K" to 1.0)
    )

    /** Converts within one dimension, with the selected unit owning the scale exactly once.
     * @param group Named physical dimension.
     * @param from Explicit source unit.
     * @param to Explicit destination unit.
     * @param value Finite scalar in the source unit; suffix scaling belongs outside this API.
     * @return Converted scalar, rejecting mismatched units, overflow and temperatures below absolute zero.
     */
    fun convert(group: String, from: String, to: String, value: Double): Double {
        require(value.isFinite()) { "Enter a finite decimal value." }
        val units = requireNotNull(groups[group]) { "Unknown unit group." }
        require(from in units && to in units) { "Choose units from the same physical dimension." }
        val result = if (group == "Temperature") {
            val kelvin = when(from) { "°C" -> value + 273.15; "°F" -> (value - 32) * 5 / 9 + 273.15; else -> value }
            require(kelvin >= -1e-10) { "Temperature is below absolute zero." }
            when(to) { "°C" -> kelvin.coerceAtLeast(0.0) - 273.15; "°F" -> (kelvin.coerceAtLeast(0.0) - 273.15) * 9 / 5 + 32; else -> kelvin.coerceAtLeast(0.0) }
        } else value * units.getValue(from) / units.getValue(to)
        require(result.isFinite()) { "Conversion exceeds the numerical range." }
        return result
    }
}
