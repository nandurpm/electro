@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package com.electrokit.app.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.electrokit.app.R
import com.electrokit.app.data.*
import com.electrokit.app.domain.Engineering
import com.electrokit.app.ui.components.ComparisonTable
import com.electrokit.app.ui.components.ElectroPanel
import com.electrokit.app.viewmodel.AppViewModel

/**
 * Stable sort modes keep persistence and branching independent of translated labels.
 *
 * @property title Resource label shown to the user.
 */
private enum class ComponentSort(val title: Int) {
    NAME(R.string.component_sort_name),
    CATEGORY(R.string.component_sort_category),
    RECENT(R.string.component_sort_recent),
}

/**
 * Searchable offline library using existing favourite IDs and recent-history persistence.
 *
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
fun ComponentReferenceLibrary(
    assets: List<ComponentAsset>,
    guides: Map<String, ComponentGuide>,
    query: String,
    prefs: PreferencesState,
    vm: AppViewModel,
    modifier: Modifier = Modifier,
    onOpen: (ComponentAsset) -> Unit,
) {
    var filter by rememberSaveable { mutableStateOf("All") }
    var sort by rememberSaveable { mutableStateOf(ComponentSort.NAME) }
    var favorites by rememberSaveable { mutableStateOf(false) }
    var selected by rememberSaveable { mutableStateOf(listOf<String>()) }
    var comparing by rememberSaveable { mutableStateOf(false) }
    val grid = rememberLazyGridState()
    val filters =
        listOf(
            "All",
            "Passive",
            "Active",
            "Semiconductor",
            "ICs",
            "Sensors",
            "Power",
            "Connectors",
            "Electromechanical",
            "Display",
            "Protection",
            "Equipment",
        )
    val shown =
        remember(assets, guides, query, filter, sort, favorites, prefs.favourites, prefs.recent) {
            assets
                .filter { asset ->
                    val guide = guides[componentFamily(asset).orEmpty()]
                    val category = guide?.category ?: asset.category
                    val text =
                        listOfNotNull(
                                asset.name,
                                asset.category,
                                category,
                                guide?.aliases?.joinToString(" "),
                                guide?.principle,
                                guide?.application,
                            )
                            .joinToString(" ")
                    val matches =
                        filter == "All" ||
                            category.contains(filter, true) ||
                            (filter == "Semiconductor" &&
                                componentFamily(asset) in listOf("bjt", "mosfet", "diode", "led"))
                    matches &&
                        text.contains(query.trim(), true) &&
                        (!favorites || "component:${asset.id}" in prefs.favourites)
                }
                .sortedWith(
                    compareBy<ComponentAsset> {
                            when (sort) {
                                ComponentSort.CATEGORY ->
                                    guides[componentFamily(it).orEmpty()]?.category ?: it.category
                                ComponentSort.RECENT ->
                                    prefs.recent
                                        .indexOf("component:${it.id}")
                                        .let { n -> if (n < 0) 999 else n }
                                        .toString()
                                        .padStart(3, '0')
                                else -> it.name.lowercase()
                            }
                        }
                        .thenBy { it.name }
                )
        }
    LazyVerticalGrid(
        GridCells.Adaptive(160.dp),
        modifier,
        state = grid,
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        stringResource(R.string.components_heading),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        stringResource(
                            R.string
                                .component_explore_identify_understand_and_test_electronic_components
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(filters) { name ->
                        FilterChip(filter == name, { filter = name }, label = { Text(name) })
                    }
                }
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        favorites,
                        { favorites = !favorites },
                        label = { Text(stringResource(R.string.component_favorites_only)) },
                    )
                    var sorting by remember { mutableStateOf(false) }
                    Box {
                        TextButton({ sorting = true }) {
                            Text(
                                stringResource(
                                    R.string.component_sort_selected,
                                    stringResource(sort.title),
                                )
                            )
                        }
                        DropdownMenu(sorting, { sorting = false }) {
                            ComponentSort.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(option.title)) },
                                    onClick = {
                                        sort = option
                                        sorting = false
                                    },
                                )
                            }
                        }
                    }
                    TextButton({ comparing = true }, enabled = selected.size == 2) {
                        Text(stringResource(R.string.component_compare_count, selected.size))
                    }
                }
                if (selected.isNotEmpty())
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        assets
                            .filter { it.id in selected }
                            .forEach { asset ->
                                InputChip(
                                    true,
                                    { selected = selected - asset.id },
                                    label = { Text(asset.name) },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            stringResource(R.string.compare_remove, asset.name),
                                        )
                                    },
                                )
                            }
                        TextButton({ selected = emptyList() }) {
                            Text(stringResource(R.string.compare_clear))
                        }
                    }
                Text(
                    pluralStringResource(R.plurals.component_count_hint, shown.size, shown.size),
                    Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (shown.isEmpty())
                    WorkbenchEmpty(
                        "No matching components",
                        "Try a different category, name or marking. Favorites are shared with the main Favorites tab.",
                    )
            }
        }
        items(shown, key = { it.id }) { asset ->
            val guide = guides[componentFamily(asset).orEmpty()]
            ElectroPanel(onClick = { onOpen(asset) }, modifier = Modifier.fillMaxWidth()) {
                ComponentImage(asset, Modifier.fillMaxWidth().aspectRatio(1.1f))
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        guide?.category ?: asset.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(asset.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        guide?.principle
                            ?: "Open the existing reference for operating principles and connection precautions.",
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            asset.id in selected,
                            { checked ->
                                selected =
                                    if (!checked) selected - asset.id
                                    else if (selected.size < 2) selected + asset.id else selected
                            },
                            modifier =
                                Modifier.semantics { contentDescription = "Compare ${asset.name}" },
                            enabled = asset.id in selected || selected.size < 2,
                        )
                        Spacer(Modifier.weight(1f))
                        IconToggleButton(
                            checked = "component:${asset.id}" in prefs.favourites,
                            onCheckedChange = { vm.star("component:${asset.id}") },
                        ) {
                            Icon(
                                if ("component:${asset.id}" in prefs.favourites)
                                    rememberVectorPainter(Icons.Default.Star)
                                else painterResource(R.drawable.ic_favorite_outline),
                                if ("component:${asset.id}" in prefs.favourites)
                                    stringResource(R.string.component_remove_favorite, asset.name)
                                else stringResource(R.string.component_add_favorite, asset.name),
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.component_view_reference),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
    if (comparing)
        AlertDialog(
            onDismissRequest = { comparing = false },
            title = { Text(stringResource(R.string.component_compare_component_types)) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        stringResource(
                            R.string
                                .component_family_characteristics_only_no_device_ratings_inferred_swipe_side
                        )
                    )
                    val pair = assets.filter { it.id in selected }
                    val pairGuides = pair.map { guides[componentFamily(it).orEmpty()] }
                    ComparisonTable(
                        pair.map { it.name },
                        listOf(
                            stringResource(R.string.component_principle) to
                                pairGuides.map {
                                    it?.principle ?: stringResource(R.string.component_not_supplied)
                                },
                            stringResource(R.string.component_parameters) to
                                pairGuides.map {
                                    it?.specifications?.joinToString("\n")
                                        ?: stringResource(R.string.component_not_supplied)
                                },
                            stringResource(R.string.component_application) to
                                pairGuides.map {
                                    it?.application
                                        ?: stringResource(R.string.component_not_supplied)
                                },
                        ),
                    )
                }
            },
            confirmButton = {
                TextButton({ comparing = false }) { Text(stringResource(R.string.component_close)) }
            },
        )
}

/**
 * Expandable reference block keeps detailed information available without an endless initial page.
 *
 * @param title Section heading.
 * @param initialExpanded Initial visibility, retained across rotation.
 * @param content Authored content, never fetched from a network.
 * @return Unit with an accessible Material toggle.
 */
