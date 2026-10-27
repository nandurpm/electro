package com.electrokit.app.domain

/**
 * Centralises English domain labels, formulas and validation prose while keeping the engine
 * Android-independent. Construction holds metadata/helpers; physical validation is performed by the
 * named operations, not by field assignment.
 */
object Messages {
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val INPUTS_EXCEED_THE_NUMERICAL_CALCULATION_RANGE =
        "Inputs exceed the numerical calculation range."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val VALUES_MUST_BE_POSITIVE_AND_FINITE = "Values must be positive and finite."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val ENTER_EXACTLY_TWO_OF_V_I_R_AND = "Enter exactly two of V, I, R and P."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val VOLTAGE_V = "Voltage (V)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val CURRENT_A = "Current (A)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val RESISTANCE = "Resistance (Ω)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val POWER_W = "Power (W)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val ENTER_AT_LEAST_ONE_COMPONENT_VALUE = "Enter at least one component value."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val FREQUENCY_HZ = "Frequency (Hz)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val DUTY = "Duty (%)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val HIGH_S = "High (s)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val LOW_S = "Low (s)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val VOLTAGE_DROP_EXCEEDS_SUPPLY =
        "Calculated drop exceeds the supply voltage; use a larger cross-section or a shorter run."
    /** Rejects an LED string whose forward voltage exhausts the supply. */
    const val SUPPLY_MUST_EXCEED_THE_LED_STRING_VOLTAGE =
        "Supply must exceed the LED string voltage."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val ADJUST_CURRENT_MUST_BE_ZERO_OR_POSITIVE = "Adjust current must be zero or positive."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val TEMPERATURE_MUST_BE_BETWEEN_200_AND_850_C =
        "Temperature must be between −200 and 850 °C."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val RESISTANCE_CORRESPONDS_TO_A_TEMPERATURE_OUTSIDE_200_850 =
        "Resistance corresponds to a temperature outside −200…850 °C."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val RESISTANCE_OUTSIDE_200_850_C = "Resistance outside −200…850 °C."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val BLACK = "Black"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val BROWN = "Brown"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val RED = "Red"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val ORANGE = "Orange"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val YELLOW = "Yellow"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val GREEN = "Green"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val BLUE = "Blue"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val VIOLET = "Violet"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val GREY = "Grey"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val WHITE = "White"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val GOLD = "Gold"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val SILVER = "Silver"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val SELECT_TWO_OR_THREE_VALID_DIGITS_WITH_A =
        "Select two or three valid digits with a nonzero first digit."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val MULTIPLIER_MUST_BE_BETWEEN_0_01_AND_ONE =
        "Multiplier must be between 0.01 and one billion."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val VALUE_OUTSIDE_AVAILABLE_BAND_RANGE = "Value outside available band range."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val INVALID_R_NOTATION = "Invalid R notation"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val EIA_96_CODE_MUST_START_WITH_A_NUMBER =
        "EIA-96 code must start with a number from 01 to 96."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val INVALID_EIA_96_MULTIPLIER = "Invalid EIA-96 multiplier"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val USE_3_4_DIGITS_R_NOTATION_OR_EIA = "Use 3/4 digits, R notation or EIA-96 (01A)."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val NO_PRACTICAL_PRINTED_R_NOTATION_EXISTS_FOR_THIS =
        "No practical printed R notation exists for this value."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val CALCULATE = "Calculate"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val R_SIGNIFICANT_DIGITS_10_MULTIPLIER_INDUCTOR_BANDS_ENCODE =
        "R = significant digits × 10^multiplier; inductor bands encode µH."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val SMD_MARKING = "SMD marking"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val CODE_VALUE = "Code → value"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val VALUE_CODE = "Value → code"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val TEXT_3_DIGITS_FIRST_2_SIGNIFICANT_DIGITS_FINAL_DIGIT =
        "3 digits: first 2 significant digits; final digit is the power of ten. EIA-96 uses the 96-value lookup."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val V_IR_P_VI_I_R_V_R = "V = IR; P = VI = I²R = V²/R. Enter exactly two quantities."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val SERIES_LOSS_RESISTANCE = "Series loss resistance"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val XC_1_2_FC_XL_2_FL_F = "Xc = 1/(2πfC); Xl = 2πfL; f₀ = 1/(2π√LC); series Q = √(L/C)/R."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val RC_LOW_PASS = "RC low-pass"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val RC_HIGH_PASS = "RC high-pass"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val RL_LOW_PASS = "RL low-pass"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val RL_HIGH_PASS = "RL high-pass"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val RC_FC_1_2_RC_RL_FC_R =
        "RC: fc = 1/(2πRC). RL: fc = R/(2πL). Ideal first-order −3 dB cutoff."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val INPUT_VOLTAGE = "Input voltage"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val UPPER_RESISTOR_R1 = "Upper resistor R1"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val LOWER_RESISTOR_R2 = "Lower resistor R2"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val TARGET_OUTPUT = "Target output"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val LOAD_BLANK_UNLOADED = "Load (blank = unloaded)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val FIND_OUTPUT = "Find output"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val FIND_R2 = "Find R2"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val VOUT_VIN_R2_RLOAD_R1_R2_RLOAD_REVERSE =
        "Vout = Vin × (R2 ∥ Rload)/(R1 + R2 ∥ Rload). Reverse solution includes the load."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val TARGET_R2_R1 = "Target R2/R1"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val PREFERRED_R1 = "Preferred R1"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val SEARCH_STANDARD_VALUES_FROM_1_TO_10_M =
        "Search standard values from 1 Ω to 10 MΩ. Minimise ratio error; preferred R1 breaks ties."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val VALUES_SEPARATED_BY_COMMA = "Values separated by comma"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val SERIES_R_PARALLEL_1_1_R = "Series = ΣR; parallel = 1/Σ(1/R)."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val PARALLEL_C_SERIES_1_1_C = "Parallel = ΣC; series = 1/Σ(1/C)."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val INITIAL_SUPPLY_VOLTAGE = "Initial / supply voltage"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val ELAPSED_TIME = "Elapsed time"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val TARGET_PERCENTAGE = "Target percentage"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val RC_CHARGING_V_VS_1_E_T_DISCHARGE =
        "τ = RC; charging V = Vs(1−e^(−t/τ)); discharge V = V₀e^(−t/τ)."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val INPUT_R1 = "Input R1"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val FEEDBACK_R2 = "Feedback R2"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val INPUT_V1 = "Input V1"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val INPUT_V2 = "Input V2"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val SECOND_SUMMING_INPUT_RESISTOR = "Second summing input resistor"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val IDEAL_OP_AMP_INVERTING_RF_RIN_NON_INVERTING =
        "Ideal op amp: inverting −Rf/Rin; non-inverting 1+Rf/Rin; summing −RfΣ(Vi/Ri); differential (R2/R1)(V2−V1), with matched resistor ratios. Output limited by actual supply rails."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val LED_FORWARD_VOLTAGE = "LED forward voltage"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val CURRENT_PER_STRING = "Current per string"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val LEDS_PER_SERIES_STRING = "LEDs per series string"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val PARALLEL_STRINGS_ONE_RESISTOR_EACH = "Parallel strings (one resistor each)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val R_VS_N_VF_IF_ONE_RESISTOR_PER =
        "R = (Vs−N×Vf)/If. One resistor per parallel string; choose the next higher E24 value. Verify LED forward-voltage extremes."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val R1_OUTPUT_TO_ADJUST = "R1 (output to adjust)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val R2_ADJUST_TO_GROUND = "R2 (adjust to ground)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val TARGET_VOLTAGE = "Target voltage"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val ADJUST_CURRENT = "Adjust current"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val R1_R2_VOUT = "R1/R2 → Vout"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val TARGET_R2 = "Target → R2"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val VOUT_1_25_1_R2_R1_IADJ_R2 =
        "Vout = 1.25(1+R2/R1)+Iadj×R2. Nominal reference and Iadj; verify minimum load, dropout and thermal limits in the device datasheet."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val TIMING_CAPACITOR = "Timing capacitor"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val ASTABLE_TH_LN_2_RA_RB_C_TL =
        "Astable tH = ln(2)(RA+RB)C; tL = ln(2)RB C. Monostable t = ln(3)RA C. Ideal thresholds; component leakage affects long periods."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val VOLTAGE_ACROSS_DEVICE = "Voltage across device"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val AMBIENT_TEMPERATURE = "Ambient temperature"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val TOTAL_JUNCTION_TO_AMBIENT_THERMAL_RESISTANCE =
        "Total junction-to-ambient thermal resistance"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val V_I = "V × I"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val I_R = "I² × R"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val V_R = "V² / R"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val P_VI_I_R_OR_V_R_TJ =
        "P = VI, I²R or V²/R. Tj ≈ Ta + Pθtotal. θtotal must include junction-case, interface and heatsink-air for heatsink estimates."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val ACTIVE_LOAD = "Active load"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val IDLE_LOAD = "Idle load"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val ACTIVE_DUTY = "Active duty"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val EFFECTIVE_USABLE_CAPACITY = "Effective usable capacity"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val HOURS_CAPACITY_USABLE_FRACTION_IACTIVE_DUTY_IIDLE_1 =
        "Hours = capacity×usable fraction / (Iactive×duty+Iidle×(1−duty)). Estimate at the stated battery voltage; not a converter energy model."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val MEAN_COIL_DIAMETER = "Mean coil diameter"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val WINDING_LENGTH = "Winding length"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val TARGET_INDUCTANCE = "Target inductance"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val FIND_INDUCTANCE = "Find inductance"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val FIND_TURNS = "Find turns"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val WHEELER_SINGLE_LAYER_AIR_CORE_L_H_R =
        "Wheeler single-layer air core: L(µH) = r²N²/(9r+10l), r and l in inches. Approximate, best for l > 0.8r."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val ONE_WAY_LENGTH = "One-way length"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val CONDUCTOR_AREA = "Conductor area"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val AWG_3_THROUGH_40 = "AWG (−3 through 40)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val LOAD_CURRENT = "Load current"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val SUPPLY_VOLTAGE = "Supply voltage"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val CONDUCTOR_TEMPERATURE = "Conductor temperature"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val COPPER_MM = "Copper mm²"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val ALUMINIUM_MM = "Aluminium mm²"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val COPPER_AWG = "Copper AWG"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val ALUMINIUM_AWG = "Aluminium AWG"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val TWO_WIRE_DC_V_2LI_A_20_COPPER =
        "Two-wire DC: ΔV = 2LIρ/A; ρ20 copper = 0.017241, aluminium = 0.028264 Ω·mm²/m. Temperature corrected; excludes contact resistance and AC reactance."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val TEMPERATURE_RISE = "Temperature rise"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val COPPER_WEIGHT = "Copper weight"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val IPC_2221_EMPIRICAL_ESTIMATE_I_K_T_0 =
        "IPC-2221 empirical estimate: I=k ΔT^0.44 A^0.725. A in square mils; k=0.048 external / 0.024 internal. Nominal 1 oz thickness 1.378 mil. Not IPC-2152 thermal simulation."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val RMS_VOLTAGE_LINE_LINE_FOR_3 = "RMS voltage (line-line for 3φ)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val RMS_CURRENT = "RMS current"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val POWER_FACTOR = "Power factor"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val ACTIVE_POWER = "Active power"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val SINGLE_PHASE_FROM_CURRENT = "Single phase from current"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val THREE_PHASE_FROM_CURRENT = "Three phase from current"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val SINGLE_PHASE_FROM_KW = "Single phase from kW"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val THREE_PHASE_FROM_KW = "Three phase from kW"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val S_VI_SINGLE_PHASE_OR_3_VLL_IL =
        "S = VI (single phase) or √3 VLL IL (balanced three phase); P = S×PF; Q = S√(1−PF²). No harmonic model."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val INPUT_VALUE = "Input value"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val WAVE_PROPAGATION_SPEED = "Wave propagation speed"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val PERIOD_S = "Period (s)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val ANGULAR_FREQUENCY_RAD_S = "Angular frequency (rad/s)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val WAVELENGTH_M = "Wavelength (m)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val T_1_F_2_F_V_F_WAVE = "T=1/f; ω=2πf; λ=v/f. Wave speed is medium dependent."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val RESOLUTION_BITS = "Resolution bits"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val REFERENCE_VOLTAGE = "Reference voltage"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val ADC_CODE = "ADC code"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val COUNTS_VOLTAGE = "Counts → voltage"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val VOLTAGE_COUNTS = "Voltage → counts"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val IDEAL_UNIPOLAR_ADC_LSB_VREF_2_N_CODE =
        "Ideal unipolar ADC: LSB = Vref/2^N; code floor(Vin/LSB), clamped to 0…2^N−1. Voltage displayed at lower edge of code bin."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val SEARCH_THE_FORMULA_REFERENCE_BELOW = "Search the formula reference below."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val TEXT_4_BANDS = "4 bands"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val TEXT_5_BANDS = "5 bands"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val SELECT_NEAREST_REPRESENTABLE_SIGNIFICANT_DIGITS_FINAL_BAND_GOLD =
        "Select nearest representable significant digits; final band gold (5%) or brown (1%)."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val MEASURED_RESISTANCE = "Measured resistance"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val PT100_CVD_R_T = "PT100 CVD R → T"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val PT100_CVD_T_R = "PT100 CVD T → R"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val PT1000_CVD_R_T = "PT1000 CVD R → T"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val PT1000_CVD_T_R = "PT1000 CVD T → R"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val PT100_LINEAR_R_T = "PT100 linear R → T"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val PT100_LINEAR_T_R = "PT100 linear T → R"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val PT1000_LINEAR_R_T = "PT1000 linear R → T"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val PT1000_LINEAR_T_R = "PT1000 linear T → R"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val IEC_60751_NOMINAL_PLATINUM_COEFFICIENTS_A_3_9083E =
        "IEC 60751 nominal platinum coefficients: A=3.9083e−3; B=−5.775e−7; C=−4.183e−12 below 0 °C. Range −200…850 °C. Lead resistance is excluded."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val STAR_A_DELTA_AB = "Star A / delta AB"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val STAR_B_DELTA_BC = "Star B / delta BC"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val STAR_C_DELTA_CA = "Star C / delta CA"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val STAR_DELTA = "Star → delta"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val DELTA_STAR = "Delta → star"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val DELTA_AB_RARB_RBRC_RCRA_RC_STAR_A =
        "Delta AB = (RaRb+RbRc+RcRa)/Rc. Star A = RabRca/(Rab+Rbc+Rca)."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val ZENER_VOLTAGE = "Zener voltage"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val TARGET_ZENER_CURRENT = "Target Zener current"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val RS_VS_VZ_IL_IZ_PZ_VZIZ_PR =
        "Rs=(Vs−Vz)/(IL+Iz); Pz=VzIz; Pr=(Vs−Vz)(IL+Iz). Check no-load Zener power and minimum/maximum supply conditions."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val REFERENCE_LOAD_IMPEDANCE = "Reference load impedance"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val DB_VOLTAGE_RATIO = "dB → voltage ratio"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val DB_POWER_RATIO = "dB → power ratio"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val VOLTAGE_RATIO_DB = "Voltage ratio → dB"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val POWER_RATIO_DB = "Power ratio → dB"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val DBM_WATTS = "dBm → watts"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val WATTS_DBM = "Watts → dBm"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val DBV_VRMS = "dBV → Vrms"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val VRMS_DBV = "Vrms → dBV"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val DBU_VRMS = "dBu → Vrms"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val VRMS_DBU = "Vrms → dBu"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val TEXT_20LOG10_VOLTAGE_RATIO_EQUAL_IMPEDANCE_10LOG10_POWER_RATIO =
        "20log10 voltage ratio (equal impedance); 10log10 power ratio. 0 dBm=1 mW; 0 dBV=1 Vrms; 0 dBu=√0.6 Vrms. Load power derived as V²/Z."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val INPUT_OUTPUT_VALUE = "Input / output value"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val INPUT_MINIMUM = "Input minimum"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val INPUT_MAXIMUM = "Input maximum"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val OUTPUT_MINIMUM = "Output minimum"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val OUTPUT_MAXIMUM = "Output maximum"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val Y_YMIN_X_XMIN_YMAX_YMIN_XMAX_XMIN =
        "y = ymin+(x−xmin)(ymax−ymin)/(xmax−xmin). Extrapolation permitted; zero-width ranges rejected."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val AMPLITUDE_INPUT = "Amplitude input"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val SINE_PEAK = "Sine: peak"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val SINE_PEAK_TO_PEAK = "Sine: peak-to-peak"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val SINE_RMS = "Sine: RMS"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val SQUARE_PEAK = "Square: peak"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val SQUARE_PEAK_TO_PEAK = "Square: peak-to-peak"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val SQUARE_RMS = "Square: RMS"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val TRIANGLE_PEAK = "Triangle: peak"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val TRIANGLE_PEAK_TO_PEAK = "Triangle: peak-to-peak"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val TRIANGLE_RMS = "Triangle: RMS"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val SYMMETRIC_ZERO_CENTRED_WAVEFORMS_VRMS_VP_2_SINE =
        "Symmetric zero-centred waveforms. Vrms=Vp/√2 sine, Vp square, Vp/√3 triangle. Average is rectified average; signed cycle average is zero."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val UNKNOWN_CALCULATOR = "Unknown calculator"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val MAXIMUM_CODE = "Maximum code"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val TURNS_ROUND_UP = "Turns (round up)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val XC = "Xc (Ω)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val XL = "Xl (Ω)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val RESONANT_FREQUENCY_HZ = "Resonant frequency (Hz)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val SERIES_Q = "Series Q"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val VOUT_V = "Vout (V)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val TARGET_MUST_BE_BELOW_VIN = "Target must be below Vin."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val LOAD_TOO_LOW_FOR_TARGET_OUTPUT = "Load too low for target output."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val R2 = "R2 (Ω)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val TARGET_RATIO_MUST_BE_BETWEEN_0_0000001_AND =
        "Target ratio must be between 0.0000001 and 10000000 for resistors from 1 Ω to 10 MΩ."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val NO_STANDARD_VALUE_PAIR_REACHES_THAT_RATIO_BETWEEN =
        "No standard-value pair reaches that ratio between 1 Ω and 10 MΩ."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val R1 = "R1 (Ω)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val ERROR = "Error (%)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val EVERY_LIST_ENTRY_MUST_BE_A_VALID_POSITIVE =
        "Every list entry must be a valid positive value."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val ELAPSED_TIME_MUST_BE_ZERO_OR_POSITIVE = "Elapsed time must be zero or positive."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val TARGET_PERCENTAGE_MUST_BE_BETWEEN_0_AND_100 =
        "Target percentage must be between 0 and 100."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val TIME_CONSTANT_S = "Time constant (s)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val TIME_TO_TARGET_S = "Time to target (s)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val GAIN_V_V = "Gain (V/V)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val IDEAL_OUTPUT_V = "Ideal output (V)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val IDEAL_R_PER_STRING = "Ideal R per string (Ω)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val E24_R_PER_STRING = "E24 R per string (Ω)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val ACTUAL_STRING_CURRENT_A = "Actual string current (A)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val TOTAL_CURRENT_A = "Total current (A)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val RESISTOR_DISSIPATION_PER_STRING_W = "Resistor dissipation per string (W)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val SUGGESTED_RATING_W = "Suggested rating ≥ (W)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val TARGET_VOLTAGE_MUST_EXCEED_THE_1_25_V =
        "Target voltage must exceed the 1.25 V reference."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val PULSE_WIDTH_S = "Pulse width (s)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val DISSIPATION_W = "Dissipation (W)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val ESTIMATED_JUNCTION_C = "Estimated junction (°C)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val ACTIVE_DUTY_MUST_BE_BETWEEN_0_AND_100 = "Active duty must be between 0 and 100%."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val EFFECTIVE_USABLE_CAPACITY_MUST_BE_GREATER_THAN_0 =
        "Effective usable capacity must be greater than 0 and at most 100%."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val IDLE_LOAD_MUST_BE_ZERO_OR_POSITIVE = "Idle load must be zero or positive."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val AVERAGE_LOAD_CURRENT_MUST_EXCEED_ZERO = "Average load current must exceed zero."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val AVERAGE_CURRENT_MA = "Average current (mA)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val RUNTIME_HOURS = "Runtime (hours)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val RUNTIME_DAYS = "Runtime (days)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val INDUCTANCE_H = "Inductance (H)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val AREA_MM = "Area (mm²)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val VOLTAGE_DROP_V = "Voltage drop (V)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val DROP = "Drop (%)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val LOAD_VOLTAGE_V = "Load voltage (V)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val REQUIRED_TRACE_WIDTH_MM = "Required trace width (mm)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val POWER_FACTOR_MUST_BE_BETWEEN_0_AND_1 = "Power factor must be between 0 and 1."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val POWER_FACTOR_MUST_EXCEED_ZERO_TO_SOLVE_CURRENT =
        "Power factor must exceed zero to solve current."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val APPARENT_POWER_VA = "Apparent power (VA)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val ACTIVE_POWER_W = "Active power (W)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val REACTIVE_POWER_VAR = "Reactive power (var)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val INPUT_VOLTAGE_MUST_BE_BETWEEN_ZERO_AND_THE =
        "Input voltage must be between zero and the reference voltage."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val CODE_BIN_LOWER_VOLTAGE_V = "Code-bin lower voltage (V)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val LSB_V = "LSB (V)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val BROWN_1 = "Brown ±1%"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val GOLD_5 = "Gold ±5%"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val TEMPERATURE_C = "Temperature (°C)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val DELTA_AB = "Delta AB (Ω)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val DELTA_BC = "Delta BC (Ω)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val DELTA_CA = "Delta CA (Ω)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val STAR_A = "Star A (Ω)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val STAR_B = "Star B (Ω)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val STAR_C = "Star C (Ω)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val SUPPLY_MUST_EXCEED_ZENER_VOLTAGE = "Supply must exceed Zener voltage."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val LOAD_CURRENT_MUST_BE_ZERO_OR_POSITIVE = "Load current must be zero or positive."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val SERIES_RESISTOR = "Series resistor (Ω)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val ZENER_CURRENT_A = "Zener current (A)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val ZENER_POWER_W = "Zener power (W)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val RESISTOR_POWER_W = "Resistor power (W)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val NO_LOAD_ZENER_POWER_W = "No-load Zener power (W)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val INPUT_RATIO_OR_POWER_MUST_EXCEED_ZERO_FOR =
        "Input ratio or power must exceed zero for logarithmic conversion."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val VOLTAGE_VRMS = "Voltage (Vrms)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val LOAD_POWER_W = "Load power (W)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val CONVERTED_VALUE = "Converted value"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val INPUT_AND_OUTPUT_RANGES_MUST_HAVE_NON_ZERO =
        "Input and output ranges must have non-zero span."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val MAPPED_VALUE = "Mapped value"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val PEAK_V = "Peak (V)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val PEAK_TO_PEAK_V = "Peak-to-peak (V)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val RMS_V = "RMS (V)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val RECTIFIED_AVERAGE_V = "Rectified average (V)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val SIGNED_AVERAGE_V = "Signed average (V)"
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val FAILED_REQUIREMENT = "Failed requirement."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val CHECK_THE_INPUT_VALUES_AND_SELECTED_MODE = "Check the input values and selected mode."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val ENTER_A_VALID_MINIMUM_VALUE = "Enter a valid minimum value."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val ENTER_A_VALID_MAXIMUM_VALUE = "Enter a valid maximum value."
    /**
     * Shared English calculation label/equation or validation text; centralised to prevent
     * inconsistent presentation.
     */
    const val MINIMUM_MUST_NOT_EXCEED_MAXIMUM = "Minimum must not exceed maximum."

