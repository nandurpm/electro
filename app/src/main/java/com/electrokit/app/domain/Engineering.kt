package com.electrokit.app.domain

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.Locale
import kotlin.math.*

/**
 * Defines one scalar syntax so calculator inputs and module filters agree about explicit suffix
 * precedence. Construction holds metadata/helpers; physical validation is performed by the named
 * operations, not by field assignment.
 */
object Engineering {
    /** Accepted suffix exponents; case distinguishes milli from mega. */
    private val powers =
        mapOf(
            'p' to -12,
            'n' to -9,
            'u' to -6,
            'µ' to -6,
            'μ' to -6,
            'm' to -3,
            'k' to 3,
            'K' to 3,
            'M' to 6,
            'G' to 9,
            'T' to 12,
            'R' to 0,
            'r' to 0,
        )
    /** Allowed explicit physical units; unknown trailing text is rejected. */
    private val units = UnitCatalog.baseUnits
    /**
     * Anchored bare-number syntax checked before suffix parsing. Full-token anchoring rejects Java
     * f/d suffixes instead of treating them as valid scalar units.
     */
    private val plain = Regex("^[+-]?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)(?:[eE][+-]?[0-9]+)?$")
    /**
     * Numeric-head/suffix grammar permits embedded digits only for engineering notation. The suffix
     * is parsed separately; embedded digits are valid only for notation such as 4k7 or 2R2.
     */
    private val scalar =
        Regex("^([+-]?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)(?:[eE][+-]?[0-9]+)?)(.*)$")

    /**
     * Retains explicit scale/unit metadata so a selector cannot multiply an already scaled value
     * twice.
     *
     * @param value Input scalar in the operation’s documented units.
     * @param explicitScale True when typed scale or units must override the dropdown multiplier.
     * @param unit Physical output unit; blank denotes a dimensionless result. Construction holds
     *   metadata/helpers; physical validation is performed by the named operations, not by field
     *   assignment.
     */
    data class Parsed(
        /** Input scalar in the operation’s documented units. */
        val value: Double,
        /**
         * True when the input already carries a unit/scale, preventing duplicate dropdown scaling.
         */
        val explicitScale: Boolean,
        /** Physical output unit; blank denotes a dimensionless result. */
        val unit: String = "",
    )

    /**
     * Distinguishes bare numbers from pasted physical values and preserves scale provenance for
     * dropdown precedence.
     *
     * @param text Input or output text as described above; treated as data, never executed.
     * @return A finite scalar plus scale/unit metadata, or null for unsupported syntax. Invalid
     *   input follows the nullable/typed-validation contract above; this operation performs no I/O.
     */
    fun parsed(text: String): Parsed? {
        // Accept both the Greek omega and the compatibility OHM SIGN in pasted values.
        val s =
            text
                .replace(Regex("(?i)meg"), "M")
                .trim()
                .replace('−', '-')
                .replace("Ω", "Ω")
                .replace(Regex("\\s+"), "")
        if (plain.matches(s))
            return s.toDoubleOrNull()?.takeIf(Double::isFinite)?.let { Parsed(it, false) }
        val match = scalar.matchEntire(s) ?: return null
        val number = match.groupValues[1]
        val tail = match.groupValues[2]
        if (tail.isEmpty()) return null
        if (tail in units && tail != "m")
            return number.toDoubleOrNull()?.takeIf(Double::isFinite)?.let { Parsed(it, true, tail) }
        val power = powers[tail[0]] ?: return null
        val rest = tail.drop(1)
        val digits = rest.takeWhile { it in '0'..'9' }
        val unit = rest.drop(digits.length)
        if (
            unit !in units ||
                (digits.isNotEmpty() && (number.contains('.') || number.contains('e', true)))
        )
            return null
        val significant = if (digits.isEmpty()) number else "$number.$digits"
        return significant
            .toDoubleOrNull()
            // Divide for negative scales so the reported 10 µF default matches the exact Double
            // literal 1e-5.
            ?.let { if (power < 0) it / 10.0.pow(-power) else it * 10.0.pow(power) }
            ?.takeIf(Double::isFinite)
            ?.let { Parsed(it, true, unit) }
    }

