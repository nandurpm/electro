package com.electrokit.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.electrokit.app.R
import com.electrokit.app.data.Diagram
import kotlin.math.*

/**
 * Draws recognisable original hardware illustrations without substituting invented contact layouts.
 *
 * @param diagram Dataset caption and logical contacts; the table remains the wiring reference.
 * @param modifier Parent layout constraints.
 * @param enlarged True renders a larger scrollable inspection drawing without another expand
 *   action.
 * @return True when this renderer handled the drawing, false for existing specialised schematics.
 *   Malformed optional geometry falls back to the existing renderer; drawing failures propagate.
 */
@Composable
fun illustratedConnector(
    diagram: Diagram,
    modifier: Modifier = Modifier,
    enlarged: Boolean = false,
): Boolean {
    var expanded by rememberSaveable(diagram.caption) { mutableStateOf(false) }
    val kind = diagram.shape.removePrefix("illustration:")
    if (!diagram.shape.startsWith("illustration:")) return false
    val detailed = kind in setOf("board", "dip", "regulator")
    Column(
        modifier
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Canvas(
            Modifier.fillMaxWidth()
                .height(if (enlarged) 440.dp else if (detailed) 270.dp else 190.dp)
                .semantics {
                    contentDescription =
                        diagram.caption + ". " + diagram.pins.joinToString { it.label }
                }
        ) {
            // A fixed design space keeps proportions stable across phone densities and
            // orientations.
            val factor = min(size.width / 600f, size.height / 320f)
            withTransform({
                translate((size.width - 600f * factor) / 2, (size.height - 320f * factor) / 2)
                scale(factor, factor, Offset.Zero)
            }) {
                hardware(kind, diagram)
            }
        }
        Text(
            diagram.caption,
            color = Color(0xFF263C46),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        )
        if (!enlarged)
            TextButton(onClick = { expanded = true }) {
                Text(stringResource(R.string.enlarge_diagram))
            }
    }
    if (expanded)
        Dialog(
            onDismissRequest = { expanded = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Column(
                Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(12.dp)
            ) {
                TextButton(onClick = { expanded = false }) {
                    Text(stringResource(R.string.close_diagram))
                }
                Box(
                    Modifier.fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState())
                ) {
                    illustratedConnector(diagram, Modifier.width(820.dp), enlarged = true)
                }
            }
        }
    return true
}

/**
 * Renders hardware in a 600 by 320 coordinate system with bevels, contacts and restrained shadows.
 *
 * @param kind Recognised illustration family.
 * @param diagram Original dataset supplies labels; logical maps retain their logical status.
 * @return Unit. Invalid family keys produce no illustration; canvas errors propagate.
 */
