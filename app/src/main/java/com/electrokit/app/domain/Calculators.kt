package com.electrokit.app.domain

import kotlin.math.*

/**
 * Defines one stable input key and its presentation independently of mutable Compose state.
 *
 * @param key Stable data key rather than a translated label.
 * @param label Visible field/cell caption used for accessible presentation.
 * @param initial Initial tab/default text, according to the enclosing operation.
 * @param unit Physical output unit; blank denotes a dimensionless result.
 * @param text True identifies marking/list text and bypasses scalar-unit parsing. Construction
 *   holds metadata/helpers; physical validation is performed by the named operations, not by field
 *   assignment.
 */
data class Field(
    /** Stable data key rather than a translated label. */
    val key: String,
    /** Visible contact identifier, not a certified PCB footprint number. */
    val label: String,
    /** Initial tab/default text, according to the enclosing operation. */
    val initial: String = "",
    /** Physical output unit; blank denotes a dimensionless result. */
    val unit: String = "",
    /** True marks free-form code/list input that must bypass scalar-unit conversion. */
    val text: Boolean = false,
)

/**
 * Groups fields, modes and assumptions so the UI and engine do not maintain competing
 * specifications.
 *
 * @param fields Ordered calculator input definitions.
 * @param modes Ordered selectable calculation modes.
 * @param formula Equation and assumptions shown alongside results. Construction holds
 *   metadata/helpers; physical validation is performed by the named operations, not by field
 *   assignment.
 */
data class Calculator(
    /** Ordered calculator input definitions. */
    val fields: List<Field>,
    /** Per-calculator mode indices, clamped by the engine when read. */
    val modes: List<String> = listOf(Messages.CALCULATE),
    /** Equation and assumptions shown alongside results. */
    val formula: String,
)

/**
 * Separates successful values from validation failures so the UI never infers validity by parsing
 * prose.
 *
 * @param text True identifies marking/list text and bypasses scalar-unit parsing.
 * @param plot Normalised chart samples; empty means no plot. Construction holds metadata/helpers;
 *   physical validation is performed by the named operations, not by field assignment.
 */
sealed class Calculation(
    /** True marks free-form code/list input that must bypass scalar-unit conversion. */
    val text: String,
    /** Normalised chart samples; empty means no plot. */
    val plot: List<Double> = emptyList(),
) {
    /**
     * Carries a validated text result and optional normalised samples for a response plot.
     *
     * @param text True identifies marking/list text and bypasses scalar-unit parsing.
     * @param plot Normalised chart samples; empty means no plot. Construction holds
     *   metadata/helpers; physical validation is performed by the named operations, not by field
     *   assignment.
     */
    class Value(text: String, plot: List<Double> = emptyList()) : Calculation(text, plot)

    /**
     * Carries an actionable validation message instead of letting invalid input crash the
     * composition.
     *
     * @param message Recoverable validation text. Construction holds metadata/helpers; physical
     *   validation is performed by the named operations, not by field assignment.
     */
    class Error(message: String) : Calculation(message)
}

/**
 * Defines all calculator inputs and evaluates mode-specific equations behind a typed validation
 * boundary. Construction holds metadata/helpers; physical validation is performed by the named
 * operations, not by field assignment.
 */
object Calculators {
    /**
     * Builds one compact field declaration while retaining a stable persistence key.
     *
     * @param k Stable field key used by snapshots and calculator evaluation.
     * @param l User-visible field label from the shared domain messages.
     * @param v Initial example text, preserved verbatim for reset behaviour.
     * @param u Physical field unit; blank means unitless or free text.
     * @param text True bypasses numeric-unit conversion for markings or list text.
     * @return A Field definition; construction stores metadata and does not validate or throw input
     *   errors.
     */
    private fun f(k: String, l: String, v: String = "", u: String = "", text: Boolean = false) =
        Field(k, l, v, u, text)

