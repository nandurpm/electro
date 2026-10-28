package com.electrokit.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.electrokit.app.R
import com.electrokit.app.data.Diagram
import kotlin.math.*

/**
 * Band-index palette keeps digit, multiplier and tolerance colours identical in drawings and
 * selectors.
 */
val BandColours =
    listOf(
            0xFF151515,
            0xFF754123,
            0xFFE52D27,
            0xFFF57C00,
            0xFFFFEB3B,
            0xFF18943B,
            0xFF245BCD,
            0xFF8B36A5,
            0xFF999999,
            0xFFF5F5F5,
            0xFFD4AF37,
            0xFFC0C0C0,
        )
        .map { Color(it) }

/**
 * Draws the original brand/reference motif while allowing decorative callers to suppress redundant
 * semantics.
 *
 * @param key Stable data key rather than a translated label.
 * @param modifier Caller-owned layout and semantics constraints.
 * @param description Optional original module summary, or icon accessibility text at the drawing
 *   entry point.
 * @return Unit; rendering effects and state changes are delivered through callbacks or observable
 *   state. Rendering/callback failures propagate; recoverable persistence failures belong to the
 *   ViewModel.
 */
@Composable
fun EntryIcon(key: String, modifier: Modifier = Modifier, description: String? = null) {
    Canvas(modifier.size(36.dp).semantics { description?.let { contentDescription = it } }) {
        val w = size.width
        val h = size.height
        val index = key.drop(1).toIntOrNull() ?: 1
        val col = Color.hsv((index * 137.5f) % 360, 0.43f, 0.95f)
        /**
         * Shares stroke styling between connected schematic segments so visual joins stay
         * consistent.
         *
         * @param a First endpoint or branch scalar in the enclosing drawing/equation.
         * @param b Second endpoint or branch scalar in the enclosing drawing/equation.
         * @return Unit; rendering effects and state changes are delivered through callbacks or
         *   observable state. Rendering/callback failures propagate; recoverable persistence
         *   failures belong to the ViewModel.
         */
        fun line(a: Offset, b: Offset) = drawLine(col, a, b, 2.dp.toPx(), StrokeCap.Round)
        // Each key has its own circuit path and contact pattern, drawn as vectors.
        if (key.startsWith("c")) {
            line(Offset(0f, h / 2), Offset(w * .22f, h / 2))
            line(Offset(w * .78f, h / 2), Offset(w, h / 2))
            drawRoundRect(
                col,
                Offset(w * .22f, h * .28f),
                Size(w * .56f, h * .44f),
                CornerRadius(3f),
                style = Stroke(2.dp.toPx()),
            )
            repeat(1 + index % 4) { j ->
                drawLine(
                    col,
                    Offset(w * (.3f + j * .13f), h * .3f),
                    Offset(w * (.3f + j * .13f), h * .7f),
                    1.dp.toPx(),
                )
            }
        } else {
            drawRoundRect(
                col,
                Offset(w * .1f, h * .18f),
                Size(w * .8f, h * .64f),
                CornerRadius(if (key.startsWith("p")) 7f else 2f),
                style = Stroke(2.dp.toPx()),
            )
            repeat(2 + index % 5) { j ->
                val x = w * (.2f + j * (.6f / (1 + index % 5)))
                drawCircle(col, 2.dp.toPx(), Offset(x, h * .5f))
            }
        }
        drawCircle(col, 2.dp.toPx(), Offset(w * .12f, h * (.07f + (index % 4) * .03f)))
    }
}

/**
 * Updates colour bands on an original resistor or coil drawing from the current input state.
 *
 * @param bands Current colour indices in drawing order.
 * @param inductor True draws a coil whose encoded value is in microhenries.
 * @return Unit; rendering effects and state changes are delivered through callbacks or observable
 *   state. Rendering/callback failures propagate; recoverable persistence failures belong to the
 *   ViewModel.
 */
