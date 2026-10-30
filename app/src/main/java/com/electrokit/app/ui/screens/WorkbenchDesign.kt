@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.electrokit.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.electrokit.app.BuildConfig
import com.electrokit.app.data.ComponentAsset
import com.electrokit.app.data.Entry
import com.electrokit.app.data.PreferencesState
import com.electrokit.app.ui.components.CategoryBadge
import com.electrokit.app.ui.components.ElectroPanel
import com.electrokit.app.ui.components.ToolIcon
import com.electrokit.app.ui.components.categoryAccent
import com.electrokit.app.viewmodel.AppViewModel

/** Darkens an accent so a vertical gradient keeps white captions readable.
 * @param color Saturated brand accent.
 * @return The same hue at roughly half brightness.
 */
private fun shade(color: Color): Color = color.copy(red = color.red * .55f, green = color.green * .55f, blue = color.blue * .55f)

/** Gentle repeating emphasis for important electrical controls; avoids a fast strobe.
 * @param active Whether the animation should run for this visual element.
 * @return Current pulse intensity between 0.72 and 1.0, or 1.0 when inactive.
 */
@Composable
fun neonPulseFactor(active: Boolean): Float {
    if (!active) return 1f
    return rememberInfiniteTransition(label = "neon pulse").animateFloat(
        initialValue = .72f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "neon intensity",
    ).value
}

/** Colored gradient launcher tile used by the Home quick-access grid.
 * @param title Visible caption; also selects the symbol and accent through [categoryAccent].
 * @param modifier Caller-owned size, normally a weighted cell of a three-column row.
 * @param onClick Existing navigation callback; the tile performs no work itself.
 * @return Unit; the tile exposes its caption as a button and hides the decorative symbol from accessibility.
 */
@Composable
fun QuickTile(title: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val (symbol, color) = categoryAccent(title)
    val shape = RoundedCornerShape(14.dp)
    val pulse = neonPulseFactor(title.contains("Calculat", ignoreCase = true))
    Column(
        modifier.heightIn(min = 104.dp).shadow(10.dp, shape, clip = false)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .background(Brush.verticalGradient(listOf(color.copy(alpha = .96f), shade(color))))
            .then(Modifier.border(1.dp, color.copy(alpha = .9f), shape))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.size(48.dp).graphicsLayer { alpha = .82f + pulse * .18f }
            .clip(RoundedCornerShape(12.dp)).background(Color.Black.copy(alpha = .20f)), contentAlignment = Alignment.Center) {
            Text(symbol, Modifier.clearAndSetSemantics { }, color = Color.White, fontSize = 29.sp, fontWeight = FontWeight.Bold)
        }
        Text(title, Modifier.padding(top = 6.dp), color = Color.White, style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center, maxLines = 2)
    }
}

/** Compact "Recently used" row: tool motif, title and a chevron, without the favourite action.
 * @param entry Registry entry that was recently opened.
 * @param onClick Opens the same stable registry route as the catalogue row.
 * @return Unit; selection is delivered only through the callback.
 */
