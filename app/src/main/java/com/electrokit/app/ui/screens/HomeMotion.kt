package com.electrokit.app.ui.screens

import android.animation.ValueAnimator
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect

/** Supplies a slow decorative phase only while resumed and system animations are enabled.
 * @return Animation state; callers read it in drawing/layer code, avoiding per-frame composition.
 */
@Composable
fun rememberHomeMotion(): State<Float> {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(false) }
    LifecycleResumeEffect(context) {
        enabled = if (Build.VERSION.SDK_INT >= 26) ValueAnimator.areAnimatorsEnabled()
            else Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
        onPauseOrDispose { enabled = false }
    }
    return if (enabled) rememberInfiniteTransition(label = "Home circuit flow").animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart),
        label = "Circuit energy")
    else remember { mutableFloatStateOf(.3f) }
}

/** Decorative circuit traces and a moving energy dot, hidden from accessibility by Canvas.
 * @param phase Lifecycle-aware motion phase read only while drawing.
 * @param modifier Caller-owned bounds.
 * @return Unit; artwork does not intercept input or represent a measured electrical value.
 */
@Composable
fun HomeCircuitBackdrop(phase: State<Float>, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) {
        val cyan = Color(0xFF5CDAFF)
        val step = 24.dp.toPx()
        for (x in 0..(size.width / step).toInt()) for (y in 0..(size.height / step).toInt()) {
            drawCircle(cyan.copy(alpha = .08f), .8.dp.toPx(), Offset(x * step, y * step))
        }
        for (i in 0..3) {
            val y = size.height * (.18f + i * .19f)
            val start = size.width * .62f
            val bend = size.width * (.73f + i * .035f)
            val path = Path().apply {
                moveTo(start, y); lineTo(bend, y)
                lineTo(bend + 22.dp.toPx(), y - 22.dp.toPx()); lineTo(size.width, y - 22.dp.toPx())
            }
            drawPath(path, cyan.copy(alpha = .18f), style = Stroke(1.dp.toPx()))
            drawCircle(cyan.copy(alpha = .38f), 3.dp.toPx(), Offset(start, y), style = Stroke(1.dp.toPx()))
            val progress = (phase.value + i * .25f) % 1f
            val dot = Offset(bend + 22.dp.toPx() + (size.width - bend - 22.dp.toPx()) * progress, y - 22.dp.toPx())
            drawCircle(cyan.copy(alpha = .13f), 7.dp.toPx(), dot)
            drawCircle(cyan.copy(alpha = .8f), 2.dp.toPx(), dot)
        }
    }
}