    /**
     * Accepts a finite scalar or returns null; explicit suffixes are applied only once.
     *
     * @param text Input or output text as described above; treated as data, never executed.
     * @return The finite base-unit scalar, or null for invalid or non-finite input. Invalid input
     *   follows the nullable/typed-validation contract above; this operation performs no I/O.
     */
    fun parse(text: String): Double? = parsed(text)?.value

    /**
     * Resolves explicit physical units before dropdown scale to prevent silent thousand-fold
     * errors.
     *
     * @param text Input or output text as described above; treated as data, never executed.
     * @param exponent Dropdown base-ten scale, ignored when text already supplies explicit scale.
     * @param fieldUnit Expected physical unit used to reject incompatible pasted values.
     * @return A finite scalar in the expected field unit, or null for incompatible units/invalid
     *   input. Invalid input follows the nullable/typed-validation contract above; this operation
     *   performs no I/O.
     */
    fun input(text: String, exponent: Int, fieldUnit: String = ""): Double? {
        // A bare m remains milli globally, but in a metre field its explicit physical unit wins.
        val compact = text.trim().replace(" ", "").replace('−', '-')
        val metres = if (fieldUnit == "m" && compact.endsWith("m")) compact.dropLast(1) else ""
        val quantity =
            if (plain.matches(metres))
                metres.toDoubleOrNull()?.takeIf(Double::isFinite)?.let { Parsed(it, true, "m") }
            else parsed(text)
        return quantity?.let {
            if (fieldUnit !in UnitCatalog.fieldUnits) return null
            val fieldScale = UnitCatalog.scale(fieldUnit)
            val baseUnit = UnitCatalog.canonical(fieldUnit)
            if (it.unit.isNotBlank() && baseUnit.isNotBlank() && it.unit != baseUnit) return null
            val normalised = if (it.unit.isNotBlank()) it.value / fieldScale else it.value
            (normalised * if (it.explicitScale) 1.0 else 10.0.pow(exponent)).takeIf(
                Double::isFinite
            )
        }
    }

    /**
     * Exposes suffix precedence to the UI without duplicating parser grammar.
     *
     * @param text Input or output text as described above; treated as data, never executed.
     * @param exponent Dropdown base-ten scale, ignored when text already supplies explicit scale.
     * @return True when explicit input scale overrides a non-base selector. Invalid input follows
     *   the nullable/typed-validation contract above; this operation performs no I/O.
     */
    fun ignoresSelector(text: String, exponent: Int): Boolean =
        exponent != 0 && parsed(text)?.explicitScale == true

    /**
     * Rounds human-readable decimals to four significant digits while retaining exact fractional
     * band tolerances.
     *
     * @param value Input scalar in the operation’s documented units.
     * @return Plain decimal text rounded to four significant digits, with trailing fractional zeros
     *   removed. Invalid physical constraints throw IllegalArgumentException for the calculator
     *   boundary to report; no persistence occurs here.
     */
    fun decimal(value: Double): String {
        require(value.isFinite()) { Messages.INPUTS_EXCEED_THE_NUMERICAL_CALCULATION_RANGE }
        return BigDecimal.valueOf(if (value == 0.0) 0.0 else value)
            .round(MathContext(4, RoundingMode.HALF_UP))
            .stripTrailingZeros()
            .toPlainString()
    }

    /**
     * Uses ordinary decimal percentages so tolerance values never acquire milli-percent prefixes.
     *
     * @param value Input scalar in the operation’s documented units.
     * @return Rounded decimal text followed by the literal percent sign. Invalid physical
     *   constraints throw IllegalArgumentException for the calculator boundary to report; no
     *   persistence occurs here.
     */
    fun percent(value: Double): String = decimal(value) + "%"