@Composable
fun ResistorDrawing(bands: List<Int>, inductor: Boolean = false) {
    val unknown = stringResource(R.string.unknown_colour)
    val componentName = stringResource(if (inductor) R.string.inductor else R.string.resistor)
    val description =
        stringResource(
            R.string.bands_accessibility,
            componentName,
            bands.joinToString {
                com.electrokit.app.domain.Resistors.names.getOrElse(it) { unknown }
            },
        )
    Canvas(Modifier.fillMaxWidth().height(150.dp).semantics { contentDescription = description }) {
        val w = size.width
        val h = size.height
        val y = h / 2
        drawLine(
            Brush.verticalGradient(listOf(Color.DarkGray, Color.LightGray, Color.Gray)),
            Offset(w * .08f, y),
            Offset(w * .92f, y),
            12.dp.toPx(),
        )
        if (inductor) {
            val path = Path()
            path.moveTo(w * .22f, y)
            for (i in 0..240) {
                val t = i / 240f
                path.lineTo(w * (.22f + .56f * t), y + sin(t * 8 * PI).toFloat() * h * .2f)
            }
            drawPath(path, Color(0xFFBCA75C), style = Stroke(9.dp.toPx()))
        } else {
            val path =
                Path().apply {
                    moveTo(w * .22f, y)
                    cubicTo(w * .18f, h * .05f, w * .32f, h * .08f, w * .38f, h * .3f)
                    lineTo(w * .62f, h * .3f)
                    cubicTo(w * .68f, h * .08f, w * .82f, h * .05f, w * .78f, y)
                    cubicTo(w * .82f, h * .95f, w * .68f, h * .92f, w * .62f, h * .7f)
                    lineTo(w * .38f, h * .7f)
                    cubicTo(w * .32f, h * .92f, w * .18f, h * .95f, w * .22f, y)
                    close()
                }
            drawPath(
                path,
                Brush.verticalGradient(
                    listOf(Color(0xFF9C9273), Color(0xFFE8DFBA), Color(0xFFB4A784))
                ),
            )
        }
        bands.forEachIndexed { i, c ->
            val x = w * (.37f + i * (.27f / (bands.size - 1)))
            drawRect(BandColours[c], Offset(x, h * .3f), Size(w * .025f, h * .4f))
        }
    }
}

/**
 * Draws normalised response samples in a bounded chart and avoids scaling from an empty sample set.
 *
 * @param points Normalised samples ordered along the horizontal axis.
 * @return Unit; rendering effects and state changes are delivered through callbacks or observable
 *   state. Rendering/callback failures propagate; recoverable persistence failures belong to the
 *   ViewModel.
 */
@Composable
fun ResponsePlot(points: List<Double>) {
    val description = stringResource(R.string.response_accessibility)
    if (points.isEmpty()) return
    Canvas(
        Modifier.fillMaxWidth().height(145.dp).padding(12.dp).semantics {
            contentDescription = description
        }
    ) {
        val w = size.width
        val h = size.height
        repeat(5) { i ->
            drawLine(Color.Gray.copy(alpha = .3f), Offset(0f, h * i / 4), Offset(w, h * i / 4), 1f)
        }
        val p = Path()
        points.forEachIndexed { i, y ->
            val x = i * w / (points.size - 1)
            val py = h * (1 - y.coerceIn(0.0, 1.0).toFloat())
            if (i == 0) p.moveTo(x, py) else p.lineTo(x, py)
        }
        drawPath(p, Color(0xFF8BC34A), style = Stroke(3.dp.toPx()))
    }
}

/**
 * Renders JSON identification geometry; contact labels do not certify a mechanical footprint or
 * mating view.
 *
 * @param diagram Decoded shape, caption and contact geometry.
 * @param modifier Caller-owned layout and semantics constraints.
 * @return Unit; rendering effects and state changes are delivered through callbacks or observable
 *   state. Rendering/callback failures propagate; recoverable persistence failures belong to the
 *   ViewModel.
 */
