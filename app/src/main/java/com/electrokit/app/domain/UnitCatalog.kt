package com.electrokit.app.domain

/**
 * Shares declared physical-unit rules between field validation and pasted-scalar parsing; no I/O.
 */
object UnitCatalog {
    /**
     * Base units retain field-specific conventions such as millimetres and ounces per square foot.
     */
    val baseUnits =
        setOf(
            "",
            "Ω",
            "F",
            "H",
            "V",
            "A",
            "Hz",
            "s",
            "W",
            "mm",
            "mm²",
            "%",
            "°C",
            "mAh",
            "KiB",
            "bytes",
            "m",
            "m/s",
            "oz/ft²",
            "°C/W",
            "VA",
            "var",
            "rpm",
            "h",
            "kWh",
            "J",
            "°",
            "km/h",
        )
    /** Already-scaled or non-SI units must not acquire another prefix, such as mmm or mmA. */
    val plainDisplayUnits =
        setOf("mm", "mm²", "mA", "kW", "mAh", "KiB", "bytes", "°C", "°C/W", "oz/ft²")
    /** Non-base field units convert through their canonical physical unit exactly once. */
    private val aliases =
        mapOf("mA" to ("A" to 0.001), "kW" to ("W" to 1000.0), "kVA" to ("VA" to 1000.0))
    /**
     * Accepted field units are derived from parser units, preventing separate lists from drifting.
     */
    val fieldUnits = baseUnits + aliases.keys

    /**
     * Resolves a field alias before comparing pasted physical dimensions.
     *
     * @param unit Declared field unit.
     * @return Canonical unit; unknown units remain unchanged for validation to reject. No
     *   exceptions.
     */
    fun canonical(unit: String): String = aliases[unit]?.first ?: unit

    /**
     * Provides the conversion from a declared field quantity to its canonical unit.
     *
     * @param unit Declared field unit.
     * @return Scale factor; base units use one. No I/O or validation exceptions.
     */
    fun scale(unit: String): Double = aliases[unit]?.second ?: 1.0
}