    /**
     * Provides a single shared field/mode/formula definition for each stable calculator ID.
     *
     * @param id Stable calculator or registry identifier; independent of the visible title.
     * @return Shared fields, modes and formula assumptions for the calculator. Invalid input
     *   follows the nullable/typed-validation contract above; this operation performs no I/O.
     */
    fun spec(id: Int): Calculator =
        when (id) {
            // c1 Colour bands: R=digits × 10^multiplier Ω; tolerance is percent.
            // c3 Inductor bands: L=digits × 10^multiplier µH.
            1,
            3 ->
                Calculator(
                    emptyList(),
                    formula = Messages.R_SIGNIFICANT_DIGITS_10_MULTIPLIER_INDUCTOR_BANDS_ENCODE,
                )
            // c2 SMD: significant digits × 10^last digit Ω, or EIA-96 lookup.
            2 ->
                Calculator(
                    listOf(
                        f("code", Messages.SMD_MARKING, "102", text = true),
                        f("value", Messages.LABEL_RESISTANCE, "1k", "Ω"),
                    ),
                    listOf(Messages.CODE_VALUE, Messages.VALUE_CODE),
                    Messages.TEXT_3_DIGITS_FIRST_2_SIGNIFICANT_DIGITS_FINAL_DIGIT,
                )
            // c4 Ohm: V=IR and P=VI; volts, amperes, ohms and watts.
            4 ->
                Calculator(
                    listOf(
                        f("v", Messages.LABEL_VOLTAGE, "12", "V"),
                        f("i", Messages.LABEL_CURRENT, "", "A"),
                        f("r", Messages.LABEL_RESISTANCE, "1k", "Ω"),
                        f("p", Messages.LABEL_POWER, "", "W"),
                    ),
                    formula = Messages.V_IR_P_VI_I_R_V_R,
                )
            // c5 Reactance: Xc=1/(2πfC), Xl=2πfL Ω; f0=1/(2π√LC) Hz; Q=√(L/C)/R.
            5 ->
                Calculator(
                    listOf(
                        f("c", Messages.LABEL_CAPACITANCE, "100n", "F"),
                        f("l", Messages.LABEL_INDUCTANCE, "10m", "H"),
                        f("freq", Messages.LABEL_FREQUENCY, "1k", "Hz"),
                        f("r", Messages.SERIES_LOSS_RESISTANCE, "10", "Ω"),
                    ),
                    formula = Messages.XC_1_2_FC_XL_2_FL_F,
                )
            // c6 Filters: fc=1/(2πRC) or R/(2πL) Hz; magnitude is unitless.
            6 ->
                Calculator(
                    listOf(
                        f("r", Messages.LABEL_RESISTANCE, "1k", "Ω"),
                        f("c", Messages.LABEL_CAPACITANCE, "100n", "F"),
                        f("l", Messages.LABEL_INDUCTANCE, "10m", "H"),
                    ),
                    listOf(
                        Messages.RC_LOW_PASS,
                        Messages.RC_HIGH_PASS,
                        Messages.RL_LOW_PASS,
                        Messages.RL_HIGH_PASS,
                    ),
                    Messages.RC_FC_1_2_RC_RL_FC_R,
                )
            // c7 Divider: Vout=Vin(R2 || load)/(R1+(R2 || load)) V; reverse solves R2 Ω.
            7 ->
                Calculator(
                    listOf(
                        f("vin", Messages.INPUT_VOLTAGE, "12", "V"),
                        f("r1", Messages.UPPER_RESISTOR_R1, "10k", "Ω"),
                        f("r2", Messages.LOWER_RESISTOR_R2, "10k", "Ω"),
                        f("target", Messages.TARGET_OUTPUT, "5", "V"),
                        f("load", Messages.LOAD_BLANK_UNLOADED, "", "Ω"),
                    ),
                    listOf(Messages.FIND_OUTPUT, Messages.FIND_R2),
                    Messages.VOUT_VIN_R2_RLOAD_R1_R2_RLOAD_REVERSE,
                )
            // c8 Ratio: minimise error of R2/R1 over preferred Ω pairs; ratio is unitless.
            8 ->
                Calculator(
                    listOf(
                        f("ratio", Messages.TARGET_R2_R1, "2.5"),
                        f("base", Messages.PREFERRED_R1, "10k", "Ω"),
                    ),
                    listOf("E12", "E24", "E96"),
                    Messages.SEARCH_STANDARD_VALUES_FROM_1_TO_10_M,
                )
            // c9 Resistors: series=ΣR, parallel=1/Σ(1/R), in Ω.
            // c10 Capacitors: parallel=ΣC, series=1/Σ(1/C), in F.
            9,
            10 ->
                Calculator(
                    listOf(
                        f(
                            "values",
                            Messages.VALUES_SEPARATED_BY_COMMA,
                            if (id == 9) "1k,2k,3k" else "100n,220n,1u",
                            if (id == 9) "Ω" else "F",
                            true,
                        )
                    ),
                    formula =
                        if (id == 9) Messages.SERIES_R_PARALLEL_1_1_R
                        else Messages.PARALLEL_C_SERIES_1_1_C,
                )
            // c11 RC: tau=RC s; Vcharge=V(1−exp(−t/tau)), Vdischarge=V exp(−t/tau).
            11 ->
                Calculator(
                    listOf(
                        f("r", Messages.LABEL_RESISTANCE, "10k", "Ω"),
                        f("c", Messages.LABEL_CAPACITANCE, "100u", "F"),
                        f("v", Messages.INITIAL_SUPPLY_VOLTAGE, "5", "V"),
                        f("t", Messages.ELAPSED_TIME, "1", "s"),
                        f("percent", Messages.TARGET_PERCENTAGE, "63.2", "%"),
                    ),
                    listOf(Messages.LABEL_CHARGE, Messages.LABEL_DISCHARGE),
                    Messages.RC_CHARGING_V_VS_1_E_T_DISCHARGE,
                )
            // c12 Ideal op amp: −Rf/Rin, 1+Rf/Rin, summing and matched differential; output V.
            12 ->
                Calculator(
                    listOf(
                        f("r1", Messages.INPUT_R1, "10k", "Ω"),
                        f("r2", Messages.FEEDBACK_R2, "100k", "Ω"),
                        f("v1", Messages.INPUT_V1, "0.1", "V"),
                        f("v2", Messages.INPUT_V2, "0.2", "V"),
                        f("r3", Messages.SECOND_SUMMING_INPUT_RESISTOR, "10k", "Ω"),
                    ),
                    listOf(
                        Messages.LABEL_INVERTING,
                        Messages.LABEL_NON_INVERTING,
                        Messages.LABEL_SUMMING,
                        Messages.LABEL_DIFFERENTIAL,
                    ),
                    Messages.IDEAL_OP_AMP_INVERTING_RF_RIN_NON_INVERTING,
                )
            // c13 LED string: R=(Vs−N Vf)/If Ω; power=If²R W per branch.
            13 ->
                Calculator(
                    listOf(
                        f("v", Messages.LABEL_SUPPLY, "12", "V"),
                        f("vf", Messages.LED_FORWARD_VOLTAGE, "2", "V"),
                        f("i", Messages.CURRENT_PER_STRING, "20m", "A"),
                        f("n", Messages.LEDS_PER_SERIES_STRING, "3"),
                        f("strings", Messages.PARALLEL_STRINGS_ONE_RESISTOR_EACH, "1"),
                    ),
                    formula = Messages.R_VS_N_VF_IF_ONE_RESISTOR_PER,
                )
            // c14 LM317: Vout=1.25(1+R2/R1)+Iadj R2 V; reverse solves R2 Ω.
            14 ->
                Calculator(
                    listOf(
                        f("r1", Messages.R1_OUTPUT_TO_ADJUST, "240", "Ω"),
                        f("r2", Messages.R2_ADJUST_TO_GROUND, "720", "Ω"),
                        f("v", Messages.TARGET_VOLTAGE, "5", "V"),
                        f("iadj", Messages.ADJUST_CURRENT, "50u", "A"),
                    ),
                    listOf(Messages.R1_R2_VOUT, Messages.TARGET_R2),
                    Messages.VOUT_1_25_1_R2_R1_IADJ_R2,
                )
            // c15 NE555 astable: tH=ln2(RA+RB)C, tL=ln2 RB C s; mono t=ln3 RC s.
            15 ->
                Calculator(
                    listOf(
                        f("r1", "RA", "1k", "Ω"),
                        f("r2", "RB", "10k", "Ω"),
                        f("c", Messages.TIMING_CAPACITOR, "10u", "F"),
                    ),
                    listOf(Messages.LABEL_ASTABLE, Messages.LABEL_MONOSTABLE),
                    Messages.ASTABLE_TH_LN_2_RA_RB_C_TL,
                )
            // c16 Dissipation: P=VI=I²R=V²/R W; Tj=Ta+P theta °C.
            16 ->
                Calculator(
                    listOf(
                        f("v", Messages.VOLTAGE_ACROSS_DEVICE, "5", "V"),
                        f("i", Messages.LABEL_CURRENT, "0.1", "A"),
                        f("r", Messages.LABEL_RESISTANCE, "50", "Ω"),
                        f("ta", Messages.AMBIENT_TEMPERATURE, "25", "°C"),
                        f(
                            "theta",
                            Messages.TOTAL_JUNCTION_TO_AMBIENT_THERMAL_RESISTANCE,
                            "20",
                            "°C/W",
                        ),
                    ),
                    listOf(Messages.V_I, Messages.I_R, Messages.V_R),
                    Messages.P_VI_I_R_OR_V_R_TJ,
                )
            // c17 Battery hours=capacity_mAh×usable_fraction/(active_mA×duty+idle_mA×(1−duty)).
            17 ->
                Calculator(
                    listOf(
                        f("capacity", Messages.LABEL_CAPACITY, "2000", "mAh"),
                        f("i", Messages.ACTIVE_LOAD, "100", "mA"),
                        f("idle", Messages.IDLE_LOAD, "1", "mA"),
                        f("duty", Messages.ACTIVE_DUTY, "50", "%"),
                        f("eff", Messages.EFFECTIVE_USABLE_CAPACITY, "85", "%"),
                    ),
                    formula = Messages.HOURS_CAPACITY_USABLE_FRACTION_IACTIVE_DUTY_IIDLE_1,
                )
            // c18 Wheeler: L=r²N²/(9r+10l) µH, r/l inches; reverse solves turns.
            18 ->
                Calculator(
                    listOf(
                        f("n", Messages.LABEL_TURNS, "20"),
                        f("d", Messages.MEAN_COIL_DIAMETER, "20", "mm"),
                        f("length", Messages.WINDING_LENGTH, "30", "mm"),
                        f("target", Messages.TARGET_INDUCTANCE, "10u", "H"),
                    ),
                    listOf(Messages.FIND_INDUCTANCE, Messages.FIND_TURNS),
                    Messages.WHEELER_SINGLE_LAYER_AIR_CORE_L_H_R,
                )
            // c19 DC drop: ΔV=2 length rho I/area V, length m, area mm².
            19 ->
                Calculator(
                    listOf(
                        f("length", Messages.ONE_WAY_LENGTH, "10", "m"),
                        f("area", Messages.CONDUCTOR_AREA, "2.5", "mm²"),
                        f("awg", Messages.AWG_3_THROUGH_40, "14"),
                        f("i", Messages.LOAD_CURRENT, "10", "A"),
                        f("v", Messages.SUPPLY_VOLTAGE, "24", "V"),
                        f("temp", Messages.CONDUCTOR_TEMPERATURE, "20", "°C"),
                    ),
                    listOf(
                        Messages.COPPER_MM,
                        Messages.ALUMINIUM_MM,
                        Messages.COPPER_AWG,
                        Messages.ALUMINIUM_AWG,
                    ),
                    Messages.TWO_WIRE_DC_V_2LI_A_20_COPPER,
                )
            // c20 IPC-2221: I=k ΔT^0.44 A^0.725; mil² cross-section converts to width mm.
            20 ->
                Calculator(
                    listOf(
                        f("i", Messages.LABEL_CURRENT, "2", "A"),
                        f("dt", Messages.TEMPERATURE_RISE, "10", "°C"),
                        f("oz", Messages.COPPER_WEIGHT, "1", "oz/ft²"),
                    ),
                    listOf(Messages.LABEL_EXTERNAL, Messages.LABEL_INTERNAL),
                    Messages.IPC_2221_EMPIRICAL_ESTIMATE_I_K_T_0,
                )
            // c21 AC: S=VI or √3 VI VA; P=S PF W; Q=√(S²−P²) var.
            21 ->
                Calculator(
                    listOf(
                        f("v", Messages.RMS_VOLTAGE_LINE_LINE_FOR_3, "230", "V"),
                        f("i", Messages.RMS_CURRENT, "10", "A"),
                        f("pf", Messages.POWER_FACTOR, "0.8"),
                        f("kw", Messages.ACTIVE_POWER, "2", "kW"),
                    ),
                    listOf(
                        Messages.SINGLE_PHASE_FROM_CURRENT,
                        Messages.THREE_PHASE_FROM_CURRENT,
                        Messages.SINGLE_PHASE_FROM_KW,
                        Messages.THREE_PHASE_FROM_KW,
                    ),
                    Messages.S_VI_SINGLE_PHASE_OR_3_VLL_IL,
                )
            // c22 Frequency: T=1/f s, omega=2πf rad/s, wavelength=c/f m.
            22 ->
                Calculator(
                    listOf(
                        f("value", Messages.INPUT_VALUE, "1k"),
                        f("speed", Messages.WAVE_PROPAGATION_SPEED, "299792458", "m/s"),
                    ),
                    listOf(
                        Messages.FREQUENCY_HZ,
                        Messages.PERIOD_S,
                        Messages.ANGULAR_FREQUENCY_RAD_S,
                        Messages.WAVELENGTH_M,
                    ),
                    Messages.T_1_F_2_F_V_F_WAVE,
                )
            // c23 ADC: LSB=Vref/2^bits V; code=floor(V/LSB), clamped at 2^bits−1.
            23 ->
                Calculator(
                    listOf(
                        f("bits", Messages.RESOLUTION_BITS, "12"),
                        f("vref", Messages.REFERENCE_VOLTAGE, "3.3", "V"),
                        f("counts", Messages.ADC_CODE, "2048"),
                        f("v", Messages.INPUT_VOLTAGE, "1.65", "V"),
                    ),
                    listOf(Messages.COUNTS_VOLTAGE, Messages.VOLTAGE_COUNTS),
                    Messages.IDEAL_UNIPOLAR_ADC_LSB_VREF_2_N_CODE,
                )
            // c24 Formula browser reuses the shipping calculator equations and assumptions.
            24 -> Calculator(emptyList(), formula = Messages.SEARCH_THE_FORMULA_REFERENCE_BELOW)
            // c25 Reverse colour code rounds Ω to two/three significant digits and a multiplier.
            25 ->
                Calculator(
                    listOf(f("v", Messages.LABEL_RESISTANCE, "1.1k", "Ω")),
                    listOf(Messages.TEXT_4_BANDS, Messages.TEXT_5_BANDS),
                    Messages.SELECT_NEAREST_REPRESENTABLE_SIGNIFICANT_DIGITS_FINAL_BAND_GOLD,
                )
            // c26 RTD: R=R0(1+At+Bt²+C(t−100)t³) Ω below zero; inverse yields °C.
            26 ->
                Calculator(
                    listOf(
                        f("r", Messages.MEASURED_RESISTANCE, "100", "Ω"),
                        f("t", Messages.LABEL_TEMPERATURE, "0", "°C"),
                    ),
                    listOf(
                        Messages.PT100_CVD_R_T,
                        Messages.PT100_CVD_T_R,
                        Messages.PT1000_CVD_R_T,
                        Messages.PT1000_CVD_T_R,
                        Messages.PT100_LINEAR_R_T,
                        Messages.PT100_LINEAR_T_R,
                        Messages.PT1000_LINEAR_R_T,
                        Messages.PT1000_LINEAR_T_R,
                    ),
                    Messages.IEC_60751_NOMINAL_PLATINUM_COEFFICIENTS_A_3_9083E,
                )
            // c27 Star/delta: delta_ab=(RaRb+RbRc+RcRa)/Rc; star_a=RabRca/Σdelta Ω.
            27 ->
                Calculator(
                    listOf(
                        f("a", Messages.STAR_A_DELTA_AB, "100", "Ω"),
                        f("b", Messages.STAR_B_DELTA_BC, "200", "Ω"),
                        f("c", Messages.STAR_C_DELTA_CA, "300", "Ω"),
                    ),
                    listOf(Messages.STAR_DELTA, Messages.DELTA_STAR),
                    Messages.DELTA_AB_RARB_RBRC_RCRA_RC_STAR_A,
                )
            // c28 Zener: Rs=(Vs−Vz)/(Iz+Iload) Ω, Pz=Vz Iz W.
            28 ->
                Calculator(
                    listOf(
                        f("vs", Messages.LABEL_SUPPLY, "12", "V"),
                        f("vz", Messages.ZENER_VOLTAGE, "5.1", "V"),
                        f("load", Messages.LOAD_CURRENT, "10m", "A"),
                        f("iz", Messages.TARGET_ZENER_CURRENT, "10m", "A"),
                    ),
                    formula = Messages.RS_VS_VZ_IL_IZ_PZ_VZIZ_PR,
                )
            // c29 Decibels: voltage ratio=10^(dB/20), power ratio=10^(dB/10); stated references.
            29 ->
                Calculator(
                    listOf(
                        f("value", Messages.INPUT_VALUE, "6"),
                        f("z", Messages.REFERENCE_LOAD_IMPEDANCE, "600", "Ω"),
                    ),
                    listOf(
                        Messages.DB_VOLTAGE_RATIO,
                        Messages.DB_POWER_RATIO,
                        Messages.VOLTAGE_RATIO_DB,
                        Messages.POWER_RATIO_DB,
                        Messages.DBM_WATTS,
                        Messages.WATTS_DBM,
                        Messages.DBV_VRMS,
                        Messages.VRMS_DBV,
                        Messages.DBU_VRMS,
                        Messages.VRMS_DBU,
                    ),
                    Messages.TEXT_20LOG10_VOLTAGE_RATIO_EQUAL_IMPEDANCE_10LOG10_POWER_RATIO,
                )
            // c30 Range: y=y0+(x−x0)(y1−y0)/(x1−x0); chosen engineering units.
            30 ->
                Calculator(
                    listOf(
                        f("x", Messages.INPUT_OUTPUT_VALUE, "12"),
                        f("a", Messages.INPUT_MINIMUM, "4"),
                        f("b", Messages.INPUT_MAXIMUM, "20"),
                        f("c", Messages.OUTPUT_MINIMUM, "0"),
                        f("d", Messages.OUTPUT_MAXIMUM, "100"),
                    ),
                    listOf(Messages.LABEL_FORWARD, Messages.LABEL_REVERSE),
                    Messages.Y_YMIN_X_XMIN_YMAX_YMIN_XMAX_XMIN,
                )
            // c31 RMS: sine Vp/√2, square Vp, triangle Vp/√3; symmetric waves in volts.
            31 ->
                Calculator(
                    listOf(f("v", Messages.AMPLITUDE_INPUT, "1", "V")),
                    listOf(
                        Messages.SINE_PEAK,
                        Messages.SINE_PEAK_TO_PEAK,
                        Messages.SINE_RMS,
                        Messages.SQUARE_PEAK,
                        Messages.SQUARE_PEAK_TO_PEAK,
                        Messages.SQUARE_RMS,
                        Messages.TRIANGLE_PEAK,
                        Messages.TRIANGLE_PEAK_TO_PEAK,
                        Messages.TRIANGLE_RMS,
                    ),
                    Messages.SYMMETRIC_ZERO_CENTRED_WAVEFORMS_VRMS_VP_2_SINE,
                )
            else -> throw IllegalArgumentException(Messages.UNKNOWN_CALCULATOR)
        }

