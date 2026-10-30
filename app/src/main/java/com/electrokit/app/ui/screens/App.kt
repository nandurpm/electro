@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class,
)

package com.electrokit.app.ui.screens

import android.content.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.*
import androidx.compose.ui.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.*
import com.electrokit.app.BuildConfig
import com.electrokit.app.R
import com.electrokit.app.data.*
import com.electrokit.app.domain.*
import com.electrokit.app.ui.components.*
import com.electrokit.app.ui.theme.*
import com.electrokit.app.viewmodel.AppViewModel
import kotlinx.coroutines.launch

/**
 * Uses the Android plain-text chooser so the user decides which external application receives a
 * result.
 *
 * @param context Android context used only for local assets/storage or the user-directed share
 *   chooser.
 * @param text Input or output text as described above; treated as data, never executed.
 * @return Unit; rendering effects and state changes are delivered through callbacks or observable
 *   state. Rendering/callback failures propagate; recoverable persistence failures belong to the
 *   ViewModel.
 */
fun share(context: Context, text: String) {
    if (text.toByteArray(Charsets.UTF_8).size > 128 * 1024 || text.length > 64 * 1024) {
        android.widget.Toast.makeText(context, "Too much text to share. Narrow your selection and try again.", android.widget.Toast.LENGTH_LONG).show()
        return
    }
    try {
    context.startActivity(
        Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            },
            context.getString(R.string.share_chooser),
        )
    )
    } catch (_: android.content.ActivityNotFoundException) {
        android.widget.Toast.makeText(context, R.string.share_unavailable, android.widget.Toast.LENGTH_LONG).show()
    }
}

/**
 * Connects navigation, lifecycle flushing and recoverable storage errors to one application host.
 *
 * @param vm Lifecycle-owned application state and persistence coordinator.
 * @param prefs Current immutable preference snapshot.
 * @param onCheckUpdates Opens the manual GitHub release check.
 * @return Unit; rendering effects and state changes are delivered through callbacks or observable
 *   state. Rendering/callback failures propagate; recoverable persistence failures belong to the
 *   ViewModel.
 */
