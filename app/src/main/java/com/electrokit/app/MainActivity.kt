package com.electrokit.app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.electrokit.app.data.StartupTheme
import com.electrokit.app.ui.screens.ElectroApp
import com.electrokit.app.ui.theme.ElectroTheme
import com.electrokit.app.update.ReleaseChecker
import com.electrokit.app.update.UpdatePolicy
import com.electrokit.app.update.UpdatePriority
import com.electrokit.app.update.UpdateRelease
import com.electrokit.app.update.UpdateSettings
import com.electrokit.app.viewmodel.AppViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** A single Compose host; version checks never run on the UI thread or upload personal data. */
class MainActivity : ComponentActivity() {
    /** @param savedInstanceState Framework-saved Activity state. @return Unit after UI initialization. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val startupLight = StartupTheme.light(this)
        val startupAvailable = StartupTheme.available(this)
        window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(
            when {
                !startupAvailable -> 0xFF12243A.toInt()
                startupLight -> 0xFFF5F7FA.toInt()
                else -> 0xFF0C1422.toInt()
            }
        ))
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.rgb(18, 36, 58)),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.rgb(12, 20, 34)),
        )
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).show(
            androidx.core.view.WindowInsetsCompat.Type.systemBars()
        )
        setContent {
            val vm: AppViewModel = viewModel()
            val prefs = vm.preferences.collectAsStateWithLifecycle().value
            val loaded = vm.loaded.collectAsStateWithLifecycle().value
            val systemDark = isSystemInDarkTheme()
            val light = if (loaded) when (prefs.themeMode) {
                "system" -> !systemDark
                "light" -> true
                "dark" -> false
                else -> prefs.light
            } else startupLight
            SideEffect {
                if (loaded) StartupTheme.remember(this, light)
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                window.navigationBarColor =
                    if (light && android.os.Build.VERSION.SDK_INT >= 26) 0xFFF5F7FA.toInt()
                    else 0xFF0C1422.toInt()
                androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
                    .isAppearanceLightNavigationBars = light
                androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
                    .isAppearanceLightStatusBars = light
            }

            val scope = rememberCoroutineScope()
            var pending by remember {
                mutableStateOf(UpdateSettings.requiredRelease(this, BuildConfig.VERSION_NAME))
            }
            var checkStatus by remember { mutableStateOf<String?>(null) }
            var checking by remember { mutableStateOf(false) }

            /** @param manual True for explicit request. @return Unit; asynchronous check updates UI. */
            fun checkForUpdates(manual: Boolean) {
                if (checking) return
                checking = true
                scope.launch {
                    val response = runCatching {
                        withContext(Dispatchers.IO) { ReleaseChecker.fetchLatest() }
                    }
                    checking = false
                    response.onSuccess { latest ->
                        when (UpdatePolicy.priority(BuildConfig.VERSION_NAME, latest.version)) {
                            UpdatePriority.REQUIRED_FEATURE -> {
                                UpdateSettings.rememberRequired(this@MainActivity, latest)
                                pending = latest
                            }
                            UpdatePriority.OPTIONAL_PATCH -> if (pending == null) pending = latest
                            UpdatePriority.NONE -> if (manual && pending == null) {
                                checkStatus = "ELECTRO " + BuildConfig.VERSION_NAME + " is up to date."
                            }
                        }
                    }.onFailure {
                        // Offline is allowed unless a previously VERIFIED required update is pending.
                        if (manual) checkStatus =
                            "Unable to check GitHub releases. Connect to the internet and retry."
                    }
                }
            }

            LaunchedEffect(Unit) {
                if (!BuildConfig.DEBUG && UpdateSettings.autoCheck(this@MainActivity)) checkForUpdates(false)
            }
            ElectroTheme(light) {
                Box {
                    ElectroApp(vm, prefs, onCheckUpdates = { checkForUpdates(true) })
                    val release = pending
                    if (release != null) {
                        val required = UpdatePolicy.priority(BuildConfig.VERSION_NAME, release.version) ==
                            UpdatePriority.REQUIRED_FEATURE
                        AlertDialog(
                            onDismissRequest = { pending = null },
                            title = { Text(if (required) "ELECTRO feature update available"
                                else "ELECTRO update available") },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(
                                    9.dp)) {
                                    Text("Installed: " + BuildConfig.VERSION_NAME +
                                        "    New: " + release.version)
                                    Text(if (required)
                                        "A new feature version is available. You can keep using offline tools and install the official APK later."
                                    else
                                        "A maintenance patch is available. Update now or choose Later.")
                                    Text("WHAT'S NEW", style = MaterialTheme.typography.titleSmall)
                                    Text(release.notes.take(650), style = MaterialTheme.typography.bodySmall)
                                    Text("The APK downloads through your browser. Android requests your approval to install it.",
                                        style = MaterialTheme.typography.bodySmall)
                                }
                            },
                            confirmButton = {
                                Button(onClick = {
                                    try {
                                        startActivity(
                                            Intent(Intent.ACTION_VIEW, Uri.parse(release.apkUrl))
                                                .addCategory(Intent.CATEGORY_BROWSABLE)
                                        )
                                    } catch (_: ActivityNotFoundException) {
                                        Toast.makeText(this@MainActivity,
                                            "A browser is required to open the update",
                                            Toast.LENGTH_LONG).show()
                                    }
                                }) {
                                    Text("Download Update")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { pending = null }) { Text("Later") }
                            },
                            properties = DialogProperties(
                                dismissOnBackPress = true, dismissOnClickOutside = true
                            ),
                        )
                    }
                    if (checkStatus != null) AlertDialog(
                        onDismissRequest = { checkStatus = null },
                        title = { Text("Check for Updates") },
                        text = { Text(checkStatus ?: "") },
                        confirmButton = {
                            TextButton(onClick = { checkStatus = null }) { Text("OK") }
                        },
                    )
                }
            }
        }
    }
}
