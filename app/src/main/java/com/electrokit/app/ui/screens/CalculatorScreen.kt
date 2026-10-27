package com.electrokit.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import com.electrokit.app.R
import com.electrokit.app.data.*
import com.electrokit.app.domain.*
import com.electrokit.app.ui.components.*
import com.electrokit.app.ui.theme.*
import com.electrokit.app.viewmodel.AppViewModel
import kotlin.math.*

/**
 * Connects saveable raw input snapshots and unit selectors to live domain results without
 * persisting derived display text.
 *
 * @param entry Registry route and presentation metadata.
 * @param vm Lifecycle-owned application state and persistence coordinator.
 * @param prefs Current immutable preference snapshot.
 * @param back Caller-owned back-navigation callback.
 * @return Unit; rendering effects and state changes are delivered through callbacks or observable
 *   state. Rendering/callback failures propagate; recoverable persistence failures belong to the
 *   ViewModel.
 */
@Composable
fun CalculatorScreen(entry: Entry, vm: AppViewModel, prefs: PreferencesState, back: () -> Unit) {
    val id = entry.id.drop(1).toInt()
    val spec = remember(id) { Calculators.spec(id) }
    val initial =
        remember(entry.id) {
            prefs.values[entry.id] ?: spec.fields.associate { it.key to it.initial }
        }
    var raw by rememberSaveable(entry.id) { mutableStateOf<Map<String, String>>(initial.toMap()) }
    var mode by
        rememberSaveable(entry.id) {
            mutableIntStateOf((prefs.modes[entry.id] ?: 0).coerceIn(spec.modes.indices))
        }
    var more by remember { mutableStateOf(false) }
    var resetKey by rememberSaveable { mutableIntStateOf(0) }
    val scaled =
        raw.mapValues { (k, v) ->
            val f = spec.fields.firstOrNull { it.key == k }
            if (f != null && !f.text && v.isNotBlank())
                Engineering.input(v, raw["unit_$k"]?.toIntOrNull() ?: 0, f.unit)?.toString()
                    ?: "Invalid or out-of-range input"
            else v
        }
    var bandResult by rememberSaveable(entry.id) { mutableStateOf("") }
    var calculated by remember(entry.id) { mutableStateOf<Calculation?>(null) }
    val result =
        calculated ?: if (id in listOf(1, 3)) Calculation.Value(bandResult)
        else Calculators.calculate(id, scaled, mode)
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    /**
     * Transforms the latest preference record atomically; the ViewModel wrapper retains the exact
     * failed mutation for Retry.
     *
     * @param map New immutable field snapshot.
     * @return Unit; rendering effects and state changes are delivered through callbacks or
     *   observable state. Rendering/callback failures propagate; recoverable persistence failures
     *   belong to the ViewModel.
     */
    fun update(map: Map<String, String>) {
        raw = map.toMap()
        calculated = null
        vm.values(entry.id, map)
    }
    val shareText =
        entry.title +
            "\n" +
            spec.modes[mode.coerceIn(spec.modes.indices)] +
            "\n" +
            spec.fields.joinToString("\n") {
                "${it.label}: ${if(it.text)raw[it.key].orEmpty() else Engineering.parse(scaled[it.key].orEmpty())?.let { value->Engineering.format(value,it.unit) } ?: raw[it.key].orEmpty()}"
            } +
            "\n" +
            result.text +
            "\n\n" +
            spec.formula
    Scaffold(
        topBar = {
            Bar(entry.title, back) {
                TextButton({ share(context, shareText) }) {
                    Icon(
                        Icons.Default.Share,
                        stringResource(R.string.share_results_description),
                        tint = Color.White,
                    )
                    Text(
                        stringResource(R.string.share_results),
                        color = Color.White,
                        fontSize = 12.sp,
                    )
                }
                IconButton({ more = true }) {
                    Icon(Icons.Default.MoreVert, stringResource(R.string.calculator_menu))
                }
                DropdownMenu(more, { more = false }) {
                    DropdownMenuItem(
                        text = { Text("Save calculation") },
                        onClick = { more = false; vm.saveCalculation(shareText) },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.ui_reset)) },
                        onClick = {
                            more = false
                            mode = 0
                            vm.mode(entry.id, 0)
                            update(spec.fields.associate { it.key to it.initial })
                            resetKey++
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.ui_copy_result)) },
                        onClick = {
                            more = false
                            clipboard.setText(AnnotatedString(shareText))
                        },
                    )
                }
            }
        }
    ) { padding ->
        if (id == 1 || id == 3) {
            key(resetKey) {
                BandScreen(
                    id == 3,
                    raw,
                    { update(it) },
                    { bandResult = it },
                    Modifier.padding(padding),
                )
            }
            return@Scaffold
        }
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(12.dp)) {
            Text("ENGINEERING WORKBENCH", style = MaterialTheme.typography.labelSmall, color = Heading)
            Text(entry.title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(vertical = 12.dp))
            if (spec.modes.size > 1)
                Choice(
                    stringResource(R.string.mode),
                    spec.modes,
                    spec.modes[mode.coerceIn(spec.modes.indices)],
                    {
                        mode = spec.modes.indexOf(it)
                        calculated = null
                        vm.mode(entry.id, mode)
                    },
                )
            if (id == 24) {
                FormulaBrowser(vm.repository.registry)
                return@Column
            }
            Text(
                stringResource(R.string.inputs),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(
                stringResource(R.string.live_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            spec.fields
                .filter { field ->
                    id != 12 ||
                        when (field.key) {
                            "v2" -> mode >= 2
                            "r3" -> mode == 2
                            else -> true
                        }
                }
                .forEach { field ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            raw[field.key].orEmpty(),
                            { update(raw + (field.key to it)) },
                            Modifier.weight(1f).padding(vertical = 5.dp),
                            shape = RoundedCornerShape(14.dp),
                            label = { Text(field.label) },
                            supportingText = {
                                Text(
                                    if (
                                        Engineering.ignoresSelector(
                                            raw[field.key].orEmpty(),
                                            raw["unit_${field.key}"]?.toIntOrNull() ?: 0,
                                        )
                                    )
                                        stringResource(R.string.suffix_hint)
                                    else if (field.unit.isBlank())
                                        stringResource(R.string.decimal_hint)
                                    else stringResource(R.string.base_unit_hint, field.unit)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            singleLine = !field.text,
                        )
                        if (field.unit in listOf("Ω", "F", "H", "V", "A", "Hz", "s", "W")) {
                            val units =
                                listOf("p", "n", "µ", "m", "", "k", "M", "G").map {
                                    it + field.unit
                                }
                            val exponents = listOf(-12, -9, -6, -3, 0, 3, 6, 9)
                            val e = raw["unit_${field.key}"]?.toIntOrNull() ?: 0
                            Choice(
                                "",
                                units,
                                units[exponents.indexOf(e).coerceAtLeast(0)],
                                {
                                    update(
                                        raw +
                                            ("unit_${field.key}" to
                                                exponents[units.indexOf(it)].toString())
                                    )
                                },
                                Modifier.width(95.dp),
                            )
                        }
                    }
                }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { calculated = Calculators.calculate(id, scaled, mode) }) { Text("Calculate") }
                OutlinedButton(onClick = {
                    mode = 0; calculated = null; vm.mode(entry.id, 0)
                    update(spec.fields.associate { it.key to it.initial })
                }) { Text("Reset") }
            }
            Card(
                Modifier.fillMaxWidth().padding(vertical = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = .10f)
                    ),
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        stringResource(R.string.ui_result),
                        color = Heading,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        result.text,
                        color =
                            if (result is Calculation.Error) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    ResponsePlot(result.plot)
                }
            }
            if (id == 25) {
                runCatching {
                        Resistors.encode(Engineering.parse(scaled["v"].orEmpty()) ?: 0.0, mode == 1)
                    }
                    .getOrNull()
                    ?.let { b ->
                        ResistorDrawing(
                            b.dropLast(1) +
                                when (b.last()) {
                                    -1 -> 10
                                    -2 -> 11
                                    else -> b.last()
                                } +
                                if (mode == 1) 1 else 10
                        )
                    }
            }
            if (id == 15) TimerCircuit(mode == 1)
            if (id == 27) TransformationDiagram()
            Text(
                stringResource(R.string.ui_formula_assumptions),
                color = Heading,
                fontWeight = FontWeight.Bold,
            )
            Text(spec.formula, Modifier.padding(vertical = 8.dp))
            Text("Substitution", style = MaterialTheme.typography.titleMedium)
            spec.fields.forEach { field ->
                Text("${field.label} = ${raw[field.key].orEmpty().ifBlank { "not entered" }} ${field.unit}", style = MaterialTheme.typography.bodyMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton({ clipboard.setText(AnnotatedString(shareText)) }) { Text("Copy result") }
                TextButton({ vm.saveCalculation(shareText) }) { Text("Save result") }
            }
            Text(
                stringResource(R.string.input_convention),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Keeps the band palette, component drawing and shared result text in sync for resistance and
 * inductance references.
 *
 * @param inductor True draws a coil whose encoded value is in microhenries.
 * @param raw Saved/raw text; null represents an absent record.
 * @param save Supplied (Map<String, String>) -> Unit value consumed by the operation described
 *   above.
 * @param onResult Receives current band-result text for copying and sharing.
 * @param modifier Caller-owned layout and semantics constraints.
 * @return Unit; rendering effects and state changes are delivered through callbacks or observable
 *   state. Rendering/callback failures propagate; recoverable persistence failures belong to the
 *   ViewModel.
 */
@Composable
fun BandScreen(
    inductor: Boolean,
    raw: Map<String, String>,
    save: (Map<String, String>) -> Unit,
    onResult: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val bandDescriptionTemplate = stringResource(R.string.band_description)
    var five by rememberSaveable { mutableStateOf(raw["five"] == "true") }
    /**
     * Clamps restored colour indices before table indexing so damaged preferences cannot crash the
     * screen.
     *
     * @param k Stable field key used to read or update the saved calculator snapshot.
     * @param default Fallback band index used when no valid persisted selection exists.
     * @return A valid colour-table index after clamping restored state. Rendering/callback failures
     *   propagate; recoverable persistence failures belong to the ViewModel.
     */
    fun read(k: String, default: Int): Int {
        val parsed = raw[k]?.toIntOrNull() ?: default
        return when (k) {
            "d1" -> parsed.coerceIn(1, 9)
            "d2",
            "d3" -> parsed.coerceIn(0, 9)
            "mult" -> parsed.coerceIn(0, 11)
            else -> parsed.takeIf { it in Resistors.tolerance } ?: default
        }
    }
    val digits =
        if (five) listOf(read("d1", 1), read("d2", 1), read("d3", 0))
        else listOf(read("d1", 1), read("d2", 1))
    val mult = read("mult", 2)
    val tol = read("tol", 10)
    val value =
        runCatching {
                Resistors.decode(
                    digits,
                    when (mult) {
                        10 -> -1
                        11 -> -2
                        else -> mult
                    },
                )
            }
            .getOrNull()
    val text =
        if (value != null)
            Engineering.format(if (inductor) value * 1e-6 else value, if (inductor) "H" else "Ω") +
                " ±${Engineering.percent(Resistors.tolerance[tol]?:5.0)}"
        else stringResource(R.string.invalid_bands)
    LaunchedEffect(text) { onResult(text) }
    /**
     * Sends a new immutable band snapshot rather than mutating a map shared with persistence.
     *
     * @param k Stable field key used to read or update the saved calculator snapshot.
     * @param v Voltage in volts, or resistance scalar when used by the SMD encoder.
     * @return Unit; rendering effects and state changes are delivered through callbacks or
     *   observable state. Rendering/callback failures propagate; recoverable persistence failures
     *   belong to the ViewModel.
     */
    fun set(k: String, v: Int) {
        save(raw + (k to v.toString()))
    }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.use_five_bands), Modifier.weight(1f))
            Switch(
                five,
                { next ->
                    val b = value?.let { runCatching { Resistors.encode(it, next) }.getOrNull() }
                    five = next
                    val additions = mutableMapOf("five" to next.toString())
                    if (b != null) {
                        b.dropLast(1).forEachIndexed { i, d -> additions["d${i+1}"] = d.toString() }
                        additions["mult"] =
                            when (b.last()) {
                                -1 -> 10
                                -2 -> 11
                                else -> b.last()
                            }.toString()
                    }
                    save(raw + additions)
                },
            )
        }
        ResistorDrawing(digits + mult + tol, inductor)
        Row(
            Modifier.align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text, fontSize = 24.sp)
            Icon(
                if (value != null) Icons.Default.Check else Icons.Default.Close,
                if (value != null) stringResource(R.string.valid)
                else stringResource(R.string.invalid),
                tint = if (value != null) Heading else MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(12.dp),
            )
        }
        Row(Modifier.fillMaxWidth()) {
            val keys =
                if (five) listOf("d1", "d2", "d3", "mult", "tol")
                else listOf("d1", "d2", "mult", "tol")
            keys.forEach { key ->
                val options =
                    when (key) {
                        "d1" -> (1..9).toList()
                        "d2",
                        "d3" -> (0..9).toList()
                        "mult" -> (0..11).toList()
                        else -> listOf(1, 2, 5, 6, 7, 8, 10, 11)
                    }
                val selected =
                    when (key) {
                        "d1" -> digits[0]
                        "d2" -> digits[1]
                        "d3" -> digits.getOrElse(2) { 0 }
                        "mult" -> mult
                        else -> tol
                    }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        when (key) {
                            "mult" -> "×"
                            "tol" -> "±"
                            else -> key.removePrefix("d")
                        },
                        fontWeight = FontWeight.Bold,
                    )
                    (0..11).forEach { colour ->
                        if (colour !in options) Spacer(Modifier.height(52.dp))
                        else {
                            val base = BandColours[colour]
                            val selectedNow = colour == selected
                            Column(
                                Modifier.fillMaxWidth()
                                    .heightIn(min = 52.dp)
                                    .padding(3.dp)
                                    .semantics {
                                        contentDescription =
                                            Resistors.names[colour] +
                                                " " +
                                                key +
                                                if (key == "tol")
                                                    bandDescriptionTemplate.format(
                                                        java.util.Locale.getDefault(),
                                                        Resistors.tolerance[colour],
                                                    )
                                                else ""
                                        this.selected = selectedNow
                                    }
                                    .selectable(
                                        selectedNow,
                                        role = Role.RadioButton,
                                        onClick = { set(key, colour) },
                                    ),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Box(
                                    Modifier.fillMaxWidth()
                                        .height(25.dp)
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    base.copy(
                                                        alpha = if (selectedNow) 1f else .42f
                                                    ),
                                                    Color.White.copy(
                                                        alpha = if (selectedNow) .5f else .08f
                                                    ),
                                                    base.copy(alpha = if (selectedNow) 1f else .42f),
                                                )
                                            ),
                                            RoundedCornerShape(2.dp),
                                        )
                                        .border(
                                            if (selectedNow) 2.dp else 0.5.dp,
                                            if (selectedNow) MaterialTheme.colorScheme.onSurface
                                            else Color.Gray,
                                            RoundedCornerShape(2.dp),
                                        )
                                )
                                Text(
                                    if (key == "tol")
                                        Engineering.percent(Resistors.tolerance.getValue(colour))
                                    else Resistors.names[colour],
                                    fontSize = 12.sp,
                                    maxLines = 2,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Searches the shipping calculator specifications instead of maintaining a second formula
 * catalogue.
 *
 * @param registry Shipping catalogue used to derive formula titles without a duplicate index.
 * @return Unit; rendering effects and state changes are delivered through callbacks or observable
 *   state. Rendering/callback failures propagate; recoverable persistence failures belong to the
 *   ViewModel.
 */
@Composable
fun FormulaBrowser(registry: List<Entry>) {
    var q by rememberSaveable { mutableStateOf("") }
    Search(q, { q = it }, stringResource(R.string.search_formulas))
    registry
        .filter { it.screenType == "calculator" && it.id != "c24" }
        .map { it.title to Calculators.spec(it.id.drop(1).toInt()).formula }
        .filter { (title, formula) -> (title + " " + formula).contains(q, true) }
        .forEach { (title, formula) ->
            Text(title, color = Heading, fontWeight = FontWeight.Bold)
            Text(formula, Modifier.padding(bottom = 14.dp))
        }
}

/**
 * Draws the selected NE555 timing network with original vectors and an accessible description.
 *
 * @param mono True selects the monostable timing network.
 * @return Unit; rendering effects and state changes are delivered through callbacks or observable
 *   state. Rendering/callback failures propagate; recoverable persistence failures belong to the
 *   ViewModel.
 */
@Composable
fun TimerCircuit(mono: Boolean) {
    // Resources must resolve before the non-composable Canvas draw callback executes.
    val circuitLabels =
        mapOf(
            "NE555" to stringResource(R.string.timing_label_0),
            "VCC" to stringResource(R.string.timing_label_1),
            "8,4" to stringResource(R.string.timing_label_2),
            "1" to stringResource(R.string.timing_label_3),
            "GND" to stringResource(R.string.timing_label_4),
            "3 OUT" to stringResource(R.string.timing_label_5),
            "RA" to stringResource(R.string.timing_label_6),
            "RB" to stringResource(R.string.timing_label_7),
            "2,6" to stringResource(R.string.timing_label_8),
            "2 TRIG" to stringResource(R.string.timing_label_9),
            "C" to stringResource(R.string.timing_label_10),
            "5" to stringResource(R.string.timing_label_11),
            "10 nF" to stringResource(R.string.timing_label_12),
        )
    val circuitDescription =
        if (mono) stringResource(R.string.mono_accessibility)
        else stringResource(R.string.astable_accessibility)
    val fg = MaterialTheme.colorScheme.onSurface
    Text(
        if (mono) stringResource(R.string.mono_description)
        else stringResource(R.string.astable_description),
        color = Heading,
    )
    Canvas(
        Modifier.fillMaxWidth().height(260.dp).semantics { contentDescription = circuitDescription }
    ) {
        val w = size.width
        val h = size.height
        /**
         * Converts normalised drawing coordinates into pixel-space offsets so geometry scales with
         * the available canvas.
         *
         * @param x Coordinate in this renderer’s documented canvas space.
         * @param y Coordinate in this renderer’s documented canvas space.
         * @return A pixel-space Offset. Rendering/callback failures propagate; recoverable
         *   persistence failures belong to the ViewModel.
         */
        fun pt(x: Float, y: Float) = Offset(w * x, h * y)
        /**
         * Shares stroke styling between connected schematic segments so visual joins stay
         * consistent.
         *
         * @param x1 Normalised horizontal start coordinate, scaled by this canvas width.
         * @param y1 Normalised vertical start coordinate, scaled by this canvas height.
         * @param x2 Normalised horizontal end coordinate, scaled by this canvas width.
         * @param y2 Normalised vertical end coordinate, scaled by this canvas height.
         * @return Unit; rendering effects and state changes are delivered through callbacks or
         *   observable state. Rendering/callback failures propagate; recoverable persistence
         *   failures belong to the ViewModel.
         */
        fun line(x1: Float, y1: Float, x2: Float, y2: Float) =
            drawLine(fg, pt(x1, y1), pt(x2, y2), 2.dp.toPx())
        /**
         * Draws a circuit annotation as vectors at the current canvas size.
         *
         * @param s Annotation text drawn inside the timing-network canvas.
         * @param x Coordinate in this renderer’s documented canvas space.
         * @param y Coordinate in this renderer’s documented canvas space.
         * @return Unit; rendering effects and state changes are delivered through callbacks or
         *   observable state. Rendering/callback failures propagate; recoverable persistence
         *   failures belong to the ViewModel.
         */
        fun text(s: String, x: Float, y: Float) {
            drawContext.canvas.nativeCanvas.drawText(
                s,
                w * x,
                h * y,
                android.graphics.Paint(3).apply {
                    color = fg.toArgb()
                    textSize = 12 * density
                },
            )
        }
        drawRect(fg, pt(.4f, .26f), Size(w * .3f, h * .44f), style = Stroke(2.dp.toPx()))
        text(circuitLabels.getValue("NE555"), .45f, .5f)
        line(.12f, .1f, .8f, .1f)
        text(circuitLabels.getValue("VCC"), .02f, .12f)
        line(.52f, .1f, .52f, .26f)
        text(circuitLabels.getValue("8,4"), .53f, .23f)
        line(.55f, .7f, .55f, .9f)
        text(circuitLabels.getValue("1"), .57f, .8f)
        line(.12f, .9f, .8f, .9f)
        text(circuitLabels.getValue("GND"), .82f, .92f)
        line(.7f, .45f, .88f, .45f)
        text(circuitLabels.getValue("3 OUT"), .74f, .4f)
        line(.18f, .1f, .18f, .18f)
        drawRect(fg, pt(.15f, .18f), Size(w * .06f, h * .13f), style = Stroke(2f))
        text(circuitLabels.getValue("RA"), .06f, .27f)
        line(.18f, .31f, .18f, .38f)
        line(.18f, .38f, .4f, .38f)
        text(if (mono) "6,7" else "7", .31f, .36f)
        if (!mono) {
            drawRect(fg, pt(.15f, .43f), Size(w * .06f, h * .13f), style = Stroke(2f))
            line(.18f, .38f, .18f, .43f)
            line(.18f, .56f, .18f, .65f)
            line(.18f, .65f, .4f, .65f)
            text(circuitLabels.getValue("RB"), .06f, .52f)
            text(circuitLabels.getValue("2,6"), .31f, .62f)
        } else {
            line(.18f, .38f, .18f, .65f)
            line(.03f, .62f, .4f, .62f)
            text(circuitLabels.getValue("2 TRIG"), .03f, .59f)
        }
        line(.18f, .65f, .18f, .72f)
        line(.12f, .72f, .24f, .72f)
        line(.12f, .77f, .24f, .77f)
        line(.18f, .77f, .18f, .9f)
        text(circuitLabels.getValue("C"), .25f, .77f)
        line(.7f, .63f, .78f, .63f)
        line(.78f, .63f, .78f, .73f)
        line(.74f, .73f, .82f, .73f)
        line(.74f, .77f, .82f, .77f)
        line(.78f, .77f, .78f, .9f)
        text(circuitLabels.getValue("5"), .72f, .61f)
        text(circuitLabels.getValue("10 nF"), .81f, .76f)
    }
    Text(stringResource(R.string.timer_note), style = MaterialTheme.typography.bodySmall)
}

/**
 * Explains star/delta topology with original vectors instead of downloaded artwork.
 *
 * @return Unit; rendering effects and state changes are delivered through callbacks or observable
 *   state. Rendering/callback failures propagate; recoverable persistence failures belong to the
 *   ViewModel.
 */
@Composable
fun TransformationDiagram() {
    val diagramDescription = stringResource(R.string.transformation_accessibility)
    val fg = MaterialTheme.colorScheme.onSurface
    Canvas(
        Modifier.fillMaxWidth().height(150.dp).semantics { contentDescription = diagramDescription }
    ) {
        val w = size.width
        val h = size.height
        val a = Offset(w * .25f, h * .1f)
        val b = Offset(w * .05f, h * .9f)
        val c = Offset(w * .45f, h * .9f)
        val mid = Offset(w * .25f, h * .6f)
        listOf(a, b, c).forEach { drawLine(fg, it, mid, 4f) }
        val p =
            Path().apply {
                moveTo(w * .75f, h * .1f)
                lineTo(w * .55f, h * .9f)
                lineTo(w * .95f, h * .9f)
                close()
            }
        drawPath(p, fg, style = Stroke(4f))
    }
}
