@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.electrokit.app.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.electrokit.app.R
import com.electrokit.app.data.*
import kotlin.math.*

/**
 * Presents an original educational escalator side cutaway with actual selectable reference routes.
 *
 * @param data Technical records and explicitly approximate source-backed marker positions.
 * @param navigate Opens the selected component; no machine-control action is provided.
 * @return Unit. Legend buttons expose every marker to accessibility services; drawing failure
 *   propagates.
 */
@Composable
fun EscalatorCutaway(data: EngineeringContent, navigate: (String) -> Unit) {
    var enlarged by rememberSaveable { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.engine_cutaway),
                style = MaterialTheme.typography.titleMedium,
            )
            CutawayCanvas(data, false, navigate)
            Text(
                stringResource(R.string.engine_drawing_notice),
                style = MaterialTheme.typography.bodySmall,
            )
            TextButton({ enlarged = true }) { Text(stringResource(R.string.engine_enlarge)) }
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                data.markers.forEachIndexed { index, marker ->
                    val title = data.records.firstOrNull { it.id == marker.record }?.title.orEmpty()
                    OutlinedButton({ navigate("record:" + marker.record) }) {
                        Text("${index + 1} · " + title.substringBefore(" — "))
                    }
                }
            }
        }
    }
    if (enlarged)
        Dialog(
            onDismissRequest = { enlarged = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Column(
                Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(12.dp)
            ) {
                TextButton({ enlarged = false }) { Text(stringResource(R.string.engine_close)) }
                Text(
                    stringResource(R.string.engine_drawing_notice),
                    style = MaterialTheme.typography.bodySmall,
                )
                CutawayCanvas(data, true, navigate)
            }
        }
}

/**
 * Converts taps and pinch/pan gestures back into the exact common drawing coordinate space.
 *
 * @param data Validated approximate marker metadata.
 * @param interactive True enables zoom/pan inside the inspection dialog, leaving list scrolling
 *   alone.
 * @param navigate Receives the selected record route.
 * @return Unit; scale/pan are bounded, and taps outside markers do nothing.
 */
