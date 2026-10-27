package com.electrokit.app.domain

import kotlin.math.*

/**
 * User-visible validation messages for the new pure calculator boundary; no UI resources or I/O.
 */
object EngineeringMessages {
    /** Missing/nonfinite/wrong-dimension scalar feedback. */
    const val INPUT = "Enter a finite value in every required field, using the displayed units."
    /** Invalid physical range feedback. */
    const val RANGE =
        "Check the physical ranges: positive quantities, PF/efficiency limits and whole-number poles or teeth."
    /** Overflow/underflow feedback. */
    const val NUMERICAL =
        "The values exceed the supported numerical range; use less extreme inputs."
}

/** Pure motor, power, geometry and kinematic calculations with explicit physical assumptions. */
object EngineeringTools {
    /** Exact source/target units and numerical factors for the available converter choices. */
    val conversions =
        linkedMapOf(
            "W → kW" to Triple("W", "kW", .001),
            "kW → W" to Triple("kW", "W", 1000.0),
            "A → mA" to Triple("A", "mA", 1000.0),
            "mA → A" to Triple("mA", "A", .001),
            "mm → m" to Triple("mm", "m", .001),
            "m → mm" to Triple("m", "mm", 1000.0),
            "m/s → km/h" to Triple("m/s", "km/h", 3.6),
            "km/h → m/s" to Triple("km/h", "m/s", 1 / 3.6),
            "kWh → J" to Triple("kWh", "J", 3600000.0),
            "J → kWh" to Triple("J", "kWh", 1 / 3600000.0),
        )

