package com.electrokit.app.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.electrokit.app.viewmodel.EngineeringAccess

/** Single access boundary used by every engineering route, including back-stack destinations.
 * @param back Optional return action when locked.
 * @param content Protected subtree, invoked only for an unlocked activity session.
 * @return Unit; never composes protected data while locked.
 */
@Composable
fun EngineeringGate(back: (() -> Unit)? = null, content: @Composable () -> Unit) {
    val activity = LocalContext.current as ComponentActivity
    val access: EngineeringAccess = viewModel(activity)
    DisposableEffect(activity) {
        access.enterProtected(activity.window)
        onDispose { access.leaveProtected(activity.window) }
    }
    if (access.unlocked) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { access.lock() }) { Text("Lock engineering") }
            }
            Box(Modifier.weight(1f)) { content() }
        }
    } else {
        var pin by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
            if (back != null) TextButton(back) { Text("Back") }
            Text("Engineering access", style = MaterialTheme.typography.headlineMedium)
            Text("Enter your local access code to open escalator references.", Modifier.padding(vertical = 12.dp))
            OutlinedTextField(pin, { if (it.length <= 3 && it.all(Char::isDigit)) pin = it },
                label = { Text("Access code") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                isError = error != null, modifier = Modifier.fillMaxWidth())
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = { error = access.unlock(pin); pin = "" }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) { Text("Unlock") }
            Text("Basic local restriction. It does not encrypt the bundled references.", style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 16.dp))
        }
    }
}
