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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.electrokit.app.domain.UnitConversions
import com.electrokit.app.domain.Engineering

/** Converts selected units offline without applying engineering prefix scaling twice.
 * @param back Returns to the tools catalogue.
 * @return Unit; invalid input is displayed as validation text instead of a numeric result.
 */
@Composable
fun UnitConverterScreen(back: () -> Unit) {
    var group by rememberSaveable { mutableStateOf("Voltage") }
    var from by rememberSaveable { mutableStateOf("V") }
    var to by rememberSaveable { mutableStateOf("mV") }
    var raw by rememberSaveable { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current
    val units = UnitConversions.groups.getValue(group).keys.toList()
    val result = runCatching {
        val v = raw.replace(',', '.').toDoubleOrNull() ?: error("Enter a decimal value; choose its unit below.")
        "${Engineering.format(UnitConversions.convert(group, from, to, v))} $to"
    }.getOrElse { it.message.orEmpty() }
    Scaffold(topBar = { Bar("Engineering unit converter", back) }) { padding ->
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Choice("Quantity", UnitConversions.groups.keys.toList(), group, {
                group = it; val next = UnitConversions.groups.getValue(it).keys.toList(); from = next[0]; to = next[1]
            })
            OutlinedTextField(raw, { raw = it }, label = { Text("Value") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Choice("From", units, from, { from = it })
            Choice("To", units, to, { to = it })
            Card(Modifier.fillMaxWidth()) { Text(result, Modifier.padding(24.dp), style = MaterialTheme.typography.headlineSmall) }
            TextButton({ clipboard.setText(AnnotatedString("$raw $from = $result")) }) { Text("Copy conversion") }
            Text("SI prefix scales are exact. Temperature includes scale offsets. Inputs use the selected units; this converter does not accept an additional unit suffix.")
            Text("Wire gauge diameter and area are available in Tools → Reference → AWG Table. A gauge conversion is not an ampacity or cable-selection recommendation.")
        }
    }
}
