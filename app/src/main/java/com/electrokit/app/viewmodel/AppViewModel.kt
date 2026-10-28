package com.electrokit.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.electrokit.app.R
import com.electrokit.app.data.AppRepository
import com.electrokit.app.data.PreferencesState
import com.electrokit.app.data.Repository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Coordinates asset loading, atomic preference mutations and unsaved input snapshots across
 * Activity recreation.
 *
 * @param application Application context used for lifecycle-independent local storage.
 * @param repository Injectable storage boundary; production uses the bundled asset/DataStore
 *   repository. Initialisation starts asynchronous loading; recoverable failures are exposed
 *   through error state.
 */
class AppViewModel
@JvmOverloads
constructor(
    application: Application,
    /** Injectable storage boundary; production uses the bundled asset/DataStore repository. */
    val repository: AppRepository = Repository(application),
    /** Disk worker; tests may inject a controlled dispatcher without changing production threading. */
    private val ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO,
) : AndroidViewModel(application) {
    /** Latest preference snapshot overlaid with uncommitted edits. */
    private val state = MutableStateFlow(PreferencesState())
    /** Preference observation owned by storage and exposed read-only by the ViewModel. */
    val preferences = state.asStateFlow()
    /** Successful initial asset/preference loading state. */
    private val loadingComplete = MutableStateFlow(false)
    /** Read-only initialisation status for the loading UI. */
    val loaded = loadingComplete.asStateFlow()
    /** Recoverable operation error; raw exception details are not displayed. */
    private val errors = MutableStateFlow<String?>(null)
    /** Read-only banner text for the current recoverable storage failure. */
    val error = errors.asStateFlow()
    /** Exact failed operation, retained until retry or dismissal. */
    private var retryAction: (() -> Unit)? = null
    /** Asset/preference subscription cancelled before restarting loading. */
    private var loading: Job? = null
    /** Main-record input job; the durable journal already protects its pending values. */
    private var saveJob: Job? = null
    /** Debounces typing only; never cancels an in-flight journal transaction. */
    private var debounceJob: Job? = null
    /** Serializes commits so an older disk transaction cannot overwrite newer typing. */
    private val inputMutex = Mutex()
    /** Identifies the visible failure; unrelated successful operations cannot dismiss it. */
    private var errorKind: String? = null
    /**
     * Latest unsaved snapshots, owned on the ViewModel main thread. Main-thread snapshots overlay
     * disk emissions, so an older record cannot undo new typing.
     */
    private val pending = mutableMapOf<String, Map<String, String>>()

    init {
        retryLoading()
    }

    /**
     * Restarts the asset/preference subscription so a transient read failure can recover without
     * restarting the app.
     *
     * @return Unit; rendering effects and state changes are delivered through callbacks or
     *   observable state. Recoverable storage failures are exposed through error state;
     *   cancellation is rethrown.
     */
    fun retryLoading() {
        loading?.cancel()
        errors.value = null
        loading =
            viewModelScope.launch {
                try {
                    repository.loadAssets()
                    withContext(ioDispatcher) { repository.stagedInputs() }.forEach { (id, values) ->
                        if (id !in pending) pending[id] = values
                    }
                    if (pending.isNotEmpty()) flushInputs()
                    repository.preferences.collect {
                        state.value = it.copy(values = it.values + pending)
                        loadingComplete.value = true
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    retryAction = ::retryLoading
                    errorKind = "load"
                errors.value = getApplication<Application>().getString(R.string.error_load)
                }
            }
    }

    /**
     * Transforms the latest preference record atomically; the ViewModel wrapper retains the exact
     * failed mutation for Retry.
     *
     * @param transform Atomic transformation of the latest persisted snapshot.
     * @return Unit; rendering effects and state changes are delivered through callbacks or
     *   observable state. Recoverable storage failures are exposed through error state;
     *   cancellation is rethrown.
     */
    private fun update(transform: (PreferencesState) -> PreferencesState) {
        viewModelScope.launch {
            try {
                repository.update(transform)
                if (errorKind == "settings") { errors.value = null; retryAction = null; errorKind = null }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                retryAction = { update(transform) }
                errorKind = "settings"
                errors.value = getApplication<Application>().getString(R.string.error_settings)
            }
        }
    }

    /**
     * Replays the operation associated with the visible error instead of flushing unrelated input
     * fields.
     *
     * @return Unit; rendering effects and state changes are delivered through callbacks or
     *   observable state. Recoverable storage failures are exposed through error state;
     *   cancellation is rethrown.
     */
    fun retryFailedAction() {
        retryAction?.invoke()
    }

    /**
     * Clears both the banner and its retained action so a dismissed operation cannot be replayed
     * accidentally.
     *
     * @return Unit; rendering effects and state changes are delivered through callbacks or
     *   observable state. Recoverable storage failures are exposed through error state;
     *   cancellation is rethrown.
     */
    fun dismissError() {
        retryAction = null
        errors.value = null
    }

    /**
     * Explicitly clears saved state; the ViewModel cancels pending input work before reloading
     * defaults.
     *
     * @return Unit; rendering effects and state changes are delivered through callbacks or
     *   observable state. Recoverable storage failures are exposed through error state;
     *   cancellation is rethrown.
     */
    fun resetPreferences() {
        viewModelScope.launch {
            try {
                // Wait for an in-flight fsync before resetting, or old data could reappear.
                debounceJob?.cancelAndJoin()
                saveJob?.cancelAndJoin()
                pending.clear()
                inputMutex.withLock { withContext(ioDispatcher) { repository.resetPreferences() } }
                retryLoading()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                retryAction = ::resetPreferences
                errorKind = "reset"
                errors.value = getApplication<Application>().getString(R.string.error_reset)
            }
        }
    }

    /**
     * Persists an explicit palette selection rather than toggling an unobserved transient UI flag.
     *
     * @param light True selects the light palette.
     * @return Unit; rendering effects and state changes are delivered through callbacks or
     *   observable state. Recoverable storage failures are exposed through error state;
     *   cancellation is rethrown.
     */
    fun theme(light: Boolean) = update { it.copy(light = light, themeMode = if (light) "light" else "dark") }

    /** Persists appearance while retaining the legacy Boolean for older installation data.
 * @param mode One of dark, light or system.
 * @return Unit; persistence errors surface through the shared error state.
 */
    fun appearance(mode: String) = update { it.copy(themeMode = mode, light = mode == "light") }

    /** Records public navigation only; bounds the persistent list to twenty stable IDs.
 * @param id Public registry identifier, excluding protected controller details.
 * @return Unit; records at most twenty most-recent distinct routes.
 */
    fun visited(id: String) = update { it.copy(recent = (listOf(id) + it.recent.filterNot { old -> old == id }).take(20)) }

    /** Saves an explicit calculation snapshot without changing the existing raw-input journal.
 * @param text Explicitly saved public calculator result, inputs and model.
 * @return Unit; retains at most fifty snapshots in existing storage.
 */
    fun saveCalculation(text: String) = update { it.copy(calculations = (listOf(text) + it.calculations).take(50)) }

    /** Clears explicit result history only; raw calculator input and favourites are retained.
 * @return Unit; clears result history after UI confirmation, retaining raw inputs and favorites.
 */
    fun clearCalculations() = update { it.copy(calculations = emptyList()) }

    /**
     * Toggles catalogue order inside the atomic preference transformation.
     *
     * @return Unit; rendering effects and state changes are delivered through callbacks or
     *   observable state. Recoverable storage failures are exposed through error state;
     *   cancellation is rethrown.
     */
    fun sort() = update { it.copy(alphabetical = !it.alphabetical) }

    /**
     * Toggles a favourite by stable registry ID so title changes do not lose the saved selection.
     *
     * @param id Stable calculator or registry identifier; independent of the visible title.
     * @return Unit; rendering effects and state changes are delivered through callbacks or
     *   observable state. Recoverable storage failures are exposed through error state;
     *   cancellation is rethrown.
     */
    fun star(id: String) = update {
        it.copy(favourites = if (id in it.favourites) it.favourites - id else it.favourites + id)
    }

    /**
     * Persists the selected calculator mode separately from its raw field snapshot.
     *
     * @param id Stable calculator or registry identifier; independent of the visible title.
     * @param mode Calculator mode index retained independently of raw scalar inputs.
     * @return Unit; rendering effects and state changes are delivered through callbacks or
     *   observable state. Recoverable storage failures are exposed through error state;
     *   cancellation is rethrown.
     */
    fun mode(id: String, mode: Int) = update { it.copy(modes = it.modes + (id to mode)) }

    /**
     * Updates visible input immediately and queues an atomic backup on Dispatchers.IO.
     * Abrupt process termination before the worker commits can lose the latest edit.
     *
     * @param id Stable calculator or registry identifier; independent of the visible title.
     * @param values Caller-owned field/component snapshot; not mutated by this operation.
     * @return Unit; rendering effects and state changes are delivered through callbacks or
     *   observable state. Recoverable storage failures are exposed through error state;
     *   cancellation is rethrown.
     */
    fun values(id: String, values: Map<String, String>) {
        pending[id] = values.toMap()
        state.value = state.value.copy(values = state.value.values + (id to values.toMap()))
        // Debounce edits without cancelling a durable write already in progress.
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            kotlinx.coroutines.delay(350)
            flushInputs()
        }
    }

    /**
     * Schedules a pending main-record write before lifecycle stop or an explicit save retry.
     *
     * @return Unit; rendering effects and state changes are delivered through callbacks or
     *   observable state. Recoverable storage failures are exposed through error state;
     *   cancellation is rethrown.
     */
    fun flushInputs() {
        debounceJob?.cancel()
        debounceJob = null
        if (saveJob?.isActive != true) saveJob = viewModelScope.launch { inputMutex.withLock { persistInputs() } }
    }

    /**
     * Commits a snapshot and removes only unchanged pending entries so newer edits cannot be
     * discarded by an older write.
     *
     * @return Unit; rendering effects and state changes are delivered through callbacks or
     *   observable state. Recoverable storage failures are exposed through error state;
     *   cancellation is rethrown.
     */
    private suspend fun persistInputs() {
        while (pending.isNotEmpty()) {
        val snapshot = pending.toMap()
        try {
            withContext(ioDispatcher) {
                snapshot.forEach { (id, values) -> repository.stageInputs(id, values) }
                repository.update { it.copy(values = it.values + snapshot) }
                repository.clearStagedInputs(snapshot)
            }
            // Remove only unchanged entries: edits made during this write must remain pending.
            snapshot.forEach { (id, values) -> if (pending[id] == values) pending.remove(id) }
            if (errorKind == "inputs") { errors.value = null; retryAction = null; errorKind = null }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            retryAction = ::flushInputs
            errorKind = "inputs"
                errors.value = getApplication<Application>().getString(R.string.error_inputs)
            return
        }
        }
    }
}