@Composable
fun RecentRow(entry: Entry, onClick: () -> Unit) {
    ElectroPanel(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center) { ToolIcon(entry.icon) }
            Spacer(Modifier.width(12.dp))
            Text(entry.title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, maxLines = 2)
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Large category card used by the Tools hub: accent badge, title, one-line scope and optional artwork.
 * @param title Visible category heading.
 * @param subtitle One-line description of what the category contains.
 * @param badge Key understood by [categoryAccent] for the badge symbol and colour.
 * @param asset Optional bundled public component image; all artwork is general-purpose.
 * @param onClick Opens the category through the caller's existing navigation state.
 * @return Unit; the whole card is one accessible button.
 */
@Composable
fun ToolCategoryCard(title: String, subtitle: String, badge: String, asset: ComponentAsset?, onClick: () -> Unit) {
    val pulse = neonPulseFactor(title.contains("Calculat", ignoreCase = true))
    ElectroPanel(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
        val showArtwork = maxWidth >= 350.dp && LocalDensity.current.fontScale <= 1.3f
        Row(Modifier.fillMaxWidth().heightIn(min = 88.dp)
            .background(Brush.horizontalGradient(listOf(categoryAccent(badge).second.copy(alpha = .12f), Color.Transparent)))
            .padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            CategoryBadge(badge, Modifier.size(48.dp).graphicsLayer { alpha = .82f + pulse * .18f })
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            }
            if (asset != null && showArtwork) {
                Spacer(Modifier.width(8.dp))
                ComponentImage(asset, Modifier.size(72.dp).clip(RoundedCornerShape(10.dp)))
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.padding(start = 8.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        }
    }
}

/** Static description of one Tools hub card; targets reuse the existing category keys of the Tools tab. */
private data class HubCard(
    /** Visible card heading. */
    val title: String,
    /** One-line scope description. */
    val subtitle: String,
    /** Badge key accepted by [categoryAccent]. */
    val badge: String,
    /** Existing Tools category key, or "Converters" for the unit-converter route. */
    val target: String,
    /** Optional bundled public component image id; all images are ordinary component references. */
    val imageId: String? = null,
)

/** Mockup-style Tools landing page: one large card per category instead of a row of filter chips.
 * @param components Bundled component metadata used only for the optional public card artwork.
 * @param modifier Host-provided insets and layout bounds.
 * @param onSelect Receives a public category key or the converter route.
 * @return Unit after the caller opens the requested tool group.
 */
@Composable
fun ToolsHub(components: List<ComponentAsset>, modifier: Modifier = Modifier, onSelect: (String) -> Unit) {
    val cards = remember {
        listOf(
            HubCard("Electrical Calculators", "Power, voltage, current and circuits", "Calculators", "Calculators", "digital_multimeter"),
            HubCard("Electronics", "Parts, AVR and PIC databases", "Electronics", "Data", "axial_resistor"),
            HubCard("Pin-outs", "Connectors, ports and interfaces", "Pin-outs", "Pin-outs"),
            HubCard("Reference", "Tables, charts and resources", "Reference", "Reference"),
            HubCard("Converters", "Unit conversions", "Converters", "Converters"),
            HubCard("Extra Utilities", "Transformer, cable sizing and scientific tools", "Utilities", "Utilities"),
        )
    }
    LazyColumn(modifier, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(cards, key = { it.title }) { card ->
            ToolCategoryCard(card.title, card.subtitle, card.badge, components.firstOrNull { it.id == card.imageId }) { onSelect(card.target) }
        }
    }
}

/** One Profile row: accent badge, title, subtitle and either a navigation chevron or an expandable detail section.
 * @param title Row heading.
 * @param subtitle Current value or one-line summary.
 * @param symbol Decorative single-character symbol drawn in the badge.
 * @param accent Badge colour.
 * @param onClick Optional navigation callback; ignored when [details] is supplied.
 * @param details Optional content revealed beneath the row when it is tapped.
 * @return Unit; the expanded state survives rotation and is keyed by the title.
 */
@Composable
private fun ProfileRow(title: String, subtitle: String, symbol: String, accent: Color, onClick: (() -> Unit)? = null,
    details: (@Composable ColumnScope.() -> Unit)? = null) {
    var expanded by rememberSaveable(title) { mutableStateOf(false) }
    val interactive = onClick != null || details != null
    ElectroPanel(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 60.dp)
                .then(if (interactive) Modifier.clickable(role = Role.Button) { if (details != null) expanded = !expanded else onClick?.invoke() } else Modifier)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(9.dp)).background(Brush.linearGradient(listOf(accent, shade(accent)))),
                contentAlignment = Alignment.Center) {
                Text(symbol, Modifier.clearAndSetSemantics { }, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (interactive) Icon(
                when {
                    details != null && expanded -> Icons.Default.KeyboardArrowUp
                    details != null -> Icons.Default.KeyboardArrowDown
                    else -> Icons.AutoMirrored.Filled.ArrowForward
                },
                null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (expanded && details != null) Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp), content = details)
    }
}