@Composable
private fun GuideSection(
    title: String,
    initialExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    var expanded by rememberSaveable(title) { mutableStateOf(initialExpanded) }
    ElectroPanel(Modifier.fillMaxWidth().animateContentSize()) {
        TextButton(
            { expanded = !expanded },
            Modifier.fillMaxWidth().semantics {
                stateDescription = if (expanded) "Expanded" else "Collapsed"
            },
        ) {
            Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                null,
            )
        }
        if (expanded)
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = content,
            )
    }
}

/**
 * Structured component detail with preserved legacy content and explicitly verified pinout
 * selection.
 *
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
fun ComponentReferenceDetail(
    asset: ComponentAsset,
    guide: ComponentGuide?,
    legacy: List<Pair<String, String>>,
    prefs: PreferencesState,
    vm: AppViewModel,
    back: () -> Unit,
    calculator: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            Bar(asset.name, back) {
                IconToggleButton(
                    checked = "component:${asset.id}" in prefs.favourites,
                    onCheckedChange = { vm.star("component:${asset.id}") },
                ) {
                    Icon(
                        if ("component:${asset.id}" in prefs.favourites)
                            rememberVectorPainter(Icons.Default.Star)
                        else painterResource(R.drawable.ic_favorite_outline),
                        stringResource(
                            if ("component:${asset.id}" in prefs.favourites)
                                R.string.component_remove_favorite
                            else R.string.component_add_favorite,
                            asset.name,
                        ),
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { ComponentImage(asset, Modifier.fillMaxWidth().height(220.dp), detail = true) }
            item {
                Text(guide?.category ?: asset.category, color = MaterialTheme.colorScheme.primary)
                Text(asset.name, style = MaterialTheme.typography.headlineSmall)
                Text(
                    stringResource(
                        R.string
                            .component_general_family_reference_illustration_does_not_identify_an_exact_
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (guide != null) {
                item {
                    GuideSection(stringResource(R.string.component_how_it_works), true) {
                        Text(guide.principle, style = MaterialTheme.typography.bodyLarge)
                        TextButton({ calculator("r7") }) {
                            Text(stringResource(R.string.component_open_schematic_symbol_reference))
                        }
                    }
                }
                item {
                    GuideSection(stringResource(R.string.component_electrical_specifications)) {
                        Text(
                            stringResource(
                                R.string
                                    .component_parameters_to_verify_in_the_exact_part_datasheet_these_are_not_ra
                            ),
                            color = MaterialTheme.colorScheme.primary,
                        )
                        guide.specifications.forEach { Text("• $it") }
                    }
                }
                item {
                    GuideSection(stringResource(R.string.component_identification_markings)) {
                        Text(guide.identification)
                        if (componentFamily(asset) in listOf("resistor", "potentiometer")) {
                            TextButton({ calculator("c1") }) {
                                Text(
                                    stringResource(R.string.component_open_resistor_band_calculator)
                                )
                            }
                            TextButton({ calculator("c2") }) {
                                Text(stringResource(R.string.component_open_smd_marking_decoder))
                            }
                        }
                        if (componentFamily(asset) == "capacitor") {
                            var code by rememberSaveable { mutableStateOf("") }
                            OutlinedTextField(
                                code,
                                { code = it },
                                label = {
                                    Text(
                                        stringResource(
                                            R.string.component_three_digit_capacitor_code
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            val valid =
                                code.matches(Regex("[0-9]{2}[0-7]")) &&
                                    code.take(2).toIntOrNull()?.let { it > 0 } == true
                            Text(
                                if (valid)
                                    Engineering.format(
                                        code.take(2).toDouble() *
                                            Math.pow(10.0, code.last().digitToInt().toDouble()) *
                                            1e-12,
                                        "F",
                                    )
                                else
                                    "Use a standard nonzero three-digit pF code ending 0–7. Special codes, voltage and tolerance need the maker's documentation."
                            )
                        }
                    }
                }
                item {
                    GuideSection(stringResource(R.string.component_pin_configuration)) {
                        if (asset.id == "dip_integrated_circuit") VerifiedTimerPins()
                        else
                            Text(
                                stringResource(
                                    R.string
                                        .component_no_exact_part_package_is_selected_terminal_numbers_are_deliberate
                                )
                            )
                    }
                }
                item {
                    GuideSection(
                        stringResource(R.string.component_how_to_test_using_a_digital_multimeter)
                    ) {
                        var reviewed by rememberSaveable(asset.id) { mutableStateOf(listOf<Int>()) }
                        Text(
                            stringResource(
                                R.string
                                    .component_reading_checklist_not_a_pass_fail_measurement_or_safety_certifica
                            ),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        guide.testing.forEachIndexed { index, step ->
                            Row(verticalAlignment = Alignment.Top) {
                                Checkbox(
                                    index in reviewed,
                                    { checked ->
                                        reviewed =
                                            if (checked) reviewed + index else reviewed - index
                                    },
                                    modifier =
                                        Modifier.semantics {
                                            contentDescription =
                                                "Reviewed test guidance step ${index + 1}"
                                        },
                                )
                                Text(
                                    "${index + 1}. $step",
                                    Modifier.weight(1f).padding(top = 12.dp),
                                )
                            }
                        }
                        TextButton({ calculator("c4") }) {
                            Text(stringResource(R.string.component_open_ohm_s_law_calculator))
                        }
                    }
                }
                item {
                    GuideSection(
                        stringResource(R.string.component_educational_circuit_application)
                    ) {
                        if (componentFamily(asset) in listOf("resistor", "capacitor"))
                            PassiveExampleDiagram(componentFamily(asset) == "capacitor")
                        Text(guide.application)
                        Text(
                            stringResource(
                                R.string
                                    .component_educational_model_only_verify_real_parts_limits_protection_and_co
                            ),
                            color = MaterialTheme.colorScheme.primary,
                        )
                        val route =
                            when (componentFamily(asset)) {
                                "resistor" -> "c7"
                                "capacitor" -> "c11"
                                "led" -> "c13"
                                "ic" -> "c15"
                                "inductor" -> "c5"
                                else -> "c4"
                            }
                        TextButton({ calculator(route) }) {
                            Text(stringResource(R.string.component_open_related_calculator))
                        }
                    }
                }
                item {
                    GuideSection(stringResource(R.string.ui_references)) {
                        Text(guide.source)
                        Text(
                            stringResource(
                                R.string
                                    .component_manufacturer_reference_stored_offline_as_a_citation_no_new_device
                            )
                        )
                    }
                }
            }
            item {
                GuideSection(
                    stringResource(R.string.component_original_component_reference),
                    guide == null,
                ) {
                    legacy.forEach { (title, body) ->
                        Text(title, fontWeight = FontWeight.Bold)
                        Text(body)
                    }
                }
            }
        }
    }
}

/**
 * Interactive pinout checked against TI SLFS022K, March 2026, Table 4-1 and P-package top view.
 *
 * @return A selectable NE555 PDIP-8 diagram only; not a universal IC mapping.
 */
