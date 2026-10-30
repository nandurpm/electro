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
import com.electrokit.app.domain.Calculation
import com.electrokit.app.domain.UnitConversions
import com.electrokit.app.domain.Engineering
import kotlinx.coroutines.launch

/** Converts selected units offline, keeping invalid input out of clipboard actions.
 * @param back Returns to the tools catalogue.
 * @return Unit; blank input shows guidance and valid conversions can be copied.
 */
@Composable
fun UnitConverterScreen(back: () -> Unit) {
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
            Choice("From", units, from, { from = it })
            TextButton({ val previous = from; from = to; to = previous }, Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.workbench_swap))
            }
            Choice("To", units, to, { to = it })
            WorkbenchResult(result, stringResource(R.string.workbench_converter_hint))
            OutlinedButton({
                clipboard.setText(AnnotatedString("$raw $from = ${result?.text}"))
                scope.launch { snackbar.showSnackbar(copied) }
            }, enabled = result is Calculation.Value, modifier = Modifier.fillMaxWidth()) { Text("Copy conversion") }
            Text("SI prefix scales are exact. Temperature includes scale offsets. Inputs use the selected units; this converter does not accept an additional unit suffix.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Wire gauge diameter and area are available in Tools → Reference → AWG Table. A gauge conversion is not an ampacity or cable-selection recommendation.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
