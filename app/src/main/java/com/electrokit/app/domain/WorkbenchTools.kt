package com.electrokit.app.domain

import kotlin.math.*

/** SI-input engineering utility metadata.
 * @property id Stable route key.
 * @property title Display name.
 * @property fields Named input labels and explicit units.
 * @property formula Physical model used to derive the result.
 * @property limitation Boundary of the simplified calculation.
 */
data class WorkbenchTool(val id: String, val title: String, val fields: List<String>, val formula: String, val limitation: String)

/** Additional tools use explicit assumptions instead of embedding cable or equipment ratings. */
object WorkbenchTools {
    /** Public utility catalogue, independent of protected controller reference data. */
    val all = listOf(
        WorkbenchTool("radix", "Binary / HEX converter", listOf("Value", "Source base (2, 10, 16)"), "Convert an unsigned integer between base 2, 10 and 16", "Whole numbers only, up to 256 input characters. ASCII is shown only for printable single-byte values."),
        WorkbenchTool("transformer", "Ideal transformer", listOf("Primary voltage (V)", "Primary turns", "Secondary turns"), "Vs = Vp × Ns / Np", "Ideal AC transformer ratio only; losses, insulation, frequency, VA and regulation are not modeled."),
        WorkbenchTool("ups", "Battery / UPS runtime", listOf("Battery voltage (V)", "Capacity (Ah)", "Usable depth (%)", "Efficiency (%)", "Load (W)"), "t = V × Ah × depth/100 × efficiency/100 / P", "Constant-power estimate. Battery aging, temperature, discharge-rate effects and inverter standby losses are excluded. Not a runtime guarantee."),
        WorkbenchTool("cable", "DC cable voltage-drop sizing", listOf("One-way length (m)", "Current (A)", "Resistivity (Ω·mm²/m)", "Allowed drop (V)"), "A = 2 × ρ × L × I / ΔV", "Two-conductor DC resistance model with user-entered resistivity. This does not select code-compliant cable, insulation, ampacity, protection, temperature or standard sizes."),
        WorkbenchTool("pf", "Single-phase power factor", listOf("Real power (W)", "RMS voltage (V)", "RMS current (A)"), "PF = P / (Vrms × Irms)", "Single-phase total power factor. This does not resolve leading/lagging behavior, harmonics or three-phase imbalance."),
        WorkbenchTool("energy", "Energy consumption", listOf("Power (W)", "Time (h)", "Price per kWh"), "E = P × t / 1000; cost = E × price", "Constant power and a flat user-entered tariff; demand charges and time-of-use rates are excluded."),
        WorkbenchTool("temperature", "Temperature converter", listOf("Celsius (°C)"), "°F = °C × 9/5 + 32; K = °C + 273.15", "Thermodynamic temperature cannot be below absolute zero."),
        WorkbenchTool("scientific", "Scientific calculator", listOf("Expression"), "+ − * / ^ ( ) sqrt(x) sin(x) cos(x) tan(x) ln(x) log(x); pi, e", "Angles in radians. Finite real arithmetic only. Exponentiation binds more tightly than unary minus. No code or scripts are executed.")
    )

    /** Evaluates finite SI inputs, rejecting invalid physical domains before applying a model.
     * @param id Stable utility ID.
     * @param raw Ordered field values, with engineering prefixes accepted by the shared parser.
     * @return Result with units or a readable validation error; no state or I/O is modified.
     */
    fun calculate(id: String, raw: List<String>): String = evaluate(id, raw).text

    /** Keeps validation failures separate from results so UI actions cannot save error text.
     * @param id Stable utility ID.
     * @param raw Ordered input values from the tool fields.
     * @return A valid calculation or a readable validation error.
     */
    fun evaluate(id: String, raw: List<String>): Calculation = try {
        Calculation.Value(calculateChecked(id, raw))
    } catch (e: IllegalArgumentException) {
        Calculation.Error(e.message ?: "Invalid input.")
    } catch (e: IllegalStateException) {
        Calculation.Error(e.message ?: "Invalid input.")
    }

