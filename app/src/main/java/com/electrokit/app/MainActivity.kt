package com.electrokit.app

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import android.os.Build
import android.provider.Settings
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
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.electrokit.app.data.StartupTheme
import com.electrokit.app.ui.screens.ElectroApp
import com.electrokit.app.ui.theme.ElectroTheme
import com.electrokit.app.update.ApkUpdateDownloader
import com.electrokit.app.update.ReleaseChecker
import com.electrokit.app.update.UpdatePolicy
import com.electrokit.app.update.UpdatePriority
import com.electrokit.app.update.UpdateRelease
import com.electrokit.app.update.UpdateSettings
import com.electrokit.app.viewmodel.AppViewModel
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
            var downloading by remember { mutableStateOf(false) }
            var downloadPercent by remember { mutableIntStateOf(0) }
            var downloadTask by remember { mutableStateOf<Job?>(null) }
            var downloaded by remember { mutableStateOf<Pair<String, File>?>(null) }

            /** @param apk Verified app-private update file. @return Unit after launching Android's native installer or source-permission settings. */
            fun installDownloadedUpdate(apk: File) {
                try {
                    if (Build.VERSION.SDK_INT >= 26 && !packageManager.canRequestPackageInstalls()) {
                        Toast.makeText(this@MainActivity,
                            "Allow installing updates from ELECTRO, then tap Install Update again.",
                            Toast.LENGTH_LONG).show()
                        startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                            Uri.parse("package:" + packageName)))
                        return
                    }
                    val contentUri = FileProvider.getUriForFile(
                        this@MainActivity, packageName + ".updates", apk)
                    val install = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(contentUri, "application/vnd.android.package-archive")
                        clipData = ClipData.newUri(contentResolver, "ELECTRO update", contentUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(install)
                } catch (_: ActivityNotFoundException) {
                    checkStatus = "Android package installer is unavailable on this device."
                } catch (_: SecurityException) {
                    checkStatus = "Android blocked the installer. Check the Install unknown apps permission for ELECTRO."
                } catch (_: IllegalArgumentException) {
                    checkStatus = "Unable to share the verified update file with the Android installer."
                }
            }

            /** @param release Confirmed release shown in the popup. @return Unit; reports download progress and opens Android installer. */
            fun downloadInsideApp(release: UpdateRelease) {
                if (downloading) return
                downloading = true
                downloadPercent = 0
                downloadTask = scope.launch {
                    try {
                        // Revalidate the currently published release before touching the APK.
                        val fresh = withContext(Dispatchers.IO) { ReleaseChecker.fetchLatest() }
                        if (fresh.version != release.version || fresh.apkUrl != release.apkUrl) {
                            if (UpdatePolicy.priority(BuildConfig.VERSION_NAME, fresh.version) ==
                                UpdatePriority.REQUIRED_FEATURE
                            ) UpdateSettings.rememberRequired(this@MainActivity, fresh)
                            pending = fresh
                            downloaded = null
                            checkStatus = "A different official release is now available. Review its version and tap Update again."
                            return@launch
                        }
                        val apk = ApkUpdateDownloader.download(this@MainActivity, fresh) { percent ->
                            downloadPercent = percent
                        }
                        downloaded = fresh.version to apk
                        downloading = false
                        installDownloadedUpdate(apk)
                    } catch (_: CancellationException) {
                        // The user cancelled the transfer. The downloader deletes partial APKs.
                    } catch (_: Exception) {
                        checkStatus = "Could not download or verify the official APK. Check the connection and available storage, then retry."
                    } finally {
                        downloading = false
                        downloadTask = null
                    }
                }
            }

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
                                if (pending?.version != latest.version) downloaded = null
                                pending = latest
                            }
                            UpdatePriority.OPTIONAL_PATCH -> if (pending == null) {
                                downloaded = null
                                pending = latest
                            }
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
                            onDismissRequest = { if (!required && !downloading) pending = null },
                            title = { Text(if (required) "Update ELECTRO to continue"
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
                                    Text("The APK downloads privately inside ELECTRO. Android will ask for your permission to install it.",
                                        style = MaterialTheme.typography.bodySmall)
                                    if (downloading) {
                                        LinearProgressIndicator(
                                            progress = { downloadPercent / 100f },
                                            modifier = Modifier.fillMaxWidth())
                                        Text("Downloading APK: " + downloadPercent + "%",
                                            style = MaterialTheme.typography.bodySmall)
                                    }
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
                                Button(
                                    enabled = !downloading,
                                    onClick = {
                                        val installedFile = downloaded?.takeIf {
                                            it.first == release.version && it.second.isFile
                                        }?.second
                                        if (installedFile != null) installDownloadedUpdate(installedFile)
                                        else downloadInsideApp(release)
                                    },
                                ) {
                                    Text(when {
                                        downloading -> "Downloading..."
                                        downloaded?.first == release.version && downloaded?.second?.isFile == true ->
                                            "Install Update"
                                        required -> "Update to Continue"
                                        else -> "Download Update"
                                    })
                                }
                            },
                            dismissButton = {
                                if (downloading) {
                                    TextButton(onClick = { downloadTask?.cancel() }) { Text("Cancel download") }
                                } else if (!required) {
                                    TextButton(onClick = { pending = null }) { Text("Later") }
                                }
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
