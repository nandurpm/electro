@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.electrokit.app.ui.screens

import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.electrokit.app.ui.components.ElectroPanel as Card
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.ui.res.painterResource
import com.electrokit.app.R
import com.electrokit.app.BuildConfig
import com.electrokit.app.data.*
import com.electrokit.app.viewmodel.*
import com.electrokit.app.domain.WorkbenchTools
import com.electrokit.app.ui.components.CategoryBadge
import com.electrokit.app.ui.components.MultimeterLesson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Shared bitmap budget; large reference sheets never load into the scrolling catalogue. */
private val imageCache = object : LruCache<String, android.graphics.Bitmap>(8 * 1024 * 1024) {
    /** Counts actual decoded memory rather than compressed asset bytes.
     * @param key Asset-and-resolution cache key.
     * @param value Decoded bitmap.
     * @return Decoded allocation size in bytes.
     */
    override fun sizeOf(key: String, value: android.graphics.Bitmap): Int = value.byteCount
}

/** Decodes local component art on IO at bounded resolution; a failed asset has a visible fallback.
 * @param asset Bundled image metadata; not a verified manufacturer photograph.
 * @param modifier Caller-owned image bounds.
 * @param detail True permits a larger sampled decode for a detail page.
 * @return Unit; failed image decoding produces a readable fallback and never triggers network I/O.
 */
@Composable
fun ComponentImage(asset: ComponentAsset, modifier: Modifier = Modifier, detail: Boolean = false) {
    val context = LocalContext.current
    val target = if (detail) 1024 else 256
    val key = "${asset.image}:$target"
    var bitmap by remember(key) { mutableStateOf(imageCache.get(key)) }
    LaunchedEffect(key) {
        bitmap = withContext(Dispatchers.IO) {
            imageCache.get(key) ?: runCatching {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.assets.open(asset.image).use { BitmapFactory.decodeStream(it, null, bounds) }
                val options = BitmapFactory.Options().apply {
                    inSampleSize = 1
                    while (bounds.outWidth / inSampleSize > target * 2 || bounds.outHeight / inSampleSize > target * 2) inSampleSize *= 2
                }
                context.assets.open(asset.image).use { BitmapFactory.decodeStream(it, null, options) }
            }.getOrNull()?.also { imageCache.put(key, it) }
        }
    }
    Box(modifier.background(MaterialTheme.colorScheme.surfaceContainerLowest), contentAlignment = Alignment.Center) {
        bitmap?.let { Image(it.asImageBitmap(), asset.name, Modifier.fillMaxSize().padding(8.dp), contentScale = ContentScale.Fit) }
            ?: Text(asset.name, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(12.dp))
    }
}

/** Five stable destinations wrap the original registry, calculators, databases and engineering routes.
 * @param vm Existing catalogue, preferences and input-persistence owner.
 * @param prefs Latest immutable committed preference snapshot.
 * @param openEntry Opens an original public registry route.
 * @param info Opens an existing information tab.
 * @param settings Opens the standalone appearance page.
 * @param engineering Existing source-traceable engineering asset owner.
 * @param openEngineering Opens a protected engineering route through its shared session gate.
 * @return Unit after composing the selected destination; storage errors remain visible in the host banner.
 */