    /**
     * Separates physical engineering notation from unitless decimal/scientific notation to preserve
     * dimensional meaning.
     *
     * @param value Input scalar in the operation’s documented units.
     * @param unit Physical output unit; blank denotes a dimensionless result.
     * @return Formatted quantity text; dimensionless values never receive an SI prefix letter.
     *   Invalid physical constraints throw IllegalArgumentException for the calculator boundary to
     *   report; no persistence occurs here.
     */
    fun format(value: Double, unit: String = ""): String {
        require(value.isFinite()) { Messages.INPUTS_EXCEED_THE_NUMERICAL_CALCULATION_RANGE }
        if (unit == "%") return percent(value)
        // Keep an already scaled label intact instead of emitting invalid compound prefixes.
        if (unit in UnitCatalog.plainDisplayUnits) return format(value) + " " + unit
        if (unit.isBlank() || unit == "V/V") {
            val decimal = BigDecimal.valueOf(if (value == 0.0) 0.0 else value)
            val plain = (if (abs(value) < 1e6) decimal.round(MathContext(6, RoundingMode.HALF_UP)) else decimal)
                .stripTrailingZeros().toPlainString()
            return plain + if (unit.isBlank()) "" else " $unit"
        }
        if (value == 0.0) return "0 $unit".trim()
        var exponent = (floor(log10(abs(value)) / 3).toInt() * 3)
        if (exponent !in -12..12)
            return String.format(Locale.US, "%.6g", value) + if (unit.isBlank()) "" else " $unit"
        var scaled = value / 10.0.pow(exponent)
        // Promote a rounded mantissa at 1000 before choosing its prefix (avoid 1000 mΩ).
        if (abs(String.format(Locale.US, "%.6g", scaled).toDouble()) >= 1000.0 && exponent < 12) {
            exponent += 3
            scaled = value / 10.0.pow(exponent)
        }
        val prefix =
            mapOf(
                    -12 to "p",
                    -9 to "n",
                    -6 to "µ",
                    -3 to "m",
                    0 to "",
                    3 to "k",
                    6 to "M",
                    9 to "G",
                    12 to "T",
                )
                .getValue(exponent)
        val number =
            String.format(Locale.US, "%.6g", scaled).let {
                if ('.' in it && !it.contains('e', true)) it.trimEnd('0').trimEnd('.') else it
            }
        return "$number $prefix$unit".trim()
    }
}

/**
 * Keeps physical equations Android-independent so known physical points can be tested without a UI.
 * Construction holds metadata/helpers; physical validation is performed by the named operations,
 * not by field assignment.
 */
object Circuits {
    /**
     * Rejects non-positive and non-finite quantities before physical divisions and square roots.
     *
     * @param x Quantities required to be finite and strictly greater than zero.
     * @return Unit after all constraints pass. Invalid physical constraints throw
     *   IllegalArgumentException for the calculator boundary to report; no persistence occurs here.
     */
    fun positive(vararg x: Double) {
        require(x.all { it.isFinite() && it > 0 }) { Messages.VALUES_MUST_BE_POSITIVE_AND_FINITE }
    }

    /**
     * Solves V=IR and P=VI from exactly two independent positive physical quantities.
     *
     * @param v Voltage in volts, or resistance scalar when used by the SMD encoder.
     * @param i Current in amperes.
     * @param r Resistance in ohms.
     * @param p Power in watts.
     * @return Voltage, current, resistance and power keyed by dimensional labels. Invalid physical
     *   constraints throw IllegalArgumentException for the calculator boundary to report; no
     *   persistence occurs here.
     */
    fun ohm(v: Double?, i: Double?, r: Double?, p: Double?): Map<String, Double> {
        require(listOf(v, i, r, p).count { it != null } == 2) {
            Messages.ENTER_EXACTLY_TWO_OF_V_I_R_AND
        }
        positive(*listOfNotNull(v, i, r, p).toDoubleArray())
        val resistance =
            when {
                r != null -> r
                v != null && i != null -> v / i
                v != null && p != null -> v * v / p
                else -> p!! / (i!! * i)
            }
        val current =
            when {
                i != null -> i
                v != null -> v / resistance
                else -> sqrt(p!! / resistance)
            }
        return mapOf(
            Messages.VOLTAGE_V to current * resistance,
            Messages.CURRENT_A to current,
            Messages.RESISTANCE to resistance,
            Messages.POWER_W to current * current * resistance,
        )
    }

