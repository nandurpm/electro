package com.electrokit.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.electrokit.app.R
import com.electrokit.app.data.ComponentAsset
import com.electrokit.app.domain.Calculation
import com.electrokit.app.ui.components.ElectroPanel

/** Responsive navy hero keeps illustrative artwork separate from readable text.
 * @param asset Optional public component illustration; loading does not block navigation.
 * @param explore Opens the existing Tools destination.
 * @return Unit after composing the dashboard introduction.
 */
@Composable
fun WorkbenchHero(asset: ComponentAsset?, explore: () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
        .background(Brush.linearGradient(listOf(Color(0xFF0B3957), Color(0xFF071A2C), Color(0xFF06111F))))) {
        val compact = maxWidth < 350.dp || LocalDensity.current.fontScale > 1.3f
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.workbench_eyebrow), color = Color(0xFF83E2FF),
                style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Electrical & Electronics", color = Color.White, style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() })
                    Text(stringResource(R.string.workbench_intro), color = Color(0xFFD2E2EF),
                        style = MaterialTheme.typography.bodyMedium)
                }
                if (!compact && asset != null) ComponentImage(asset,
                    Modifier.size(104.dp).clip(RoundedCornerShape(16.dp)))
            }
            Button(explore, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF83E2FF), contentColor = Color(0xFF082039))) {
                Text(stringResource(R.string.workbench_explore))
            }
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
                .chunked(columns).forEach { row ->
                    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { title -> QuickTile(title, Modifier.weight(1f).fillMaxHeight()) { onOpen(title) } }
                    }
                }
        }
    }
}