    /**
     * Names the failing field without exposing implementation-level requirement text.
     *
     * @param label Visible field/cell caption used for accessible presentation.
     * @return An actionable sentence naming the invalid field; no exceptions. Returns prose only
     *   and does not throw validation exceptions.
     */
    fun invalidField(label: String): String = "Enter a valid $label."

    /**
     * Explains a positive-input constraint using the user-visible field name.
     *
     * @param label Visible field/cell caption used for accessible presentation.
     * @return An actionable positive-value instruction; no exceptions. Returns prose only and does
     *   not throw validation exceptions.
     */
    fun positiveField(label: String): String = "$label must be greater than zero."

    /**
     * Explains whole-number bounds before integer conversion.
     *
     * @param label Visible field/cell caption used for accessible presentation.
     * @param range Inclusive supported whole-number interval.
     * @return An instruction including both whole-number limits; no exceptions. Returns prose only
     *   and does not throw validation exceptions.
     */
    fun wholeField(label: String, range: IntRange): String =
        "$label must be a whole number from ${range.first} to ${range.last}."

    /**
     * Labels an explicit decimal marking instead of implying a numeric multiplier code exists.
     *
     * @param plain Plain printed R notation, with no exponent.
     * @return Labelled notation; this function only constructs text and does not throw validation
     *   errors.
     */
    fun rNotation(plain: String): String = "Use R notation: $plain"