    /**
     * Includes the optional parallel load when evaluating the lower divider leg.
     *
     * @param v Voltage in volts, or resistance scalar when used by the SMD encoder.
     * @param r1 Upper/timing resistance in ohms.
     * @param r2 Lower/timing resistance in ohms.
     * @param load Optional parallel load resistance in ohms.
     * @return Output voltage in volts. Invalid physical constraints throw IllegalArgumentException
     *   for the calculator boundary to report; no persistence occurs here.
     */
    fun divider(v: Double, r1: Double, r2: Double, load: Double? = null): Double {
        positive(v, r1, r2)
        if (load != null) positive(load)
        val lower = if (load != null) parallel(listOf(r2, load)) else r2
        return v * lower / (r1 + lower)
    }

    /**
     * Adds positive component values; capacitor callers swap the series/parallel interpretation
     * explicitly.
     *
     * @param values Caller-owned field/component snapshot; not mutated by this operation.
     * @return Sum in the same physical units as the inputs. Invalid physical constraints throw
     *   IllegalArgumentException for the calculator boundary to report; no persistence occurs here.
     */
    fun series(values: List<Double>): Double {
        require(values.isNotEmpty()) { Messages.ENTER_AT_LEAST_ONE_COMPONENT_VALUE }
        positive(*values.toDoubleArray())
        return values.sum()
    }

    /**
     * Uses reciprocal addition so a positive parallel resistance remains below either branch.
     *
     * @param values Caller-owned field/component snapshot; not mutated by this operation.
     * @return Reciprocal-sum equivalent in the input units. Invalid physical constraints throw
     *   IllegalArgumentException for the calculator boundary to report; no persistence occurs here.
     */
    fun parallel(values: List<Double>): Double {
        series(values)
        return 1 / values.sumOf { 1 / it }
    }

    /**
     * Uses nominal NE555 one-third/two-thirds thresholds to calculate astable timing.
     *
     * @param r1 Upper/timing resistance in ohms.
     * @param r2 Lower/timing resistance in ohms.
     * @param c Capacitance in farads.
     * @return Frequency in Hz, duty in percent and high/low durations in seconds. Invalid physical
     *   constraints throw IllegalArgumentException for the calculator boundary to report; no
     *   persistence occurs here.
     */
    fun astable(r1: Double, r2: Double, c: Double): Map<String, Double> {
        positive(r1, r2, c)
        val h = ln(2.0) * (r1 + r2) * c
        val l = ln(2.0) * r2 * c
        return mapOf(
            Messages.FREQUENCY_HZ to 1 / (h + l),
            Messages.DUTY to 100 * h / (h + l),
            Messages.HIGH_S to h,
            Messages.LOW_S to l,
        )
    }

    /**
     * Uses the nominal NE555 two-thirds-supply threshold for monostable pulse duration.
     *
     * @param r Resistance in ohms.
     * @param c Capacitance in farads.
     * @return Nominal pulse duration in seconds. Invalid physical constraints throw
     *   IllegalArgumentException for the calculator boundary to report; no persistence occurs here.
     */
    fun mono(r: Double, c: Double): Double {
        positive(r, c)
        return ln(3.0) * r * c
    }

