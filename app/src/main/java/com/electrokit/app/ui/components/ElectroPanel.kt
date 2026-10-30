package com.electrokit.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import com.electrokit.app.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.clearAndSetSemantics

/** Bordered instrument panel shared by detail pages and catalogues.
 * @param modifier Caller layout bounds.
 * @param shape Panel outline.
 * @param colors Semantic foreground and background colors.
 * @param content Existing interactive contents.
 * @return A themed panel without changing state ownership.
 */
@Composable
fun ElectroPanel(modifier: Modifier = Modifier, shape: Shape = MaterialTheme.shapes.medium,
    colors: CardColors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    content: @Composable ColumnScope.() -> Unit) {
    Card(modifier, shape = shape, colors = colors,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), content = content)
}

/** Clickable variant preserves Material pressed and accessibility behavior.
 * @param onClick Existing destination action.
 * @param modifier Caller layout bounds.
 * @param shape Panel outline.
 * @param content Existing contents.
 * @return A clickable bordered panel.
 */
@Composable
fun ElectroPanel(onClick: () -> Unit, modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium, content: @Composable ColumnScope.() -> Unit) {
    Card(onClick = onClick, modifier = modifier, shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), content = content)
}

/** Selects the decorative symbol and brand accent shared by badges, tiles and category cards.
 * @param title Category caption used to select a stable accent and symbol.
 * @return Pair of a one-character symbol and its saturated accent colour; unknown titles use the neutral green.
 */
fun categoryAccent(title: String): Pair<String, Color> = when {
    title.contains("transformer", true) -> "" to Color(0xFFAF5B19)
    title.contains("battery", true) -> "" to Color(0xFF1767A8)
    title.contains("cable", true) -> "" to Color(0xFF007F79)
    title.contains("power factor", true) -> "" to Color(0xFF7440C8)
    title.contains("energy", true) -> "" to Color(0xFF00855E)
    title.contains("alc", true) -> "ϟ" to Color(0xFFF66A24)
    title.contains("omponent", true) -> "⚙" to Color(0xFF1767FF)
    title.contains("earn", true) || title.contains("Lesson", true) -> "▣" to Color(0xFF7440E8)
    title.contains("onvert", true) -> "⇄" to Color(0xFF007CAD)
    title.contains("scalator", true) -> "↗" to Color(0xFF00867F)
    title == "Pin-outs" -> "◈" to Color(0xFFB0307A)
    title == "Reference" -> "≡" to Color(0xFF4C5FD5)
    title == "Utilities" -> "∑" to Color(0xFFB36B00)
    else -> "⌘" to Color(0xFF008C61)
}

/** Reference-inspired vivid category badge with a decorative symbol.
 * @param title Category used to select a stable accent and symbol.
 * @param modifier Caller size, normally at least 40 dp.
 * @return A decorative icon; the adjacent action supplies its accessible label.
 */
@Composable
fun CategoryBadge(title: String, modifier: Modifier = Modifier) {
    val (symbol, color) = categoryAccent(title)
    Surface(modifier, shape = RoundedCornerShape(9.dp), border = BorderStroke(1.dp, color.copy(alpha = .8f))) {
        Box(Modifier.background(Brush.linearGradient(listOf(color, color.copy(red = color.red * .5f, green = color.green * .5f, blue = color.blue * .5f)))).padding(6.dp), contentAlignment = Alignment.Center) {
            val illustration = when {
                title.contains("transformer", true) -> R.drawable.ic_tool_transformer
                title.contains("battery", true) -> R.drawable.ic_tool_battery
                title.contains("cable", true) -> R.drawable.ic_tool_cable
                title.contains("power factor", true) -> R.drawable.ic_tool_powerfactor
                title.contains("energy", true) -> R.drawable.ic_tool_energy
                title.contains("binary", true) -> R.drawable.ic_tool_radix
                title.contains("scientific", true) -> R.drawable.ic_home_calculator
                else -> null
            }
            if (illustration != null) Icon(painterResource(illustration), null, Modifier.size(28.dp), tint = Color.White)
            else Icon(categoryIcon(title), null, Modifier.size(28.dp), tint = Color.White)
        }
    }
}

/** Vector symbols shared by launcher tiles, categories and profile rows.
 * @param title Visible category or action label.
 * @return A bundled Material icon, without a font glyph dependency.
 */
fun categoryIcon(title: String): ImageVector = when {
    title.contains("calc", true) -> Icons.Default.Create
    title.contains("convert", true) -> Icons.Default.Refresh
    title.contains("learn", true) || title.contains("lesson", true) -> Icons.Default.List
    title.contains("favorite", true) -> Icons.Default.Star
    title.contains("privacy", true) -> Icons.Default.Lock
    title.contains("about", true) -> Icons.Default.Info
    title.contains("appearance", true) -> Icons.Default.Settings
    title.contains("update", true) -> Icons.Default.Refresh
    title.contains("component", true) -> Icons.Default.Settings
    else -> Icons.Default.Build
}