@Composable
fun DiagramView(diagram: Diagram, modifier: Modifier = Modifier) {
    if (illustratedConnector(diagram, modifier)) return
    val nodeTemplate = stringResource(R.string.node_label)
    val fg = Color(0xFFE1EAF0)
    Column(
        modifier
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF14252D))
            .border(1.dp, Color(0xFF35505B), RoundedCornerShape(20.dp))
    ) {
        Canvas(
            Modifier.fillMaxWidth()
                .height(
                    if (
                        diagram.shape == "usbA" ||
                            diagram.shape.contains("Mini") ||
                            diagram.shape.contains("Micro")
                    )
                        100.dp
                    else
                        maxOf(170, diagram.pins.map { it.y }.distinct().size * 32 + 32)
                            .coerceAtMost(1000)
                            .dp
                )
                .padding(12.dp)
                .semantics {
                    contentDescription =
                        diagram.caption + ". " + diagram.pins.joinToString { it.label }
                }
        ) {
            val w = size.width
            val h = size.height
            for (x in 0..(w / 24.dp.toPx()).toInt()) drawLine(
                Color(0xFF25404A).copy(alpha = .45f),
                Offset(x * 24.dp.toPx(), 0f),
                Offset(x * 24.dp.toPx(), h),
                1f,
            )
            for (y in 0..(h / 24.dp.toPx()).toInt()) drawLine(
                Color(0xFF25404A).copy(alpha = .45f),
                Offset(0f, y * 24.dp.toPx()),
                Offset(w, y * 24.dp.toPx()),
                1f,
            )
            /**
             * Shares stroke styling between connected schematic segments so visual joins stay
             * consistent.
             *
             * @param a First endpoint or branch scalar in the enclosing drawing/equation.
             * @param b Second endpoint or branch scalar in the enclosing drawing/equation.
             * @param width Canvas stroke width used to keep schematic connections visually
             *   consistent.
             * @return Unit; rendering effects and state changes are delivered through callbacks or
             *   observable state. Rendering/callback failures propagate; recoverable persistence
             *   failures belong to the ViewModel.
             */
            fun line(a: Offset, b: Offset, width: Float = 3f) = drawLine(fg, a, b, width)
            /**
             * Places vector annotations in the drawing coordinate system with explicit foreground
             * sizing.
             *
             * @param text Input or output text as described above; treated as data, never executed.
             * @param x Coordinate in this renderer’s documented canvas space.
             * @param y Coordinate in this renderer’s documented canvas space.
             * @param color Annotation foreground chosen for contrast against the technical drawing
             *   panel.
             * @param sz Base annotation text size converted using display density.
             * @return Unit; rendering effects and state changes are delivered through callbacks or
             *   observable state. Rendering/callback failures propagate; recoverable persistence
             *   failures belong to the ViewModel.
             */
            fun label(text: String, x: Float, y: Float, color: Color = fg, sz: Float = 14f) {
                drawContext.canvas.nativeCanvas.drawText(
                    text,
                    x,
                    y,
                    android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                        this.color = color.toArgb()
                        textSize = sz.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                    },
                )
            }
            if (diagram.shape == "bus") {
                for (j in 0..1) drawLine(
                    if (j == 0) Color(0xFFE0C840) else Color(0xFF5FC4E0),
                    Offset(w * .08f, h * (.35f + j * .16f)),
                    Offset(w * .92f, h * (.35f + j * .16f)),
                    3f,
                )
                repeat(3) { i ->
                    val x = w * (.2f + i * .3f)
                    val xa = x - w * .018f
                    val xb = x + w * .018f
                    line(Offset(xa, h * .35f), Offset(xa, h * .7f))
                    line(Offset(xb, h * .51f), Offset(xb, h * .7f))
                    drawCircle(Color(0xFFE0C840), 3 * density, Offset(xa, h * .35f))
                    drawCircle(Color(0xFF5FC4E0), 3 * density, Offset(xb, h * .51f))
                    drawRect(
                        fg,
                        Offset(x - w * .085f, h * .7f),
                        Size(w * .17f, h * .17f),
                        style = Stroke(2f),
                    )
                    label(
                        nodeTemplate.format(java.util.Locale.getDefault(), i + 1),
                        x,
                        h * .98f,
                        sz = 11f,
                    )
                }
                for (x in listOf(w * .08f, w * .92f)) {
                    line(Offset(x, h * .35f), Offset(x, h * .39f))
                    drawRect(
                        fg,
                        Offset(x - 3 * density, h * .39f),
                        Size(6 * density, h * .08f),
                        style = Stroke(2f),
                    )
                    line(Offset(x, h * .47f), Offset(x, h * .51f))
                    label("Rₜ", x, h * .2f)
                }
            } else if (diagram.shape.startsWith("switch:")) {
                val type = diagram.shape.substringAfter(':')
                /**
                 * Distinguishes an electrical contact from the connector outline.
                 *
                 * @param x Coordinate in this renderer’s documented canvas space.
                 * @param y Coordinate in this renderer’s documented canvas space.
                 * @return Unit; rendering effects and state changes are delivered through callbacks
                 *   or observable state. Rendering/callback failures propagate; recoverable
                 *   persistence failures belong to the ViewModel.
                 */
                fun contact(x: Float, y: Float) {
                    drawCircle(fg, 4 * density, Offset(w * x, h * y), style = Stroke(2f))
                }
                /**
                 * Draws a schematic connection between the supplied normalised endpoints.
                 *
                 * @param x1 Normalised horizontal start coordinate, scaled by this canvas width.
                 * @param y1 Normalised vertical start coordinate, scaled by this canvas height.
                 * @param x2 Normalised horizontal end coordinate, scaled by this canvas width.
                 * @param y2 Normalised vertical end coordinate, scaled by this canvas height.
                 * @return Unit; rendering effects and state changes are delivered through callbacks
                 *   or observable state. Rendering/callback failures propagate; recoverable
                 *   persistence failures belong to the ViewModel.
                 */
                fun segment(x1: Float, y1: Float, x2: Float, y2: Float) =
                    line(Offset(w * x1, h * y1), Offset(w * x2, h * y2))
                /**
                 * Reuses switch-pole geometry for the alternative-contact diagrams.
                 *
                 * @param y Coordinate in this renderer’s documented canvas space.
                 * @return Unit; rendering effects and state changes are delivered through callbacks
                 *   or observable state. Rendering/callback failures propagate; recoverable
                 *   persistence failures belong to the ViewModel.
                 */
                fun changeover(y: Float) {
                    contact(.3f, y)
                    contact(.7f, y - .13f)
                    contact(.7f, y + .13f)
                    segment(.08f, y, .3f, y)
                    segment(.3f, y, .66f, y - .13f)
                    segment(.7f, y - .13f, .92f, y - .13f)
                    segment(.7f, y + .13f, .92f, y + .13f)
                }
                when (type) {
                    "SPDT" -> changeover(.5f)
                    "DPDT" -> {
                        changeover(.28f)
                        changeover(.72f)
                        drawLine(
                            fg,
                            Offset(w * .48f, h * .2f),
                            Offset(w * .48f, h * .64f),
                            2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
                        )
                    }
                    "Rotary" -> {
                        contact(.4f, .5f)
                        segment(.1f, .5f, .4f, .5f)
                        repeat(5) { i ->
                            val angle = (-70 + i * 35) * PI / 180
                            val x = .4f + .32f * cos(angle).toFloat()
                            val y = .5f + .4f * sin(angle).toFloat()
                            contact(x, y)
                        }
                        segment(.4f, .5f, .7f, .5f)
                    }
                    "PushNO",
                    "PushNC" -> {
                        contact(.3f, .6f)
                        contact(.7f, .6f)
                        segment(.1f, .6f, .3f, .6f)
                        segment(.7f, .6f, .9f, .6f)
                        val yy = if (type == "PushNC") .6f else .4f
                        segment(.25f, yy, .75f, yy)
                        segment(.5f, .12f, .5f, yy)
                        segment(.38f, .12f, .62f, .12f)
                    }
                    else -> {
                        contact(.3f, .6f)
                        contact(.7f, .6f)
                        segment(.1f, .6f, .3f, .6f)
                        segment(.7f, .6f, .9f, .6f)
                        segment(.3f, .6f, .68f, .3f)
                    }
                }
            } else if (diagram.shape.startsWith("symbol:") || diagram.shape.startsWith("logic:")) {
                val s = diagram.shape.substringAfter(':')
                val a = w * .3f
                val b = w * .7f
                val y = h * .5f
                line(Offset(w * .1f, y), Offset(a, y))
                line(Offset(b, y), Offset(w * .9f, y))
                when (s) {
                    "capacitor" -> {
                        line(Offset(w * .47f, h * .25f), Offset(w * .47f, h * .75f))
                        line(Offset(w * .53f, h * .25f), Offset(w * .53f, h * .75f))
                        line(Offset(a, y), Offset(w * .47f, y))
                        line(Offset(w * .53f, y), Offset(b, y))
                    }
                    "ground" -> {
                        line(Offset(w * .5f, h * .2f), Offset(w * .5f, y))
                        repeat(3) { i ->
                            val d = w * (.18f - i * .05f)
                            line(
                                Offset(w / 2 - d, y + i * h * .1f),
                                Offset(w / 2 + d, y + i * h * .1f),
                            )
                        }
                    }
                    "switch" -> {
                        drawCircle(fg, 5f, Offset(a, y))
                        drawCircle(fg, 5f, Offset(b, y))
                        line(Offset(a, y), Offset(b, h * .25f))
                    }
                    "op_amp" -> {
                        val p =
                            Path().apply {
                                moveTo(a, h * .15f)
                                lineTo(b, y)
                                lineTo(a, h * .85f)
                                close()
                            }
                        drawPath(p, fg, style = Stroke(3f))
                        label("+", a + w * .05f, h * .38f)
                        label("−", a + w * .05f, h * .7f)
                    }
                    "inductor" -> {
                        val p = Path()
                        for (i in 0..100) {
                            val t = i / 100f
                            val x = a + (b - a) * t
                            val yy = y - sin(t * 4 * PI).toFloat() * h * .18f
                            if (i == 0) p.moveTo(x, yy) else p.lineTo(x, yy)
                        }
                        drawPath(p, fg, style = Stroke(3f))
                    }
                    "diode",
                    "LED" -> {
                        val p =
                            Path().apply {
                                moveTo(a, h * .25f)
                                lineTo(b, y)
                                lineTo(a, h * .75f)
                                close()
                            }
                        drawPath(p, fg, style = Stroke(3f))
                        line(Offset(b, h * .25f), Offset(b, h * .75f))
                        if (s == "LED") {
                            line(Offset(w * .6f, h * .2f), Offset(w * .7f, h * .08f))
                            label("↗", w * .75f, h * .2f)
                        }
                    }
                    "battery" -> {
                        line(Offset(w * .45f, h * .15f), Offset(w * .45f, h * .85f))
                        line(Offset(w * .55f, h * .3f), Offset(w * .55f, h * .7f))
                        line(Offset(a, y), Offset(w * .45f, y))
                        line(Offset(w * .55f, y), Offset(b, y))
                        label("+", w * .4f, h * .15f)
                    }
                    "voltage_source",
                    "current_source" -> {
                        drawCircle(fg, h * .3f, Offset(w / 2, y), style = Stroke(3f))
                        label(if (s == "voltage_source") "+ / −" else "↑", w / 2, y + 5 * density)
                    }
                    else -> {
                        drawRoundRect(
                            fg,
                            Offset(a, h * .2f),
                            Size(b - a, h * .6f),
                            CornerRadius(if (diagram.shape.startsWith("logic:")) h * .1f else 0f),
                            style = Stroke(3f),
                        )
                        label(
                            when (s) {
                                "AND",
                                "NAND" -> "&"
                                "OR",
                                "NOR" -> "≥1"
                                "XOR",
                                "XNOR" -> "=1"
                                "NOT" -> "1"
                                "resistor" -> "R"
                                "fuse" -> "F"
                                else -> s
                            },
                            w / 2,
                            y + 6 * density,
                        )
                        if (s in listOf("NAND", "NOR", "XNOR", "NOT"))
                            drawCircle(
                                fg,
                                6 * density,
                                Offset(b + 6 * density, y),
                                style = Stroke(2f),
                            )
                    }
                }
            } else if (diagram.shape.startsWith("usb")) {
                val kind = diagram.shape
                val bw = if (kind == "usbB") minOf(w * .88f, h * .7f) else w * .88f
                val x = (w - bw) / 2
                val y = h * .12f
                val bh = h * .7f
                val shell = Color(0xFFDDDCD5)
                val inner = Color(0xFF858585)
                val bevel = kind.contains("B") || kind.contains("Mini") || kind.contains("Micro")
                val outer =
                    Path().apply {
                        if (kind == "usbB") {
                            moveTo(x + bw * .18f, y)
                            lineTo(x + bw * .82f, y)
                            lineTo(x + bw, y + bh * .18f)
                            lineTo(x + bw, y + bh)
                            lineTo(x, y + bh)
                            lineTo(x, y + bh * .18f)
                            close()
                        } else {
                            moveTo(x + bw * .1f, y)
                            lineTo(x + bw * .9f, y)
                            lineTo(x + bw, y + bh * .2f)
                            lineTo(x + bw * .94f, y + bh * .78f)
                            lineTo(x + bw * .8f, y + bh)
                            lineTo(x + bw * .2f, y + bh)
                            lineTo(x + bw * .06f, y + bh * .78f)
                            lineTo(x, y + bh * .2f)
                            close()
                        }
                    }
                if (bevel)
                    drawPath(
                        outer,
                        Brush.verticalGradient(listOf(Color.White, shell, Color(0xFF8A979D))),
                    )
                else
                    drawRoundRect(
                        Brush.verticalGradient(listOf(Color.White, shell, Color(0xFF8A979D))),
                        Offset(x, y),
                        Size(bw, bh),
                        CornerRadius(if (kind.contains("C")) bh / 2 else 14f),
                    )
                drawRoundRect(
                    Brush.verticalGradient(listOf(Color(0xFF35434A), inner)),
                    Offset(x + bw * .1f, y + bh * .15f),
                    Size(bw * .8f, bh * .7f),
                    CornerRadius(6f),
                )
                drawRoundRect(
                    if (kind.contains("3")) Color(0xFF0685BB) else Color(0xFFC0C0BA),
                    Offset(x + bw * .17f, y + bh * .25f),
                    Size(bw * .66f, bh * .5f),
                    CornerRadius(3f),
                )
                diagram.pins.forEach { pin ->
                    val px = x + bw * pin.x
                    val py = y + bh * pin.y
                    drawRect(
                        Color(0xFFFFD342),
                        Offset(px - 3 * density, py),
                        Size(6 * density, 4 * density),
                    )
                    val tagWidth = maxOf(18f, pin.label.length * 8f) * density
                    drawRoundRect(
                        Color(0xFF14252D),
                        Offset(px - tagWidth / 2, py - 20 * density),
                        Size(tagWidth, 17 * density),
                        CornerRadius(3 * density),
                    )
                    label(
                        pin.label,
                        px,
                        py - 4 * density,
                        Color(0xFFFFE082),
                        if (diagram.pins.size > 12) 8f else 12f,
                    )
                }
            } else {
                val x = w * .04f
                val y = h * .05f
                val rw = w * .92f
                val rh = h * .9f
                if (diagram.shape == "round")
                    drawOval(
                        Brush.verticalGradient(listOf(Color(0xFFCAD6DD), Color(0xFF526A78))),
                        Offset(x, y),
                        Size(rw, rh),
                    )
                else if (diagram.shape == "bevel") {
                    val path =
                        Path().apply {
                            moveTo(x + rw * .08f, y)
                            lineTo(x + rw * .92f, y)
                            lineTo(x + rw, y + rh * .25f)
                            lineTo(x + rw * .88f, y + rh)
                            lineTo(x + rw * .12f, y + rh)
                            lineTo(x, y + rh * .25f)
                            close()
                        }
                    drawPath(
                        path,
                        Brush.verticalGradient(listOf(Color(0xFF839EAC), Color(0xFF344F5D))),
                    )
                } else
                    drawRoundRect(
                        Brush.verticalGradient(listOf(Color(0xFF407C70), Color(0xFF224F4E))),
                        Offset(x, y),
                        Size(rw, rh),
                        CornerRadius(12f),
                        style = Fill,
                    )
                diagram.pins.forEach { pin ->
                    val px = x + rw * pin.x
                    val py = y + rh * pin.y
                    drawRoundRect(
                        Brush.verticalGradient(listOf(Color(0xFFFFE6A4), Color(0xFFBA852D))),
                        Offset(px - 4 * density, py),
                        Size(8 * density, 11 * density),
                        CornerRadius(1f),
                    )
                    label(
                        pin.label,
                        px,
                        py - 3 * density,
                        Color(0xFFFFD54F),
                        if (diagram.pins.size > 20) 10f else 14f,
                    )
                }
            }
        }
        Text(
            diagram.caption,
            color = Color(0xFFB5D2DF),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        )
    }
}