@Composable
private fun VerifiedTimerPins() {
    var chosen by rememberSaveable { mutableStateOf(false) }
    var selected by rememberSaveable { mutableIntStateOf(1) }
    var enlarged by rememberSaveable { mutableStateOf(false) }
    val pins =
        listOf(
            "GND — Ground",
            "TRIG — Trigger input",
            "OUT — Timer output",
            "RESET — Active-low reset",
            "CONT — Control voltage",
            "THRES — Threshold input",
            "DISCH — Discharge output",
            "VCC — Supply",
        )
    Text(
        stringResource(
            R.string.component_select_a_verified_part_package_before_showing_numbered_terminals
        )
    )
    FilterChip(
        chosen,
        { chosen = !chosen },
        label = { Text(stringResource(R.string.component_ti_ne555_pdip_8_top_view)) },
    )
    if (chosen) {
        Text(
            stringResource(
                R.string.component_top_view_notch_at_top_pins_count_counterclockwise_generic_library
            )
        )
        for (i in 0..3) Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (pin in listOf(i + 1, 8 - i)) OutlinedButton(
                { selected = pin },
                Modifier.weight(1f).semantics { this.selected = selected == pin },
            ) {
                Text("$pin ${pins[pin - 1].substringBefore(" —")}")
            }
        }
        Text("Pin $selected: ${pins[selected - 1]}", style = MaterialTheme.typography.titleMedium)
        TextButton({ enlarged = true }) { Text(stringResource(R.string.component_enlarge_pinout)) }
        Text(
            stringResource(
                R.string.component_source_ti_slfs022k_revised_march_2026_p_pdip_package_table_4_1_ht
            ),
            style = MaterialTheme.typography.bodySmall,
        )
    }
    if (enlarged)
        AlertDialog(
            onDismissRequest = { enlarged = false },
            title = { Text(stringResource(R.string.component_ne555_pdip_8_top_view)) },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(stringResource(R.string.pinout_top_view))
                    for (row in 0..3) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (pin in listOf(row + 1, 8 - row)) OutlinedButton(
                            { selected = pin },
                            Modifier.weight(1f).semantics { this.selected = selected == pin },
                        ) {
                            Text(
                                "$pin ${pins[pin - 1].substringBefore(" —")}",
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                    Text(
                        "Pin $selected: ${pins[selected - 1]}",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            },
            confirmButton = {
                TextButton({ enlarged = false }) { Text(stringResource(R.string.component_close)) }
            },
        )
}

/**
 * Original generic circuit topology; terminal placement is functional, not a package pinout.
 *
 * @param rc True draws an RC charging network; false draws a two-resistor divider.
 * @return Offline schematic using IEC resistor rectangles and capacitor plates.
 */
@Composable
private fun PassiveExampleDiagram(rc: Boolean) {
    val ink = MaterialTheme.colorScheme.onSurface
    val accent = MaterialTheme.colorScheme.primary
    Text(
        if (rc) "Educational RC charging network" else "Educational unloaded voltage divider",
        fontWeight = FontWeight.Bold,
    )
    Canvas(
        Modifier.fillMaxWidth().height(220.dp).semantics {
            contentDescription =
                if (rc)
                    "RC network: input through resistor to output, with capacitor from output to ground"
                else "Voltage divider: R1 from input to output, R2 from output to ground"
        }
    ) {
        val x = size.width * .45f
        val unit = size.height / 210f
        val paint =
            android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = ink.toArgb()
                textSize = 12.sp.toPx()
            }
        /**
         * Draws the vertical conductor between normalized circuit coordinates.
         *
         * @param y1 Starting coordinate.
         * @param y2 Ending coordinate.
         * @return Unit after drawing a conductor, not a package lead assignment.
         */
        fun wire(y1: Float, y2: Float) {
            drawLine(ink, Offset(x, y1 * unit), Offset(x, y2 * unit), 2.dp.toPx())
        }
        drawCircle(accent, 4.dp.toPx(), Offset(x, 16 * unit))
        drawContext.canvas.nativeCanvas.drawText("Vin", x + 24.dp.toPx(), 22 * unit, paint)
        wire(16f, 36f)
        drawRect(
            ink,
            Offset(x - 12 * unit, 36 * unit),
            Size(24 * unit, 38 * unit),
            style = Stroke(2.dp.toPx()),
        )
        drawContext.canvas.nativeCanvas.drawText(
            if (rc) "R" else "R1",
            x + 23.dp.toPx(),
            62 * unit,
            paint,
        )
        wire(74f, 108f)
        drawCircle(accent, 4.dp.toPx(), Offset(x, 100 * unit))
        drawLine(
            ink,
            Offset(x, 100 * unit),
            Offset(size.width - 20.dp.toPx(), 100 * unit),
            2.dp.toPx(),
        )
        drawContext.canvas.nativeCanvas.drawText(
            if (rc) "Vcap" else "Vout",
            size.width - 60.dp.toPx(),
            90 * unit,
            paint,
        )
        if (rc) {
            wire(108f, 126f)
            drawLine(
                ink,
                Offset(x - 24 * unit, 126 * unit),
                Offset(x + 24 * unit, 126 * unit),
                2.dp.toPx(),
            )
            drawLine(
                ink,
                Offset(x - 24 * unit, 139 * unit),
                Offset(x + 24 * unit, 139 * unit),
                2.dp.toPx(),
            )
            wire(139f, 179f)
        } else {
            wire(108f, 119f)
            drawRect(
                ink,
                Offset(x - 12 * unit, 119 * unit),
                Size(24 * unit, 38 * unit),
                style = Stroke(2.dp.toPx()),
            )
            wire(157f, 179f)
        }
        drawContext.canvas.nativeCanvas.drawText(
            if (rc) "C" else "R2",
            x + 30.dp.toPx(),
            146 * unit,
            paint,
        )
        for (i in 0..2) drawLine(
            ink,
            Offset(x - (24 - i * 7) * unit, (179 + i * 8) * unit),
            Offset(x + (24 - i * 7) * unit, (179 + i * 8) * unit),
            2.dp.toPx(),
        )
        drawContext.canvas.nativeCanvas.drawText("GND", x + 32.dp.toPx(), 188 * unit, paint)
    }
}