@Composable
fun MainScreen(vm: AppViewModel, prefs: PreferencesState, openEntry: (String) -> Unit,
    info: (Int) -> Unit, settings: () -> Unit, engineering: EngineeringViewModel, openEngineering: (String) -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var query by rememberSaveable(tab) { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("Calculators") }
    var detail by rememberSaveable { mutableStateOf<String?>(null) }
    var hub by rememberSaveable { mutableStateOf(true) }
    var menu by remember { mutableStateOf(false) }
    BackHandler(enabled = detail != null) { detail = null }
    BackHandler(enabled = detail == null && tab != 0) { tab = 0; query = "" }
    BackHandler(enabled = detail == null && tab == 1 && !hub) { hub = true; category = "Calculators"; query = "" }
    val context = LocalContext.current
    val components by produceState<List<ComponentAsset>>(emptyList()) { value = withContext(Dispatchers.IO) { loadComponents(context) } }
    val access: EngineeringAccess = viewModel(requireNotNull(context.componentActivity()))
    val publicComponents = components.filter { !it.advanced }
    val names = listOf("Home", "Tools", "Learn", "Favorites", "Profile")
    val component = components.firstOrNull { detail == "component:${it.id}" }
    val lesson = learningPages.firstOrNull { detail == "learn:${it.id}" }
    val utility = WorkbenchTools.all.firstOrNull { detail == "utility:${it.id}" }
    if (detail == "unit-converter") { UnitConverterScreen { detail = null }; return }
    if (utility != null) { UtilityScreen(utility, vm, prefs) { detail = null }; return }
    if (component != null) {
        val screen: @Composable () -> Unit = { ComponentDetail(component, prefs, vm) { detail = null } }
        if (component.advanced) EngineeringGate({ detail = null }, screen) else screen()
        return
    }
    if (lesson != null) { LearningDetail(lesson, prefs, vm) { detail = null }; return }
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column {
                Bar("ELECTRO", brand = true) {
                    IconButton(onClick = { info(0) }) { Icon(Icons.Default.Info, "Info") }
                    IconButton(onClick = { menu = true }) { Icon(Icons.Default.Settings, "Menu") }
                    DropdownMenu(menu, { menu = false }) {
                        DropdownMenuItem(text = { Text("Settings") }, onClick = { menu = false; settings() })
                        DropdownMenuItem(text = { Text("About ELECTRO") }, onClick = { menu = false; info(0) })
                    }
                }
                if (tab != 4 && !(tab == 1 && category in listOf("Data", "Engineering"))) Search(query, { query = it }, shape = RoundedCornerShape(28.dp))
            }
        },
        bottomBar = {
            NavigationBar(Modifier.testTag("main_navigation").height((64f + 64f * (LocalDensity.current.fontScale - 1f).coerceAtLeast(0f)).dp), windowInsets = WindowInsets(0, 0, 0, 0)) {
                names.forEachIndexed { i, name ->
                    Column(Modifier.weight(1f).fillMaxHeight().selectable(selected = tab == i, role = Role.Tab,
                        onClick = { tab = i; query = ""; if (i == 1) { category = "Calculators"; hub = true }; if (i == 2) category = "Components" })
                        .padding(horizontal = 2.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                        Box(Modifier.clip(RoundedCornerShape(10.dp)).background(if (tab == i) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                            .padding(horizontal = 16.dp, vertical = 4.dp)) {
                            Icon(when(i) { 0 -> Icons.Default.Home; 1 -> Icons.Default.Build; 2 -> Icons.Default.Info; 3 -> Icons.Default.Star; else -> Icons.Default.Person }, null, Modifier.size(24.dp))
                        }
                        Text(name, Modifier.fillMaxWidth(), maxLines = 2, minLines = if (LocalDensity.current.fontScale > 1.3f) 2 else 1,
                            fontSize = if (LocalDensity.current.fontScale > 1.3f) 9.sp else 11.sp, lineHeight = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }
        }
    ) { padding ->
        val body = Modifier.padding(padding).fillMaxSize()
        when (tab) {
            0 -> LazyColumn(body, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.surfaceContainerLowest))).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("ELECTRICAL • ELECTRONICS", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                                Text("Electrical Engineering\nTools for Real World", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp))
                                Text("Calculate · Design · Learn", style = MaterialTheme.typography.bodySmall)
                                TextButton({ tab = 1; hub = true; category = "Calculators" }) { Text("Explore tools →") }
                            }
                            publicComponents.firstOrNull { it.id == "induction_motor" }?.let {
                                Box(Modifier.width(112.dp).height(140.dp).clip(RoundedCornerShape(8.dp))) {
                                    ComponentImage(it, Modifier.fillMaxSize().graphicsLayer { scaleX = 1.7f; scaleY = 1.7f })
                                }
                            }
                        }
                    }
                }
                if (query.isBlank()) {
                    item { SectionTitle("Quick Access") }
                    item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        QuickTile("Calculators", Modifier.weight(1f)) { tab = 1; category = "Calculators"; hub = false }
                        QuickTile("Components", Modifier.weight(1f)) { tab = 2; category = "Components" }
                        QuickTile("Diagrams", Modifier.weight(1f)) { tab = 1; category = "Pin-outs"; hub = false }
                    } }
                    item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        QuickTile("Escalator", Modifier.weight(1f)) { openEngineering("home") }
                        QuickTile("Learning", Modifier.weight(1f)) { tab = 2; category = "Lessons" }
                        QuickTile("Converters", Modifier.weight(1f)) { detail = "unit-converter" }
                    } }
                    if (prefs.recent.isNotEmpty()) {
                        item { SectionTitle("Recently Used") }
                        items(prefs.recent.mapNotNull { id -> vm.repository.registry.find { it.id == id } }.take(4)) {
                            RecentRow(it) { openEntry(it.id) }
                        }
                    }
                    item { SectionTitle("Featured calculators") }
                }
                items(vm.repository.registry.filter { it.screenType == "calculator" && it.title.contains(query, true) }.take(if (query.isBlank()) 3 else 100), key = { it.id }) {
                    EntryRow(it, { openEntry(it.id) }, it.id in prefs.favourites) { vm.star(it.id) }
                }
                if (query.isNotBlank()) {
                    items(publicComponents.filter { it.name.contains(query, true) }.take(12), key = { "asset:${it.id}" }) { c ->
                        ListItem(headlineContent = { Text(c.name) }, supportingContent = { Text(c.category) }, modifier = Modifier.clickable { detail = "component:${c.id}" })
                    }
                    items(learningPages.filter { it.title.contains(query, true) }) { page ->
                        QuickCard(page.title, Modifier.fillMaxWidth()) { detail = "learn:${page.id}" }
                    }
                } else {
                    item { SectionTitle("Component discovery") }
                    items(publicComponents.filter { it.id in listOf("digital_multimeter", "magnetic_contactor", "axial_resistor") }) { c ->
                        Card(onClick = { detail = "component:${c.id}" }, shape = RoundedCornerShape(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ComponentImage(c, Modifier.size(104.dp))
                                Column(Modifier.padding(16.dp)) { Text(c.name, style = MaterialTheme.typography.titleMedium); Text(c.category, style = MaterialTheme.typography.bodySmall) }
                            }
                        }
                    }
                }
            }
            1 -> if (hub && query.isBlank()) ToolsHub(publicComponents, body) { target ->
                if (target == "Converters") detail = "unit-converter" else { category = target; hub = false }
            } else Column(body) {
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("Calculators", "Pin-outs", "Reference", "Data", "Engineering", "Utilities")) { name ->
                        FilterChip(category == name, { category = name }, label = { Text(name) })
                    }
                }
                when (category) {
                    "Utilities" -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item { QuickCard("Engineering unit converter", Modifier.fillMaxWidth()) { detail = "unit-converter" } }
                        items(WorkbenchTools.all.filter { it.title.contains(query, true) }) { tool ->
                            QuickCard(tool.title, Modifier.fillMaxWidth()) { detail = "utility:${tool.id}" }
                        }
                    }
                    "Data" -> Databases(vm, prefs, Modifier.weight(1f), openEntry, { info(1) })
                    "Engineering" -> EngineeringScreen("home", engineering, vm, prefs, openEngineering, openEntry)
                    else -> {
                        val group = when(category) { "Pin-outs" -> "PIN-OUTS"; "Reference" -> "RESOURCES"; else -> "CALCULATORS" }
                        val entries = vm.repository.registry.filter { (it.tab == group || (group == "CALCULATORS" && it.screenType == "module")) && it.title.contains(query, true) }
                            .sortedWith(compareByDescending<Entry> { it.id in prefs.favourites }.thenBy { if (prefs.alphabetical) it.title else "" })
                        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(8.dp)) {
                            item { SectionTitle("${entries.size} ${category.lowercase()}") }
                            if (entries.isEmpty()) item { Text("No matching tools. Try another search.", Modifier.padding(16.dp)) }
                            items(entries, key = { it.id }) { entry -> EntryRow(entry, { openEntry(entry.id) }, entry.id in prefs.favourites) { vm.star(entry.id) } }
                        }
                    }
                }
            }
            2 -> Column(body) {
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("Components", "Lessons", "Escalator images")) { name -> FilterChip(category == name, { category = name }, label = { Text(name) }) }
                }
                if (category == "Lessons") LazyColumn {
                    items(learningPages.filter { it.title.contains(query, true) }) { page ->
                        QuickCard(page.title, Modifier.fillMaxWidth().padding(12.dp)) { detail = "learn:${page.id}" }
                    }
                } else {
                    val gallery: @Composable () -> Unit = {
                        LazyVerticalGrid(GridCells.Adaptive(150.dp), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            gridItems((if (category == "Escalator images") components.filter { it.advanced } else publicComponents)
                                .filter { it.name.contains(query, true) || it.category.contains(query, true) }, key = { it.id }) { c ->
                                Card(onClick = { detail = "component:${c.id}" }, shape = RoundedCornerShape(10.dp)) {
                                    ComponentImage(c, Modifier.fillMaxWidth().aspectRatio(1f))
                                    Text(c.name, Modifier.padding(12.dp), style = MaterialTheme.typography.titleSmall)
                                }
                            }
                        }
                    }
                    if (category == "Escalator images") EngineeringGate(content = gallery) else gallery()
                }
            }
            3 -> LazyColumn(body, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { SectionTitle("Your favorites") }
                items(vm.repository.registry.filter { it.id in prefs.favourites && it.title.contains(query, true) }) {
                    EntryRow(it, { openEntry(it.id) }, true) { vm.star(it.id) }
                }
                items(publicComponents.filter { "component:${it.id}" in prefs.favourites && it.name.contains(query, true) }) { c ->
                    QuickCard(c.name, Modifier.fillMaxWidth()) { detail = "component:${c.id}" }
                }
                if (access.unlocked) items(components.filter { it.advanced && "component:${it.id}" in prefs.favourites && it.name.contains(query, true) }) { c ->
                    QuickCard(c.name, Modifier.fillMaxWidth()) { detail = "component:${c.id}" }
                }
                items(learningPages.filter { "learn:${it.id}" in prefs.favourites && it.title.contains(query, true) }) { p ->
                    QuickCard(p.title, Modifier.fillMaxWidth()) { detail = "learn:${p.id}" }
                }
                item { TextButton({ openEngineering("home") }) { Text(if (access.unlocked) "Engineering bookmarks" else "Unlock engineering bookmarks") } }
                item { SectionTitle("Saved calculations") }
                if (prefs.calculations.isEmpty()) item { Text("Save a result from a calculator's menu. Inputs are retained automatically.") }
                items(prefs.calculations.filter { it.contains(query, true) }) { result ->
                    Card { Text(result, Modifier.padding(16.dp)); TextButton({ share(context, result) }) { Text("Share calculation") } }
                }
            }
            else -> ProfileScreen(vm, prefs, body, { tab = 3 }, { info(0) })
        }
    }
}

