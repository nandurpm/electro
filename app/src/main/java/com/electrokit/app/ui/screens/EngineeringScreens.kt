@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package com.electrokit.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.electrokit.app.R
import com.electrokit.app.data.*
import com.electrokit.app.domain.*
import com.electrokit.app.ui.components.*
import com.electrokit.app.viewmodel.*

/**
 * Hosts source-traceable engineering routes using the existing preferences and navigation boundary.
 *
 * @param route Stable section/detail/tool identifier, decoded by the caller.
 * @param engineering Asset-loading ViewModel shared by the application navigation host.
 * @param vm Existing ViewModel owns bookmarks and durable calculator inputs.
 * @param prefs Existing saved state, so upgrades retain the same user data.
 * @param navigate Navigates to another engineering route.
 * @param legacy Opens an existing calculator/reference without removing the original catalogue.
 * @param back Returns to the preceding page; null embeds the home inside a main pager tab.
 * @return Unit. Failed asset loading shows an explicit retry action; unknown IDs show a recoverable
 *   empty state.
 */
@Composable
fun EngineeringScreen(
    route: String,
    engineering: EngineeringViewModel,
    vm: AppViewModel,
    prefs: PreferencesState,
    navigate: (String) -> Unit,
    legacy: (String) -> Unit,
    back: (() -> Unit)? = null,
) {
    EngineeringGate(back) {
        EngineeringContentScreen(route, engineering, vm, prefs, navigate, legacy, back)
    }
}

/** Renders engineering data only after the shared session boundary has granted access.
 * @param route Protected stable section, tool or reference identifier.
 * @param engineering Source-traceable engineering asset-loading owner.
 * @param vm Existing input and bookmark persistence owner.
 * @param prefs Latest committed saved state.
 * @param navigate Opens another route behind the same gate.
 * @param legacy Opens an original public calculator or reference.
 * @param back Optional return action for a standalone destination.
 * @return Unit; loading errors expose an explicit retry without discarding content.
 */
