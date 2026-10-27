package com.electrokit.app.viewmodel

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.electrokit.app.domain.LocalPin

/** Activity-session access state: survives rotation, never persists across process recreation. */
class EngineeringAccess : ViewModel() {
    /** Observable grant for this activity ViewModel only; never saved or restored. */
    var unlocked by mutableStateOf(false)
        private set
    /** Rejected attempts since the last cooldown or successful verification. */
    private var failures = 0
    /** Monotonic retry deadline; wall-clock changes cannot bypass the delay. */
    private var retryAt = 0L
    /** Overlapping navigation animations may compose multiple gates against the same Activity. */
    private var protectedSurfaces = 0

    /** Marks a protected surface without retaining its Window in the ViewModel.
     * @param window Current Activity window, used only for this call.
     * @return Unit; secure previews remain enabled until all overlapping gates leave.
     */
    fun enterProtected(window: android.view.Window) {
        protectedSurfaces++
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
    }

    /** Releases one gate after navigation or lifecycle disposal.
     * @param window Window belonging to the disposed gate.
     * @return Unit; the last active gate restores ordinary window behavior.
     */
    fun leaveProtected(window: android.view.Window) {
        protectedSurfaces = (protectedSurfaces - 1).coerceAtLeast(0)
        if (protectedSurfaces == 0) window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
    }

    /** Rate-limits failed attempts in memory; returns a user-facing outcome without echoing input.
 * @param candidate Entered code, never copied into persistent state or error messages.
 * @return Null on success; otherwise a user-facing rejection or temporary rate-limit message.
 */
    fun unlock(candidate: String): String? {
        if (SystemClock.elapsedRealtime() < retryAt) return "Too many attempts. Wait 30 seconds."
        if (LocalPin.verify(candidate)) {
            unlocked = true
            failures = 0
            return null
        }
        failures++
        if (failures >= 5) {
            retryAt = SystemClock.elapsedRealtime() + 30_000
            failures = 0
        }
        return "Access code not accepted."
    }

    /** Immediately removes protected content from every composable sharing this session.
 * @return Unit; synchronously removes access for all destinations sharing this ViewModel.
 */
    fun lock() { unlocked = false }
}