    private fun calculateChecked(id: String, raw: List<String>): String {
        val tool = requireNotNull(all.firstOrNull { it.id == id }) { "Unknown utility." }
        require(raw.size == tool.fields.size) { "Enter every required field." }
        return if (id == "radix") {
            val base = raw[1].trim().toIntOrNull()
            require(base in listOf(2, 10, 16)) { "Source base must be 2, 10 or 16." }
            val input = raw[0].trim()
            require(input.length in 1..256 && input.all { it.digitToIntOrNull(base!!) != null }) { "Enter valid unsigned digits for the selected base (up to 256 characters)." }
            val value = java.math.BigInteger(input, base!!)
            "Binary: ${value.toString(2)}\nDecimal: ${value.toString(10)}\nHEX: ${value.toString(16).uppercase(java.util.Locale.ROOT)}" +
                if (value >= java.math.BigInteger.valueOf(32) && value <= java.math.BigInteger.valueOf(126)) "\nASCII: ${value.toInt().toChar()}" else "\nASCII: not a printable single-byte character"
        } else if (id == "scientific") Engineering.format(Expression(raw.single().lowercase(java.util.Locale.ROOT)).evaluate())
        else {
            val v = raw.map { Engineering.parse(it)?.takeIf(Double::isFinite) ?: error("Enter a valid number in every field.") }
            /** Positive input used for nonzero denominators and physical dimensions.
             * @param i Ordered field index from the selected model.
             * @return Validated scalar; invalid domains throw a readable argument error.
             */
            fun positive(i: Int): Double = v[i].also { require(it > 0) { "This model requires positive inputs." } }
            /** Nonnegative quantity allowing a valid zero result.
             * @param i Ordered field index from the selected model.
             * @return Validated scalar; invalid domains throw a readable argument error.
             */
            fun nonnegative(i: Int): Double = v[i].also { require(it >= 0) { "This model requires nonnegative inputs." } }
            /** Positive percentage at most 100, converted to a dimensionless factor.
             * @param i Ordered field index from the selected model.
             * @return Validated scalar; invalid domains throw a readable argument error.
             */
            fun percent(i: Int): Double = positive(i).also { require(it <= 100) { "Percentages must be above zero and at most 100." } } / 100
            when (id) {
                "transformer" -> Engineering.format(positive(0) * positive(2) / positive(1), "V")
                "ups" -> Engineering.format(positive(0) * positive(1) * percent(2) * percent(3) / positive(4), "h")
                "cable" -> Engineering.format(2 * positive(0) * positive(1) * positive(2) / positive(3), "mm²")
                "pf" -> (nonnegative(0) / (positive(1) * positive(2))).also { require(it <= 1 + 1e-12) { "Real power cannot exceed apparent power in this model." } }.let { "PF = ${Engineering.format(it.coerceAtMost(1.0))}" }
                "energy" -> (nonnegative(0) * nonnegative(1) / 1000).let { "Energy: ${Engineering.format(it, "kWh")}\nCost: ${java.math.BigDecimal.valueOf(it * nonnegative(2)).setScale(2, java.math.RoundingMode.HALF_UP).toPlainString()} (entered currency)" }
                "temperature" -> { require(v[0] >= -273.15) { "Temperature is below absolute zero." }; "${Engineering.format(v[0] * 9 / 5 + 32, "°F")}\n${Engineering.format(v[0] + 273.15, "K")}" }
                else -> error("Unknown utility.")
            }
        }
    }
}

/** Bounded recursive-descent arithmetic parser; accepts a small mathematical grammar only.
 * @property text Expression supplied by the user, never passed to an interpreter.
 */
private class Expression(private val text: String) {
    /** Current parsing offset; length and recursion are bounded before evaluation. */
    private var index = 0

    /** Parses a complete expression, requiring finite real output.
     * @return Finite value; malformed input, excessive nesting and domain errors throw an argument error.
     */
    fun evaluate(): Double {
        require(text.length in 1..256 && text.count { it == '(' } <= 24) { "Use an expression of 1–256 characters with at most 24 groups." }
        val result = sum()
        whitespace()
        require(index == text.length && result.isFinite()) { "Invalid expression or non-finite result." }
        return result
    }

    /** Skips ordinary whitespace without accepting arbitrary tokens. @return Unit. */
    private fun whitespace() { while (index < text.length && text[index].isWhitespace()) index++ }

    /** Consumes one optional operator.
     * @param c Expected character.
     * @return True when consumed; false leaves the next non-space token untouched.
     */
    private fun eat(c: Char): Boolean { whitespace(); if (index < text.length && text[index] == c) { index++; return true }; return false }

    /** Parses addition/subtraction. @return Left-associative sum of terms. */
    private fun sum(): Double { var v = product(); while (true) v = when { eat('+') -> v + product(); eat('-') -> v - product(); else -> return v } }

    /** Parses multiplication/division. @return Left-associative product, rejecting division by zero. */
    private fun product(): Double { var v = unary(); while (true) v = when { eat('*') -> v * unary(); eat('/') -> { val d = unary(); require(d != 0.0) { "Division by zero." }; v / d }; else -> return v } }

    /** Parses unary signs with exponentiation precedence. @return Signed value. */
    private fun unary(): Double = when { eat('+') -> unary(); eat('-') -> -unary(); else -> power() }

    /** Parses right-associative exponentiation. @return Base raised to the optional exponent. */
    private fun power(): Double { val v = atom(); return if (eat('^')) v.pow(unary()) else v }

    /** Parses a number, named constant/function or parenthesized expression.
     * @return Real scalar; unknown functions and malformed numbers throw an argument error.
     */
    private fun atom(): Double {
        whitespace()
        if (eat('(')) { val v = sum(); require(eat(')')) { "Missing closing parenthesis." }; return v }
        val start = index
        while (index < text.length && text[index].isLetter()) index++
        if (index > start) {
            val name = text.substring(start, index)
            if (name == "pi") return PI
            if (name == "e") return E
            require(eat('(')) { "Functions require parentheses." }
            val v = sum(); require(eat(')')) { "Missing closing parenthesis." }
            return when(name) { "sqrt" -> sqrt(v); "sin" -> sin(v); "cos" -> cos(v); "tan" -> tan(v); "ln" -> ln(v); "log" -> log10(v); else -> throw IllegalArgumentException("Unknown function.") }
        }
        while (index < text.length && (text[index] in '0'..'9' || text[index] == '.')) index++
        if (index < text.length && text[index] in "eE") {
            index++; if (index < text.length && text[index] in "+-") index++
            while (index < text.length && text[index] in '0'..'9') index++
        }
        return text.substring(start, index).toDoubleOrNull() ?: throw IllegalArgumentException("Expected a number.")
    }
}