    /**
     * Finds a limiting resistor per LED string; parallel strings require separate limiting
     * resistors.
     *
     * @param v Voltage in volts, or resistance scalar when used by the SMD encoder.
     * @param vf Forward voltage drop of one LED, in volts.
     * @param i Current in amperes.
     * @param n Preferred-series size or LED count as stated by this operation.
     * @return Per-string limiting resistance in ohms. Invalid physical constraints throw
     *   IllegalArgumentException for the calculator boundary to report; no persistence occurs here.
     */
    fun led(v: Double, vf: Double, i: Double, n: Double = 1.0): Double {
        positive(v, vf, i, n)
        require(v > vf * n) { Messages.SUPPLY_MUST_EXCEED_THE_LED_STRING_VOLTAGE }
        return (v - vf * n) / i
    }

    /**
     * Includes adjust-pin current so high divider resistances do not silently omit its voltage
     * contribution.
     *
     * @param r1 Upper/timing resistance in ohms.
     * @param r2 Lower/timing resistance in ohms.
     * @param iadj Regulator adjust-pin current in amperes.
     * @return Nominal regulated output voltage in volts. Invalid physical constraints throw
     *   IllegalArgumentException for the calculator boundary to report; no persistence occurs here.
     */
    fun lm317(r1: Double, r2: Double, iadj: Double = 50e-6): Double {
        positive(r1, r2)
        require(iadj.isFinite() && iadj >= 0) { Messages.ADJUST_CURRENT_MUST_BE_ZERO_OR_POSITIVE }
        return 1.25 * (1 + r2 / r1) + iadj * r2
    }

    /**
     * Computes the ideal first-order cutoff rather than implying a component-specific bandwidth
     * guarantee.
     *
     * @param r Resistance in ohms.
     * @param c Capacitance in farads.
     * @return Ideal cutoff frequency in hertz. Invalid physical constraints throw
     *   IllegalArgumentException for the calculator boundary to report; no persistence occurs here.
     */
    fun rc(r: Double, c: Double): Double {
        positive(r, c)
        return 1 / (2 * PI * r * c)
    }

    /**
     * Inverts the empirical IPC-2221 current equation; the result is an estimate, not certified
     * ampacity.
     *
     * @param i Current in amperes.
     * @param dt Permitted temperature rise in degrees Celsius.
     * @param oz Copper weight in ounces per square foot.
     * @param internal True selects the IPC internal-layer coefficient.
     * @return Estimated width in millimetres. Invalid physical constraints throw
     *   IllegalArgumentException for the calculator boundary to report; no persistence occurs here.
     */
    fun traceWidth(i: Double, dt: Double, oz: Double, internal: Boolean): Double {
        positive(i, dt, oz)
        // IPC-2221 uses square mils: I=k ΔT^0.44 A^0.725; internal layers use half the external k.
        val k = if (internal) 0.024 else 0.048
        val area = (i / (k * dt.pow(0.44))).pow(1 / 0.725) // square mils
        return area / (oz * 1.378) * 0.0254 // mil to mm; nominal 35 µm per oz
    }

    /**
     * Includes both conductors of a DC loop while accepting one-way cable length.
     *
     * @param length One-way conductor length in metres.
     * @param area Conductor cross-section in square millimetres.
     * @param i Current in amperes.
     * @param rho Resistivity in ohm square millimetres per metre.
     * @return DC two-wire loop drop in volts. Invalid physical constraints throw
     *   IllegalArgumentException for the calculator boundary to report; no persistence occurs here.
     */
    fun voltageDrop(length: Double, area: Double, i: Double, rho: Double = 0.017241): Double {
        positive(length, area, i, rho)
        return 2 * length * rho * i / area
    }