    /**
     * Turns invalid input and unsupported numerical ranges into typed errors while preserving
     * cancellation.
     *
     * @param id Stable calculator or registry identifier; independent of the visible title.
     * @param values Caller-owned field/component snapshot; not mutated by this operation.
     * @param requestedMode Persisted mode index, clamped to supported modes before evaluation.
     * @return Calculation.Value or actionable Calculation.Error; invalid input does not escape as
     *   an ordinary exception. Ordinary invalid-input failures become Calculation.Error;
     *   cancellation is rethrown.
     */
    fun calculate(id: Int, values: Map<String, String>, requestedMode: Int = 0): Calculation =
        try {
            val calculator = spec(id)
            val mode = requestedMode.coerceIn(calculator.modes.indices)
            /**
             * Names a required input when scalar parsing fails so the user can correct the right
             * field.
             *
             * @param k Stable field key used to read or update the saved calculator snapshot.
             * @return Required finite scalar. Invalid physical constraints throw
             *   IllegalArgumentException for the calculator boundary to report; no persistence
             *   occurs here.
             */
            fun n(k: String): Double =
                Engineering.parse(values[k].orEmpty())
                    ?: throw IllegalArgumentException(
                        Messages.invalidField(
                            spec(id).fields.firstOrNull { it.key == k }?.label ?: k
                        )
                    )
            /**
             * Enforces positive physical inputs before arithmetic that would otherwise divide by
             * zero.
             *
             * @param k Stable field key used to read or update the saved calculator snapshot.
             * @return Required positive finite scalar. Invalid physical constraints throw
             *   IllegalArgumentException for the calculator boundary to report; no persistence
             *   occurs here.
             */
            fun pos(k: String): Double =
                n(k).also {
                    require(it > 0) {
                        Messages.positiveField(calculator.fields.first { f -> f.key == k }.label)
                    }
                }
            /**
             * Allows inactive or optional fields to remain empty without rejecting the whole
             * calculation.
             *
             * @param k Stable field key used to read or update the saved calculator snapshot.
             * @return Null for an empty optional field; otherwise its finite scalar. Invalid
             *   physical constraints throw IllegalArgumentException for the calculator boundary to
             *   report; no persistence occurs here.
             */
            fun optional(k: String) = if (values[k].isNullOrBlank()) null else n(k)
            /**
             * Checks finite outputs and applies the format appropriate to each declared unit.
             *
             * @param pairs Supplied Pair<String, Double> value consumed by the operation described
             *   above.
             * @return A successful Calculation containing finite, formatted quantities. Invalid
             *   physical constraints throw IllegalArgumentException for the calculator boundary to
             *   report; no persistence occurs here.
             */
            fun out(vararg pairs: Pair<String, Double>): Calculation {
                require(pairs.all { it.second.isFinite() }) {
                    Messages.INPUTS_EXCEED_THE_NUMERICAL_CALCULATION_RANGE
                }
                return Calculation.Value(
                    pairs.joinToString("\n") { (label, value) ->
                        val unit = Regex(" \\(([^()]*)\\)$").find(label)?.groupValues?.get(1)
                        val title = if (unit != null) label.substringBeforeLast(" (") else label
                        val rendered =
                            if (
                                label in
                                    listOf(
                                        Messages.LABEL_CODE,
                                        Messages.MAXIMUM_CODE,
                                        Messages.TURNS_ROUND_UP,
                                    )
                            )
                                java.math.BigDecimal.valueOf(value)
                                    .stripTrailingZeros()
                                    .toPlainString()
                            else Engineering.format(value, unit.orEmpty())
                        "$title: $rendered"
                    }
                )
            }
            /**
             * Routes named circuit results through the same finite-value and display rules as other
             * calculator cases.
             *
             * @param m Supplied Map<String, Double> value consumed by the operation described
             *   above.
             * @return A successful Calculation containing finite, formatted quantities. Invalid
             *   physical constraints throw IllegalArgumentException for the calculator boundary to
             *   report; no persistence occurs here.
             */
            fun lines(m: Map<String, Double>) = out(*m.toList().toTypedArray())
            /**
             * Validates discrete bounds before converting to Int, avoiding truncation and overflow
             * acceptance.
             *
             * @param k Stable field key used to read or update the saved calculator snapshot.
             * @param range Inclusive supported whole-number interval.
             * @return Validated whole-number input within the inclusive bounds. Invalid physical
             *   constraints throw IllegalArgumentException for the calculator boundary to report;
             *   no persistence occurs here.
             */
            fun integer(k: String, range: IntRange): Int {
                val x = n(k)
                require(x == floor(x) && x >= range.first && x <= range.last) {
                    Messages.wholeField(calculator.fields.first { it.key == k }.label, range)
                }
                return x.toInt()
            }
            when (id) {
                // c2 SMD: significant digits × 10^last digit Ω, or EIA-96 lookup.
                2 ->
                    if (mode == 0)
                        out(Messages.RESISTANCE to Resistors.smd(values["code"].orEmpty()))
                    else Calculation.Value(Resistors.smdCodes(pos("value")))
                // c4 Ohm: V=IR and P=VI; volts, amperes, ohms and watts.
                4 -> lines(Circuits.ohm(optional("v"), optional("i"), optional("r"), optional("p")))
                // c5 Reactance: Xc=1/(2πfC), Xl=2πfL Ω; f0=1/(2π√LC) Hz; Q=√(L/C)/R.
                5 -> {
                    val c = pos("c")
                    val l = pos("l")
                    val f = pos("freq")
                    val resistance = optional("r")
                    require(resistance == null || resistance >= 0) { "Resistance cannot be negative." }
                    val results = mutableListOf(
                        Messages.XC to 1 / (2 * PI * f * c),
                        Messages.XL to 2 * PI * f * l,
                        Messages.RESONANT_FREQUENCY_HZ to 1 / (2 * PI * sqrt(l * c)),
                    )
                    if (resistance != null && resistance > 0) results.add(Messages.SERIES_Q to sqrt(l / c) / resistance)
                    out(*results.toTypedArray())
                }
                // c6 Filters: fc=1/(2πRC) or R/(2πL) Hz; magnitude is unitless.
                6 -> {
                    val r = pos("r")
                    val fc = if (mode < 2) Circuits.rc(r, pos("c")) else r / (2 * PI * pos("l"))
                    Calculation.Value(
                        Messages.filterResult(Engineering.format(fc, "Hz")),
                        (0..100).map {
                            val x = 10.0.pow(-2 + it / 25.0)
                            if (mode % 2 == 0) 1 / sqrt(1 + x * x) else x / sqrt(1 + x * x)
                        },
                    )
                }
                // c7 Divider: Vout=Vin(R2 || load)/(R1+(R2 || load)) V; reverse solves R2 Ω.
                7 -> {
                    val v = pos("vin")
                    val r1 = pos("r1")
                    val load = optional("load")
                    if (mode == 0) out(Messages.VOUT_V to Circuits.divider(v, r1, pos("r2"), load))
                    else {
                        val t = pos("target")
                        require(t < v) { Messages.TARGET_MUST_BE_BELOW_VIN }
                        val eq = t * r1 / (v - t)
                        val r2 =
                            if (load != null) {
                                Circuits.positive(load)
                                require(load > eq) { Messages.LOAD_TOO_LOW_FOR_TARGET_OUTPUT }
                                eq * load / (load - eq)
                            } else eq
                        out(Messages.R2 to r2)
                    }
                }
                // c8 Ratio: minimise error of R2/R1 over preferred Ω pairs; ratio is unitless.
                8 -> {
                    val target = pos("ratio")
                    val base = pos("base")
                    require(target in 1e-7..1e7) {
                        Messages.TARGET_RATIO_MUST_BE_BETWEEN_0_0000001_AND
                    }
                    val ns = listOf(12, 24, 96)[mode]
                    val all =
                        (0..7)
                            .flatMap { e -> Resistors.standard(ns).map { it * 10.0.pow(e) } }
                            .filter { it <= 1e7 }
                    val best =
                        all.flatMap { a ->
                                listOf(Resistors.nearest(a * target, ns))
                                    .filter { it in 1.0..1e7 }
                                    .map { b -> Triple(a, b, abs(b / a / target - 1)) }
                            }
                            .minWithOrNull(
                                compareBy<Triple<Double, Double, Double>> { it.third }
                                    .thenBy { abs(log10(it.first / base)) }
                            ) ?: throw IllegalArgumentException(Messages.NO_STANDARD_VALUE_PAIR_REACHES_THAT_RATIO_BETWEEN)
                    out(
                        Messages.R1 to best.first,
                        Messages.R2 to best.second,
                        Messages.LABEL_RATIO to best.second / best.first,
                        Messages.ERROR to best.third * 100,
                    )
                }
                // c9 Resistors: series=ΣR, parallel=1/Σ(1/R), in Ω.
                // c10 Capacitors: parallel=ΣC, series=1/Σ(1/C), in F.
                9,
                10 -> {
                    val list =
                        values["values"].orEmpty().split(',', ';', '\n').map(String::trim).filter(String::isNotEmpty).map {
                            Engineering.parse(it)
                                ?: throw IllegalArgumentException(Messages.EVERY_LIST_ENTRY_MUST_BE_A_VALID_POSITIVE)
                        }
                    val sum = Circuits.series(list)
                    val reciprocal = Circuits.parallel(list)
                    out(
                        Messages.componentLabel(true, if (id == 9) "Ω" else "F") to
                            if (id == 9) sum else reciprocal,
                        Messages.componentLabel(false, if (id == 9) "Ω" else "F") to
                            if (id == 9) reciprocal else sum,
                    )
                }
                // c11 RC: tau=RC s; Vcharge=V(1−exp(−t/tau)), Vdischarge=V exp(−t/tau).
                11 -> {
                    val tau = pos("r") * pos("c")
                    val v = pos("v")
                    val t = n("t")
                    require(t >= 0) { Messages.ELAPSED_TIME_MUST_BE_ZERO_OR_POSITIVE }
                    val pc = n("percent") / 100
                    require(pc > 0 && pc < 1) {
                        Messages.TARGET_PERCENTAGE_MUST_BE_BETWEEN_0_AND_100
                    }
                    val charge = mode == 0
                    Calculation.Value(
                        out(
                                Messages.TIME_CONSTANT_S to tau,
                                Messages.VOLTAGE_V to
                                    v * (if (charge) 1 - exp(-t / tau) else exp(-t / tau)),
                                Messages.TIME_TO_TARGET_S to -tau * ln(if (charge) 1 - pc else pc),
                            )
                            .text + "\nPlot: V/Vs against 0…5τ",
                        (0..100).map { if (charge) 1 - exp(-it / 20.0) else exp(-it / 20.0) },
                    )
                }
                // c12 Ideal op amp: −Rf/Rin, 1+Rf/Rin, summing and matched differential; output V.
                12 -> {
                    val g = pos("r2") / pos("r1")
                    val a = n("v1")
                    val b = if (mode >= 2) n("v2") else 0.0
                    val gain =
                        when (mode) {
                            0,
                            2 -> -g
                            1 -> 1 + g
                            else -> g
                        }
                    out(
                        Messages.GAIN_V_V to gain,
                        Messages.IDEAL_OUTPUT_V to
                            when (mode) {
                                0,
                                1 -> gain * a
                                2 -> -pos("r2") * (a / pos("r1") + b / pos("r3"))
                                else -> g * (b - a)
                            },
                    )
                }
                // c13 LED string: R=(Vs−N Vf)/If Ω; power=If²R W per branch.
                13 -> {
                    val v = pos("v")
                    val vf = pos("vf")
                    val i = pos("i")
                    val count = integer("n", 1..1000)
                    val strings = integer("strings", 1..1000)
                    val r = Circuits.led(v, vf, i, count.toDouble())
                    val std = Resistors.nearest(r, 24, true)
                    val actual = (v - vf * count) / std
                    val p = actual * actual * std
                    out(
                        Messages.IDEAL_R_PER_STRING to r,
                        Messages.E24_R_PER_STRING to std,
                        Messages.ACTUAL_STRING_CURRENT_A to actual,
                        Messages.TOTAL_CURRENT_A to actual * strings,
                        Messages.RESISTOR_DISSIPATION_PER_STRING_W to p,
                        Messages.SUGGESTED_RATING_W to 2 * p,
                    )
                }
                // c14 LM317: Vout=1.25(1+R2/R1)+Iadj R2 V; reverse solves R2 Ω.
                14 -> {
                    val r1 = pos("r1")
                    val adj = n("iadj")
                    require(adj >= 0) { Messages.ADJUST_CURRENT_MUST_BE_ZERO_OR_POSITIVE }
                    if (mode == 0) out(Messages.VOUT_V to Circuits.lm317(r1, pos("r2"), adj))
                    else {
                        val v = pos("v")
                        require(v > 1.25) { Messages.TARGET_VOLTAGE_MUST_EXCEED_THE_1_25_V }
                        out(Messages.R2 to (v - 1.25) / (1.25 / r1 + adj))
                    }
                }
                // c15 NE555 astable: tH=ln2(RA+RB)C, tL=ln2 RB C s; mono t=ln3 RC s.
                15 ->
                    if (mode == 0) lines(Circuits.astable(pos("r1"), pos("r2"), pos("c")))
                    else out(Messages.PULSE_WIDTH_S to Circuits.mono(pos("r1"), pos("c")))
                // c16 Dissipation: P=VI=I²R=V²/R W; Tj=Ta+P theta °C.
                16 -> {
                    val p =
                        when (mode) {
                            0 -> pos("v") * pos("i")
                            1 -> pos("i").pow(2) * pos("r")
                            else -> pos("v").pow(2) / pos("r")
                        }
                    out(
                        Messages.DISSIPATION_W to p,
                        Messages.ESTIMATED_JUNCTION_C to n("ta") + p * pos("theta"),
                    )
                }
                // c17 Battery hours=capacity_mAh×usable_fraction/(active_mA×duty+idle_mA×(1−duty)).
                17 -> {
                    val d = n("duty") / 100
                    val e = n("eff") / 100
                    require(d in 0.0..1.0) { Messages.ACTIVE_DUTY_MUST_BE_BETWEEN_0_AND_100 }
                    require(e > 0 && e <= 1) {
                        Messages.EFFECTIVE_USABLE_CAPACITY_MUST_BE_GREATER_THAN_0
                    }
                    val idle = n("idle")
                    require(idle >= 0) { Messages.IDLE_LOAD_MUST_BE_ZERO_OR_POSITIVE }
                    val avg = pos("i") * d + idle * (1 - d)
                    require(avg > 0) { Messages.AVERAGE_LOAD_CURRENT_MUST_EXCEED_ZERO }
                    val h = pos("capacity") * e / avg
                    out(
                        Messages.AVERAGE_CURRENT_MA to avg,
                        Messages.RUNTIME_HOURS to h,
                        Messages.RUNTIME_DAYS to h / 24,
                    )
                }
                // c18 Wheeler: L=r²N²/(9r+10l) µH, r/l inches; reverse solves turns.
                18 -> {
                    val r = pos("d") / 50.8
                    val l = pos("length") / 25.4
                    val factor = r * r / (9 * r + 10 * l) * 1e-6
                    if (mode == 0) out(Messages.INDUCTANCE_H to factor * pos("n").pow(2))
                    else out(Messages.TURNS_ROUND_UP to ceil(sqrt(pos("target") / factor)))
                }
                // c19 DC drop: ΔV=2 length rho I/area V, length m, area mm².
                19 -> {
                    val area =
                        if (mode >= 2)
                            PI / 4 * (0.127 * 92.0.pow((36 - integer("awg", -3..40)) / 39.0)).pow(2)
                        else pos("area")
                    require(n("temp") >= -273.15) { "Temperature is below absolute zero." }
                    val rho =
                        (if (mode % 2 == 0) 0.017241 else 0.028264) *
                            (1 + (if (mode % 2 == 0) 0.00393 else 0.00403) * (n("temp") - 20))
                    require(rho > 0) { "Temperature is outside the positive-resistivity range of this linear model." }
                    val drop = Circuits.voltageDrop(pos("length"), area, pos("i"), rho)
                    val v = pos("v")
                    require(drop < v) { Messages.VOLTAGE_DROP_EXCEEDS_SUPPLY }
                    out(
                        Messages.AREA_MM to area,
                        Messages.VOLTAGE_DROP_V to drop,
                        Messages.DROP to 100 * drop / v,
                        Messages.LOAD_VOLTAGE_V to v - drop,
                    )
                }
                // c20 IPC-2221: I=k ΔT^0.44 A^0.725; mil² cross-section converts to width mm.
                20 ->
                    out(
                        Messages.REQUIRED_TRACE_WIDTH_MM to
                            Circuits.traceWidth(pos("i"), pos("dt"), pos("oz"), mode == 1)
                    )
                // c21 AC: S=VI or √3 VI VA; P=S PF W; Q=√(S²−P²) var.
                21 -> {
                    val pf = n("pf")
                    require(pf in 0.0..1.0) { Messages.POWER_FACTOR_MUST_BE_BETWEEN_0_AND_1 }
                    val factor = if (mode % 2 == 1) sqrt(3.0) else 1.0
                    val current =
                        if (mode < 2) pos("i")
                        else {
                            require(pf > 0) {
                                Messages.POWER_FACTOR_MUST_EXCEED_ZERO_TO_SOLVE_CURRENT
                            }
                            pos("kw") * 1000 / (pos("v") * factor * pf)
                        }
                    val s = pos("v") * current * factor
                    out(
                        Messages.APPARENT_POWER_VA to s,
                        Messages.ACTIVE_POWER_W to s * pf,
                        Messages.REACTIVE_POWER_VAR to s * sqrt(1 - pf * pf),
                        Messages.CURRENT_A to current,
                    )
                }
                // c22 Frequency: T=1/f s, omega=2πf rad/s, wavelength=c/f m.
                22 -> {
                    val x = pos("value")
                    val speed = pos("speed")
                    val f =
                        when (mode) {
                            0 -> x
                            1 -> 1 / x
                            2 -> x / (2 * PI)
                            else -> speed / x
                        }
                    out(
                        Messages.FREQUENCY_HZ to f,
                        Messages.PERIOD_S to 1 / f,
                        Messages.ANGULAR_FREQUENCY_RAD_S to 2 * PI * f,
                        Messages.WAVELENGTH_M to speed / f,
                    )
                }
                // c23 ADC: LSB=Vref/2^bits V; code=floor(V/LSB), clamped at 2^bits−1.
                23 -> {
                    val bits = integer("bits", 1..24)
                    val levels = 2.0.pow(bits)
                    val ref = pos("vref")
                    val lsb = ref / levels
                    val c =
                        if (mode == 0) integer("counts", 0..(levels.toInt() - 1))
                        else {
                            val v = n("v")
                            require(v in 0.0..ref) {
                                Messages.INPUT_VOLTAGE_MUST_BE_BETWEEN_ZERO_AND_THE
                            }
                            floor(v / lsb).toInt().coerceIn(0, levels.toInt() - 1)
                        }
                    out(
                        Messages.LABEL_CODE to c.toDouble(),
                        Messages.CODE_BIN_LOWER_VOLTAGE_V to c * lsb,
                        Messages.LSB_V to lsb,
                        Messages.MAXIMUM_CODE to levels - 1,
                    )
                }
                // c25 Reverse colour code rounds Ω to two/three significant digits and a
                // multiplier.
                25 -> {
                    val b = Resistors.encode(pos("v"), mode == 1)
                    Calculation.Value(
                        (b.dropLast(1).map { Resistors.names[it] } +
                                Resistors.names[
                                        if (b.last() == -1) 10
                                        else if (b.last() == -2) 11 else b.last()] +
                                if (mode == 1) Messages.BROWN_1 else Messages.GOLD_5)
                            .joinToString(" • ") +
                            "\nRepresented: " +
                            Engineering.format(Resistors.decode(b.dropLast(1), b.last()), "Ω")
                    )
                }
                // c26 RTD: R=R0(1+At+Bt²+C(t−100)t³) Ω below zero; inverse yields °C.
                26 -> {
                    val r0 = if (mode % 4 >= 2) 1000.0 else 100.0
                    val linear = mode >= 4
                    if (mode % 2 == 0)
                        out(Messages.TEMPERATURE_C to Circuits.rtdTemperature(pos("r"), r0, linear))
                    else out(Messages.RESISTANCE to Circuits.rtd(n("t"), r0, linear))
                }
                // c27 Star/delta: delta_ab=(RaRb+RbRc+RcRa)/Rc; star_a=RabRca/Σdelta Ω.
                27 -> {
                    val a = pos("a")
                    val b = pos("b")
                    val c = pos("c")
                    if (mode == 0) {
                        val s = a * b + b * c + c * a
                        out(
                            Messages.DELTA_AB to s / c,
                            Messages.DELTA_BC to s / a,
                            Messages.DELTA_CA to s / b,
                        )
                    } else {
                        val s = a + b + c
                        out(
                            Messages.STAR_A to a * c / s,
                            Messages.STAR_B to a * b / s,
                            Messages.STAR_C to b * c / s,
                        )
                    }
                }
                // c28 Zener: Rs=(Vs−Vz)/(Iz+Iload) Ω, Pz=Vz Iz W.
                28 -> {
                    val vs = pos("vs")
                    val vz = pos("vz")
                    require(vs > vz) { Messages.SUPPLY_MUST_EXCEED_ZENER_VOLTAGE }
                    val il = n("load")
                    require(il >= 0) { Messages.LOAD_CURRENT_MUST_BE_ZERO_OR_POSITIVE }
                    val iz = pos("iz")
                    val r = (vs - vz) / (il + iz)
                    out(
                        Messages.SERIES_RESISTOR to r,
                        Messages.ZENER_CURRENT_A to iz,
                        Messages.ZENER_POWER_W to vz * iz,
                        Messages.RESISTOR_POWER_W to (vs - vz) * (il + iz),
                        Messages.NO_LOAD_ZENER_POWER_W to vz * (il + iz),
                    )
                }
                // c29 Decibels: voltage ratio=10^(dB/20), power ratio=10^(dB/10); stated
                // references.
                29 -> {
                    val x = n("value")
                    val v =
                        when (mode) {
                            0 -> 10.0.pow(x / 20)
                            1 -> 10.0.pow(x / 10)
                            2 -> {
                                require(x > 0) {
                                    Messages.INPUT_RATIO_OR_POWER_MUST_EXCEED_ZERO_FOR
                                }
                                20 * log10(x)
                            }
                            3 -> {
                                require(x > 0) {
                                    Messages.INPUT_RATIO_OR_POWER_MUST_EXCEED_ZERO_FOR
                                }
                                10 * log10(x)
                            }
                            4 -> 1e-3 * 10.0.pow(x / 10)
                            5 -> {
                                require(x > 0) {
                                    Messages.INPUT_RATIO_OR_POWER_MUST_EXCEED_ZERO_FOR
                                }
                                10 * log10(x / 1e-3)
                            }
                            6 -> 10.0.pow(x / 20)
                            7 -> {
                                require(x > 0) {
                                    Messages.INPUT_RATIO_OR_POWER_MUST_EXCEED_ZERO_FOR
                                }
                                20 * log10(x)
                            }
                            8 -> sqrt(0.6) * 10.0.pow(x / 20)
                            else -> {
                                require(x > 0) {
                                    Messages.INPUT_RATIO_OR_POWER_MUST_EXCEED_ZERO_FOR
                                }
                                20 * log10(x / sqrt(0.6))
                            }
                        }
                    if (mode == 6 || mode == 8)
                        out(Messages.VOLTAGE_VRMS to v, Messages.LOAD_POWER_W to v * v / pos("z"))
                    else out(Messages.CONVERTED_VALUE to v)
                }
                // c30 Range: y=y0+(x−x0)(y1−y0)/(x1−x0); chosen engineering units.
                30 -> {
                    val a = n("a")
                    val b = n("b")
                    val c = n("c")
                    val d = n("d")
                    require(a != b && c != d) {
                        Messages.INPUT_AND_OUTPUT_RANGES_MUST_HAVE_NON_ZERO
                    }
                    val x = n("x")
                    out(
                        Messages.MAPPED_VALUE to
                            if (mode == 0) c + (x - a) * (d - c) / (b - a)
                            else a + (x - c) * (b - a) / (d - c)
                    )
                }
                // c31 RMS: sine Vp/√2, square Vp, triangle Vp/√3; symmetric waves in volts.
                31 -> {
                    val x = pos("v")
                    val type = mode / 3
                    val rmsFactor =
                        when (type) {
                            0 -> sqrt(2.0)
                            1 -> 1.0
                            else -> sqrt(3.0)
                        }
                    val peak =
                        when (mode % 3) {
                            0 -> x
                            1 -> x / 2
                            else -> x * rmsFactor
                        }
                    out(
                        Messages.PEAK_V to peak,
                        Messages.PEAK_TO_PEAK_V to 2 * peak,
                        Messages.RMS_V to peak / rmsFactor,
                        Messages.RECTIFIED_AVERAGE_V to
                            peak *
                                when (type) {
                                    0 -> 2 / PI
                                    1 -> 1.0
                                    else -> 0.5
                                },
                        Messages.SIGNED_AVERAGE_V to 0.0,
                    )
                }
                else -> Calculation.Value(spec(id).formula)
            }
        } catch (e: java.util.concurrent.CancellationException) {
            throw e
        } catch (e: Exception) {
            Calculation.Error(
                e.takeIf { it is IllegalArgumentException || it is ArithmeticException }
                    ?.message?.takeUnless { it == Messages.FAILED_REQUIREMENT || it.isBlank() }
                    ?: Messages.CHECK_THE_INPUT_VALUES_AND_SELECTED_MODE
            )
        }
}