    /**
     * Keeps printed-code prose together while calculations remain in the resistor engine.
     *
     * @param three Three-digit code or its explicit alternative.
     * @param four Four-digit code or its explicit alternative.
     * @param code Nearest EIA-96 marking.
     * @param value Formatted physical resistance for that nearest marking.
     * @return Multi-line code summary; no I/O or validation exceptions.
     */
    fun smdResult(three: String, four: String, code: String, value: String): String =
        "3-digit: $three\n4-digit: $four\nEIA-96 nearest: $code ($value)"

    /**
     * Describes the normalised filter plot so users do not infer linear frequency scaling.
     *
     * @param value Cutoff frequency formatted in hertz.
     * @return Cutoff and plot-scale text; no I/O or validation exceptions.
     */
    fun filterResult(value: String): String =
        "Cutoff: $value\nPlot: magnitude 0…1 over 0.01fc…100fc (log frequency)"

    /**
     * Builds a dimensional series/parallel output label without scattering English result prose.
     *
     * @param series True labels the series equivalent, false labels the parallel equivalent.
     * @param unit Physical unit for the component family.
     * @return Dimensional result label; no exceptions are thrown.
     */
    fun componentLabel(series: Boolean, unit: String): String =
        (if (series) "Series" else "Parallel") + " ($unit)"

