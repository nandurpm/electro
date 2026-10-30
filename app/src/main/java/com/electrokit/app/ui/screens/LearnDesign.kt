package com.electrokit.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.electrokit.app.data.LearningPage
import com.electrokit.app.ui.components.ElectroPanel

/** Concept diagram for a lesson; intentionally not an equipment-specific wiring schedule.
 * @param page Selected offline topic.
 * @param tamil Whether the explanatory caption is Tamil.
 * @return A high-contrast visual introduction using the current palette.
 */
@Composable
fun LessonVisual(page: LearningPage, tamil: Boolean) {
    val nodes = when (page.id) {
        "can" -> listOf("Node 1", "CAN_H / CAN_L", "Node 2")
        "rs485" -> listOf("Device 1", "RS-485 pair", "Device 2")
        "modbus" -> listOf("Client", "Request →", "Server")
        "rs232" -> listOf("DTE", "RS-232", "DCE")
        "ethernet" -> listOf("Plug", "8 contacts", "T568A / B")
        "interfaces" -> listOf("Connector", "Pinout", "Device")
        else -> listOf("Select mode", "Connect", "Read")
    }
    ElectroPanel(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(if (tamil) "படம் மூலம் புரிந்துகொள்ளுங்கள்" else "THE BIG PICTURE", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer)
            val diagramText = MaterialTheme.colorScheme.onPrimaryContainer.toArgb()
            if (page.id in listOf("can", "rs485")) Canvas(Modifier.fillMaxWidth().height(164.dp)) {
                val rail1 = size.height * .32f
                val rail2 = size.height * .50f
                val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    color = diagramText; textSize = 12.sp.toPx()
                }
                drawContext.canvas.nativeCanvas.drawText(if (page.id == "can") "CAN_H" else "Differential pair +", 0f, rail1 - 12.dp.toPx(), paint)
                drawContext.canvas.nativeCanvas.drawText(if (page.id == "can") "CAN_L" else "Differential pair −", 0f, rail2 + 20.dp.toPx(), paint)
                drawLine(Color(0xFF1376BE), Offset(0f, rail1), Offset(size.width, rail1), 3.dp.toPx())
                drawLine(Color(0xFFB95E12), Offset(0f, rail2), Offset(size.width, rail2), 3.dp.toPx())
                for (i in 0..2) {
                    val x = size.width * (.15f + i * .34f)
                    drawLine(Color(0xFF1376BE), Offset(x, rail1), Offset(x, size.height * .82f), 2.dp.toPx())
                    drawLine(Color(0xFFB95E12), Offset(x + 12.dp.toPx(), rail2), Offset(x + 12.dp.toPx(), size.height * .82f), 2.dp.toPx())
                    drawContext.canvas.nativeCanvas.drawText("Node ${i + 1}", x - 12.dp.toPx(), size.height * .98f, paint)
                }
            }
            if (page.id !in listOf("can", "rs485")) nodes.forEachIndexed { index, node ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primary) {
                        Text("${index + 1}", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                    Text(node, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                if (index != nodes.lastIndex) Icon(Icons.Default.KeyboardArrowDown, null, Modifier.padding(start = 10.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Text(if (tamil) "இது பொதுவான கருத்துப் படம். சரியான wiring-க்கு device manual பார்க்கவும்."
                else "Concept overview only. Use the exact device manual for wiring.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

/** Numbered learning section preserves all source sentences while improving scanability.
 * @param number Section position in the unchanged lesson.
 * @param title Original technical heading.
 * @param body Complete selected-language explanation.
 * @return Readable points, or a colour-coded numbered table for T568 contact lists.
 */
@Composable
fun LessonSection(number: Int, title: String, body: String) {
    ElectroPanel(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(number.toString().padStart(2, '0'), color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f).semantics { heading() })
            }
            if (title.startsWith("T568")) {
                body.lines().filter { !it.matches(Regex("^[1-8] .*")) && it.isNotBlank() }.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
                body.lines().filter { it.matches(Regex("^[1-8] .*")) }.forEach { line ->
                    val wire = line.substringAfter(' ')
                    val colors = wire.split('/').map { name -> when (name.trim()) {
                        "green" -> Color(0xFF168747); "orange" -> Color(0xFFDF6E1B)
                        "blue" -> Color(0xFF2165CF); "brown" -> Color(0xFF8B553B)
                        else -> Color(0xFFF0F0E8)
                    } }
                    Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(10.dp)).padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(line.substringBefore(' '), style = MaterialTheme.typography.titleMedium)
                        Row(Modifier.width(36.dp).height(16.dp)) { colors.forEach { color -> Box(Modifier.weight(1f).fillMaxHeight().background(color)) } }
                        Text(wire, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else {
                val points = body.split(Regex("(?<!\\d\\.)(?<=[.!?])\\s+")).filter { it.isNotBlank() }
                points.forEach { point ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("•", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyLarge)
                        Text(point, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