    /**
     * Evaluates the nominal platinum curve over the supported temperature interval, with an
     * optional linear approximation.
     *
     * @param t Temperature in degrees Celsius.
     * @param r0 Nominal RTD resistance at zero degrees Celsius in ohms.
     * @param linear True selects the stated linear platinum approximation.
     * @return Nominal resistance in ohms. Invalid physical constraints throw
     *   IllegalArgumentException for the calculator boundary to report; no persistence occurs here.
     */
    fun rtd(t: Double, r0: Double, linear: Boolean = false): Double {
        require(t in -200.0..850.0) { Messages.TEMPERATURE_MUST_BE_BETWEEN_200_AND_850_C }
        positive(r0)
        // Platinum CVD: A=3.9083e−3, B=−5.775e−7, C=−4.183e−12; the cubic correction is below zero
        // only.
        // Linear mode deliberately uses alpha=0.00385 and is not the full CVD curve.
        return if (linear) r0 * (1 + 0.00385 * t)
        else
            r0 *
                (1 + 3.9083e-3 * t - 5.775e-7 * t * t +
                    if (t < 0) -4.183e-12 * (t - 100) * t * t * t else 0.0)
    }

    /**
     * Inverts the monotonic platinum curve and quantises microdegrees to remove bisection residue
     * near zero.
     *
     * @param r Resistance in ohms.
     * @param r0 Nominal RTD resistance at zero degrees Celsius in ohms.
     * @param linear True selects the stated linear platinum approximation.
     * @return Temperature quantised to 1e-6 °C, with signed zero normalised. Invalid physical
     *   constraints throw IllegalArgumentException for the calculator boundary to report; no
     *   persistence occurs here.
     */
    fun rtdTemperature(r: Double, r0: Double, linear: Boolean = false): Double {
        positive(r, r0)
        if (linear)
            return ((r / r0 - 1) / 0.00385).also {
                require(it in -200.0..850.0) {
                    Messages.RESISTANCE_CORRESPONDS_TO_A_TEMPERATURE_OUTSIDE_200_850
                }
            }
        require(r in rtd(-200.0, r0)..rtd(850.0, r0)) { Messages.RESISTANCE_OUTSIDE_200_850_C }
        var lo = -200.0
        var hi = 850.0
        repeat(70) {
            val m = (lo + hi) / 2
            if (rtd(m, r0) < r) lo = m else hi = m
        }
        return (round(((lo + hi) / 2) * 1e6) / 1e6).let { if (it == 0.0) 0.0 else it }
    }
}

/**
 * Shares colour, preferred-value and printed-marking rules across forward and reverse component
 * lookups. Construction holds metadata/helpers; physical validation is performed by the named
 * operations, not by field assignment.
 */
object Resistors {
    /** Colour names in standard band-index order. */
    val names =
        listOf(
            Messages.BLACK,
            Messages.BROWN,
            Messages.RED,
            Messages.ORANGE,
            Messages.YELLOW,
            Messages.GREEN,
            Messages.BLUE,
            Messages.VIOLET,
            Messages.GREY,
            Messages.WHITE,
            Messages.GOLD,
            Messages.SILVER,
        )
    /** Tolerance percentages keyed by colour; these numbers are percent, not fractions. */
    val tolerance =
        mapOf(1 to 1.0, 2 to 2.0, 5 to 0.5, 6 to 0.25, 7 to 0.1, 8 to 0.05, 10 to 5.0, 11 to 10.0)

    /**
     * Converts significant colour digits and a decimal multiplier into a physical resistance.
     *
     * @param digits Significant colour-band digit indices.
     * @param multiplier Base-ten exponent, including negative gold/silver multipliers.
     * @return Decoded physical resistance in ohms, or a preference snapshot in the persistence
     *   codec. Invalid physical constraints throw IllegalArgumentException for the calculator
     *   boundary to report; no persistence occurs here.
     */
    fun decode(digits: List<Int>, multiplier: Int): Double {
        require(digits.size in 2..3 && digits[0] in 1..9 && digits.all { it in 0..9 }) {
            Messages.SELECT_TWO_OR_THREE_VALID_DIGITS_WITH_A
        }
        require(multiplier in -2..9) { Messages.MULTIPLIER_MUST_BE_BETWEEN_0_01_AND_ONE }
        return digits.fold(0) { a, b -> a * 10 + b } * 10.0.pow(multiplier)
    }