/** Shared section typography gives catalogue and dashboard a consistent visual hierarchy.
 * @param title Visible section heading.
 * @return Unit; displays a heading without modifying state.
 */
@Composable
private fun SectionTitle(title: String) { Text(title, Modifier.padding(vertical = 8.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }

/** Native action card; it remains usable with large font sizes instead of using a fixed height.
 * @param title Accessible card caption.
 * @param modifier Caller-owned size and spacing.
 * @param click Explicit navigation or action callback.
 * @return Unit; activates the callback only on user selection.
 */
@Composable
private fun QuickCard(title: String, modifier: Modifier, click: () -> Unit) {
    Card(onClick = click, modifier = modifier, shape = RoundedCornerShape(10.dp)) {
        Column(Modifier.fillMaxWidth().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            CategoryBadge(title, Modifier.size(42.dp))
            Text(title, Modifier.padding(top = 8.dp), style = MaterialTheme.typography.labelLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

/** Existing DataStore owns appearance, favorites and calculator history; clearing requires confirmation.
 * @param vm Existing DataStore mutation owner.
 * @param prefs Current committed appearance and history state.
 * @param modifier Host-provided insets and layout bounds.
 * @return Unit; destructive history clearing requires the displayed confirmation.
 */
@Composable
fun ElectroSettings(vm: AppViewModel, prefs: PreferencesState, modifier: Modifier = Modifier) {
    var confirm by remember { mutableStateOf(false) }
    Column(modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Profile & settings")
        Card(Modifier.fillMaxWidth()) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            CategoryBadge("Components", Modifier.size(48.dp)); Spacer(Modifier.width(12.dp))
            Column { Text("ELECTRO", style = MaterialTheme.typography.titleLarge); Text("Your engineering workspace", style = MaterialTheme.typography.bodySmall) }
        } }
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
        Text("Appearance", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("dark", "light", "system").forEach { mode -> FilterChip((prefs.themeMode.ifBlank { if (prefs.light) "light" else "dark" }) == mode,
                { vm.appearance(mode) }, label = { Text(mode.replaceFirstChar { it.uppercase() }) }) }
        }
        } }
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Language: English")
        Text("Offline by design", style = MaterialTheme.typography.titleMedium)
        Text("No accounts, analytics, ads or network permission. Favorites, settings, inputs and saved results stay on this device. Android sharing sends only the text you choose to share.")
        Text("Engineering access is a basic local restriction. References are bundled, not encrypted.")
        TextButton({ confirm = true }) { Text("Clear saved calculation history") }
        TextButton({ vm.sort() }) { Text(if (prefs.alphabetical) "Use original tool order" else "Sort tools A–Z") }
        } }
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("ELECTRO ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})", style = MaterialTheme.typography.titleMedium)
        Text("Electrical • Electronics • Engineering\nAndroid 7.0+ • Native Kotlin & Jetpack Compose")
        Text("Component artwork is illustrative. Confirm exact parts, ratings and wiring against the original manufacturer documentation.")
        } }
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text("Clear saved results?") }, text = { Text("Your favorites and calculator inputs will be retained.") },
        confirmButton = { TextButton({ vm.clearCalculations(); confirm = false }) { Text("Clear history") } },
        dismissButton = { TextButton({ confirm = false }) { Text("Cancel") } })
}