    /** Shared English label or mode name, independent of persisted field keys. */
    const val LABEL_RESISTANCE = "Resistance"

    /** Shared English label or mode name, independent of persisted field keys. */
    const val LABEL_VOLTAGE = "Voltage"

    /** Shared English label or mode name, independent of persisted field keys. */
    const val LABEL_CURRENT = "Current"

    /** Shared English label or mode name, independent of persisted field keys. */
    const val LABEL_POWER = "Power"

    /** Shared English label or mode name, independent of persisted field keys. */
    const val LABEL_CAPACITANCE = "Capacitance"

    /** Shared English label or mode name, independent of persisted field keys. */
    const val LABEL_INDUCTANCE = "Inductance"

    /** Shared English label or mode name, independent of persisted field keys. */
    const val LABEL_FREQUENCY = "Frequency"

    /** Shared English label or mode name, independent of persisted field keys. */
    const val LABEL_CHARGE = "Charge"

    /** Shared English label or mode name, independent of persisted field keys. */
    const val LABEL_DISCHARGE = "Discharge"

    /** Shared English label or mode name, independent of persisted field keys. */
    const val LABEL_INVERTING = "Inverting"

    /** Shared English label or mode name, independent of persisted field keys. */
    const val LABEL_NON_INVERTING = "Non-inverting"

