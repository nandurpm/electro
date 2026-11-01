package com.electrokit.app.update

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.electrokit.app.BuildConfig
import com.electrokit.app.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Owns update state across Activity recreation; all metadata and preference IO runs off main. */
class UpdateViewModel
@JvmOverloads
constructor(
    application: Application,
    private val worker: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO,
    private val fetch: (android.content.Context, Boolean) -> UpdateRelease = { context, manual ->
        ReleaseChecker.fetchLatest(context, manual)
    },
) : AndroidViewModel(application) {
    private val context = application
    private val state = MutableStateFlow(UpdateState())
    /** Read-only lifecycle-owned update presentation state. */
    val updates = state.asStateFlow()
    private var initialized = false
    private val operations = Mutex()

    init {
        viewModelScope.launch {
            operations.withLock {
                val restored =
                    withContext(worker) {
                        UpdateSettings.requiredRelease(context, BuildConfig.VERSION_NAME)
                    }
                state.value = state.value.copy(pending = restored)
                initialized = true
                if (!BuildConfig.DEBUG && withContext(worker) { UpdateSettings.autoCheck(context) })
                    check(false)
            }
        }
    }

    /**
     * @param manual True bypasses network cache and an optional patch dismissal.
     * @return Unit; asynchronous metadata checks update presentation state.
     */
    fun check(manual: Boolean) {
        if (!initialized || state.value.checking) return
        state.value = state.value.copy(checking = true)
        viewModelScope.launch {
            operations.withLock {
                try {
                    val latest = withContext(worker) { fetch(context, manual) }
                    var pending = state.value.pending
                    withContext(worker) {
                        if (
                            pending != null &&
                                UpdatePolicy.withdrawn(pending!!.version, latest.version)
                        ) {
                            UpdateSettings.clearRequired(context)
                            pending = null
                        }
                        when (UpdatePolicy.priority(BuildConfig.VERSION_NAME, latest.version)) {
                            UpdatePriority.REQUIRED_FEATURE -> {
                                UpdateSettings.rememberRequired(context, latest)
                                pending = latest
                            }
                            UpdatePriority.OPTIONAL_PATCH ->
                                if (
                                    pending == null &&
                                        (manual ||
                                            !UpdateSettings.patchDismissed(context, latest.version))
                                )
                                    pending = latest
                            UpdatePriority.NONE -> Unit
                        }
                    }
                    state.value =
                        state.value.copy(
                            pending = pending,
                            checking = false,
                            message =
                                if (manual && pending == null)
                                    context.getString(
                                        R.string.update_current_version,
                                        BuildConfig.VERSION_NAME,
                                    )
                                else null,
                        )
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    state.value =
                        state.value.copy(
                            checking = false,
                            message =
                                if (manual)
                                    context.getString(
                                        if (e is ReleaseRateLimitException)
                                            R.string.update_rate_limited
                                        else R.string.update_network_failed
                                    )
                                else null,
                        )
                }
            }
        }
    }

    /** @return Unit; remembers Later only for an optional version, never a required update. */
    fun dismissPatch() {
        val release = state.value.pending ?: return
        if (
            UpdatePolicy.priority(BuildConfig.VERSION_NAME, release.version) !=
                UpdatePriority.OPTIONAL_PATCH
        )
            return
        // Keep the dialog until preferences are updated so recreation cannot race dismissal.
        viewModelScope.launch {
            operations.withLock {
                withContext(worker) { UpdateSettings.dismissPatch(context, release.version) }
                if (state.value.pending == release) state.value = state.value.copy(pending = null)
            }
        }
    }

    /** @return Unit; reports a browser-launch failure without discarding the update. */
    fun browserFailed() {
        state.value = state.value.copy(message = context.getString(R.string.update_browser_failed))
    }

    /** @return Unit; clears only the manual status dialog. */
    fun dismissMessage() {
        state.value = state.value.copy(message = null)
    }
}

/**
 * Immutable dialog state, independent of Activity and composition lifetime.
 *
 * @property pending Verified release awaiting action.
 * @property checking Whether a metadata check is in flight.
 * @property message Localized manual-check or browser status.
 */
data class UpdateState(
    val pending: UpdateRelease? = null,
    val checking: Boolean = false,
    val message: String? = null,
)
