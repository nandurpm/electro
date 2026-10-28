package com.electrokit.app.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.electrokit.app.viewmodel.EngineeringAccess

/** Single access boundary used by every engineering route, including back-stack destinations.
 * @param back Optional return action when locked.
 * @param content Protected subtree, invoked only for an unlocked activity session.
 * @return Unit; never composes protected data while locked.
 */
@Composable
fun EngineeringGate(back: (() -> Unit)? = null, content: @Composable () -> Unit) {
    val activity = requireNotNull(LocalContext.current.componentActivity())
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
        var busy by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        val submit: () -> Unit = {
            if (!busy) {
                val attempt = pin
                pin = ""
                busy = true
                scope.launch { try { error = access.unlockAsync(attempt) } finally { busy = false } }
            }
        }
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
            if (back != null) TextButton(back) { Text("Back") }
            Text("Advanced references", style = MaterialTheme.typography.headlineMedium)
            Text("Enter the local display code to show escalator references. This is not data encryption.", Modifier.padding(vertical = 12.dp))
            OutlinedTextField(pin, { if (it.length <= 3 && it.all { c -> c in '0'..'9' }) pin = it },
                label = { Text("Access code") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                isError = error != null, modifier = Modifier.fillMaxWidth())
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) }
            Button(
                onClick = submit,
                enabled = !busy,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            ) { Text("Unlock") }
            Text("Basic local restriction. It does not encrypt the bundled references.", style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 16.dp))
        }
    }
}