    /** Shared English label or mode name, independent of persisted field keys. */
    const val LABEL_SUMMING = "Summing"

    /** Shared English label or mode name, independent of persisted field keys. */
    const val LABEL_DIFFERENTIAL = "Differential"

    /** Shared English label or mode name, independent of persisted field keys. */
    const val LABEL_SUPPLY = "Supply"

    /** Shared English label or mode name, independent of persisted field keys. */
    const val LABEL_ASTABLE = "Astable"

    /** Shared English label or mode name, independent of persisted field keys. */
    const val LABEL_MONOSTABLE = "Monostable"

    /** Shared English label or mode name, independent of persisted field keys. */
    const val LABEL_CAPACITY = "Capacity"

    /** Shared English label or mode name, independent of persisted field keys. */
    const val LABEL_TURNS = "Turns"

    /** Shared English label or mode name, independent of persisted field keys. */
    const val LABEL_EXTERNAL = "External"

    /** Shared English label or mode name, independent of persisted field keys. */
    const val LABEL_INTERNAL = "Internal"

    /** Shared English label or mode name, independent of persisted field keys. */
    const val LABEL_TEMPERATURE = "Temperature"

    /** Shared English label or mode name, independent of persisted field keys. */
    const val LABEL_FORWARD = "Forward"

    /** Shared English label or mode name, independent of persisted field keys. */
    const val LABEL_REVERSE = "Reverse"

    /** Shared English label or mode name, independent of persisted field keys. */
    const val LABEL_CODE = "Code"

    /** Shared English label or mode name, independent of persisted field keys. */
    const val LABEL_RATIO = "Ratio"
}