    /**
     * Rounds resistance to the representable band precision; the persistence codec instead
     * serialises its state argument.
     *
     * @param value Input scalar in the operation’s documented units.
     * @param five True uses three significant digits (five-band resistor).
     * @return Band digits and multiplier exponent, or JSON text in the persistence codec. Invalid
     *   physical constraints throw IllegalArgumentException for the calculator boundary to report;
     *   no persistence occurs here.
     */
    fun encode(value: Double, five: Boolean = false): List<Int> {
        Circuits.positive(value)
        val count = if (five) 3 else 2
        var e = floor(log10(value)).toInt() - count + 1
        var d = (value / 10.0.pow(e)).roundToInt()
        if (d >= 10.0.pow(count)) {
            d /= 10
            e++
        }
        require(e in -2..9) { Messages.VALUE_OUTSIDE_AVAILABLE_BAND_RANGE }
        return d.toString().map { it.digitToInt() } + e
    }

    /** Preferred E12 mantissas for a normalised decade. */
    val e12 = listOf(1.0, 1.2, 1.5, 1.8, 2.2, 2.7, 3.3, 3.9, 4.7, 5.6, 6.8, 8.2)
    /** Preferred E24 mantissas for a normalised decade. */
    val e24 =
        listOf(
            1.0,
            1.1,
            1.2,
            1.3,
            1.5,
            1.6,
            1.8,
            2.0,
            2.2,
            2.4,
            2.7,
            3.0,
            3.3,
            3.6,
            3.9,
            4.3,
            4.7,
            5.1,
            5.6,
            6.2,
            6.8,
            7.5,
            8.2,
            9.1,
        )
    /** Preferred E96 integer significands; normalisation divides by 100. */
    val e96 =
        listOf(
            100,
            102,
            105,
            107,
            110,
            113,
            115,
            118,
            121,
            124,
            127,
            130,
            133,
            137,
            140,
            143,
            147,
            150,
            154,
            158,
            162,
            165,
            169,
            174,
            178,
            182,
            187,
            191,
            196,
            200,
            205,
            210,
            215,
            221,
            226,
            232,
            237,
            243,
            249,
            255,
            261,
            267,
            274,
            280,
            287,
            294,
            301,
            309,
            316,
            324,
            332,
            340,
            348,
            357,
            365,
            374,
            383,
            392,
            402,
            412,
            422,
            432,
            442,
            453,
            464,
            475,
            487,
            499,
            511,
            523,
            536,
            549,
            562,
            576,
            590,
            604,
            619,
            634,
            649,
            665,
            681,
            698,
            715,
            732,
            750,
            768,
            787,
            806,
            825,
            845,
            866,
            887,
            909,
            931,
            953,
            976,
        )

    /**
     * Keeps preferred mantissas independent of decade expansion for nearest-value and ratio
     * searches.
     *
     * @param n Preferred-series size or LED count as stated by this operation.
     * @return Normalised preferred mantissas for the requested series. Invalid input follows the
     *   nullable/typed-validation contract above; this operation performs no I/O.
     */
    fun standard(n: Int) =
        when (n) {
            12 -> e12
            24 -> e24
            else -> e96.map { it / 100.0 }
        }

    /**
     * Searches adjacent decades so candidates near a decade boundary are not missed.
     *
     * @param value Input scalar in the operation’s documented units.
     * @param n Preferred-series size or LED count as stated by this operation.
     * @param up True restricts nearest-value candidates to at least the requested value.
     * @return Closest permitted standard resistance in ohms. Invalid physical constraints throw
     *   IllegalArgumentException for the calculator boundary to report; no persistence occurs here.
     */
    fun nearest(value: Double, n: Int = 24, up: Boolean = false): Double {
        Circuits.positive(value)
        val e = floor(log10(value)).toInt()
        return ((e - 1)..(e + 1))
            .flatMap { power -> standard(n).map { it * 10.0.pow(power) } }
            .filter { !up || it >= value || abs(it - value) <= value * 1e-12 }
            .minBy { abs(it - value) }
    }

