package com.electrokit.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.electrokit.app.R
import com.electrokit.app.data.ComponentAsset
import com.electrokit.app.domain.Calculation
import com.electrokit.app.ui.components.ElectroPanel

/** Illustrated dashboard hero with lifecycle-aware circuit motion and an existing Tools action.
 * @param asset Optional bundled multimeter illustration.
 * @param calculatorCount Actual available calculators, not a marketing constant.
 * @param componentCount Actual loaded component count.
 * @param explore Opens the existing Tools destination.
 * @return Unit; large text uses a text-first layout and artwork remains decorative.
 */
@Composable
fun WorkbenchHero(asset: ComponentAsset?, calculatorCount: Int = 0, componentCount: Int = 0, explore: () -> Unit) {
    val phase = rememberHomeMotion()
    val resources = LocalContext.current.resources
    val shape = RoundedCornerShape(24.dp)
    BoxWithConstraints(Modifier.fillMaxWidth().clip(shape)
        .background(Brush.linearGradient(listOf(Color(0xFF123C60), Color(0xFF08243C), Color(0xFF071526))))
        .border(1.dp, Color(0xFF2E7598), shape)) {
        val compact = maxWidth < 330.dp || LocalDensity.current.fontScale > 1.3f
        HomeCircuitBackdrop(phase, Modifier.matchParentSize())
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(7.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFF70E8BE)))
                Text(stringResource(R.string.home_pocket_lab), color = Color(0xFF9FE8FF),
                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(stringResource(R.string.home_electrical), color = Color.White,
                        style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.semantics { heading() })
                    Text(stringResource(R.string.home_electronics), color = Color(0xFF82E4FF),
                        style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                    Text(stringResource(R.string.home_tagline), color = Color(0xFFD1E6F3),
                        style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
                }
                if (!compact && asset != null) Box(Modifier.width(124.dp).height(144.dp).clip(RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(112.dp).clip(RoundedCornerShape(56.dp))
                        .background(Brush.radialGradient(listOf(Color(0xFF2789AC).copy(alpha = .35f), Color.Transparent)))
                        .border(1.dp, Color(0xFF66DFFF).copy(alpha = .25f), RoundedCornerShape(56.dp)))
                    ComponentImage(asset, Modifier.size(120.dp).graphicsLayer {
                        scaleX = 1.6f; scaleY = 1.6f
                        translationY = kotlin.math.sin(phase.value * 2 * Math.PI).toFloat() * 3.dp.toPx()
                    }, framed = false)
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Button(explore, shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8AE8FF), contentColor = Color(0xFF082039))) {
                    Text(stringResource(R.string.workbench_explore), fontWeight = FontWeight.Bold)
                }
            }
            if (calculatorCount > 0 && componentCount > 0) Text(
                stringResource(R.string.home_library_counts,
                    resources.getQuantityString(R.plurals.home_calculator_count, calculatorCount, calculatorCount),
                    resources.getQuantityString(R.plurals.home_component_count, componentCount, componentCount)),
                color = Color(0xFFB9D8E9), style = MaterialTheme.typography.labelSmall)
        }
    }
}

/** Consistent result surface distinguishes valid values, guidance and validation feedback.
 * @param result Null before calculation, otherwise a typed result or validation error.
 * @param hint Guidance displayed before a value is available.
 * @return Unit; failed input is announced politely and cannot appear as a successful value.
 */
@Composable
fun WorkbenchResult(result: Calculation?, hint: String) {
    val error = result is Calculation.Error
    ElectroPanel(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (error) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(20.dp).semantics { liveRegion = LiveRegionMode.Polite },
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(if (error) R.string.workbench_check_input else R.string.workbench_result_heading),
                style = MaterialTheme.typography.labelLarge,
                color = if (error) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer)
            Text(result?.text ?: hint,
                style = if (result is Calculation.Value) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyMedium,
                fontWeight = if (result is Calculation.Value) FontWeight.SemiBold else FontWeight.Normal,
                color = if (error) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

/** Readable, themed empty state shared by searchable collections.
 * @param title Short explanation of the missing content.
 * @param description Suggested next step without changing the current query.
 * @return Unit after rendering an informational panel.
 */
@Composable
fun WorkbenchEmpty(title: String, description: String) {
    ElectroPanel(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Adaptive launcher rows retain all six destinations at large text sizes.
 * @param onOpen Receives the selected existing category label.
 * @return Unit; tiles wrap to two columns on compact displays or with enlarged text.
 */
@Composable
fun WorkbenchQuickAccess(onOpen: (String) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columns = if (maxWidth < 330.dp || LocalDensity.current.fontScale > 1.3f) 2 else 3
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("Calculators", "Components", "Diagrams", "Utilities", "Learning", "Converters")
                .chunked(columns).forEachIndexed { rowIndex, row ->
                    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEachIndexed { columnIndex, title -> QuickTile(title, Modifier.weight(1f).fillMaxHeight(), entranceDelayMillis = (rowIndex * columns + columnIndex) * 45) { onOpen(title) } }
                    }
                }
        }
    }
}