/** Component pages distinguish generic educational material from unverified part-specific information.
 * @param asset Selected local illustrative image.
 * @param prefs Current favorite keys.
 * @param vm Existing favorite-persistence owner.
 * @param back Returns to the preceding library screen.
 * @return Unit; all part-specific ratings remain explicitly unverified.
 */
@Composable
private fun ComponentDetail(asset: ComponentAsset, prefs: PreferencesState, vm: AppViewModel, back: () -> Unit) {
    val id = "component:${asset.id}"
    Scaffold(topBar = { Bar(asset.name, back) { IconButton({ vm.star(id) }) { Icon(painterResource(if (id in prefs.favourites) R.drawable.ic_star_filled else R.drawable.ic_star_outline), "Favorite component") } } }) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { ComponentImage(asset, Modifier.fillMaxWidth().heightIn(max = 420.dp).aspectRatio(1f).clip(RoundedCornerShape(10.dp)), detail = true) }
            item { Text(asset.category.uppercase(), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium); SectionTitle(asset.name) }
            val info = componentNotes(asset)
            items(info) { (title, body) -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(title, style = MaterialTheme.typography.titleMedium); Text(body, Modifier.padding(top = 8.dp)) } } }
        }
    }
}

/** Generic operating principles do not assign a manufacturer rating or terminal layout to artwork.
 * @param asset Component family inferred from its supplied descriptive filename.
 * @return Generic educational sections, without invented manufacturer ratings or contact assignments.
 */