@Composable
private fun EngineeringContentScreen(
    route: String,
    engineering: EngineeringViewModel,
    vm: AppViewModel,
    prefs: PreferencesState,
    navigate: (String) -> Unit,
    legacy: (String) -> Unit,
    back: (() -> Unit)? = null,
) {
    val loaded by engineering.content.collectAsStateWithLifecycle()
    val failed by engineering.failed.collectAsStateWithLifecycle()
    val pair = loaded
    if (pair == null) {
        Column(Modifier.fillMaxSize().padding(24.dp)) {
            if (back != null) TextButton(back) { Text(stringResource(R.string.back)) }
            if (failed) {
                Text(stringResource(R.string.engine_load_error))
                Button({ engineering.reload() }) { Text(stringResource(R.string.ui_retry)) }
            } else CircularProgressIndicator()
        }
        return
    }
    val (data, tools) = pair
    val record = data.records.firstOrNull { route == "record:" + it.id }
    val fault = data.faults.firstOrNull { route == "fault:" + it.code }
    val source = data.sources.firstOrNull { route == "source:" + it.id }
    val tool = tools.firstOrNull { route == "tool:" + it.id }
    val title =
        record?.title
            ?: fault?.let { it.code + " — " + it.message }
            ?: source?.title
            ?: tool?.title
            ?: data.sections.firstOrNull { it.id == route }?.title
            ?: stringResource(R.string.engine_title)
    Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0), topBar = { if (back != null) Bar(title, back) }) { padding ->
        when {
            route == "home" ->
                EngineeringHome(
                    data,
                    tools,
                    vm.repository.registry,
                    navigate,
                    legacy,
                    Modifier.padding(padding),
                )
            route == "faults" -> FaultExplorer(data, vm, prefs, navigate, Modifier.padding(padding))
            tool != null -> EngineeringCalculator(tool, vm, prefs, Modifier.padding(padding))
            else ->
                LazyColumn(
                    Modifier.padding(padding).fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (fault != null) {
                        item { EngineeringHeading(fault.code, fault.message) }
                        item {
                            DetailActions(
                                fault.code +
                                    " — " +
                                    fault.message +
                                    "\n" +
                                    fault.group +
                                    " | " +
                                    fault.reset +
                                    "\n" +
                                    fault.description +
                                    "\n" +
                                    fault.possibleCauses +
                                    "\n" +
                                    fault.checks +
                                    "\n" +
                                    fault.status,
                                fault.code in prefs.favourites,
                            ) {
                                vm.star(fault.code)
                            }
                        }
                        item {
                            EngineeringNote(
                                stringResource(R.string.engine_classification),
                                fault.group + " • " + fault.reset + " • " + fault.system,
                            )
                        }
                        item {
                            EngineeringNote(
                                stringResource(R.string.engine_reset_notice),
                                stringResource(R.string.engine_reset_explanation),
                            )
                        }
                        item {
                            // The source supplies a fault label, not an OEM diagnosis; one shared note avoids fabricated specificity.
                            EngineeringNote(stringResource(R.string.engine_checks), stringResource(R.string.engine_shared_guidance))
                        }
                        item {
                            IndustrialFigure(
                                if (fault.system == "Brake") "brake"
                                else if (fault.system == "Motor" || fault.system == "Drive") "motor"
                                else "sensor",
                                fault.code,
                            )
                        }
                        item {
                            EngineeringNote(
                                stringResource(R.string.engine_association),
                                fault.status,
                            )
                        }
                        items(fault.related) { id ->
                            data.records
                                .firstOrNull { it.id == id }
                                ?.let { record ->
                                    EngineeringLink(record.title, record.summary) {
                                        navigate("record:" + id)
                                    }
                                }
                        }
                        item {
                            Text(
                                stringResource(R.string.engine_related_faults),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        items(
                            data.faults.filter {
                                it.code != fault.code &&
                                    (it.related.intersect(fault.related.toSet()).isNotEmpty())
                            }
                        ) { other ->
                            EngineeringLink(
                                other.code + " — " + other.message,
                                other.group + " • " + other.reset,
                            ) {
                                navigate("fault:" + other.code)
                            }
                        }
                        items(fault.sources) { id ->
                            EngineeringLink(
                                stringResource(R.string.engine_source) + ": " + id,
                                "",
                            ) {
                                navigate("source:" + id)
                            }
                        }
                    } else if (record != null) {
                        item { EngineeringHeading(record.title, if (record.title.substringAfter(" — ") == record.summary) "" else record.summary) }
                        item {
                            DetailActions(
                                record.title +
                                    "\n" +
                                    record.summary +
                                    "\n" +
                                    record.body.joinToString("\n") +
                                    "\n" +
                                    record.status +
                                    "\n" +
                                    record.sources.joinToString(),
                                record.id in prefs.favourites,
                            ) {
                                vm.star(record.id)
                            }
                        }
                        item {
                            EngineeringNote(
                                stringResource(R.string.engine_verification),
                                record.status,
                            )
                        }
                        if (record.diagram == "cutaway") item { EscalatorCutaway(data, navigate) }
                        else if (record.diagram.isNotBlank() && record.diagram != "process")
                            item {
                                IndustrialFigure(
                                    if (record.title.startsWith("I13 —")) "switch" else record.diagram,
                                    record.title.substringBefore(" — "),
                                )
                            }
                        if (record.diagram == "process") item { ProcessSteps(record.body) }
                        else
                            items(record.body) { paragraph ->
                                Text(paragraph, style = MaterialTheme.typography.bodyLarge)
                            }
                        item {
                            Text(
                                stringResource(R.string.engine_association),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        items(record.related) { id ->
                            data.records
                                .firstOrNull { it.id == id }
                                ?.let { other ->
                                    EngineeringLink(other.title, other.summary) {
                                        navigate("record:" + id)
                                    }
                                }
                        }
                        items(data.faults.filter { record.id in it.related }) { other ->
                            EngineeringLink(
                                other.code + " — " + other.message,
                                other.group + " • " + other.reset,
                            ) {
                                navigate("fault:" + other.code)
                            }
                        }
                        items(record.sources) { id ->
                            EngineeringLink(
                                stringResource(R.string.engine_source) + ": " + id,
                                "",
                            ) {
                                navigate("source:" + id)
                            }
                        }
                    } else if (source != null) {
                        item { EngineeringHeading(source.id, source.title) }
                        item { EngineeringNote(source.type, source.status) }
                        item { Text(source.text, style = MaterialTheme.typography.bodyLarge) }
                        if (source.sha256.isNotBlank())
                            item {
                                EngineeringNote(
                                    stringResource(R.string.engine_checksum),
                                    source.sha256,
                                )
                            }
                        if (source.url.isNotBlank())
                            item {
                                DetailActions(source.url, null, null)
                                Text(source.url)
                            }
                        item {
                            Text(
                                stringResource(R.string.engine_source_records),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        items(data.records.filter { source.id in it.sources }) { other ->
                            EngineeringLink(other.title, other.summary) {
                                navigate("record:" + other.id)
                            }
                        }
                    } else {
                        item {
                            EngineeringHeading(
                                title,
                                stringResource(R.string.engine_reference_notice),
                            )
                        }
                        if (route == "mechanical") item { EscalatorCutaway(data, navigate) }
                        if (route == "io") {
                            item {
                                TerminalSelector(
                                    data.records.filter { it.id.startsWith("input-") },
                                    navigate,
                                )
                            }
                            item {
                                TerminalSelector(
                                    data.records.filter { it.id.startsWith("output-") },
                                    navigate,
                                )
                            }
                        }
                        if (route == "formulas" || route == "motor") {
                            items(tools) { t ->
                                EngineeringLink(t.title, t.formula) { navigate("tool:" + t.id) }
                            }
                            item {
                                EngineeringLink(
                                    stringResource(R.string.engine_existing_power),
                                    stringResource(R.string.engine_preserved),
                                ) {
                                    legacy("c21")
                                }
                            }
                        }
                        if (route == "cbm") item { CbmDemo(data, navigate) }
                        if (route == "library")
                            items(data.sources) { src ->
                                EngineeringLink(src.id + " — " + src.title, src.status) {
                                    navigate("source:" + src.id)
                                }
                            }
                        if (route == "wiring")
                            items(data.records.filter { it.section == "panel" }) { r ->
                                EngineeringLink(r.title, r.summary) { navigate("record:" + r.id) }
                            }
                        items(data.records.filter { it.section == route }) { r ->
                            EngineeringLink(r.title, r.summary) { navigate("record:" + r.id) }
                        }
                        if (data.sections.none { it.id == route })
                            item { Text(stringResource(R.string.engine_not_found)) }
                    }
                }
        }
    }
}

/**
 * Provides search across engineering text, exact fault fields, transcriptions, tools and legacy
 * entries.
 *
 * @param data Complete immutable engineering records and sources.
 * @param tools New calculator definitions.
 * @param registry Existing unchanged calculator/reference catalogue.
 * @param navigate Engineering route callback.
 * @param legacy Existing-entry route callback.
 * @param modifier Layout constraints from the host.
 * @return Unit. Search is local and case-insensitive; empty queries display the complete module
 *   dashboard.
 */
@Composable
private fun EngineeringHome(
    data: EngineeringContent,
    tools: List<EngineeringTool>,
    registry: List<Entry>,
    navigate: (String) -> Unit,
    legacy: (String) -> Unit,
    modifier: Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val q = query.trim()
    Column(modifier.fillMaxSize()) {
        Search(query, { query = it }, stringResource(R.string.engine_global_search))
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (q.isBlank()) {
                item {
                    EngineeringHeading(
                        stringResource(R.string.engine_title),
                        stringResource(R.string.engine_intro),
                    )
                }
                item {
                    Text(stringResource(R.string.engine_quick_actions), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                items(data.sections.filter { it.id in listOf("faults", "formulas", "library") }) { section ->
                    EngineeringLink(section.title, stringResource(when (section.id) {
                        "faults" -> R.string.engine_fault_action
                        "formulas" -> R.string.engine_formula_action
                        else -> R.string.engine_reference_action
                    }), iconKey = section.id) { navigate(section.id) }
                }
                item {
                    Text(stringResource(R.string.engine_browse_systems), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                items(data.sections.filter { it.id !in listOf("faults", "formulas", "library") }) { section ->
                    EngineeringLink(section.title, stringResource(when (section.id) {
                        "mechanical" -> R.string.engine_mechanical_action
                        "electrical", "panel", "wiring" -> R.string.engine_electrical_action
                        "io", "sensors" -> R.string.engine_io_action
                        "motor", "brakes" -> R.string.engine_drive_action
                        "testing" -> R.string.engine_testing_action
                        else -> R.string.engine_cbm_action
                    }), iconKey = section.id) { navigate(section.id) }
                }
                item {
                    var expanded by rememberSaveable { mutableStateOf(false) }
                    OutlinedButton({ expanded = !expanded }, Modifier.fillMaxWidth()) {
                        Text(stringResource(if (expanded) R.string.engine_hide_diagram else R.string.engine_show_diagram))
                    }
                    if (expanded) EscalatorCutaway(data, navigate)
                }
            } else {
                items(
                    data.faults.filter {
                        (it.code + " " + it.message + " " + it.system).contains(q, true)
                    }
                ) { fault ->
                    EngineeringLink(
                        fault.code + " — " + fault.message,
                        fault.group + " • " + fault.reset,
                    ) {
                        navigate("fault:" + fault.code)
                    }
                }
                items(
                    data.records.filter {
                        (it.id + " " + it.title + " " + it.summary + " " + it.body.joinToString())
                            .contains(q, true)
                    }
                ) { record ->
                    EngineeringLink(record.title, record.summary) {
                        navigate("record:" + record.id)
                    }
                }
                items(tools.filter { (it.title + " " + it.formula).contains(q, true) }) { tool ->
                    EngineeringLink(tool.title, tool.formula) { navigate("tool:" + tool.id) }
                }
                items(
                    data.sources.filter {
                        (it.id + " " + it.title + " " + it.text).contains(q, true)
                    }
                ) { src ->
                    EngineeringLink(src.id + " — " + src.title, src.status) {
                        navigate("source:" + src.id)
                    }
                }
                items(registry.filter { it.title.contains(q, true) }.distinctBy { it.contentId }) { entry ->
                    EngineeringLink(entry.title, stringResource(R.string.engine_existing_tool)) {
                        legacy(entry.id)
                    }
                }
                item { Text(stringResource(R.string.engine_search_end)) }
            }
        }
    }
}

/**
 * Searches and filters the exact supplied dataset without altering codes or original serials.
 *
 * @param data Complete engineering corpus.
 * @param vm Existing bookmark persistence owner.
 * @param prefs Current bookmark state.
 * @param navigate Fault detail callback.
 * @param modifier Host layout constraints.
 * @return Unit; empty combinations display an explicit empty-state message.
 */
@Composable
private fun FaultExplorer(
    data: EngineeringContent,
    vm: AppViewModel,
    prefs: PreferencesState,
    navigate: (String) -> Unit,
    modifier: Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var group by rememberSaveable { mutableStateOf("") }
    var reset by rememberSaveable { mutableStateOf("") }
    var system by rememberSaveable { mutableStateOf("") }
    var bookmarked by rememberSaveable { mutableStateOf(false) }
    val results =
        EngineeringSearch.faults(
            data.faults,
            query,
            group,
            reset,
            system,
            if (bookmarked) prefs.favourites else null,
        )
    Column(modifier.fillMaxSize()) {
        Search(query, { query = it }, stringResource(R.string.engine_fault_search))
        Column(
            Modifier.heightIn(max = 320.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            ChoiceFilter(
                stringResource(R.string.engine_group),
                listOf("") + data.faults.map { it.group }.distinct(),
                group,
            ) {
                group = it
            }
            ChoiceFilter(
                stringResource(R.string.engine_reset_type),
                listOf("") + data.faults.map { it.reset }.distinct(),
                reset,
            ) {
                reset = it
            }
            ChoiceFilter(
                stringResource(R.string.engine_system),
                listOf("") + data.faults.map { it.system }.distinct(),
                system,
                dropdown = true,
            ) {
                system = it
            }
            FilterChip(
                bookmarked,
                { bookmarked = !bookmarked },
                { Text(stringResource(R.string.engine_bookmarked)) },
            )
            Text(
                pluralStringResource(R.plurals.engine_results, results.size, results.size),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (results.isEmpty()) item { Text(stringResource(R.string.engine_no_matches)) }
            items(results, key = { it.code }) { fault ->
                Card(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        Column(
                            Modifier.weight(1f)
                                .clickable { navigate("fault:" + fault.code) }
                                .padding(16.dp)
                        ) {
                            Text(
                                fault.code,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(fault.message, style = MaterialTheme.typography.titleMedium)
                            Text(
                                fault.group + " • " + fault.reset + " • " + fault.system,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        IconButton({ vm.star(fault.code) }) {
                            Icon(
                                painterResource(
                                    if (fault.code in prefs.favourites) R.drawable.ic_star_filled
                                    else R.drawable.ic_star_outline
                                ),
                                stringResource(R.string.engine_bookmark_fault, fault.code),
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Renders explicit filter chips that remain readable under font enlargement.
 *
 * @param title Filter category resource text.
 * @param values Dataset values; empty string represents All.
 * @param selected Stable selected value.
 * @param dropdown True uses a compact dropdown for the longer system/location list.
 * @param change Receives an exact selected dataset value.
 * @return Unit; no filtering or persistence is performed here.
 */
@Composable
private fun ChoiceFilter(
    title: String,
    values: List<String>,
    selected: String,
    dropdown: Boolean = false,
    change: (String) -> Unit,
) {
    if (dropdown) {
        var expanded by remember { mutableStateOf(false) }
        Box {
            OutlinedButton({ expanded = true }) {
                Text(title + ": " + selected.ifBlank { stringResource(R.string.engine_all) })
            }
            DropdownMenu(expanded, { expanded = false }) {
                values.forEach { value ->
                    DropdownMenuItem(
                        text = { Text(value.ifBlank { stringResource(R.string.engine_all) }) },
                        onClick = {
                            change(value)
                            expanded = false
                        },
                    )
                }
            }
        }
        return
    }
    Text(title, style = MaterialTheme.typography.labelLarge)
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        values.forEach { value ->
            FilterChip(
                selected == value,
                { change(value) },
                { Text(value.ifBlank { stringResource(R.string.engine_all) }) },
            )
        }
    }
}

/**
 * Evaluates explicit new-tool fields and saves them through the existing durable input journal.
 *
 * @param tool Structured calculator model and assumptions.
 * @param vm Existing input persistence owner.
 * @param prefs Latest saved/overlaid input snapshot.
 * @param modifier Host layout constraints.
 * @return Unit; invalid or missing values show the pure domain Error without crashing.
 */
@Composable
private fun EngineeringCalculator(
    tool: EngineeringTool,
    vm: AppViewModel,
    prefs: PreferencesState,
    modifier: Modifier,
) {
    val key = "engineering-tool:" + tool.id
    val values = prefs.values[key].orEmpty()
    val result = EngineeringTools.calculate(tool.id, values)
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { EngineeringHeading(tool.title, tool.formula) }
        item { EngineeringNote(stringResource(R.string.engine_assumptions), tool.assumptions) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton({ vm.values(key, tool.fields.associate { it.key to it.sample }) }) {
                    Text(stringResource(R.string.engine_load_example))
                }
                TextButton({ vm.values(key, emptyMap()) }) {
                    Text(stringResource(R.string.engine_reset))
                }
            }
        }
        items(tool.fields) { field ->
            if (field.options.isNotEmpty()) {
                var expanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton({ expanded = true }) {
                        Text(values[field.key] ?: stringResource(R.string.engine_choose_conversion))
                    }
                    DropdownMenu(expanded, { expanded = false }) {
                        field.options.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    vm.values(key, values + (field.key to option))
                                    expanded = false
                                },
                            )
                        }
                    }
                }
            } else
                OutlinedTextField(
                    values[field.key].orEmpty(),
                    { vm.values(key, values + (field.key to it)) },
                    Modifier.fillMaxWidth(),
                    label = {
                        Text(field.label + if (field.unit.isBlank()) "" else " (${field.unit})")
                    },
                    singleLine = true,
                )
        }
        item {
            Card(
                Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            if (result is Calculation.Error)
                                MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.secondaryContainer
                    ),
            ) {
                Text(
                    result.text,
                    Modifier.padding(20.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        if (result is Calculation.Value)
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button({
                        clipboard.setText(
                            AnnotatedString(
                                tool.title + "\n" + result.text + "\n" + tool.assumptions
                            )
                        )
                    }) {
                        Text(stringResource(R.string.engine_copy_result))
                    }
                    OutlinedButton({
                        share(context, tool.title + "\n" + result.text + "\n" + tool.assumptions)
                    }) {
                        Text(stringResource(R.string.share_results))
                    }
                }
            }
    }
}

/**
 * Presents controller functions as logical numbered selections without inventing a physical pinout.
 *
 * @param records Input or grouped output records in source order.
 * @param navigate Opens the selected terminal explanation.
 * @return Unit; each accessible target is at least the Material button minimum touch height.
 */
@Composable
private fun TerminalSelector(records: List<EngineeringRecord>, navigate: (String) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.engine_logical_terminals),
                style = MaterialTheme.typography.titleMedium,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                records.forEach { r ->
                    OutlinedButton({ navigate("record:" + r.id) }) {
                        Text(r.title.substringBefore(" — "))
                    }
                }
            }
            Text(
                stringResource(R.string.engine_no_physical_pinout),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

/**
 * Makes conceptual sequence steps individually selectable without presenting live machine state.
 *
 * @param steps Educational authored steps, not verified contactor timing.
 * @return Unit; selection is saved across recreation and remains within the supplied list bounds.
 */
@Composable
private fun ProcessSteps(steps: List<String>) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.engine_conceptual), color = MaterialTheme.colorScheme.primary)
        steps.forEachIndexed { index, text ->
            Card(
                onClick = { selected = index },
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            if (selected == index) MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                    ),
            ) {
                Text(
                    "${index + 1}. $text",
                    Modifier.fillMaxWidth().padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

/**
 * Displays a consistent prominent title and technical summary.
 *
 * @param title Section title or component identifier.
 * @param subtitle Original content or contextual explanatory text.
 * @return Unit; text wraps rather than truncating at large font sizes.
 */
@Composable
private fun EngineeringHeading(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(subtitle, style = MaterialTheme.typography.bodyLarge)
    }
}

/**
 * Displays a labelled technical fact, scope note or verification statement.
 *
 * @param title Resource/data heading.
 * @param text Exact supplied or authored explanation.
 * @return Unit; no technical values are inferred by presentation.
 */
@Composable
private fun EngineeringNote(title: String, text: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/**
 * Opens a real route from a clearly labelled card instead of a nonfunctional decorative control.
 *
 * @param title Primary technical title.
 * @param subtitle Secondary content; blank suppresses the extra paragraph.
 * @param iconKey Optional stable section key selects a circuit motif; empty uses a reference motif.
 * @param open Caller-provided destination action.
 * @return Unit; callback/navigation failures propagate to the owning host.
 */
@Composable
private fun EngineeringLink(title: String, subtitle: String, iconKey: String = "", open: () -> Unit) {
    OutlinedCard(
        onClick = open, modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            // A consistent leading marker separates destinations from static technical notes.
            Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Box(Modifier.padding(8.dp)) {
                    ToolIcon(when (iconKey) {
                        "faults", "testing" -> "c16"
                        "formulas" -> "c4"
                        "motor", "mechanical" -> "c18"
                        "brakes", "sensors" -> "c13"
                        "io", "panel" -> "c15"
                        "electrical", "wiring" -> "c7"
                        "cbm" -> "c5"
                        else -> "r8"
                    })
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (subtitle.isNotBlank())
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 6.dp),
                    )
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
        }
    }
}

/**
 * Exposes user-directed copy/share and optional persistent bookmark controls.
 *
 * @param text Full reference with its authority/assumption notes.
 * @param starred Null suppresses bookmarking for source URLs.
 * @param star Persistent bookmark callback, nullable when not offered.
 * @return Unit; clipboard/sharing are invoked only after the relevant user tap.
 */
@Composable
private fun DetailActions(text: String, starred: Boolean?, star: (() -> Unit)?) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton({ clipboard.setText(AnnotatedString(text)) }) {
            Text(stringResource(R.string.engine_copy))
        }
        OutlinedButton({ share(context, text) }) { Text(stringResource(R.string.share_page)) }
        if (starred != null && star != null)
            IconButton(star) {
                Icon(
                    painterResource(
                        if (starred) R.drawable.ic_star_filled else R.drawable.ic_star_outline
                    ),
                    stringResource(
                        if (starred) R.string.remove_favourite else R.string.add_favourite
                    ),
                )
            }
    }
}

/**
 * Provides explicitly simulated CBM scenarios and links to the corresponding supplied reference
 * topics.
 *
 * @param data Supplied fault rows, used to show the original code/message without inventing trigger
 *   logic.
 * @param navigate Opens a related fault reference; never connects to or controls equipment.
 * @return Unit; sample selection survives recreation and is visibly labelled as simulation.
 */
@Composable
private fun CbmDemo(data: EngineeringContent, navigate: (String) -> Unit) {
    var sample by rememberSaveable { mutableIntStateOf(0) }
    val fault = data.faults.firstOrNull { it.code == if (sample == 1) "E065" else "E090" }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                stringResource(R.string.engine_simulated),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(stringResource(R.string.engine_scenario_note))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    sample == 0,
                    { sample = 0 },
                    { Text(stringResource(R.string.engine_reset_sample)) },
                )
                FilterChip(
                    sample == 1,
                    { sample = 1 },
                    { Text(stringResource(R.string.engine_simulate_water)) },
                )
                FilterChip(
                    sample == 2,
                    { sample = 2 },
                    { Text(stringResource(R.string.engine_simulate_comm)) },
                )
            }
            Text(
                stringResource(
                    when (sample) {
                        1 -> R.string.engine_wet_sample
                        2 -> R.string.engine_comm_sample
                        else -> R.string.engine_normal_sample
                    }
                ),
                style = MaterialTheme.typography.titleLarge,
            )
            if (sample != 0 && fault != null)
                OutlinedButton({ navigate("fault:" + fault.code) }) {
                    Text(stringResource(R.string.engine_related_code, fault.code))
                }
        }
    }
}