    /**
     * Decodes numeric, R-decimal and EIA-96 markings while rejecting arbitrary trailing text.
     *
     * @param code Printed component marking, interpreted as data.
     * @return Decoded resistance in ohms. Invalid physical constraints throw
     *   IllegalArgumentException for the calculator boundary to report; no persistence occurs here.
     */
    fun smd(code: String): Double {
        val s = code.uppercase().trim()
        if (s.contains('R'))
            return Engineering.parse(s) ?: throw IllegalArgumentException(Messages.INVALID_R_NOTATION)
        if (s.length == 3 && s.take(2).all { it in '0'..'9' } && s.last().isLetter()) {
            val idx = s.take(2).toInt()
            require(idx in 1..96) { Messages.EIA_96_CODE_MUST_START_WITH_A_NUMBER }
            val m =
                mapOf(
                    'Z' to 0.001,
                    'Y' to 0.01,
                    'R' to 0.01,
                    'X' to 0.1,
                    'S' to 0.1,
                    'A' to 1.0,
                    'B' to 10.0,
                    'H' to 10.0,
                    'C' to 100.0,
                    'D' to 1000.0,
                    'E' to 10000.0,
                    'F' to 100000.0,
                )[s.last()] ?: throw IllegalArgumentException(Messages.INVALID_EIA_96_MULTIPLIER)
            return e96[idx - 1] * m
        }
        require(s.length in 3..4 && s.all { it in '0'..'9' }) {
            Messages.USE_3_4_DIGITS_R_NOTATION_OR_EIA
        }
        return s.dropLast(1).toDouble() * 10.0.pow(s.last().digitToInt())
    }

    /**
     * Reports printed-code approximations and plain-decimal R notation without embedding scientific
     * exponents in markings.
     *
     * @param v Voltage in volts, or resistance scalar when used by the SMD encoder.
     * @return Printed-code approximations and R notation, or an explicit practical-range message.
     *   Invalid physical constraints throw IllegalArgumentException for the calculator boundary to
     *   report; no persistence occurs here.
     */
    fun smdCodes(v: Double): String {
        Circuits.positive(v)
        /**
         * Uses numeric significant digits when representable and falls back to plain R notation
         * otherwise.
         *
         * @param d Number of significant digits in a printed numeric marking.
         * @return One numeric marking or a plain-decimal R-notation alternative. Invalid physical
         *   constraints throw IllegalArgumentException for the calculator boundary to report; no
         *   persistence occurs here.
         */
        fun numeric(d: Int): String {
            val e = floor(log10(v)).toInt() - d + 1
            val sig = (v / 10.0.pow(e)).roundToInt()
            return if (e in 0..9 && sig < 10.0.pow(d))
                "$sig$e (${Engineering.format(sig*10.0.pow(e),"Ω")})"
            else {
                val plain = BigDecimal.valueOf(v).stripTrailingZeros().toPlainString()
                val notation = if ('.' in plain) plain.replace('.', 'R') else plain + "R0"
                if (notation.length <= 24) Messages.rNotation(notation)
                else Messages.NO_PRACTICAL_PRINTED_R_NOTATION_EXISTS_FOR_THIS
            }
        }
        val candidates =
            (-3..5).flatMap { e ->
                e96.mapIndexed { ix, x ->
                    Triple(
                        abs(x * 10.0.pow(e) - v),
                        "%02d".format(Locale.US, ix + 1) +
                            mapOf(
                                -3 to "Z",
                                -2 to "Y",
                                -1 to "X",
                                0 to "A",
                                1 to "B",
                                2 to "C",
                                3 to "D",
                                4 to "E",
                                5 to "F",
                            )[e],
                        x * 10.0.pow(e),
                    )
                }
            }
        val best = candidates.minBy { it.first }
        return Messages.smdResult(
            numeric(2),
            numeric(3),
            best.second,
            Engineering.format(best.third, "Ω"),
        )
    }
}
