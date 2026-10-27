package com.electrokit.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.electrokit.app.domain.WorkbenchTool
import com.electrokit.app.domain.WorkbenchTools
import com.electrokit.app.data.PreferencesState
import com.electrokit.app.viewmodel.AppViewModel

/** Edits SI inputs with explicit calculation, assumptions, result sharing and existing persistence.
 * @param tool Authored utility metadata.
 * @param vm Existing persistence owner; no second database is created.
 * @param prefs Committed preferences including previous raw utility inputs.
 * @param back Returns to the utility list.
 * @return Unit; invalid values become validation text, storage failures use the existing error banner.
 */
@Composable
fun UtilityScreen(tool: WorkbenchTool, vm: AppViewModel, prefs: PreferencesState, back: () -> Unit) {
    val key = "utility:${tool.id}"
    var raw by rememberSaveable(key) { mutableStateOf(tool.fields.indices.map { prefs.values[key]?.get(it.toString()).orEmpty() }) }
    var result by rememberSaveable(key) { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current
    val snapshot = tool.title + "\n" + tool.fields.mapIndexed { index, field -> "$field: ${raw[index]}" }.joinToString("\n") + "\n" + result + "\n" + tool.formula + "\n" + tool.limitation
    Scaffold(topBar = { Bar(tool.title, back) }) { padding ->
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("ENGINEERING UTILITY", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(tool.formula, style = MaterialTheme.typography.titleLarge)
            tool.fields.forEachIndexed { index, label ->
                OutlinedTextField(raw[index], { value ->
                    raw = raw.toMutableList().also { it[index] = value }
                    vm.values(key, raw.mapIndexed { i, v -> i.toString() to v }.toMap()); result = ""
                }, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button({ result = WorkbenchTools.calculate(tool.id, raw) }) { Text("Calculate") }
                OutlinedButton({ raw = tool.fields.map { "" }; result = ""; vm.values(key, emptyMap()) }) { Text("Reset") }
            }
            if (result.isNotEmpty()) {
                Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp)) {
                    Text("Result", style = MaterialTheme.typography.titleMedium)
                    Text(result, style = MaterialTheme.typography.headlineSmall)
                    Text("Substitution", Modifier.padding(top = 12.dp))
                    tool.fields.forEachIndexed { i, label -> Text("$label = ${raw[i]}") }
                } }
                Row {
                    TextButton({ clipboard.setText(AnnotatedString(snapshot)) }) { Text("Copy result") }
                    TextButton({ vm.saveCalculation(snapshot) }) { Text("Save calculation") }
                }
            }
            Text("Model & limitations", style = MaterialTheme.typography.titleMedium)
            Text(tool.limitation)
            Text("Use actual equipment data and independent engineering verification for design decisions.", style = MaterialTheme.typography.bodySmall)
        }
    }
}
