@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.electrokit.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.electrokit.app.data.*
import com.electrokit.app.domain.Engineering
import com.electrokit.app.ui.components.ElectroPanel
import com.electrokit.app.viewmodel.AppViewModel

/** Searchable offline library using existing favourite IDs and recent-history persistence.
 * @param assets All original component records; none are removed.
 * @param guides Authored family facts loaded from local assets.
 * @param query Live text from the shared app search.
 * @param prefs Current global bookmarks and history.
 * @param vm Existing persistence owner.
 * @param modifier Parent layout bounds.
 * @param onOpen Opens the existing component route.
 * @return Unit; filters, sort and grid position survive opening and closing details.
 */
@Composable
fun ComponentReferenceLibrary(assets: List<ComponentAsset>, guides: Map<String, ComponentGuide>, query: String,
    prefs: PreferencesState, vm: AppViewModel, modifier: Modifier = Modifier, onOpen: (ComponentAsset) -> Unit) {
    var filter by rememberSaveable { mutableStateOf("All") }
    var sort by rememberSaveable { mutableStateOf("Name") }
    var favorites by rememberSaveable { mutableStateOf(false) }
    var selected by rememberSaveable { mutableStateOf(listOf<String>()) }
    var comparing by rememberSaveable { mutableStateOf(false) }
    val grid = rememberLazyGridState()
    val filters = listOf("All", "Passive", "Active", "Semiconductor", "ICs", "Sensors", "Power", "Connectors", "Electromechanical", "Display", "Protection", "Equipment")
    val shown = remember(assets, guides, query, filter, sort, favorites, prefs.favourites, prefs.recent) {
        assets.filter { asset ->
            val guide = guides[componentFamily(asset).orEmpty()]
            val category = guide?.category ?: asset.category
            val text = listOfNotNull(asset.name, asset.category, category, guide?.aliases?.joinToString(" "), guide?.principle, guide?.application).joinToString(" ")
            val matches = filter == "All" || category.contains(filter, true) ||
                (filter == "Semiconductor" && componentFamily(asset) in listOf("bjt", "mosfet", "diode", "led"))
            matches && text.contains(query.trim(), true) && (!favorites || "component:${asset.id}" in prefs.favourites)
        }.sortedWith(compareBy<ComponentAsset> {
            when (sort) { "Category" -> guides[componentFamily(it).orEmpty()]?.category ?: it.category
                "Recently viewed" -> prefs.recent.indexOf("component:${it.id}").let { n -> if (n < 0) 999 else n }.toString().padStart(3, '0')
                else -> it.name.lowercase() }
        }.thenBy { it.name })
    }
    LazyVerticalGrid(GridCells.Adaptive(160.dp), modifier, state = grid,
        contentPadding = PaddingValues(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item(span = { GridItemSpan(maxLineSpan) }) {
        Column(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 4.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("ELECTRO — Components", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Explore, identify, understand and test electronic components.", style = MaterialTheme.typography.bodyMedium)
            Text("Search names, part numbers, related markings and applications", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filters) { name -> FilterChip(filter == name, { filter = name }, label = { Text(name) }) }
        }
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(favorites, { favorites = !favorites }, label = { Text("Favorites only") })
            var sorting by remember { mutableStateOf(false) }
            Box {
                TextButton({ sorting = true }) { Text("Sort: $sort") }
                DropdownMenu(sorting, { sorting = false }) {
                    listOf("Name", "Category", "Recently viewed").forEach { name -> DropdownMenuItem(text = { Text(name) }, onClick = { sort = name; sorting = false }) }
                }
            }
            TextButton({ comparing = true }, enabled = selected.size == 2) { Text("Compare (${selected.size}/2)") }
        }
        Text("${shown.size} components · Swipe categories →", Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        if (shown.isEmpty()) WorkbenchEmpty("No matching components", "Try a different category, name or marking. Favorites are shared with the main Favorites tab.")
        }
        }
            items(shown, key = { it.id }) { asset ->
                val guide = guides[componentFamily(asset).orEmpty()]
                ElectroPanel(onClick = { vm.visited("component:${asset.id}"); onOpen(asset) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                    ComponentImage(asset, Modifier.fillMaxWidth().aspectRatio(1.1f))
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(guide?.category ?: asset.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text(asset.name, style = MaterialTheme.typography.titleMedium)
                        Text(guide?.principle ?: "Open the existing reference for operating principles and connection precautions.", maxLines = 3, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(asset.id in selected, { checked -> selected = if (!checked) selected - asset.id else if (selected.size < 2) selected + asset.id else selected },
                                modifier = Modifier.semantics { contentDescription = "Compare ${asset.name}" }, enabled = asset.id in selected || selected.size < 2)
                            Spacer(Modifier.weight(1f))
                            IconButton({ vm.star("component:${asset.id}") }) {
                                Icon(if ("component:${asset.id}" in prefs.favourites) Icons.Default.Star else Icons.Outlined.Star,
                                    if ("component:${asset.id}" in prefs.favourites) "Remove ${asset.name} from favorites" else "Favorite ${asset.name}")
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("View reference", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            Icon(Icons.Default.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
    }
    if (comparing) AlertDialog(onDismissRequest = { comparing = false }, title = { Text("Compare component types") },
        text = { Column(Modifier.verticalScroll(rememberScrollState())) {
            Text("Family characteristics only; no device ratings inferred. Swipe sideways for both columns.")
            Row(Modifier.horizontalScroll(rememberScrollState())) {
                assets.filter { it.id in selected }.forEach { asset ->
                    val guide = guides[componentFamily(asset).orEmpty()]
                    Column(Modifier.width(200.dp).padding(8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(asset.name, fontWeight = FontWeight.Bold)
                        Text(guide?.principle ?: "Read the original component reference for this device family.")
                        Text("Parameters to verify", fontWeight = FontWeight.Bold)
                        guide?.specifications?.forEach { Text(it) }
                        Text("Application", fontWeight = FontWeight.Bold)
                        Text(guide?.application ?: "Use the exact manufacturer documentation.")
                    }
                }
            }
        } }, confirmButton = { TextButton({ comparing = false }) { Text("Close") } })
}

/** Expandable reference block keeps detailed information available without an endless initial page.
 * @param title Section heading.
 * @param initialExpanded Initial visibility, retained across rotation.
 * @param content Authored content, never fetched from a network.
 * @return Unit with an accessible Material toggle.
 */
@Composable
private fun GuideSection(title: String, initialExpanded: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    var expanded by rememberSaveable(title) { mutableStateOf(initialExpanded) }
    ElectroPanel(Modifier.fillMaxWidth()) {
        TextButton({ expanded = !expanded }, Modifier.fillMaxWidth().semantics { stateDescription = if (expanded) "Expanded" else "Collapsed" }) { Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null) }
        if (expanded) Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

/** Structured component detail with preserved legacy content and explicitly verified pinout selection.
 * @param asset Original artwork and stable favourite key.
 * @param guide Optional authored family guide.
 * @param legacy All pre-existing reference content, retained in full.
 * @param prefs Shared favourites snapshot.
 * @param vm Existing local persistence owner.
 * @param back Returns to the saved library position.
 * @param calculator Opens an existing calculator route.
 * @return Unit; generic imagery is never used as pinout authority.
 */
@Composable
fun ComponentReferenceDetail(asset: ComponentAsset, guide: ComponentGuide?, legacy: List<Pair<String, String>>,
    prefs: PreferencesState, vm: AppViewModel, back: () -> Unit, calculator: (String) -> Unit) {
    Scaffold(topBar = { Bar(asset.name, back) { IconButton({ vm.star("component:${asset.id}") }) {
        Icon(if ("component:${asset.id}" in prefs.favourites) Icons.Default.Star else Icons.Outlined.Star, "Favorite component")
    } } }) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { ComponentImage(asset, Modifier.fillMaxWidth().height(220.dp), detail = true) }
            item { Text(guide?.category ?: asset.category, color = MaterialTheme.colorScheme.primary); Text(asset.name, style = MaterialTheme.typography.headlineSmall)
                Text("General family reference. Illustration does not identify an exact part, rating or pin order.", style = MaterialTheme.typography.bodySmall) }
            if (guide != null) {
                item { GuideSection("How it works", true) { Text(guide.principle, style = MaterialTheme.typography.bodyLarge)
                    TextButton({ calculator("r7") }) { Text("Open schematic symbol reference") } } }
                item { GuideSection("Electrical specifications") {
                    Text("Parameters to verify in the exact part datasheet — these are not ratings assigned to the image.", color = MaterialTheme.colorScheme.primary)
                    guide.specifications.forEach { Text("• $it") }
                } }
                item { GuideSection("Identification & markings") {
                    Text(guide.identification)
                    if (componentFamily(asset) in listOf("resistor", "potentiometer")) {
                        TextButton({ calculator("c1") }) { Text("Open resistor band calculator") }
                        TextButton({ calculator("c2") }) { Text("Open SMD marking decoder") }
                    }
                    if (componentFamily(asset) == "capacitor") {
                        var code by rememberSaveable { mutableStateOf("") }
                        OutlinedTextField(code, { code = it }, label = { Text("Three-digit capacitor code") }, modifier = Modifier.fillMaxWidth())
                        val valid = code.matches(Regex("[0-9]{2}[0-7]")) && code.take(2).toIntOrNull()?.let { it > 0 } == true
                        Text(if (valid) Engineering.format(code.take(2).toDouble() * Math.pow(10.0, code.last().digitToInt().toDouble()) * 1e-12, "F")
                            else "Use a standard nonzero three-digit pF code ending 0–7. Special codes, voltage and tolerance need the maker's documentation.")
                    }
                } }
                item { GuideSection("Pin configuration") {
                    if (asset.id == "dip_integrated_circuit") VerifiedTimerPins()
                    else Text("No exact part/package is selected. Terminal numbers are deliberately not inferred from appearance. Consult the exact drawing and defined view; existing connector Pin-outs remain available in Tools.")
                } }
                item { GuideSection("How to test using a digital multimeter") {
                    var reviewed by rememberSaveable(asset.id) { mutableStateOf(listOf<Int>()) }
                    Text("Reading checklist, not a pass/fail measurement or safety certification.", style = MaterialTheme.typography.bodySmall)
                    guide.testing.forEachIndexed { index, step ->
                        Row(verticalAlignment = Alignment.Top) {
                            Checkbox(index in reviewed, { checked -> reviewed = if (checked) reviewed + index else reviewed - index }, modifier = Modifier.semantics { contentDescription = "Reviewed test guidance step ${index + 1}" })
                            Text("${index + 1}. $step", Modifier.weight(1f).padding(top = 12.dp))
                        }
                    }
                    TextButton({ calculator("c4") }) { Text("Open Ohm's Law calculator") }
                } }
                item { GuideSection("Educational circuit application") {
                    if (componentFamily(asset) in listOf("resistor", "capacitor")) PassiveExampleDiagram(componentFamily(asset) == "capacitor")
                    Text(guide.application)
                    Text("Educational model only. Verify real parts, limits, protection and construction before use.", color = MaterialTheme.colorScheme.primary)
                    val route = when (componentFamily(asset)) { "resistor" -> "c7"; "capacitor" -> "c11"; "led" -> "c13"; "ic" -> "c15"; "inductor" -> "c5"; else -> "c4" }
                    TextButton({ calculator(route) }) { Text("Open related calculator") }
                } }
                item { GuideSection("References") { Text(guide.source); Text("Manufacturer reference stored offline as a citation. No new device ratings are claimed without the exact datasheet.") } }
            }
            item { GuideSection("Original component reference", guide == null) { legacy.forEach { (title, body) -> Text(title, fontWeight = FontWeight.Bold); Text(body) } } }
        }
    }
}

/** Interactive pinout checked against TI SLFS022K, March 2026, Table 4-1 and P-package top view.
 * @return A selectable NE555 PDIP-8 diagram only; not a universal IC mapping.
 */
@Composable
private fun VerifiedTimerPins() {
    var chosen by rememberSaveable { mutableStateOf(false) }
    var selected by rememberSaveable { mutableIntStateOf(1) }
    var enlarged by rememberSaveable { mutableStateOf(false) }
    val pins = listOf("GND — Ground", "TRIG — Trigger input", "OUT — Timer output", "RESET — Active-low reset", "CONT — Control voltage", "THRES — Threshold input", "DISCH — Discharge output", "VCC — Supply")
    Text("Select a verified part/package before showing numbered terminals.")
    FilterChip(chosen, { chosen = !chosen }, label = { Text("TI NE555 • PDIP-8 • top view") })
    if (chosen) {
        Text("TOP VIEW • notch at top • pins count counterclockwise. Generic library artwork is not device identification.")
        for (i in 0..3) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (pin in listOf(i + 1, 8 - i)) OutlinedButton({ selected = pin }, Modifier.weight(1f)) { Text("$pin ${pins[pin - 1].substringBefore(" —")}") }
        }
        Text("Pin $selected: ${pins[selected - 1]}", style = MaterialTheme.typography.titleMedium)
        TextButton({ enlarged = true }) { Text("Enlarge pinout") }
        Text("Source: TI SLFS022K, revised March 2026, P (PDIP) package, Table 4-1. https://www.ti.com/lit/ds/symlink/ne555.pdf", style = MaterialTheme.typography.bodySmall)
    }
    if (enlarged) AlertDialog(onDismissRequest = { enlarged = false }, title = { Text("NE555 PDIP-8 • TOP VIEW") },
        text = { Column(Modifier.verticalScroll(rememberScrollState())) { pins.forEachIndexed { index, pin -> TextButton({ selected = index + 1 }) { Text("Pin ${index + 1}: $pin", style = MaterialTheme.typography.titleLarge) } } } },
        confirmButton = { TextButton({ enlarged = false }) { Text("Close") } })
}

/** Original generic circuit topology; terminal placement is functional, not a package pinout.
 * @param rc True draws an RC charging network; false draws a two-resistor divider.
 * @return Offline schematic using IEC resistor rectangles and capacitor plates.
 */
@Composable
private fun PassiveExampleDiagram(rc: Boolean) {
    val ink = MaterialTheme.colorScheme.onSurface
    val accent = MaterialTheme.colorScheme.primary
    Text(if (rc) "Educational RC charging network" else "Educational unloaded voltage divider", fontWeight = FontWeight.Bold)
    Canvas(Modifier.fillMaxWidth().height(220.dp).semantics { contentDescription = if (rc) "RC network: input through resistor to output, with capacitor from output to ground" else "Voltage divider: R1 from input to output, R2 from output to ground" }) {
        val x = size.width * .45f
        val unit = size.height / 210f
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { color = ink.toArgb(); textSize = 12.sp.toPx() }
        /** Draws the vertical conductor between normalized circuit coordinates.
         * @param y1 Starting coordinate.
         * @param y2 Ending coordinate.
         * @return Unit after drawing a conductor, not a package lead assignment.
         */
        fun wire(y1: Float, y2: Float) { drawLine(ink, Offset(x, y1 * unit), Offset(x, y2 * unit), 2.dp.toPx()) }
        drawCircle(accent, 4.dp.toPx(), Offset(x, 16 * unit))
        drawContext.canvas.nativeCanvas.drawText("Vin", x + 24.dp.toPx(), 22 * unit, paint)
        wire(16f, 36f)
        drawRect(ink, Offset(x - 12 * unit, 36 * unit), Size(24 * unit, 38 * unit), style = Stroke(2.dp.toPx()))
        drawContext.canvas.nativeCanvas.drawText(if (rc) "R" else "R1", x + 23.dp.toPx(), 62 * unit, paint)
        wire(74f, 108f)
        drawCircle(accent, 4.dp.toPx(), Offset(x, 100 * unit))
        drawLine(ink, Offset(x, 100 * unit), Offset(size.width - 20.dp.toPx(), 100 * unit), 2.dp.toPx())
        drawContext.canvas.nativeCanvas.drawText(if (rc) "Vcap" else "Vout", size.width - 60.dp.toPx(), 90 * unit, paint)
        if (rc) {
            wire(108f, 126f)
            drawLine(ink, Offset(x - 24 * unit, 126 * unit), Offset(x + 24 * unit, 126 * unit), 2.dp.toPx())
            drawLine(ink, Offset(x - 24 * unit, 139 * unit), Offset(x + 24 * unit, 139 * unit), 2.dp.toPx())
            wire(139f, 179f)
        } else {
            wire(108f, 119f)
            drawRect(ink, Offset(x - 12 * unit, 119 * unit), Size(24 * unit, 38 * unit), style = Stroke(2.dp.toPx()))
            wire(157f, 179f)
        }
        drawContext.canvas.nativeCanvas.drawText(if (rc) "C" else "R2", x + 30.dp.toPx(), 146 * unit, paint)
        for (i in 0..2) drawLine(ink, Offset(x - (24 - i * 7) * unit, (179 + i * 8) * unit), Offset(x + (24 - i * 7) * unit, (179 + i * 8) * unit), 2.dp.toPx())
        drawContext.canvas.nativeCanvas.drawText("GND", x + 32.dp.toPx(), 188 * unit, paint)
    }
}
