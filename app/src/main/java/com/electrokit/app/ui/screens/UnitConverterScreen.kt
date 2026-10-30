package com.electrokit.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.electrokit.app.R
import com.electrokit.app.viewmodel.AppViewModel
import com.electrokit.app.data.PreferencesState
import com.electrokit.app.domain.Calculation
import com.electrokit.app.domain.UnitConversions
import com.electrokit.app.domain.Engineering
import kotlinx.coroutines.launch

/** Converts selected units offline, keeping invalid input out of clipboard actions.
 * @param vm Optional existing persistence owner for saved conversions.
 * @param prefs Current saved calculation history.
 * @param back Returns to the tools catalogue.
 * @return Unit; blank input shows guidance and valid conversions can be copied.
 */
@Composable
fun UnitConverterScreen(vm: AppViewModel? = null, prefs: PreferencesState = PreferencesState(), back: () -> Unit) {
    var group by rememberSaveable { mutableStateOf("Voltage") }
    var from by rememberSaveable { mutableStateOf("V") }
    var to by rememberSaveable { mutableStateOf("mV") }
    var raw by rememberSaveable { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val copied = stringResource(R.string.workbench_copied)
    val units = UnitConversions.groups.getValue(group).keys.toList()
    val result = remember(raw, group, from, to) {
        if (raw.isBlank()) null else try {
            val value = raw.trim().replace(',', '.').toDoubleOrNull()
                ?: throw IllegalArgumentException("Enter a decimal value; choose its unit below.")
            Calculation.Value("${Engineering.format(UnitConversions.convert(group, from, to, value))} $to")
        } catch (error: IllegalArgumentException) {
            Calculation.Error(error.message ?: "Check the input value.")
        }
    }
    Scaffold(topBar = { Bar("Engineering unit converter", back) }, snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("UNIT CONVERTER", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Choice("Quantity", UnitConversions.groups.keys.toList(), group, {
                group = it
                val next = UnitConversions.groups.getValue(it).keys.toList()
                from = next[0]; to = next[1]
            })
            OutlinedTextField(raw, { raw = it }, label = { Text("Value") }, singleLine = true,
                isError = result is Calculation.Error, modifier = Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Choice("From", units, from, { from = it }, Modifier.weight(1f))
                Choice("To", units, to, { to = it }, Modifier.weight(1f))
            }
            TextButton({ val previous = from; from = to; to = previous }, Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.workbench_swap))
            }
            WorkbenchResult(result, stringResource(R.string.workbench_converter_hint))
            OutlinedButton({
                clipboard.setText(AnnotatedString("$raw $from = ${result?.text}"))
                scope.launch { snackbar.showSnackbar(copied) }
            }, enabled = result is Calculation.Value, modifier = Modifier.fillMaxWidth()) { Text("Copy conversion") }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton({ raw = "" }, Modifier.weight(1f)) { Text("Reset") }
                TextButton({ vm?.saveCalculation("Conversion\n$raw $from = ${result?.text}") },
                    enabled = result is Calculation.Value && vm != null, modifier = Modifier.weight(1f)) { Text("Save conversion") }
            }
            if (result is Calculation.Value) {
                Text("Calculation", style = MaterialTheme.typography.titleMedium)
                Text(if (group == "Temperature") when {
                    from == to -> "Same temperature scale: $raw $from = ${result.text}"
                    else -> "$raw $from → Kelvin → $to = ${result.text} (absolute temperature; scale offsets included)"
                } else "$raw $from × ${Engineering.format(UnitConversions.groups.getValue(group).getValue(from) / UnitConversions.groups.getValue(group).getValue(to))} = ${result.text}",
                    style = MaterialTheme.typography.bodyLarge)
            }
            var information by rememberSaveable { mutableStateOf(false) }
            TextButton({ information = !information }) { Text(if (information) "Hide information" else "Information & assumptions") }
            if (information) {
                Text("SI scales are exact. Values use the selected units without an extra suffix. Temperature is absolute, not a temperature difference; offsets apply and values below absolute zero are rejected.")
                Text("Wire diameter and cross-sectional area are in the AWG reference. Neither alone determines ampacity; use the applicable installation standard.")
            }
            val saved = prefs.calculations.filter { it.startsWith("Conversion\n") }.take(5)
            if (saved.isNotEmpty()) {
                Text("Saved conversions", style = MaterialTheme.typography.titleMedium)
                saved.forEach { Text(it.removePrefix("Conversion\n"), Modifier.padding(vertical = 6.dp)) }
            }
        }
    }
}
