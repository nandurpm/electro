package com.electrokit.app.update

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.electrokit.app.BuildConfig
import com.electrokit.app.R

/**
 * Scrollable update content with one action per dialog slot, including at large font sizes.
 *
 * @param state Lifecycle-owned verified release and checking state.
 * @param onLater Persists dismissal of an optional patch.
 * @param onRefresh Manually refreshes official metadata.
 * @param onOpen Opens the selected official release in the browser.
 * @param onDismissMessage Clears a manual-check message.
 * @return Unit after rendering the current dialogs.
 */
@Composable
fun UpdateDialog(
    state: UpdateState,
    onLater: () -> Unit,
    onRefresh: () -> Unit,
    onOpen: (UpdateRelease) -> Unit,
    onDismissMessage: () -> Unit,
) {
    val release = state.pending
    if (release != null) {
        val required =
            UpdatePolicy.priority(BuildConfig.VERSION_NAME, release.version) ==
                UpdatePriority.REQUIRED_FEATURE
        AlertDialog(
            onDismissRequest = { if (!required) onLater() },
            title = { Text(stringResource(R.string.update_electro_update_available)) },
            text = {
                Column(
                    Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    Text(
                        stringResource(
                            R.string.update_versions,
                            BuildConfig.VERSION_NAME,
                            release.version,
                        )
                    )
                    Text(
                        stringResource(
                            if (required) R.string.update_required_message
                            else R.string.update_optional_message
                        )
                    )
                    Text(
                        stringResource(R.string.update_what_s_new),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(release.notes, style = MaterialTheme.typography.bodySmall)
                    Text(
                        stringResource(R.string.update_open_release_hint),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
            confirmButton = {
                Button(onClick = { onOpen(release) }) {
                    Text(stringResource(R.string.update_open_release))
                }
            },
            dismissButton = {
                if (required)
                    TextButton(onClick = onRefresh, enabled = !state.checking) {
                        Text(stringResource(R.string.update_refresh))
                    }
                else TextButton(onClick = onLater) { Text(stringResource(R.string.update_later)) }
            },
            properties =
                DialogProperties(dismissOnBackPress = !required, dismissOnClickOutside = !required),
        )
    }
    if (state.message != null)
        AlertDialog(
            onDismissRequest = onDismissMessage,
            title = { Text(stringResource(R.string.update_check_for_updates)) },
            text = { Text(state.message) },
            confirmButton = {
                TextButton(onClick = onDismissMessage) { Text(stringResource(R.string.update_ok)) }
            },
        )
}
