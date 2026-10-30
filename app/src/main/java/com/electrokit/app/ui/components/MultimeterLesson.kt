package com.electrokit.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp

/** An isolated, low-voltage exercise; an illustration cannot identify a real meter's ratings. */
internal data class MeterExercise(
    val name: String,
    val dial: String,
    val redJack: String,
    val wiring: String,
    val example: String,
    val steps: List<String>,
    val normal: String,
    val diagnosis: String,
    val caution: String,
)

/** Bench-safe learning examples, not instructions for hazardous live electrical testing. */
internal val meterExercises = listOf(
    MeterExercise(
        "DC volts", "V⎓", "V/Ω", "PARALLEL: connect across the isolated battery",
        "A healthy nominal 1.5 V AA cell may read near 1.5 V at rest; voltage varies with charge and load.",
        listOf("Inspect the meter, display and leads for damage.",
            "Insert black lead into COM and red lead into V/Ω.",
            "Select DC volts. On manual-range meters choose a range above the expected voltage.",
            "For a loose AA cell, touch black to − and red to +. Keep fingers behind probe guards.",
            "Read the polarity and voltage, then remove the probes."),
        "A negative sign generally means the probes were reversed. An overrange indicator means change to a suitable higher range.",
        "A low open-circuit reading may indicate a depleted cell, but test under an appropriate load to assess performance.",
        "Use only a standalone low-voltage battery for practice. Never use the current input to measure voltage."
    ),
    MeterExercise(
        "AC low volts", "V~", "V/Ω", "PARALLEL: across an isolated low-voltage AC output",
        "An isolated teaching transformer rated 6 V AC may read approximately 6 V RMS; no-load results can differ.",
        listOf("Use only a verified isolated, low-voltage training source supervised as appropriate.",
            "Inspect leads; insert black into COM and red into V/Ω.",
            "Select AC volts and a suitable range.",
            "Place probes across the isolated output terminals without touching exposed metal.",
            "Read the indicated AC voltage, then disconnect."),
        "The displayed number is usually an RMS-equivalent reading within the instrument's stated frequency and waveform limits.",
        "A zero reading may mean a disconnected source, wrong function, defective lead or a measurement below resolution.",
        "Do not practise on wall sockets, distribution boards, or mains wiring. Live work requires qualified personnel and rated equipment."
    ),
    MeterExercise(
        "Resistance", "Ω", "V/Ω", "POWER OFF: measure across a removed resistor",
        "A standalone resistor labelled 1 kΩ should read approximately 1000 Ω within its tolerance.",
        listOf("Remove power, isolate the circuit, and discharge any stored charge safely.",
            "Remove the resistor or isolate at least one lead to eliminate parallel paths.",
            "Black into COM; red into V/Ω. Select resistance (Ω).",
            "Touch one probe to each resistor lead; keep your fingers off the exposed metal.",
            "Compare the reading with the resistor's marked value and tolerance."),
        "A finite reading near the marked value is expected; OL/open means outside range or an open path.",
        "A near-zero reading suggests a short or very low resistance; an in-circuit reading may be lower due to parallel components.",
        "Never use ohms mode on an energized circuit. Verify absence of voltage before measuring."
    ),
    MeterExercise(
        "Continuity", ")))", "V/Ω", "POWER OFF: probe opposite ends of a disconnected wire",
        "A short, sound test lead usually reads near 0 Ω; the beep threshold is meter-specific.",
        listOf("De-energize and isolate the wire from all circuits and devices.",
            "Inspect and verify that the wire cannot carry voltage.",
            "Black to COM, red to V/Ω; choose the continuity/buzzer icon.",
            "Touch the two probe tips together to check the meter and test leads.",
            "Touch opposite ends of the isolated wire and observe the displayed resistance or beep."),
        "A beep generally means resistance is below the meter's threshold; silence is not an absolute proof of an open circuit.",
        "If there is no beep on the shorted leads, check the selected mode, leads, connectors and batteries.",
        "A continuity beep is not evidence of a safe protective-earth connection or insulation quality."
    ),
    MeterExercise(
        "Diode", "▷|", "V/Ω", "POWER OFF: probe the isolated diode in both directions",
        "A typical silicon diode may show roughly 0.5–0.8 V forward drop; values vary with device and test current.",
        listOf("Completely remove power and discharge storage capacitors.",
            "Isolate the diode from parallel circuitry if necessary.",
            "Connect black to COM, red to V/Ω and select the diode symbol.",
            "Place red on anode and black on cathode (stripe side for many diodes). Record the forward reading.",
            "Reverse the probes; compare the reverse reading with the diode specification."),
        "A forward drop and reverse OL can be normal for an ordinary silicon diode; LEDs and other devices can differ.",
        "Near-zero in both directions can indicate a short; OL in both can indicate an open diode or inadequate test voltage.",
        "Do not infer LED polarity solely from a generated picture; identify the component's actual markings."
    ),
    MeterExercise(
        "DC current", "A⎓", "FUSED A/mA", "IN SERIES ONLY: battery → meter → resistor → battery",
        "For a 1.5 V AA cell and a 1 kΩ resistor, ideal current is about 1.5 mA (I = V/R).",
        listOf("Use a single AA cell and a suitable resistor in an isolated, low-energy teaching circuit.",
            "With power removed, inspect the meter's fused current input, limits and lead condition.",
            "Black goes to COM; red goes to the correctly fused mA/A jack for the expected current.",
            "Select DC current and a range that the manufacturer's instructions permit.",
            "Open the test circuit and insert the meter in SERIES with the resistor. Only then connect the cell.",
            "Disconnect the cell first. Remove the meter and return the RED lead to V/Ω."),
        "The current varies with actual cell voltage, resistor tolerance and the meter's burden voltage.",
        "Zero may indicate an open path or blown current-input fuse. Test the fuse only by the maker's safe procedure.",
        "Never place a current-mode meter directly across a battery or supply: that can create a short circuit and fire hazard."
    ),
    MeterExercise(
        "Capacitance", "—|(—", "V/Ω or C", "POWER OFF: measure an isolated, safely discharged capacitor",
        "A capacitor marked 100 µF should be near its rated value within the part's tolerance and the meter's range.",
        listOf("Remove all power. Discharge the capacitor using the method specified for that device.",
            "Verify absence of stored voltage with the correct voltage function before capacitance mode.",
            "If required by the meter manual, isolate or remove the capacitor from the circuit.",
            "Use the jack specified for capacitance, often V/Ω or a dedicated C socket.",
            "Select capacitance and connect with the polarity orientation required by the meter.",
            "Allow the reading to settle and compare with the capacitor's tolerance."),
        "A reading near the rated value is expected on compatible meters. Some instruments lack capacitance mode.",
        "OL, unstable readings or unexpected values can reflect range limits, leakage, residual charge or parallel circuit paths.",
        "Capacitors may store hazardous energy after disconnection. Do not practise with high-voltage or mains capacitors."
    )
)