@Composable
private fun CutawayCanvas(
    data: EngineeringContent,
    interactive: Boolean,
    navigate: (String) -> Unit,
) {
    var zoom by rememberSaveable { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    val description = stringResource(R.string.engine_cutaway)
    val markerDescription = stringResource(R.string.engine_marker, data.markers.size.toString())
    Column {
        if (interactive)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton({ zoom = (zoom * 1.3f).coerceAtMost(4f) }) {
                    Text(stringResource(R.string.engine_zoom_in))
                }
                OutlinedButton({ zoom = (zoom / 1.3f).coerceAtLeast(1f) }) {
                    Text(stringResource(R.string.engine_zoom_out))
                }
                TextButton({
                    zoom = 1f
                    pan = Offset.Zero
                }) {
                    Text(stringResource(R.string.engine_reset_view))
                }
            }
        Canvas(
            Modifier.fillMaxWidth()
                .height(if (interactive) 480.dp else 260.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .semantics { contentDescription = "$description. $markerDescription" }
                .then(
                    if (interactive)
                        Modifier.pointerInput(Unit) {
                            detectTransformGestures { _, change, scale, _ ->
                                zoom = (zoom * scale).coerceIn(1f, 4f)
                                pan =
                                    if (zoom == 1f) Offset.Zero
                                    else
                                        Offset(
                                            (pan.x + change.x).coerceIn(
                                                -size.width * (zoom - 1f),
                                                size.width * (zoom - 1f),
                                            ),
                                            (pan.y + change.y).coerceIn(
                                                -size.height * (zoom - 1f),
                                                size.height * (zoom - 1f),
                                            ),
                                        )
                            }
                        }
                    else Modifier
                )
                .pointerInput(data.markers, zoom, pan) {
                    detectTapGestures { tap ->
                        val base = min(size.width / 1000f, size.height / 560f)
                        val origin =
                            Offset(
                                (size.width - 1000f * base) / 2,
                                (size.height - 560f * base) / 2,
                            ) + pan
                        val point = (tap - origin) / (base * zoom)
                        data.markers
                            .minByOrNull { hypot(point.x - it.x, point.y - it.y) }
                            ?.takeIf { hypot(point.x - it.x, point.y - it.y) < 38f }
                            ?.let { navigate("record:" + it.record) }
                    }
                }
        ) {
            val base = min(size.width / 1000f, size.height / 560f)
            withTransform({
                translate(
                    (size.width - 1000f * base) / 2 + pan.x,
                    (size.height - 560f * base) / 2 + pan.y,
                )
                scale(base * zoom, base * zoom, Offset.Zero)
            }) {
                escalatorStructure()
                data.markers.forEachIndexed { index, marker ->
                    drawCircle(Color(0xFF164E40), 25f, Offset(marker.x, marker.y))
                    drawCircle(
                        Color(0xFFCAEDD9),
                        25f,
                        Offset(marker.x, marker.y),
                        style = Stroke(3f),
                    )
                    industrialLabel(
                        (index + 1).toString(),
                        marker.x,
                        marker.y + 8f,
                        Color.White,
                        23f,
                    )
                }
            }
        }
    }
}

/**
 * Builds a 1000×560 side-view cutaway without asserting construction dimensions or exact mounting.
 *
 * @return Unit; all proportions are illustrative and canvas failures propagate.
 */
private fun DrawScope.escalatorStructure() {
    val ink = Color(0xFF2D4A56)
    val steel =
        Brush.verticalGradient(listOf(Color(0xFFB9CDD5), Color(0xFF6C8E9C), com.electrokit.app.ui.theme.DrawingMetal))
    val truss =
        Path().apply {
            moveTo(80f, 420f)
            lineTo(230f, 420f)
            lineTo(750f, 180f)
            lineTo(930f, 180f)
            lineTo(930f, 295f)
            lineTo(790f, 295f)
            lineTo(270f, 530f)
            lineTo(80f, 530f)
            close()
        }
    drawPath(truss, steel)
    drawPath(truss, ink, style = Stroke(5f))
    // Truss bracing and the return path remain visible through the simplified cutaway.
    repeat(10) { i ->
        val x = 240f + i * 52f
        val y = 420f - i * 24f
        drawLine(ink.copy(alpha = .5f), Offset(x, y + 8f), Offset(x + 52f, y + 87f), 4f)
        drawLine(ink.copy(alpha = .5f), Offset(x, y + 110f), Offset(x + 52f, y - 17f), 4f)
    }
    val glass =
        Path().apply {
            moveTo(95f, 350f)
            lineTo(225f, 350f)
            lineTo(750f, 106f)
            lineTo(922f, 106f)
            lineTo(922f, 176f)
            lineTo(755f, 176f)
            lineTo(230f, 417f)
            lineTo(95f, 417f)
            close()
        }
    drawPath(
        glass,
        Brush.verticalGradient(
            listOf(Color(0xFF9BD9E0).copy(alpha = .65f), com.electrokit.app.ui.theme.DrawingMetal.copy(alpha = .7f))
        ),
    )
    drawPath(glass, Color(0xFF4A929E), style = Stroke(3f))
    val rail =
        Path().apply {
            moveTo(100f, 420f)
            cubicTo(25f, 415f, 25f, 345f, 100f, 340f)
            lineTo(225f, 340f)
            lineTo(750f, 96f)
            lineTo(915f, 96f)
            cubicTo(995f, 96f, 995f, 175f, 920f, 178f)
            lineTo(760f, 178f)
            lineTo(230f, 422f)
            close()
        }
    drawPath(rail, Color(0xFF223C45), style = Stroke(12f, cap = StrokeCap.Round))
    drawPath(rail, Color(0xFF5C7C83), style = Stroke(3f))
    // Stair treads transition between horizontal landings and the inclined run.
    repeat(25) { i ->
        val x = 225f + i * 21f
        val y = 419f - i * 9.7f
        drawRect(ink, Offset(x, y), Size(23f, 13f))
        drawLine(Color(0xFFF1D77F), Offset(x, y), Offset(x + 23f, y), 3f)
        repeat(4) { groove ->
            drawLine(
                Color(0xFF8EA7AE),
                Offset(x + 3 + groove * 5, y + 3),
                Offset(x + 3 + groove * 5, y + 10),
                1f,
            )
        }
    }
    for (x in listOf(140f, 850f)) {
        val y = if (x < 500) 480f else 245f
        drawCircle(ink, 37f, Offset(x, y))
        drawCircle(Color(0xFFC8D9DC), 28f, Offset(x, y))
        drawCircle(ink, 9f, Offset(x, y))
        repeat(8) { j ->
            val a = j * PI / 4
            drawLine(
                ink,
                Offset(x, y),
                Offset(x + cos(a).toFloat() * 27, y + sin(a).toFloat() * 27),
                3f,
            )
        }
    }
    drawLine(Color(0xFFD4AD4E), Offset(140f, 480f), Offset(850f, 245f), 6f)
    drawLine(Color(0xFF506E79), Offset(155f, 503f), Offset(860f, 267f), 5f)
    drawRoundRect(Color(0xFF477DA3), Offset(760f, 190f), Size(58f, 40f), CornerRadius(5f))
    repeat(7) { i ->
        drawLine(Color(0xFFA4C4D2), Offset(766f + i * 7, 193f), Offset(766f + i * 7, 226f), 2f)
    }
    drawRoundRect(Color(0xFF8CA6AC), Offset(825f, 198f), Size(40f, 35f), CornerRadius(5f))
    drawRect(Color(0xFF395860), Offset(885f, 195f), Size(35f, 62f))
    drawRect(Color(0xFFD2BE74), Offset(155f, 412f), Size(65f, 7f))
    drawRect(Color(0xFFD2BE74), Offset(752f, 181f), Size(65f, 7f))
}

/**
 * Draws recognisable motor, brake, controller, sensor and panel component identification artwork.
 *
 * @param kind Original renderer family; no manufacturer's construction details are implied.
 * @param label Source identifier drawn on the illustration.
 * @return Unit; the visible caption distinguishes artwork from approved wiring/layout data.
 */
@Composable
fun IndustrialFigure(kind: String, label: String) {
    val notice = stringResource(R.string.engine_illustrative)
    Card(Modifier.fillMaxWidth()) {
        Canvas(
            Modifier.fillMaxWidth().height(220.dp).semantics {
                contentDescription = "$label. $notice"
            }
        ) {
            val factor = min(size.width / 600f, size.height / 300f)
            withTransform({
                translate((size.width - 600f * factor) / 2, (size.height - 300f * factor) / 2)
                scale(factor, factor, Offset.Zero)
            }) {
                industrialBody(kind, label)
            }
        }
        Text(notice, Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
    }
}

/**
 * Uses a shared 600×300 design space for consistent, original equipment illustrations.
 *
 * @param kind Component renderer family; unknown kinds use the generic labelled electronics
 *   enclosure.
 * @param label Identifier rather than an invented rating or terminal assignment.
 * @return Unit; all geometry is illustrative and drawing errors propagate.
 */
private fun DrawScope.industrialBody(kind: String, label: String) {
    val ink = Color(0xFF213C4A)
    val metal =
        Brush.verticalGradient(
            listOf(com.electrokit.app.ui.theme.DrawingMetal, Color(0xFF8DA4B1), com.electrokit.app.ui.theme.DrawingMetal),
            50f,
            245f,
        )
    drawOval(ink.copy(alpha = .15f), Offset(110f, 252f), Size(380f, 20f))
    when (kind) {
        "motor" -> {
            drawRoundRect(ink, Offset(165f, 235f), Size(260f, 20f), CornerRadius(4f))
            drawRoundRect(
                Brush.verticalGradient(
                    listOf(Color(0xFF6CA8C5), Color(0xFF236B97), Color(0xFF163F61))
                ),
                Offset(175f, 88f),
                Size(235f, 140f),
                CornerRadius(22f),
            )
            repeat(17) { i ->
                drawLine(
                    Color(0xFFAFD2DF).copy(alpha = .65f),
                    Offset(193f + i * 12, 96f),
                    Offset(193f + i * 12, 218f),
                    3f,
                )
            }
            drawOval(metal, Offset(145f, 90f), Size(70f, 136f))
            drawOval(ink, Offset(159f, 115f), Size(40f, 86f))
            drawRoundRect(metal, Offset(76f, 145f), Size(105f, 25f), CornerRadius(4f))
            drawRoundRect(metal, Offset(244f, 53f), Size(95f, 39f), CornerRadius(6f))
            repeat(5) { i ->
                drawLine(ink, Offset(418f, 111f + i * 22), Offset(438f, 122f + i * 18), 5f)
            }
            drawRoundRect(Color(0xFFD6E1DD), Offset(258f, 132f), Size(113f, 43f), CornerRadius(4f))
            industrialLabel(label, 314f, 158f, ink, 17f)
        }
        "brake" -> {
            drawRoundRect(metal, Offset(105f, 140f), Size(390f, 25f), CornerRadius(7f))
            drawCircle(metal, 91f, Offset(300f, 150f))
            drawCircle(ink, 91f, Offset(300f, 150f), style = Stroke(4f))
            drawCircle(Color(0xFF506F7A), 34f, Offset(300f, 150f))
            repeat(8) { i ->
                val a = i * PI / 4
                drawCircle(
                    ink,
                    5f,
                    Offset(300f + cos(a).toFloat() * 66, 150f + sin(a).toFloat() * 66),
                )
            }
            for (x in listOf(191f, 359f)) {
                drawRoundRect(ink, Offset(x, 86f), Size(50f, 126f), CornerRadius(8f))
                drawRect(Color(0xFFBB7752), Offset(if (x < 300) x + 41 else x, 99f), Size(9f, 101f))
            }
            industrialLabel(label, 300f, 277f, ink, 20f)
        }
        "sensor" -> {
            drawRoundRect(ink, Offset(83f, 147f), Size(105f, 22f), CornerRadius(9f))
            drawRoundRect(metal, Offset(175f, 115f), Size(240f, 88f), CornerRadius(17f))
            repeat(19) { i ->
                drawLine(
                    Color(0xFF657F8A),
                    Offset(190f + i * 11, 120f),
                    Offset(190f + i * 11, 197f),
                    2f,
                )
            }
            drawOval(Color(0xFFDAA54C), Offset(382f, 116f), Size(48f, 86f))
            drawCircle(Color(0xFF77D3A9), 7f, Offset(212f, 143f))
            repeat(3) { i ->
                drawArc(
                    Color(0xFF329B9C),
                    -45f,
                    90f,
                    false,
                    Offset(435f + i * 19, 123f - i * 11),
                    Size(28f + i * 12, 72f + i * 22),
                    style = Stroke(3f),
                )
            }
            industrialLabel(label, 300f, 247f, ink, 20f)
        }
        "switch" -> {
            drawRoundRect(metal, Offset(205f, 128f), Size(180f, 110f), CornerRadius(10f))
            drawRoundRect(ink, Offset(232f, 142f), Size(126f, 76f), CornerRadius(7f))
            drawLine(ink, Offset(294f, 129f), Offset(377f, 63f), 12f)
            drawCircle(metal, 18f, Offset(382f, 60f))
            drawCircle(ink, 8f, Offset(382f, 60f))
            for (x in listOf(221f, 368f)) for (y in listOf(145f, 220f)) drawCircle(
                ink,
                5f,
                Offset(x, y),
            )
            industrialLabel(label, 295f, 185f, Color.White, 18f)
        }
        "transformer" -> {
            drawRoundRect(metal, Offset(145f, 70f), Size(310f, 164f), CornerRadius(8f))
            drawRect(ink, Offset(190f, 95f), Size(220f, 110f))
            for (x in listOf(200f, 330f)) {
                drawRoundRect(Color(0xFFC18149), Offset(x, 82f), Size(70f, 138f), CornerRadius(9f))
                repeat(12) { i ->
                    drawLine(
                        Color(0xFFF5BE76),
                        Offset(x + 4, 90f + i * 10),
                        Offset(x + 66, 90f + i * 10),
                        3f,
                    )
                }
            }
            industrialLabel(label, 300f, 270f, ink, 20f)
        }
        else -> {
            drawRoundRect(metal, Offset(170f, 46f), Size(260f, 195f), CornerRadius(13f))
            drawRoundRect(ink, Offset(186f, 85f), Size(228f, 108f), CornerRadius(7f))
            repeat(3) { i ->
                for (y in listOf(65f, 220f)) {
                    val x = 222f + i * 78
                    drawRoundRect(
                        Color(0xFF405B65),
                        Offset(x - 20, y - 11),
                        Size(40f, 22f),
                        CornerRadius(3f),
                    )
                    drawCircle(Color(0xFFC4D2D5), 7f, Offset(x, y))
                    drawLine(ink, Offset(x - 4, y), Offset(x + 4, y), 2f)
                }
            }
            if (kind == "contactor" || kind == "relay") {
                repeat(3) { i ->
                    drawLine(
                        Color(0xFFDBAF59),
                        Offset(220f + i * 76, 100f),
                        Offset(220f + i * 76, 176f),
                        4f,
                    )
                }
            } else
                repeat(7) { i ->
                    drawLine(
                        Color(0xFF69828E),
                        Offset(199f, 101f + i * 12),
                        Offset(260f, 101f + i * 12),
                        3f,
                    )
                }
            drawCircle(Color(0xFF79D6AE), 6f, Offset(390f, 104f))
            industrialLabel(label.take(22), 300f, 153f, Color.White, 19f)
        }
    }
}

/**
 * Places crisp original figure annotations in design coordinates, without fabricated component
 * ratings.
 *
 * @param text Technical identifier to draw.
 * @param x Text horizontal centre.
 * @param y Text baseline.
 * @param color Foreground chosen against its local illustration surface.
 * @param fontSize Design-space font size, scaled with the whole drawing.
 * @return Unit; native canvas errors propagate.
 */
private fun DrawScope.industrialLabel(
    text: String,
    x: Float,
    y: Float,
    color: Color,
    fontSize: Float,
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
