package com.electrokit.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.runtime.SideEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.electrokit.app.data.StartupTheme
import com.electrokit.app.ui.screens.ElectroApp
import com.electrokit.app.ui.theme.ElectroTheme
import com.electrokit.app.viewmodel.AppViewModel

/** Owns the single Compose host while lifecycle-aware state remains in the application ViewModel. */
class MainActivity : ComponentActivity() {
    /** Hides advanced references when leaving the app, while preserving rotation sessions.
     * @return Unit after revoking the activity-local display grant.
     */
    override fun onStop() {
        if (!isChangingConfigurations) {
            androidx.lifecycle.ViewModelProvider(this)[com.electrokit.app.viewmodel.EngineeringAccess::class.java].lock()
        }
        super.onStop()
    }

    /** Installs edge-to-edge Compose content using the saved appearance preference.
     *
     * @param savedInstanceState Framework-restored state, or null on first launch.
     * @return Unit after Activity content and system-bar styles are configured.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val startupLight = StartupTheme.light(this)
        val startupAvailable = StartupTheme.available(this)
        window.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(
                when {
                    !startupAvailable -> 0xFF12243A.toInt()
                    startupLight -> 0xFFF5F7FA.toInt()
                    else -> 0xFF0C1422.toInt()
                }
            )
        )
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.rgb(18, 36, 58)),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.rgb(12, 20, 34)),
        )
        // Keep system information visible; Compose handles safe drawing insets below.
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).apply {
            show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        }
        setContent {
            val vm: AppViewModel = viewModel()
            val prefs = vm.preferences.collectAsStateWithLifecycle().value
            val loaded = vm.loaded.collectAsStateWithLifecycle().value
            val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
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
            ElectroTheme(light) { ElectroApp(vm, prefs) }
        }
    }
}
