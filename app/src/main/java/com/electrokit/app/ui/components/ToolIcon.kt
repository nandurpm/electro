package com.electrokit.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import kotlin.math.*

/**
 * Draws an original per-tool circuit motif at current density without raster resources.
 *
 * @param key Stable data key rather than a translated label.
 * @return Unit; rendering effects and state changes are delivered through callbacks or observable
 *   state. Rendering/callback failures propagate; recoverable persistence failures belong to the
 *   ViewModel.
 */
@Composable
fun ToolIcon(key: String) {
    val light = MaterialTheme.colorScheme.surface.luminance() > .5f
    Canvas(Modifier.size(36.dp)) {
        val w = size.width
        val h = size.height
        val n = key.drop(1).toIntOrNull() ?: 1
        val ink =
            Color.hsv((n * 137.5f) % 360f, if (light) .65f else .38f, if (light) .55f else .9f)
        val gold = if (light) Color(0xFF926000) else Color(0xFFFFCE79)
        /**
         * Converts normalised icon coordinates into canvas proportions.
         *
         * @param x Coordinate in this renderer’s documented canvas space.
         * @param y Coordinate in this renderer’s documented canvas space.
         * @return A pixel-space Offset. Rendering/callback failures propagate; recoverable
         *   persistence failures belong to the ViewModel.
         */
        fun p(x: Float, y: Float) = Offset(w * x, h * y)
        /**
         * Shares stroke styling between connected schematic segments so visual joins stay
         * consistent.
         *
         * @param x Coordinate in this renderer’s documented canvas space.
         * @param y Coordinate in this renderer’s documented canvas space.
         * @param a First endpoint or branch scalar in the enclosing drawing/equation.
         * @param b Second endpoint or branch scalar in the enclosing drawing/equation.
         * @return Unit; rendering effects and state changes are delivered through callbacks or
         *   observable state. Rendering/callback failures propagate; recoverable persistence
         *   failures belong to the ViewModel.
         */
        fun line(x: Float, y: Float, a: Float, b: Float) =
            drawLine(ink, p(x, y), p(a, b), 2.dp.toPx(), StrokeCap.Round)
        /**
         * Draws a circuit annotation as vectors at the current canvas size.
         *
         * @param t Short visible symbol or component annotation.
         * @return Unit; rendering effects and state changes are delivered through callbacks or
         *   observable state. Rendering/callback failures propagate; recoverable persistence
         *   failures belong to the ViewModel.
         */
        fun text(t: String) {
            drawContext.canvas.nativeCanvas.drawText(
                t,
                w / 2,
                h * .66f,
                android.graphics.Paint(3).apply {
                    color = ink.toArgb()
                    textSize = if (t.length > 2) 14 * density else 23 * density
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface =
                        android.graphics.Typeface.create(
                            "sans-serif-medium",
                            android.graphics.Typeface.NORMAL,
                        )
                },
            )
        }
        val kind =
            if (key.startsWith("c")) n
            else if (key.startsWith("p")) 100 else if (key.startsWith("m")) 14 else (n % 5) + 100
        when (kind) {
            7 -> {
                line(.5f, .02f, .5f, .15f)
                drawRect(ink, p(.4f, .15f), Size(w * .2f, h * .24f), style = Stroke(2.dp.toPx()))
                line(.5f, .39f, .5f, .58f)
                line(.5f, .48f, .95f, .48f)
                drawCircle(gold, 3.dp.toPx(), p(.5f, .48f))
                drawRect(ink, p(.4f, .58f), Size(w * .2f, h * .24f), style = Stroke(2.dp.toPx()))
                line(.5f, .82f, .5f, .97f)
            }
            13,
            28 -> {
                line(.15f, .8f, .4f, .55f)
                line(.55f, .65f, .8f, .4f)
                drawRoundRect(
                    gold,
                    p(.25f, .16f),
                    Size(w * .45f, h * .45f),
                    CornerRadius(w * .2f),
                    style = Stroke(2.dp.toPx()),
                )
                line(.72f, .2f, .9f, .05f)
                line(.8f, .3f, .98f, .15f)
            }

            1,
            9,
            25 -> {
                line(.02f, .5f, .22f, .5f)
                line(.78f, .5f, .98f, .5f)
                drawRoundRect(
                    ink,
                    p(.22f, .3f),
                    Size(w * .56f, h * .4f),
                    CornerRadius(4f),
                    style = Stroke(2.dp.toPx()),
                )
                repeat(3) { i ->
                    drawLine(
                        if (i == 2) gold else ink,
                        p(.34f + i * .14f, .32f),
                        p(.34f + i * .14f, .68f),
                        3.dp.toPx(),
                    )
                }
            }
            3,
            18 -> {
                val path = Path()
                for (i in 0..90) {
                    val t = i / 90f
                    val x = w * (.08f + t * .84f)
                    val y = h * (.5f - .2f * sin(t * 6 * PI).toFloat())
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, ink, style = Stroke(2.dp.toPx()))
            }
            10,
            11,
            101 -> {
                line(.05f, .5f, .4f, .5f)
                line(.4f, .2f, .4f, .8f)
                line(.6f, .2f, .6f, .8f)
                line(.6f, .5f, .95f, .5f)
            }
            5,
            6,
            22,
            31 -> {
                line(.12f, .85f, .95f, .85f)
                line(.12f, .85f, .12f, .12f)
                val path = Path()
                for (i in 0..80) {
                    val t = i / 80f
                    val y =
                        if (kind == 6) .7f - .6f * (1 - exp(-5 * t))
                        else .45f - .25f * sin(2 * PI * t).toFloat()
                    if (i == 0) path.moveTo(w * (.15f + t * .8f), h * y)
                    else path.lineTo(w * (.15f + t * .8f), h * y)
                }
                drawPath(path, ink, style = Stroke(2.dp.toPx()))
            }
            12 -> {
                val path =
                    Path().apply {
                        moveTo(w * .2f, h * .15f)
                        lineTo(w * .82f, h * .5f)
                        lineTo(w * .2f, h * .85f)
                        close()
                    }
                drawPath(path, ink, style = Stroke(2.dp.toPx()))
                line(.02f, .35f, .2f, .35f)
                line(.02f, .65f, .2f, .65f)
                line(.82f, .5f, .98f, .5f)
            }
            17 -> {
                drawRoundRect(
                    ink,
                    p(.2f, .2f),
                    Size(w * .6f, h * .65f),
                    CornerRadius(4f),
                    style = Stroke(2.dp.toPx()),
                )
                line(.4f, .12f, .6f, .12f)
                line(.35f, .4f, .65f, .4f)
                line(.5f, .28f, .5f, .52f)
                line(.35f, .68f, .65f, .68f)
            }
            4 -> text("Ω")
            8 -> text("R₂/R₁")
            15 -> text("555")
            21 -> text("P")
            24 -> text("ƒ(x)")
            29 -> text("dB")
            30 -> text("⇄")
            else -> {
                drawRoundRect(
                    ink,
                    p(.15f, .23f),
                    Size(w * .7f, h * .54f),
                    CornerRadius(5f),
                    style = Stroke(2.dp.toPx()),
                )
                repeat(3 + n % 2) { i ->
                    val x = .25f + i * .16f
                    drawCircle(gold, 2.dp.toPx(), p(x, .5f))
                    if (kind < 100) {
                        line(x, .08f, x, .23f)
                        line(x, .77f, x, .93f)
                    }
                }
            }
        }
    }
}