private fun DrawScope.hardware(kind: String, diagram: Diagram) {
    val ink = Color(0xFF263746)
    val gold = Color(0xFFE8B64B)
    val steel =
        Brush.verticalGradient(
            listOf(com.electrokit.app.ui.theme.DrawingMetal, Color(0xFF8B9CA6), com.electrokit.app.ui.theme.DrawingMetal),
            65f,
            245f,
        )
    val plastic = Brush.verticalGradient(listOf(Color(0xFF428BC4), Color(0xFF195383)), 100f, 230f)
    // Soft offset shadow separates the physical shell from the paper-like diagram surface.
    drawOval(Color(0xFF233D49).copy(alpha = .12f), Offset(90f, 262f), Size(420f, 20f))
    when (kind) {
        "dsub",
        "centronics" -> {
            drawRoundRect(steel, Offset(48f, 65f), Size(504f, 185f), CornerRadius(24f))
            drawRoundRect(
                ink,
                Offset(48f, 65f),
                Size(504f, 185f),
                CornerRadius(24f),
                style = Stroke(3f),
            )
            for (x in listOf(80f, 520f)) {
                drawCircle(ink, 17f, Offset(x, 158f))
                drawCircle(Color(0xFFF9FBFC), 12f, Offset(x, 158f))
            }
            val shape =
                Path().apply {
                    moveTo(112f, 92f)
                    lineTo(488f, 92f)
                    quadraticTo(510f, 92f, 501f, 124f)
                    lineTo(480f, 216f)
                    quadraticTo(478f, 231f, 460f, 231f)
                    lineTo(140f, 231f)
                    quadraticTo(122f, 231f, 120f, 216f)
                    lineTo(99f, 124f)
                    quadraticTo(90f, 92f, 112f, 92f)
                    close()
                }
            drawPath(shape, plastic)
            drawPath(shape, ink, style = Stroke(4f))
            val n = diagram.pins.size
            val rows = if (n == 15 && diagram.caption.contains("VGA")) 3 else 2
            // D-sub numbering is sequential by row. This is an explicit male mating-face view.
            var number = 1
            repeat(rows) { row ->
                val count = if (rows == 3) 5 else if (row == 0) (n + 1) / 2 else n / 2
                repeat(count) { col ->
                    val x = 135f + (col + .5f) * 330f / count
                    val y = 126f + row * (if (rows == 3) 39f else 65f)
                    if (kind == "centronics")
                        drawRoundRect(
                            gold,
                            Offset(x - 4f, y - 10f),
                            Size(8f, 20f),
                            CornerRadius(2f),
                        )
                    else {
                        drawCircle(ink, if (count > 10) 8f else 10f, Offset(x, y))
                        drawCircle(gold, if (count > 10) 4f else 5f, Offset(x - 1f, y - 1f))
                    }
                    hardwareLabel(
                        (number++).toString(),
                        x,
                        y - 16f,
                        ink,
                        if (count > 10) 13f else 17f,
                    )
                }
            }
        }
        "round" -> {
            drawCircle(steel, 112f, Offset(300f, 159f))
            drawCircle(ink, 112f, Offset(300f, 159f), style = Stroke(3f))
            drawCircle(Color(0xFF263C49), 92f, Offset(300f, 159f))
            drawCircle(Color(0xFF49616D), 83f, Offset(300f, 159f), style = Stroke(3f))
            drawRoundRect(com.electrokit.app.ui.theme.DrawingMetal, Offset(288f, 237f), Size(24f, 19f), CornerRadius(3f))
            // Unnumbered housing illustration: uncertain DIN/XLR mating-face numbering is not
            // invented.
            repeat(diagram.pins.size) { i ->
                val angle = (210.0 + i * 300.0 / maxOf(1, diagram.pins.size - 1)) * PI / 180
                val x = 300f + cos(angle).toFloat() * 58f
                val y = 159f + sin(angle).toFloat() * 58f
                drawCircle(gold, 10f, Offset(x, y))
                drawCircle(ink, 5f, Offset(x, y))
            }
        }
        "rca",
        "jack" -> {
            drawRoundRect(ink, Offset(35f, 140f), Size(145f, 40f), CornerRadius(12f))
            drawRoundRect(
                Brush.verticalGradient(
                    listOf(Color(0xFFED7A70), Color(0xFFC03836), Color(0xFF6F1D25)),
                    110f,
                    210f,
                ),
                Offset(150f, 105f),
                Size(210f, 110f),
                CornerRadius(15f),
            )
            repeat(7) { i ->
                drawLine(
                    Color(0xFF762632),
                    Offset(165f + i * 18f, 111f),
                    Offset(165f + i * 18f, 209f),
                    3f,
                )
            }
            drawRoundRect(
                steel,
                Offset(355f, 120f),
                Size(if (kind == "rca") 100f else 175f, 80f),
                CornerRadius(8f),
            )
            if (kind == "rca")
                drawRoundRect(gold, Offset(452f, 150f), Size(93f, 20f), CornerRadius(9f))
            else repeat(3) { i -> drawRect(ink, Offset(400f + i * 39f, 120f), Size(9f, 80f)) }
            // Numbered labels refer directly to the existing contact table, not assumed wire
            // colours.
            diagram.pins.forEachIndexed { i, pin ->
                hardwareLabel(
                    pin.label,
                    if (kind == "rca") (if (i == 0) 500f else 398f) else 375f + i * 46f,
                    96f,
                    ink,
                    19f,
                )
            }
        }
        "power",
        "modular" -> {
            drawRoundRect(steel, Offset(64f, 65f), Size(472f, 184f), CornerRadius(14f))
            drawRoundRect(ink, Offset(78f, 80f), Size(444f, 154f), CornerRadius(8f))
            drawRoundRect(Color(0xFF899CA2), Offset(260f, 49f), Size(80f, 22f), CornerRadius(4f))
            diagram.pins.forEachIndexed { i, pin ->
                val n = diagram.pins.size
                val rows =
                    if (kind == "power" && n != 4) 2
                    else if (diagram.caption.contains("ATX12V")) 2 else 1
                val cols = n / rows
                val x = 95f + (i % cols + .5f) * 410f / cols
                val y = if (rows == 1) 159f else 120f + (i / cols) * 77f
                drawRoundRect(
                    Color(0xFFCED7DA),
                    Offset(x - 12f, y - 12f),
                    Size(24f, 25f),
                    CornerRadius(3f),
                )
                drawRect(
                    if (kind == "modular") gold else ink,
                    Offset(x - 6f, y - 5f),
                    Size(12f, 12f),
                )
                if (kind == "modular") hardwareLabel(pin.label, x, y - 16f, Color.White, 15f)
            }
        }
        "regulator" -> {
            drawRoundRect(steel, Offset(230f, 30f), Size(140f, 115f), CornerRadius(12f))
            drawCircle(ink, 19f, Offset(300f, 65f))
            drawCircle(com.electrokit.app.ui.theme.DrawingMetal, 13f, Offset(300f, 65f))
            drawRoundRect(ink, Offset(215f, 100f), Size(170f, 118f), CornerRadius(10f))
            diagram.pins.forEachIndexed { i, pin ->
                val x = 250f + i * 50f
                drawRect(steel, Offset(x - 8f, 218f), Size(16f, 64f))
                hardwareLabel(pin.label, x, 307f, ink, 20f)
            }
        }
        "dip" -> {
            drawRoundRect(ink, Offset(115f, 96f), Size(370f, 130f), CornerRadius(15f))
            drawCircle(Color(0xFF657783), 13f, Offset(115f, 161f))
            drawCircle(Color(0xFFB8C8D0), 6f, Offset(139f, 205f))
            repeat(7) { i ->
                val x = 145f + i * 50f
                drawRect(steel, Offset(x - 8f, 67f), Size(16f, 29f))
                drawRect(steel, Offset(x - 8f, 226f), Size(16f, 29f))
                hardwareLabel((14 - i).toString(), x, 52f, ink)
                hardwareLabel((i + 1).toString(), x, 280f, ink)
            }
            hardwareLabel(diagram.caption.substringBefore(" •"), 300f, 164f, com.electrokit.app.ui.theme.DrawingMetal, 17f)
        }
        "board" -> {
            drawRoundRect(
                Brush.linearGradient(listOf(Color(0xFF148DA0), Color(0xFF086476))),
                Offset(126f, 24f),
                Size(348f, 268f),
                CornerRadius(12f),
            )
            // This is an original identification illustration, with logical labels alongside it.
            // Thin original trace routes suggest the PCB construction without claiming a copper
            // layout.
            repeat(14) { index ->
                val x = 227f + index * 16f
                val trace =
                    Path().apply {
                        moveTo(x, 57f)
                        lineTo(x, 81f + (index % 4) * 8f)
                        lineTo(258f + index * 10f, 124f + (index % 4) * 5f)
                        lineTo(258f + index * 10f, 154f)
                    }
                drawPath(trace, Color(0xFF76BBAE).copy(alpha = .55f), style = Stroke(1.4f))
            }
            repeat(9) { index ->
                val x = 285f + (index % 3) * 22f
                val y = 84f + (index / 3) * 17f
                drawRoundRect(Color(0xFFBDC9C7), Offset(x, y), Size(15f, 6f), CornerRadius(1f))
                drawRect(Color(0xFFB6A27D), Offset(x + 4f, y), Size(7f, 6f))
            }
            drawCircle(steel, 14f, Offset(187f, 80f))
            drawCircle(Color(0xFFAD5038), 9f, Offset(187f, 80f))
            drawLine(Color(0xFFBDE0D6), Offset(180f, 116f), Offset(204f, 116f), 2f)
            for (x in listOf(142f, 456f)) for (y in listOf(40f, 276f)) {
                drawCircle(gold, 9f, Offset(x, y))
                drawCircle(com.electrokit.app.ui.theme.DrawingMetal, 5f, Offset(x, y))
            }
            drawRoundRect(steel, Offset(94f, 70f), Size(76f, 70f), CornerRadius(4f))
            drawRoundRect(ink, Offset(105f, 200f), Size(68f, 49f), CornerRadius(7f))
            drawRoundRect(ink, Offset(247f, 164f), Size(159f, 46f), CornerRadius(4f))
            repeat(14) { i ->
                val x = 253f + i * 11f
                drawRect(steel, Offset(x, 155f), Size(5f, 9f))
                drawRect(steel, Offset(x, 210f), Size(5f, 9f))
            }
            repeat(14) { i ->
                val x = 221f + i * 16f
                drawRect(ink, Offset(x, 34f), Size(13f, 22f))
                drawCircle(gold, 3f, Offset(x + 6f, 45f))
                hardwareLabel((13 - i).toString(), x + 6f, 75f, Color.White, 11f)
            }
            repeat(6) { i ->
                val x = 350f + i * 16f
                drawRect(ink, Offset(x, 254f), Size(13f, 22f))
                drawCircle(gold, 3f, Offset(x + 6f, 265f))
                hardwareLabel("A$i", x + 6f, 245f, Color.White, 11f)
            }
            repeat(8) { i ->
                val x = 199f + i * 16f
                drawRect(ink, Offset(x, 254f), Size(13f, 22f))
                drawCircle(gold, 3f, Offset(x + 6f, 265f))
            }
            repeat(3) { i ->
                drawCircle(steel, 13f, Offset(195f + i * 29f, 220f))
                drawLine(ink, Offset(186f + i * 29f, 220f), Offset(204f + i * 29f, 220f), 2f)
            }
            drawRoundRect(steel, Offset(213f, 107f), Size(54f, 22f), CornerRadius(10f))
            drawCircle(Color(0xFFE4CF61), 5f, Offset(412f, 107f))
            drawCircle(Color(0xFF76D896), 5f, Offset(430f, 107f))
            hardwareLabel("ATmega328P", 326f, 192f, com.electrokit.app.ui.theme.DrawingMetal, 12f)
            hardwareLabel("16 MHz", 240f, 140f, Color(0xFFC8E3DC), 10f)
            hardwareLabel("UNO • R3", 365f, 137f, Color.White, 23f)
        }
    }
}

/**
 * Places a sharp vector label using the illustration's density-independent design coordinates.
 *
 * @param text Contact number or hardware identification.
 * @param x Horizontal centre in design coordinates.
 * @param y Baseline in design coordinates.
 * @param color Foreground chosen for its local physical surface.
 * @param fontSize Text size in design coordinates.
 * @return Unit; native canvas failures propagate to the caller.
 */
private fun DrawScope.hardwareLabel(
    text: String,
    x: Float,
    y: Float,
    color: Color,
    fontSize: Float = 18f,
) {
    drawContext.canvas.nativeCanvas.drawText(
        text,
        x,
        y,
        android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color.toArgb()
            textSize = fontSize
            textAlign = android.graphics.Paint.Align.CENTER
            typeface =
                android.graphics.Typeface.create(
                    android.graphics.Typeface.SANS_SERIF,
                    android.graphics.Typeface.BOLD,
                )
        },
    )
}