/** Guided meter diagram, example, setup sequence, results and mistakes for each selected function. */
@Composable
fun MultimeterLesson() {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var step by rememberSaveable(selected) { mutableIntStateOf(0) }
    val exercise = meterExercises[selected]
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("PRACTICAL DIGITAL MULTIMETER LAB", style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary)
        Text("Choose a function. Practise with disconnected components or isolated low-voltage sources only.",
            style = MaterialTheme.typography.bodyMedium)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            meterExercises.forEachIndexed { index, item ->
                FilterChip(index == selected, { selected = index }, label = { Text(item.name) })
            }
        }
        Canvas(Modifier.fillMaxWidth().height(285.dp)) {
            val w = size.width; val h = size.height
            drawRoundRect(Color(0xFFF4A21F), Offset(w * .20f, 3f), Size(w * .60f, h - 6f),
                androidx.compose.ui.geometry.CornerRadius(24f))
            drawRoundRect(Color(0xFF18253A), Offset(w * .23f, h * .035f),
                Size(w * .54f, h * .92f), androidx.compose.ui.geometry.CornerRadius(18f))
            drawRect(Color(0xFFCFE5D6), Offset(w * .29f, h * .095f), Size(w * .42f, h * .19f))
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.BLACK
                textSize = w * .065f
                textAlign = android.graphics.Paint.Align.CENTER
            }
            drawContext.canvas.nativeCanvas.drawText(exercise.dial, w * .5f, h * .22f, paint)
            drawCircle(Color(0xFF41536D), h * .13f, Offset(w * .5f, h * .5f))
            val angle = (-135.0 + selected * 44) * Math.PI / 180
            drawLine(Color.White, Offset(w * .5f, h * .5f),
                Offset(w * .5f + kotlin.math.cos(angle).toFloat() * h * .09f,
                    h * .5f + kotlin.math.sin(angle).toFloat() * h * .09f), 7f)
            paint.color = android.graphics.Color.WHITE
            paint.textSize = w * .035f
            val jackX = listOf(.34f, .50f, .66f)
            listOf("FUSED A", "COM", "V / Ω").forEachIndexed { i, label ->
                val active = i == 1 || (if (selected == 5) i == 0 else i == 2)
                drawCircle(if (i == 1) Color(0xFF151515) else Color(0xFFE24A42),
                    h * .034f, Offset(w * jackX[i], h * .81f))
                drawCircle(if (active) Color(0xFFFFE66D) else Color.White,
                    h * .034f, Offset(w * jackX[i], h * .81f),
                    style = Stroke(if (active) 5f else 2f))
                drawContext.canvas.nativeCanvas.drawText(label, w * jackX[i], h * .735f, paint)
            }
            drawLine(Color(0xFF0B121F), Offset(w * .50f, h * .81f), Offset(w * .42f, h), 6f)
            val activeRed = if (selected == 5) .34f else .66f
            drawLine(Color(0xFFEF4444), Offset(w * activeRed, h * .81f), Offset(w * .88f, h), 6f)
        }
        Text("Function: " + exercise.name + "   •   Dial: " + exercise.dial,
            style = MaterialTheme.typography.titleMedium)
        Text("Black → COM   |   Red → " + exercise.redJack, color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleSmall)
        Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = MaterialTheme.shapes.medium) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("CONNECT THE TEST LEADS", style = MaterialTheme.typography.labelLarge)
                Text(exercise.wiring, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Text("GUIDED PROCEDURE", style = MaterialTheme.typography.titleMedium)
        LinearProgressIndicator(
            progress = { (step + 1).toFloat() / exercise.steps.size },
            modifier = Modifier.fillMaxWidth()
        )
        Text("Step " + (step + 1) + " of " + exercise.steps.size,
            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(exercise.steps[step], style = MaterialTheme.typography.bodyLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { step = (step - 1).coerceAtLeast(0) },
                enabled = step > 0) { Text("Previous") }
            Button(onClick = { step = (step + 1).coerceAtMost(exercise.steps.lastIndex) },
                enabled = step < exercise.steps.lastIndex) { Text("Next step") }
            TextButton(onClick = { step = 0 }) { Text("Restart") }
        }
        HorizontalDivider()
        Text("EXAMPLE READING", style = MaterialTheme.typography.titleSmall)
        Text(exercise.example)
        Text("HOW TO READ THE RESULT", style = MaterialTheme.typography.titleSmall)
        Text(exercise.normal)
        Text("IF THE RESULT LOOKS WRONG", style = MaterialTheme.typography.titleSmall)
        Text(exercise.diagnosis)
        Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.medium) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("SAFETY — DO NOT SKIP", style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer)
                Text(exercise.caution, color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
        Text("Illustrative meter only. Actual jack assignments, maximum current duration, CAT ratings and supported functions depend on your meter's manual.",
            style = MaterialTheme.typography.bodySmall)
    }
}
