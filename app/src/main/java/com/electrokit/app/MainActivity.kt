package com.electrokit.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.electrokit.app.data.StartupTheme
import com.electrokit.app.ui.screens.ElectroApp
import com.electrokit.app.ui.theme.ElectroTheme
import com.electrokit.app.update.UpdateRelease
import com.electrokit.app.viewmodel.AppViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** A single Compose host; version checks never run on the UI thread or upload personal data. */
class MainActivity : ComponentActivity() {
    /**
     * @param savedInstanceState Framework-saved Activity state. @return Unit after UI
     *   initialization.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val startupLight = StartupTheme.light(this)
        window.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(
                getColor(if (startupLight) R.color.startup_light else R.color.brand_navy)
            )
        )
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
            .show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        setContent {
            val vm: AppViewModel = viewModel()
            val prefs = vm.preferences.collectAsStateWithLifecycle().value
            val loaded = vm.loaded.collectAsStateWithLifecycle().value
            val systemDark = isSystemInDarkTheme()
            val light =
                if (loaded)
                    when (prefs.themeMode) {
                        "system" -> !systemDark
                        "light" -> true
                        "dark" -> false
                        else -> prefs.light
                    }
                else startupLight
            LaunchedEffect(loaded, light, prefs.themeMode) {
                if (loaded)
                    withContext(Dispatchers.IO) {
                        StartupTheme.remember(this@MainActivity, light, prefs.themeMode)
                    }
            }
            DisposableEffect(light) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
                    navigationBarStyle =
                        SystemBarStyle.auto(
                            getColor(R.color.startup_light),
                            getColor(R.color.brand_navy),
                        ) {
                            !light
                        },
                )
                onDispose {}
            }

            val updater: com.electrokit.app.update.UpdateViewModel = viewModel()
            val update by updater.updates.collectAsStateWithLifecycle()
            fun openReleasePage(release: UpdateRelease) {
                try {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(
                                "https://github.com/nandurpm/electro/releases/tag/" + release.tag
                            ),
                        )
                    )
                } catch (_: Exception) {
                    updater.browserFailed()
                }
            }
            ElectroTheme(light) {
                Box {
                    ElectroApp(vm, prefs, onCheckUpdates = { updater.check(true) })
                    com.electrokit.app.update.UpdateDialog(
                        update,
                        updater::dismissPatch,
                        { updater.check(true) },
                        ::openReleasePage,
                        updater::dismissMessage,
                    )
                }
            }
        }
    }
}
