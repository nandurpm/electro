package com.electrokit.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import com.electrokit.app.ui.components.ElectroPanel as Card
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.electrokit.app.R
import com.electrokit.app.domain.Calculation
import com.electrokit.app.domain.WorkbenchTool
import com.electrokit.app.domain.WorkbenchTools
import com.electrokit.app.data.PreferencesState
import com.electrokit.app.viewmodel.AppViewModel
import kotlinx.coroutines.launch

/** Edits utility inputs while only valid calculations can be copied or saved.
 * @param tool Authored utility metadata and field order.
 * @param vm Existing input and result persistence owner.
 * @param prefs Committed inputs restored on first entry.
 * @param back Returns to the preceding destination.
 * @return Unit; invalid input is shown separately from successful results.
 */
@Composable
fun UtilityScreen(tool: WorkbenchTool, vm: AppViewModel, prefs: PreferencesState, back: () -> Unit) {
    val key = "utility:${tool.id}"
    var raw by rememberSaveable(key) { mutableStateOf(tool.fields.indices.map { prefs.values[key]?.get(it.toString()).orEmpty() }) }
    var attempted by rememberSaveable(key) { mutableStateOf(false) }
    val result = remember(tool.id, raw, attempted) { if (attempted) WorkbenchTools.evaluate(tool.id, raw) else null }
    val clipboard = LocalClipboardManager.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val copied = stringResource(R.string.workbench_copied)
    val snapshot = tool.title + "\n" + tool.fields.mapIndexed { index, field -> "$field: ${raw[index]}" }.joinToString("\n") + "\n" + result?.text.orEmpty() + "\n" + tool.formula + "\n" + tool.limitation
    Scaffold(topBar = { Bar(tool.title, back) }, snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("ELECTRICAL UTILITY", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Formula", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(tool.formula, style = MaterialTheme.typography.titleMedium)
                }
            }
            Text(stringResource(R.string.workbench_input_heading), style = MaterialTheme.typography.titleMedium)
            tool.fields.forEachIndexed { index, label ->
                OutlinedTextField(raw[index], { value ->
                    raw = raw.toMutableList().also { it[index] = value }
                    vm.values(key, raw.mapIndexed { i, v -> i.toString() to v }.toMap())
                    attempted = false
                }, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button({ attempted = true }, Modifier.weight(1f)) { Text("Calculate") }
                OutlinedButton({ raw = tool.fields.map { "" }; attempted = false; vm.values(key, emptyMap()) }) { Text("Reset") }
            }
            WorkbenchResult(result, stringResource(R.string.workbench_result_hint))
            if (result is Calculation.Value) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton({
                        clipboard.setText(AnnotatedString(snapshot))
                        scope.launch { snackbar.showSnackbar(copied) }
                    }, Modifier.weight(1f)) { Text("Copy result") }
                    TextButton({ vm.saveCalculation(snapshot) }, Modifier.weight(1f)) { Text("Save calculation") }
                }
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Substitution", style = MaterialTheme.typography.titleMedium)
                        tool.fields.forEachIndexed { i, label -> Text("$label = ${raw[i]}", style = MaterialTheme.typography.bodyMedium) }
                    }
                }
            }
            Text("Model & limitations", style = MaterialTheme.typography.titleMedium)
            Text(tool.limitation, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
