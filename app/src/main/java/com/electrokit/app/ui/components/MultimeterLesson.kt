package com.electrokit.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp

/** Interactive generic meter illustration with explicit mode-dependent jack and connection notes.
 * @return Unit; this is an educational drawing, never a model-specific meter operating instruction.
 */
@Composable
fun MultimeterLesson() {
    var mode by remember { mutableIntStateOf(0) }
    val labels = listOf("DC volts", "AC volts", "Resistance", "Current")
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            labels.forEachIndexed { i, label -> FilterChip(mode == i, { mode = i }, label = { Text(label) }) }
        }
        Canvas(Modifier.fillMaxWidth().height(300.dp)) {
            val w = size.width; val h = size.height
            drawRoundRect(Color(0xFFF59E0B), Offset(w * .2f, 4f), Size(w * .6f, h - 8f), androidx.compose.ui.geometry.CornerRadius(28f))
            drawRoundRect(Color(0xFF172033), Offset(w * .23f, h * .03f), Size(w * .54f, h * .92f), androidx.compose.ui.geometry.CornerRadius(20f))
            drawRect(Color(0xFFCADAC8), Offset(w * .29f, h * .09f), Size(w * .42f, h * .2f))
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.BLACK; textSize = w * .055f; textAlign = android.graphics.Paint.Align.CENTER }
            drawContext.canvas.nativeCanvas.drawText(if (mode == 2) "Ω" else if (mode == 3) "A" else "V", w * .5f, h * .22f, paint)
            drawCircle(Color(0xFF42536A), h * .14f, Offset(w * .5f, h * .49f))
            val angle = (-120.0 + mode * 80) * Math.PI / 180
            drawLine(Color.White, Offset(w * .5f, h * .49f), Offset(w * .5f + kotlin.math.cos(angle).toFloat() * h * .1f, h * .49f + kotlin.math.sin(angle).toFloat() * h * .1f), 8f)
            paint.color = android.graphics.Color.WHITE; paint.textSize = w * .035f
            val xs = listOf(.34f, .5f, .66f)
            listOf("A / mA", "COM", "V / Ω").forEachIndexed { i, label ->
                drawCircle(if (i == 1) Color.Black else Color(0xFFEF4444), h * .035f, Offset(w * xs[i], h * .79f))
                drawCircle(Color.White, h * .035f, Offset(w * xs[i], h * .79f), style = Stroke(2f))
                drawContext.canvas.nativeCanvas.drawText(label, w * xs[i], h * .72f, paint)
            }
            drawLine(Color(0xFF07101E), Offset(w * .5f, h * .79f), Offset(w * .44f, h), 7f)
            drawLine(Color(0xFFEF4444), Offset(w * if (mode == 3) .34f else .66f, h * .79f), Offset(w * .9f, h * .98f), 7f)
        }
        Text("Generic ${labels[mode].lowercase()} illustration", style = MaterialTheme.typography.titleMedium)
        Text(when(mode) {
            2 -> "COM + V/Ω jack • power OFF, isolated and discharged • measure across the isolated part."
            3 -> "COM + correctly rated FUSED current jack • break the low-voltage circuit and insert the meter IN SERIES. Never across a source."
            else -> "COM + V/Ω jack • measure voltage IN PARALLEL. DC battery demonstration only; AC mains work requires a qualified procedure."
        })
        Text("Dial positions, current jack arrangements and ranges are illustrative. Match the real meter's labels and manual.", style = MaterialTheme.typography.bodySmall)
    }
}