private fun componentNotes(asset: ComponentAsset): List<Pair<String, String>> {
    val principle = when {
        "resistor" in asset.id -> "A resistor opposes current: V = I × R for an ohmic element. Power is dissipated as heat. Value, tolerance, temperature coefficient and power rating depend on the actual part."
        "capacitor" in asset.id -> "A capacitor stores energy in an electric field. Capacitance, working voltage, polarity, leakage and ESR depend on construction and the exact part. Stored charge may remain after disconnection."
        "inductor" in asset.id -> "An inductor stores energy in a magnetic field and opposes changes in current. Saturation current, winding resistance and insulation limits must come from its datasheet."
        "diode" in asset.id || asset.id == "led" || "rectifier" in asset.id -> "A semiconductor junction conducts asymmetrically. Rectifier, Zener and light-emitting parts have different operating requirements. Verify polarity, current limits and reverse-voltage limits for the specific device."
        "motor" in asset.id -> "An electric motor converts electrical energy into mechanical motion. Induction, DC and geared assemblies differ in supply, starting, control and protection. The nameplate and original manual define the applicable limits."
        "relay" in asset.id || "contactor" in asset.id -> "A control signal operates a switching mechanism. Coil supply and contact load are separate ratings. Contact arrangements and overload/reset behavior vary; verify the exact circuit diagram."
        "sensor" in asset.id || "probe" in asset.id || "switch" in asset.id || "encoder" in asset.id -> "The device detects a physical state or position and produces a contact or electrical output. Detection range, output type, polarity, supply and environmental rating depend on the exact model."
        "transformer" in asset.id -> "Magnetically coupled windings transfer AC energy. Primary/secondary ratings, insulation, earthing and protective devices are determined by the original design; a photograph cannot establish them."
        "mcb" in asset.id || "mccb" in asset.id -> "A circuit breaker interrupts specified overcurrent conditions. Rated current, trip curve, interrupting capacity and installation conditions must match the circuit design. It is not a substitute for all forms of shock protection."
        else -> "This illustration identifies the general component family. Electrical behavior, compatible interfaces, terminal arrangement and operating limits require the exact part number and original technical documentation."
    }
    return listOf("Description & operating principle" to principle,
        "Applications" to "Use this ${asset.category.lowercase()} reference for visual identification and learning. Select a real part only after checking the circuit requirements, environmental conditions and approved documentation.",
        "Specifications" to "No manufacturer-specific ratings have been verified for this generated illustration. Do not infer a voltage, current, safety rating or model from its appearance.",
        "Symbol, terminals & connection" to "Use the exact device's schematic symbol and numbered terminal drawing. Generated markings and connector geometry are not wiring authority. Consult the Tools → Pin-outs references and Learn → Lessons for generic interface concepts.",
        "Testing procedure" to "1. Identify the exact part. 2. Isolate all energy sources and verify the safe state. 3. Follow the manufacturer's test method with a correctly rated instrument. 4. Compare results with documented limits. This page does not authorize live testing.",
        "Troubleshooting" to "Record the symptom and actual model. Inspect for damage only in a safe isolated state. Check documented connections and supply conditions. Do not bypass interlocks or replace a part solely because it resembles this image.",
        "Safety & provenance" to "User-supplied AI-generated artwork, optimized for visual learning rather than product verification. Never rely on it for exact geometry or terminal markings. Escalator work requires qualified personnel and the equipment's approved procedures.")
}

/** Scrollable, bookmarkable lessons use authored text rather than embedding instruction screenshots.
 * @param page Authored educational content and citation URLs.
 * @param prefs Current lesson bookmarks.
 * @param vm Existing bookmark-persistence owner.
 * @param back Returns to the lesson catalogue.
 * @return Unit; citations are shared only after explicit user action.
 */
@Composable
private fun LearningDetail(page: LearningPage, prefs: PreferencesState, vm: AppViewModel, back: () -> Unit) {
    val id = "learn:${page.id}"
    Scaffold(topBar = { Bar(page.title, back) { IconButton({ vm.star(id) }) { Icon(painterResource(if (id in prefs.favourites) R.drawable.ic_star_filled else R.drawable.ic_star_outline), "Favorite lesson") } } }) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { Text("LEARN • OFFLINE REFERENCE", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium) }
            if (page.id == "multimeter") item { MultimeterLesson() }
            items(page.sections) { (title, body) -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp)) { SectionTitle(title); Text(body) } } }
        }
    }
}