@Composable
fun ElectroApp(vm: AppViewModel, prefs: PreferencesState, onCheckUpdates: () -> Unit = {}) {
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { vm.flushInputs() }
    val nav = rememberNavController()
    val loaded by vm.loaded.collectAsStateWithLifecycle()
    val err by vm.error.collectAsStateWithLifecycle()
    // A neutral brand loading surface avoids flashing the wrong persisted theme during migration.
    var confirmReset by remember { mutableStateOf(false) }
    Surface(Modifier.fillMaxSize(), contentColor = MaterialTheme.colorScheme.onBackground, color = MaterialTheme.colorScheme.background) {
        if (!loaded) {
            Column(
                Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (err == null) CircularProgressIndicator()
                else {
                    Text(err!!)
                    Button({ vm.retryLoading() }) { Text(stringResource(R.string.ui_retry)) }
                    TextButton({ confirmReset = true }) {
                        Text(
                            stringResource(
                                R.string.ui_reset_damaged_preferences_clears_saved_settings
                            )
                        )
                    }
                }
            }
            if (confirmReset) AlertDialog(
                onDismissRequest = { confirmReset = false },
                title = { Text("Reset damaged preferences?") },
                text = { Text("This deletes favorites, saved calculations and inputs. This cannot be undone.") },
                confirmButton = { TextButton({ confirmReset = false; vm.resetPreferences() }) { Text("Reset") } },
                dismissButton = { TextButton({ confirmReset = false }) { Text("Cancel") } },
            )
            return@Surface
        }
        Column(
            Modifier.fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .imePadding()
        ) {
            if (err != null)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        err!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f).padding(8.dp),
                    )
                    TextButton({ vm.retryFailedAction() }) {
                        Text(stringResource(R.string.ui_retry))
                    }
                    TextButton({ vm.dismissError() }) { Text(stringResource(R.string.ui_dismiss)) }
                }
            NavHost(nav, "main", Modifier.weight(1f)) {
                composable("main") {
                    MainScreen(
                        vm,
                        prefs,
                        { vm.visited(it); nav.navigate("entry/$it") },
                        { nav.navigate("info/$it") },
                        { nav.navigate("settings") },
                        onCheckUpdates,
                    )
                }
                composable("settings") {
                    Scaffold(
                        topBar = { Bar(stringResource(R.string.settings), { nav.popBackStack() }) }
                    ) { padding ->
                        ElectroSettings(vm, prefs, Modifier.padding(padding))
                    }
                }
                composable("plugins") {
                    Scaffold(
                        topBar = {
                            Bar(stringResource(R.string.plugins_title), { nav.popBackStack() })
                        }
                    ) { padding ->
                        Databases(
                            vm,
                            prefs,
                            Modifier.padding(padding),
                            { nav.navigate("entry/$it") },
                            { nav.navigate("info/1") },
                        )
                    }
                }
                composable("info/{tab}") { back ->
                    Info(
                        vm,
                        back.arguments?.getString("tab")?.toIntOrNull() ?: 0,
                        { nav.popBackStack() },
                    ) {
                        nav.navigate("entry/$it")
                    }
                }
                composable("entry/{id}") { back ->
                    val entry =
                        vm.repository.registry.firstOrNull {
                            it.id == back.arguments?.getString("id")
                        }
                    val page = entry?.let { vm.repository.pages[it.contentId] }
                    if (entry == null || (entry.screenType !in setOf("calculator", "module") && page == null)) {
                        // Unknown or damaged registry route: return instead of showing a dead blank screen.
                        LaunchedEffect(Unit) { nav.popBackStack() }
                    } else
                        when (entry.screenType) {
                            "calculator" ->
                                CalculatorScreen(entry, vm, prefs) { nav.popBackStack() }
                            "module" -> ModuleScreen(entry, vm) { nav.popBackStack() }
                            else ->
                                ReferenceScreen(
                                    entry,
                                    page!!,
                                ) {
                                    nav.popBackStack()
                                }
                        }
                }
            }
        }
    }
}

/**
 * Offers consistent navigation/actions while keeping child screens independent of the
 * NavController.
 *
 * @param title Visible calculator title used to select the intended test route.
 * @param back Caller-owned back-navigation callback.
 * @param brand True adds the original ElectroKit motif beside the title.
 * @param actions Child toolbar actions; large-font layout may put them below the title.
 * @return Unit; rendering effects and state changes are delivered through callbacks or observable
 *   state. Rendering/callback failures propagate; recoverable persistence failures belong to the
 *   ViewModel.
 */