/** Mockup-style Profile and settings page built from tappable rows; all data stays in the existing DataStore.
 * @param vm Existing preference mutation owner (appearance, sort order and history clearing).
 * @param prefs Latest committed preference snapshot.
 * @param modifier Host-provided insets and layout bounds.
 * @param openFavorites Switches to the Favorites tab, which also lists saved calculations.
 * @param openAbout Opens the existing About/Info pages.
 * @return Unit; destructive history clearing still requires the confirmation dialog.
 */
@Composable
fun ProfileScreen(vm: AppViewModel, prefs: PreferencesState, modifier: Modifier = Modifier, openFavorites: () -> Unit, openAbout: () -> Unit, onCheckUpdates: () -> Unit = {}) {
    var confirm by remember { mutableStateOf(false) }
    Column(modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Profile & Settings", Modifier.padding(vertical = 4.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        ElectroPanel(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                CategoryBadge("Components", Modifier.size(56.dp))
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("ELECTRO", style = MaterialTheme.typography.titleLarge)
                    Text("Your electronics workspace", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        ProfileRow("Appearance", "Light / Dark / System", "◐", Color(0xFFF66A24), details = {
            val mode = prefs.themeMode.ifBlank { if (prefs.light) "light" else "dark" }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("dark", "light", "system").forEach { choice ->
                    FilterChip(mode == choice, { vm.appearance(choice) }, label = { Text(choice.replaceFirstChar { it.uppercase() }) })
                }
            }
        })
        ProfileRow("Language", "English", "Aa", Color(0xFF1767FF))
        ProfileRow("Favorites", "Saved tools and components", "★", Color(0xFFB36B00), onClick = openFavorites)
        ProfileRow("Calculation History", "Saved calculation results", "↺", Color(0xFF008C61), onClick = openFavorites)
        ProfileRow("Data Management", "Tool order and saved results", "☰", Color(0xFF1767FF), details = {
            TextButton({ confirm = true }) { Text("Clear saved calculation history") }
            TextButton({ vm.sort() }) { Text(if (prefs.alphabetical) "Use original tool order" else "Sort tools A–Z") }
        })
        ProfileRow("App Updates", "Installed " + BuildConfig.VERSION_NAME + " • check GitHub", "⟳",
            Color(0xFF1683BF), onClick = onCheckUpdates, details = {
                val context = androidx.compose.ui.platform.LocalContext.current
                var autoCheck by remember { mutableStateOf(com.electrokit.app.update.UpdateSettings.autoCheck(context)) }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Automatic checks for new versions", Modifier.weight(1f))
                    Switch(autoCheck, onCheckedChange = { enabled ->
                        autoCheck = enabled
                        com.electrokit.app.update.UpdateSettings.setAutoCheck(context, enabled)
                    })
                }
                Text("4.3.1 / 4.3.2: Update or Later. 4.4.0 / 5.0.0: Update to Continue.",
                    style = MaterialTheme.typography.bodySmall)
                Text("Only GitHub release metadata is checked. Calculators and learning content remain offline.",
                    style = MaterialTheme.typography.bodySmall)
            })
        ProfileRow("About ELECTRO", "Version ${BuildConfig.VERSION_NAME}", "i", Color(0xFF007CAD), onClick = openAbout)
        ProfileRow("Privacy & Help", "Local data • optional GitHub updates", "✓", Color(0xFF4C5FD5), details = {
            Text("No accounts, advertising or analytics. Your calculators, inputs, favorites and lessons remain local. Optional app-version checks contact GitHub, and Android asks before installing downloaded APKs.",
                style = MaterialTheme.typography.bodyMedium)
        })
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text("Clear saved results?") },
        text = { Text("Your favorites and calculator inputs will be retained.") },
        confirmButton = { TextButton({ vm.clearCalculations(); confirm = false }) { Text("Clear history") } },
        dismissButton = { TextButton({ confirm = false }) { Text("Cancel") } })
}