    /**
     * Evaluates an explicit tool input map; no missing physical parameter is defaulted.
     *
     * @param id Stable calculator ID from engineering_tools.json.
     * @param values Raw saved text with optional unit suffixes and conversion selection.
     * @return Value with substitution steps, or actionable Error for invalid input; no input
     *   exception escapes.
     */
    fun calculate(id: String, values: Map<String, String>): Calculation {
        return try {
            /**
             * Resolves one required finite scalar in its declared field unit.
             *
             * @param key Stable field key.
             * @param unit Expected unit; blank is explicitly dimensionless.
             * @return Valid scalar or throws IllegalArgumentException, caught by the boundary.
             */
            fun x(key: String, unit: String = ""): Double {
                val raw = values[key].orEmpty()
                // The legacy parser allows omitted dimensions; new explicit ratio fields do not.
                if (unit.isBlank() && !Engineering.parsed(raw)?.unit.isNullOrBlank())
                    throw IllegalArgumentException(EngineeringMessages.INPUT)
                return Engineering.input(raw, 0, unit)
                    ?: throw IllegalArgumentException(EngineeringMessages.INPUT)
            }
            /**
             * Checks a positive physical quantity without conflating zero with missing input.
             *
             * @param v Parsed physical value.
             * @return Valid positive value; invalid values throw the range message, caught above.
             */
            fun positive(v: Double): Double {
                require(v > 0) { EngineeringMessages.RANGE }
                return v
            }
            /**
             * Formats finite outputs without silently printing Infinity, NaN or negative zero.
             *
             * @param v Calculated scalar.
             * @param unit Display unit; blank denotes a dimensionless result.
             * @return Six-significant-digit text; nonfinite results throw and become a numerical
             *   error.
             */
            fun f(v: Double, unit: String = ""): String {
                require(v.isFinite()) { EngineeringMessages.NUMERICAL }
                if (unit == "%") return Engineering.percent(v)
                return Engineering.format(if (v == 0.0) 0.0 else v) +
                    if (unit.isBlank()) "" else " $unit"
            }
            val text =
                when (id) {
                    // Balanced sinusoidal apparent, active and fundamental reactive power, SI
                    // inputs.
                    "single-phase",
                    "three-phase" -> {
                        val v = positive(x("v", "V"))
                        val i = positive(x("i", "A"))
                        val pf = x("pf")
                        require(pf in 0.0..1.0) { EngineeringMessages.RANGE }
                        val factor = if (id == "three-phase") sqrt(3.0) else 1.0
                        val s = factor * v * i
                        "S = ${f(factor)} × ${f(v)} × ${f(i)} = ${f(s / 1000, "kVA")}\nP = S × ${f(pf)} = ${f(s * pf / 1000, "kW")}\nQ = S × √(1 − PF²) = ${f(s * sqrt(1 - pf * pf) / 1000, "kVAR")}"
                    }
                    // Shaft kW is converted to W before dividing by balanced input voltage and
                    // efficiency.
                    "motor-current" -> {
                        val p = positive(x("p", "kW"))
                        val v = positive(x("v", "V"))
                        val pf = x("pf")
                        val eta = x("eta", "%")
                        require(pf > 0 && pf <= 1 && eta > 0 && eta <= 100) {
                            EngineeringMessages.RANGE
                        }
                        "I = ${f(p)} × 1000 / (√3 × ${f(v)} × ${f(pf)} × ${f(eta / 100)})\nCurrent: ${f(p * 1000 / (sqrt(3.0) * v * pf * (eta / 100)), "A")}"
                    }
                    "efficiency" -> {
                        val out = positive(x("out", "kW"))
                        val input = positive(x("in", "kW"))
                        require(out <= input) { EngineeringMessages.RANGE }
                        "η = 100 × ${f(out)} / ${f(input)}\nEfficiency: ${f(100 * out / input, "%")}"
                    }
                    "power-factor" -> {
                        val p = x("p", "kW")
                        val s = positive(x("s", "kVA"))
                        require(p >= 0 && p <= s) { EngineeringMessages.RANGE }
                        "PF = ${f(p)} / ${f(s)}\nPower factor: ${f(p / s)}"
                    }
                    // T in N·m from shaft W and rpm, using the exact 60/(2π) relationship.
                    "torque" -> {
                        val p = positive(x("p", "kW"))
                        val n = positive(x("n", "rpm"))
                        "T = 60 × ${f(p)} × 1000 / (2π × ${f(n)})\nTorque: ${f(60 * p * 1000 / (2 * PI * n), "N·m")}"
                    }
                    // Pole count is an explicit positive even integer; no 6-pole inference is
                    // hidden.
                    "slip",
                    "frequency-speed" -> {
                        val freq = positive(x("f", "Hz"))
                        val poles = positive(x("poles"))
                        require(poles >= 2 && poles % 2 == 0.0 && poles <= 1000) {
                            EngineeringMessages.RANGE
                        }
                        val ns = 120 * freq / poles
                        if (id == "slip") {
                            val n = x("n", "rpm")
                            // Negative slip represents regeneration above synchronous speed.
                            require(n >= 0) { EngineeringMessages.RANGE }
                            "ns = 120 × ${f(freq)} / ${f(poles)} = ${f(ns, "rpm")}\ns = 100 × (${f(ns)} − ${f(n)}) / ${f(ns)}\nSlip: ${f(100 * (ns - n) / ns, "%")}"
                        } else {
                            val slip = x("slip", "%")
                            require(slip < 100) { EngineeringMessages.RANGE }
                            "ns = 120 × ${f(freq)} / ${f(poles)} = ${f(ns, "rpm")}\nn = ${f(ns)} × (1 − ${f(slip)} / 100)\nRotor speed: ${f(ns * (1 - slip / 100), "rpm")}"
                        }
                    }
                    // Ideal persons/hour from metres/second, actual step pitch and explicit
                    // occupancy.
                    "capacity" -> {
                        val v = positive(x("v", "m/s"))
                        val pitch = positive(x("pitch", "m"))
                        val occ = x("occupancy")
                        require(occ >= 0) { EngineeringMessages.RANGE }
                        "C = 3600 × ${f(v)} × ${f(occ)} / ${f(pitch)}\nTheoretical capacity: ${f(3600 * v * occ / pitch, "persons/h")}"
                    }
                    "gear-ratio" -> {
                        val input = positive(x("input", "rpm"))
                        val output = positive(x("output", "rpm"))
                        "ratio = ${f(input)} / ${f(output)}\nReduction ratio: ${f(input / output)}"
                    }
                    "gear" -> {
                        val n = positive(x("n", "rpm"))
                        val ratio = positive(x("ratio"))
                        "nout = ${f(n)} / ${f(ratio)}\nOutput speed: ${f(n / ratio, "rpm")}"
                    }
                    // Pitch-line chain speed; all geometry and total drive reduction are entered
                    // explicitly.
                    "step-speed" -> {
                        val n = positive(x("n", "rpm"))
                        val ratio = positive(x("ratio"))
                        val teeth = positive(x("teeth"))
                        val pitch = positive(x("pitch", "m"))
                        require(teeth % 1 == 0.0) { EngineeringMessages.RANGE }
                        "v = (${f(n)} / ${f(ratio)}) × ${f(teeth)} × ${f(pitch)} / 60\nStep-chain speed: ${f(n / ratio * teeth * pitch / 60, "m/s")}"
                    }
                    "energy" -> {
                        val p = x("p", "kW")
                        val h = x("h", "h")
                        require(p >= 0 && h >= 0) { EngineeringMessages.RANGE }
                        "E = ${f(p)} × ${f(h)}\nElectrical energy: ${f(p * h, "kWh")}"
                    }
                    "geometry" -> {
                        val length = positive(x("length", "m"))
                        val degrees = x("angle", "°")
                        require(degrees > 0 && degrees < 90) { EngineeringMessages.RANGE }
                        val angle = degrees * PI / 180
                        "H = ${f(length)} × sin(${f(degrees)}°) = ${f(length * sin(angle), "m")}\nProjection = ${f(length)} × cos(${f(degrees)}°) = ${f(length * cos(angle), "m")}"
                    }
                    "unit-converter" -> {
                        val pair =
                            conversions[values["conversion"]]
                                ?: throw IllegalArgumentException(EngineeringMessages.INPUT)
                        val value = x("x", pair.first)
                        "${f(value, pair.first)} × ${f(pair.third)}\nConverted value: ${f(value * pair.third, pair.second)}"
                    }
                    else -> throw IllegalArgumentException(EngineeringMessages.INPUT)
                }
            Calculation.Value(text)
        } catch (failure: IllegalArgumentException) {
            Calculation.Error(
                failure.message?.takeIf {
                    it in
                        listOf(
                            EngineeringMessages.INPUT,
                            EngineeringMessages.RANGE,
                            EngineeringMessages.NUMERICAL,
                        )
                } ?: EngineeringMessages.NUMERICAL
            )
        } catch (cancelled: java.util.concurrent.CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            Calculation.Error(EngineeringMessages.NUMERICAL)
        }
    }
}