@Composable
fun Bar(
    title: String,
    back: (() -> Unit)? = null,
    brand: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val scale = LocalDensity.current.fontScale
    CompositionLocalProvider(LocalContentColor provides Color.White) {
        BoxWithConstraints(Modifier.fillMaxWidth().background(BrandNavy)) {
            val split = (!brand && maxWidth < 380.dp) || maxWidth < 320.dp || scale > 1.3f
            Column {
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (back != null)
                        IconButton(back) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                        }
                    Row(
                        Modifier.weight(1f).padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (brand) {
                            Icon(
                                painterResource(R.drawable.ic_launcher),
                                stringResource(R.string.logo_description),
                                tint = Color.Unspecified,
                                modifier = Modifier.size(38.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                        }
                        Column(Modifier.weight(1f)) {
                            Text(title, modifier = Modifier.semantics { heading() }, color = TopBarText,
                                style = if (brand) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium)
                            if (brand) Text("Electronics Toolkit", color = TopBarText,
                                style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    if (!split)
                        Row(verticalAlignment = Alignment.CenterVertically, content = actions)
                }
                if (split)
                    Row(
                        Modifier.align(Alignment.End).padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        content = actions,
                    )
            }
        }
    }
}

/**
 * Keeps search and scroll state independent per page while presenting the data-driven, swipeable
 * catalogue.
 *
 * @param vm Lifecycle-owned application state and persistence coordinator.
 * @param prefs Current immutable preference snapshot.
 * @param openEntry Opens a stable registry route through caller-owned navigation.
 * @param info Opens a requested Info tab through caller-owned navigation.
 * @param settings Opens saved appearance and preference controls.
 * @return Unit; rendering effects and state changes are delivered through callbacks or observable
 *   state. Rendering/callback failures propagate; recoverable persistence failures belong to the
 *   ViewModel.
 */

/**
 * Uses a labelled editable field and explicit clear action while the caller owns query state.
 *
 * @param value Input scalar in the operation’s documented units.
 * @param onChange Receives edited text; persistence remains with the caller.
 * @param label Visible field/cell caption used for accessible presentation.
 * @param shape Field outline; the dashboard uses a fully rounded pill, nested pages keep the compact corner.
 * @return Unit; rendering effects and state changes are delivered through callbacks or observable
 *   state. Rendering/callback failures propagate; recoverable persistence failures belong to the
 *   ViewModel.
 */
@Composable
fun Search(
    value: String,
    onChange: (String) -> Unit,
    label: String = stringResource(R.string.search),
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp),
) {
    OutlinedTextField(
        value,
        onChange,
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = shape,
        colors =
            OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            ),
        label = { Text(label) },
        singleLine = true,
        leadingIcon = { Icon(Icons.Default.Search, null) },
        trailingIcon = {
            if (value.isNotEmpty())
                IconButton({ onChange("") }) {
                    Icon(Icons.Default.Clear, stringResource(R.string.clear_search))
                }
        },
    )
}

/**
 * Combines an original circuit motif with navigation and an optional independent favourite action.
 *
 * @param entry Registry route and presentation metadata.
 * @param openEntry Opens a stable registry route through caller-owned navigation.
 * @param star Favourite state, or null to hide that action.
 * @param onStar Requests a favourite change without mutating the registry record.
 * @return Unit; rendering effects and state changes are delivered through callbacks or observable
 *   state. Rendering/callback failures propagate; recoverable persistence failures belong to the
 *   ViewModel.
 */
@Composable
fun EntryRow(entry: Entry, openEntry: () -> Unit, star: Boolean? = null, onStar: () -> Unit = {}) {
    Row(
        Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(10.dp),
            )
            .heightIn(min = 68.dp)
            .clickable(onClick = openEntry)
            .padding(start = 16.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            ToolIcon(entry.icon)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f).padding(vertical = 12.dp)) {
            Text(entry.title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            if (entry.description.isNotBlank()) {
                Text(
                    entry.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }
        }
        if (star != null)
            IconButton(onStar) {
                Icon(
                    painterResource(
                        if (star) R.drawable.ic_star_filled else R.drawable.ic_star_outline
                    ),
                    if (star) stringResource(R.string.remove_favourite)
                    else stringResource(R.string.add_favourite),
                    tint = if (star) Heading else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 2.dp),
        )
    }
}

/**
 * Presents always-available offline databases with direct open actions for personal reference use.
 *
 * @param vm Lifecycle-owned application state and persistence coordinator.
 * @param prefs Current immutable preference snapshot.
 * @param modifier Caller-owned layout and semantics constraints.
 * @param openEntry Opens a stable registry route through caller-owned navigation.
 * @param onMore Opens the included-feature inventory.
 * @return Unit; rendering effects and state changes are delivered through callbacks or observable
 *   state. Rendering/callback failures propagate; recoverable persistence failures belong to the
 *   ViewModel.
 */
@Composable
fun Databases(
    vm: AppViewModel,
    prefs: PreferencesState,
    modifier: Modifier = Modifier,
    openEntry: (String) -> Unit,
    onMore: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val cards =
        vm.repository.registry
            .filter { it.screenType == "module" && it.title.contains(query, true) }
            .sortedWith(
                compareByDescending<Entry> { it.id in prefs.favourites }
                    .thenBy { if (prefs.alphabetical) it.title else "" }
            )
    LazyColumn(modifier) {
        item { Search(query, { query = it }, stringResource(R.string.search_modules)) }
        item { Text(stringResource(R.string.module_intro), Modifier.padding(16.dp)) }
        items(cards, key = { it.id }) { entry ->
            val id = entry.id

            Column(
                Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { openEntry(id) }
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EntryIcon(id)
                    Spacer(Modifier.width(16.dp))
                    Text(entry.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    IconButton({ vm.star(id) }) {
                        Icon(
                            painterResource(
                                if (id in prefs.favourites) R.drawable.ic_star_filled
                                else R.drawable.ic_star_outline
                            ),
                            if (id in prefs.favourites) stringResource(R.string.remove_favourite)
                            else stringResource(R.string.add_favourite),
                        )
                    }
                }
                Text(entry.description, Modifier.padding(vertical = 12.dp))
                TextButton({ openEntry(id) }) {
                    Text(
                        pluralStringResource(
                            R.plurals.open_module,
                            vm.repository.parts.getValue(id).size,
                            vm.repository.parts.getValue(id).size,
                        )
                    )
                }
            }
            HorizontalDivider(color = Divider)
        }
        item {
            Text(
                stringResource(R.string.more_features),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().clickable { onMore() }.padding(16.dp),
            )
        }
    }
}

/**
 * Displays product prose and a registry-derived inventory so included features stay aligned with
 * navigation.
 *
 * @param vm Lifecycle-owned application state and persistence coordinator.
 * @param initial Initial tab/default text, according to the enclosing operation.
 * @param back Caller-owned back-navigation callback.
 * @param openEntry Opens a stable registry route through caller-owned navigation.
 * @return Unit; rendering effects and state changes are delivered through callbacks or observable
 *   state. Rendering/callback failures propagate; recoverable persistence failures belong to the
 *   ViewModel.
 */
@Composable
fun Info(vm: AppViewModel, initial: Int, back: () -> Unit, openEntry: (String) -> Unit) {
    val catalogueLabels =
        mapOf(
            "CALCULATORS" to stringResource(R.string.tab_calculators),
            "PIN-OUTS" to stringResource(R.string.tab_pinouts),
            "RESOURCES" to stringResource(R.string.tab_resources),
            "PLUGINS" to stringResource(R.string.ui_plugins),
        )

    var tab by rememberSaveable { mutableIntStateOf(initial) }
    val labels =
        listOf(
            stringResource(R.string.ui_info),
            stringResource(R.string.included),
            stringResource(R.string.changes),
            stringResource(R.string.credits),
        )
    Scaffold(
        topBar = {
            Column {
                Bar(stringResource(R.string.app_name), back)
                ScrollableTabRow(
                    tab,
                    containerColor = BrandNavy,
                    contentColor = TopBarText,
                ) {
                    labels.forEachIndexed { i, s -> Tab(tab == i, { tab = i }, text = { Text(s) }) }
                }
            }
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding)) {
            when (tab) {
                0 ->
                    item {
                        Text(
                            stringResource(R.string.info_body, BuildConfig.VERSION_NAME),
                            Modifier.padding(20.dp),
                        )
                    }
                1 ->
                    listOf("CALCULATORS", "PIN-OUTS", "RESOURCES", "PLUGINS").forEach { group ->
                        item {
                            Text(
                                catalogueLabels.getValue(group),
                                color = Heading,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                        items(vm.repository.registry.filter { it.tab == group }, key = { it.id }) {
                            EntryRow(it, { openEntry(it.id) })
                        }
                    }
                2 ->
                    item {
                        Text(
                            stringResource(R.string.changes_body, BuildConfig.VERSION_NAME),
                            Modifier.padding(20.dp),
                        )
                    }
                3 -> item { Text(stringResource(R.string.credits_body), Modifier.padding(20.dp)) }
            }
        }
    }
}

/**
 * Renders searchable JSON diagrams and copyable tables; visual USB column merging preserves
 * source/share data.
 *
 * @param entry Registry route and presentation metadata.
 * @param page Decoded offline reference content.
 * @param back Caller-owned back-navigation callback.
 * @return Unit; rendering effects and state changes are delivered through callbacks or observable
 *   state. Rendering/callback failures propagate; recoverable persistence failures belong to the
 *   ViewModel.
 */
@Composable
fun ReferenceScreen(entry: Entry, page: Page, back: () -> Unit) {
    val usbHeaders =
        listOf(
            stringResource(R.string.usb_pin_header),
            stringResource(R.string.column_name),
            stringResource(R.string.column_description),
            stringResource(R.string.column_colour),
        )
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var query by rememberSaveable { mutableStateOf("") }
    val scroll = rememberScrollState()
    val plain =
        remember(page) {
            page.title +
                "\n" +
                page.sections.joinToString("\n\n") { s ->
                    s.title +
                        "\n" +
                        s.headers.joinToString(" | ") +
                        "\n" +
                        s.rows.joinToString("\n") { it.joinToString(" | ") } +
                        "\n" +
                        s.notes
                } +
                "\n" +
                page.notes +
                "\n" +
                page.provenance
        }
    Scaffold(
        topBar = {
            Bar(entry.title, back) {
                TextButton({ share(context, plain) }) {
                    Icon(Icons.Default.Share, null, tint = TopBarText)
                    Text(stringResource(R.string.share_page), color = TopBarText, fontSize = 12.sp)
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            Search(query, { query = it }, stringResource(R.string.search_reference))
            LazyColumn(Modifier.fillMaxSize()) {
                item {
                    Text(
                        page.title,
                        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                        color = Heading,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Start,
                    )
                }
                item {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(
                            stringResource(R.string.provenance_heading),
                            color = Heading,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            page.provenance,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                        )
                    }
                }
                page.sections.forEachIndexed { index, s ->
                    val sourceRows =
                        s.rows.filter {
                            query.isBlank() ||
                                it.any { cell -> cell.contains(query, true) } ||
                                s.title.contains(query, true)
                        }
                    val usbTable = entry.contentId == "p1" && s.headers.size == 5
                    val headers = if (usbTable) usbHeaders else s.headers
                    val rows =
                        if (usbTable)
                            sourceRows.map { listOf(it.getOrElse(0) { "" } + " / " + it.getOrElse(1) { "" }, it.getOrElse(2) { "" }, it.getOrElse(3) { "" }, it.getOrElse(4) { "" }) }
                        else sourceRows
                    if (rows.isNotEmpty() || query.isBlank()) {
                        item(key = "section$index") {
                            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                Text(
                                    s.title,
                                    color = Heading,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                if (query.isBlank()) {
                                    if (entry.contentId == "p1")
                                        s.diagrams.chunked(1).forEach { pair ->
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                pair.forEach {
                                                    DiagramView(it, Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    else s.diagrams.forEach { DiagramView(it) }
                                }
                                if (s.notes.isNotBlank())
                                    Text(
                                        s.notes,
                                        Modifier.padding(vertical = 8.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                            }
                        }
                        val compact = usbTable || headers.size <= 3
                        val widths =
                            if (usbTable) listOf(.15f, .18f, .43f, .24f)
                            else List(headers.size) { 1f / headers.size }
                        item(key = "tableHint$index") {
                            Text(
                                if (compact) stringResource(R.string.table_copy_hint)
                                else stringResource(R.string.table_scroll_hint),
                                Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        stickyHeader(key = "header$index") {
                            Column(
                                Modifier.padding(horizontal = 16.dp)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                                    .background(BrandNavy)
                            ) {
                                Row(
                                    Modifier.fillMaxWidth()
                                        .then(
                                            if (compact) Modifier.height(IntrinsicSize.Min)
                                            else Modifier.horizontalScroll(scroll)
                                        )
                                ) {
                                    headers.forEachIndexed { i, cell ->
                                        TableCell(
                                            cell,
                                            true,
                                            clipboard,
                                            if (compact) Modifier.weight(widths[i])
                                            else Modifier.width(160.dp),
                                            compact,
                                        )
                                    }
                                }
                            }
                        }
                        itemsIndexed(rows) { rowIndex, row ->
                            Row(
                                Modifier.padding(horizontal = 16.dp)
                                    .fillMaxWidth()
                                    .background(
                                        if (rowIndex % 2 == 0) MaterialTheme.colorScheme.surface
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant.copy(
                                                alpha = .5f
                                            )
                                    )
                                    .then(
                                        if (compact) Modifier.height(IntrinsicSize.Min)
                                        else Modifier.horizontalScroll(scroll)
                                    )
                            ) {
                                row.forEachIndexed { i, cell ->
                                    TableCell(
                                        cell,
                                        false,
                                        clipboard,
                                        if (compact) Modifier.weight(widths[i])
                                        else Modifier.width(160.dp),
                                        compact,
                                    )
                                }
                            }
                            HorizontalDivider(
                                Modifier.padding(horizontal = 16.dp),
                                color = Divider.copy(alpha = .5f),
                            )
                        }
                    }
                }
                item {
                    Column(Modifier.padding(16.dp)) {
                        Text(page.notes, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (page.sources.isNotEmpty()) {
                            Text(stringResource(R.string.ui_references), color = Heading)
                            page.sources.forEach {
                                Text(
                                    it,
                                    fontSize = 12.sp,
                                    modifier =
                                        Modifier.clickable {
                                                clipboard.setText(AnnotatedString(it))
                                            }
                                            .padding(vertical = 4.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Fills recognised wire-colour cells and chooses contrasting text for immediate identification.
 *
 * @param value Input scalar in the operation’s documented units.
 * @param header True selects the table heading style and contrast treatment.
 * @param clipboard Clipboard destination used only after an explicit cell tap.
 * @param modifier Caller-owned layout and semantics constraints.
 * @param compact True uses the phone-fitting cell layout instead of a horizontally scrollable
 *   width.
 * @return Unit; rendering effects and state changes are delivered through callbacks or observable
 *   state. Rendering/callback failures propagate; recoverable persistence failures belong to the
 *   ViewModel.
 */
@Composable
private fun TableCell(
    value: String,
    header: Boolean,
    clipboard: androidx.compose.ui.platform.ClipboardManager,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val colour =
        when (value.lowercase().substringBefore(':').trim()) {
            "red" -> Color(0xFFC62828)
            "clear", "white" -> Color.White
            "green" -> Color(0xFF008000)
            "black" -> Color.Black
            "blue" -> Color(0xFF1565C0)
            "yellow" -> Color.Yellow
            "orange" -> Color(0xFFFF9800)
            "brown" -> Color(0xFF754123)
            "purple",
            "violet" -> Color(0xFF8E24AA)
            "grey",
            "slate" -> Color.Gray
            "tan" -> Color(0xFFD2B48C)
            "pink", "rose" -> Color(0xFFFFC0CB)
            "aqua" -> Color(0xFF00FFFF)
            else -> null
        }
    // Mixed wire colours use separate swatches so neither half compromises label contrast.
    val wireParts = value.substringBefore(':').lowercase().split('/').map { it.trim() }
    Row(
        modifier
            .heightIn(min = 52.dp)
            .then(if (compact) Modifier.fillMaxHeight() else Modifier)
            .background(if (header) BrandNavy else colour ?: Color.Transparent)
            .clickable { clipboard.setText(AnnotatedString(value)) }
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!header && wireParts.size > 1 && wireParts.all { it in setOf("red", "blue", "green", "yellow", "black", "white", "grey", "gray", "pink", "rose", "brown", "violet", "purple", "orange", "tan", "aqua") }) {
            wireParts.forEach { part ->
                val wire = when (part) {
                    "red" -> Color(0xFFC62828)
                    "blue" -> Color(0xFF1565C0)
                    "green" -> Color(0xFF008000)
                    "yellow" -> Color.Yellow
                    "black" -> Color.Black
                    "white" -> Color.White
                    "grey", "gray" -> Color.Gray
                    "pink", "rose" -> Color(0xFFFFC0CB)
                    "brown" -> Color(0xFF754123)
                    "orange" -> Color(0xFFFF9800)
                    "tan" -> Color(0xFFD2B48C)
                    "aqua" -> Color(0xFF00FFFF)
                    "violet", "purple" -> Color(0xFF8E24AA)
                    else -> Color.Transparent
                }
                Box(Modifier.size(12.dp, 24.dp).background(wire))
            }
            Spacer(Modifier.width(8.dp))
        }
        Text(
            value,
            Modifier.weight(1f),
            fontSize = 14.sp,
            lineHeight = 21.sp,
            color =
                if (header) Color.White
                else if (colour != null) {
                    if (colour.luminance() > 0.179f) Color.Black else Color.White
                } else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (header) FontWeight.SemiBold else FontWeight.Normal,
            fontStyle =
                if (value in listOf("Not connected", "Drain Wire")) FontStyle.Italic
                else FontStyle.Normal,
        )
    }
}

/**
 * Combines category/text filters with explicitly numeric metadata while leaving the bundled dataset
 * unchanged.
 *
 * @param entry Registry route and presentation metadata.
 * @param vm Lifecycle-owned application state and persistence coordinator.
 * @param back Caller-owned back-navigation callback.
 * @return Unit; rendering effects and state changes are delivered through callbacks or observable
 *   state. Rendering/callback failures propagate; recoverable persistence failures belong to the
 *   ViewModel.
 */
@Composable
fun ModuleScreen(entry: Entry, vm: AppViewModel, back: () -> Unit) {
    var q by rememberSaveable { mutableStateOf("") }
    val allLabel = stringResource(R.string.all_categories)
    val anyLabel = stringResource(R.string.any_parameter)
    var category by rememberSaveable { mutableStateOf("All") }
    var field by rememberSaveable { mutableStateOf("Any") }
    var min by rememberSaveable { mutableStateOf("") }
    var max by rememberSaveable { mutableStateOf("") }
    val data = vm.repository.parts[entry.id].orEmpty()
    var selected by rememberSaveable(entry.id) { mutableStateOf(listOf<String>()) }
    var comparison by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val keys = remember(data) { listOf("Any") + data.flatMap { it.numericKeys }.distinct() }
    val filterError = NumericFilter.validation(min, max)
    val filtered =
        data.filter { p ->
            (category == "All" || p.category == category) &&
                (q.isBlank() ||
                    (p.name + " " + p.category + " " + p.specs.values.joinToString()).contains(
                        q,
                        true,
                    )) &&
                (field == "Any" ||
                    (field in p.numericKeys && NumericFilter.matches(p.specs[field], min, max)))
        }
    Scaffold(
        topBar = {
            Bar(entry.title, back) {
                IconButton({
                    share(
                        context,
                        filtered.joinToString("\n\n") {
                            it.name + "\n" + it.specs.entries.joinToString("\n")
                        },
                    )
                }) {
                    Icon(Icons.Default.Share, stringResource(R.string.share_filtered))
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            Search(q, { q = it })
            Choice(
                stringResource(R.string.category),
                listOf("All") + data.map { it.category }.distinct(),
                category,
                { category = it },
                displayLabel = { if (it == "All") allLabel else it },
            )
            Choice(
                stringResource(R.string.numeric_parameter),
                keys,
                field,
                { field = it },
                displayLabel = { if (it == "Any") anyLabel else it },
            )
            if (field != "Any")
                Row {
                    OutlinedTextField(
                        min,
                        { min = it },
                        Modifier.weight(1f).padding(4.dp),
                        label = { Text(stringResource(R.string.ui_minimum)) },
                    )
                    OutlinedTextField(
                        max,
                        { max = it },
                        Modifier.weight(1f).padding(4.dp),
                        label = { Text(stringResource(R.string.ui_maximum)) },
                    )
                }
            if (field != "Any" && filterError != null)
                Text(
                    filterError,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp),
                )
            Text(
                pluralStringResource(R.plurals.part_count, filtered.size, filtered.size),
                Modifier.padding(8.dp),
                color = Heading,
            )
            TextButton({ comparison = true }, enabled = selected.size >= 2) { Text("Compare selected (${selected.size}/3)") }
            if (comparison) AlertDialog(onDismissRequest = { comparison = false },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
                title = { Text("Compare components") },
                text = { Column(Modifier.verticalScroll(rememberScrollState())) {
                    val parts = data.filter { it.name in selected }
                    Text("Swipe sideways for additional columns", style = MaterialTheme.typography.labelSmall)
                    Column(Modifier.horizontalScroll(rememberScrollState())) {
                        Row {
                            Text("Specification", Modifier.width(100.dp), fontWeight = FontWeight.Bold)
                            parts.forEach { Text(it.name, Modifier.width(112.dp), fontWeight = FontWeight.Bold) }
                        }
                        (parts.flatMap { it.specs.keys }.distinct() + "Source").forEach { label ->
                            HorizontalDivider(Modifier.padding(vertical = 8.dp))
                            Row {
                                Text(label, Modifier.width(100.dp))
                                parts.forEach { part -> Text(if (label == "Source") part.source else part.specs[label] ?: "Not supplied", Modifier.width(112.dp).padding(end = 12.dp)) }
                            }
                        }
                    }
                    Text("Only bundled specifications are shown. Missing fields are not inferred; verify the exact manufacturer/package.")
                } }, confirmButton = { TextButton({ comparison = false }) { Text("Close") } })
            LazyColumn(Modifier.weight(1f)) {
                items(filtered, key = { it.name }) { part ->
                    var expanded by rememberSaveable(part.name) { mutableStateOf(false) }
                    Column(
                        Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(part.name in selected, { checked ->
                                selected = if (!checked) selected - part.name else if (selected.size < 3) selected + part.name else selected
                            }, modifier = Modifier.semantics { contentDescription = "Compare ${part.name}" }, enabled = part.name in selected || selected.size < 3)
                            Text("Select for comparison", style = MaterialTheme.typography.labelMedium)
                        }
                        Text(part.name, fontWeight = FontWeight.Bold, color = Heading)
                        Text(part.category)
                        if (expanded) {
                            part.specs.forEach { (k, v) -> Text("$k: $v") }
                            Text(
                                stringResource(R.string.product_reference, part.source),
                                fontSize = 12.sp,
                            )
                            Text(stringResource(R.string.part_scope), fontStyle = FontStyle.Italic)
                        }
                    }
                    HorizontalDivider(color = Divider)
                }
            }
        }
    }
}

/**
 * Presents a controlled dropdown so selection state remains with the mode/filter owner.
 *
 * @param label Visible field/cell caption used for accessible presentation.
 * @param options Stable option keys offered by this controlled dropdown.
 * @param selected Current stable option key; displayLabel may translate its visible caption.
 * @param onSelect Receives the selected stable key so the caller can retain or persist it.
 * @param modifier Caller-owned layout and semantics constraints.
 * @param displayLabel Maps stable option keys to visible captions without changing saved
 *   selections.
 * @return Unit; rendering effects and state changes are delivered through callbacks or observable
 *   state. Rendering/callback failures propagate; recoverable persistence failures belong to the
 *   ViewModel.
 */
@Composable
fun Choice(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    displayLabel: (String) -> String = { it },
) {
    // Translate labels at presentation only; callbacks and saveable state retain stable keys.
    val selectedLabel = displayLabel(selected)
    var expanded by remember { mutableStateOf(false) }
    Box(modifier.padding(4.dp)) {
        OutlinedButton({ expanded = true }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text(if (label.isBlank()) selectedLabel else "$label: $selectedLabel")
        }
        DropdownMenu(expanded, { expanded = false }) {
            options.forEach { s ->
                DropdownMenuItem(
                    text = { Text(displayLabel(s)) },
                    onClick = {
                        onSelect(s)
                        expanded = false
                    },
                )
            }
        }
    }
}
