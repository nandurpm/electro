package com.electrokit.app.viewmodel

import android.os.Looper
import com.electrokit.app.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Groups regression assertions for retry regression tests; unexpected behaviour fails the test
 * visibly.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RetryRegressionTests {
    /**
     * Groups regression assertions for failing repository; unexpected behaviour fails the test
     * visibly.
     */
    private class FailingRepository : AppRepository {
        /** Ordered routes published only after every asset decodes. */
        override val registry = emptyList<Entry>()
        /** Shared reference content keyed by stable content ID. */
        override val pages = emptyMap<String, Page>()
        /** Bundled component datasets keyed by module ID. */
        override val parts = emptyMap<String, List<Part>>()
        /** Preference observation owned by storage and exposed read-only by the ViewModel. */
        override val preferences = MutableStateFlow(PreferencesState())
        /** Enables deterministic simulated write failure without touching real disk data. */
        var fail = true
        /** Counts writes so an unrelated empty flush cannot pass a retry test. */
        var attempts = 0
        /** Captures the real staging thread for the production-dispatcher regression. */
        @Volatile var stageOnMain: Boolean? = null
        /** Optional broken asset load isolates error ownership from successful input persistence. */
        var failLoad = false

        /** Staged snapshot survives creation of another ViewModel using this fixture. */
        private val staged = mutableMapOf<String, Map<String, String>>()

        /**
         * Stages before the asynchronous write.
         *
         * @param id Calculator key.
         * @param values Raw fields.
         * @return Unit; the fixture staging operation succeeds.
         */
        override fun stageInputs(id: String, values: Map<String, String>) {
            stageOnMain = Looper.myLooper() == Looper.getMainLooper()
            staged[id] = values.toMap()
        }

        /**
         * Returns staged fields for process-recreation assertions.
         *
         * @return Snapshot map; no failure is simulated here.
         */
        override fun stagedInputs() = staged.toMap()

        /**
         * Removes only acknowledged values.
         *
         * @param committed Main-record snapshot.
         * @return Unit; newer staged values remain.
         */
        override fun clearStagedInputs(committed: Map<String, Map<String, String>>) {
            committed.forEach { (id, values) -> if (staged[id] == values) staged.remove(id) }
        }

        /**
         * Verifies load assets; unexpected behaviour fails the test visibly.
         *
         * @return Unit; assertion failures identify regressions and are never swallowed.
         *   Assertion/time-out failures propagate; the failure fixture deliberately throws
         *   IOException while enabled.
         */
        override suspend fun loadAssets() { if (failLoad) throw java.io.IOException("Load failure") }

        /**
         * Verifies reset preferences; unexpected behaviour fails the test visibly.
         *
         * @return Unit; assertion failures identify regressions and are never swallowed.
         *   Assertion/time-out failures propagate; the failure fixture deliberately throws
         *   IOException while enabled.
         */
        override suspend fun resetPreferences() {
            preferences.value = PreferencesState()
        }

        /**
         * Verifies update; unexpected behaviour fails the test visibly.
         *
         * @param transform Atomic transformation of the latest persisted snapshot.
         * @return Unit; assertion failures identify regressions and are never swallowed.
         *   Assertion/time-out failures propagate; the failure fixture deliberately throws
         *   IOException while enabled.
         */
        override suspend fun update(transform: (PreferencesState) -> PreferencesState) {
            attempts++
            if (fail) throw java.io.IOException("Simulated disk failure")
            preferences.value = transform(preferences.value)
        }
    }

    /**
     * Verifies settings retry repeats settings mutation rather than flushing inputs; unexpected
     * behaviour fails the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun settingsRetryRepeatsSettingsMutationRatherThanFlushingInputs() {
        val repository = FailingRepository()
        val vm = AppViewModel(RuntimeEnvironment.getApplication(), repository, kotlinx.coroutines.Dispatchers.Unconfined)
        shadowOf(Looper.getMainLooper()).idle()
        vm.theme(true)
        shadowOf(Looper.getMainLooper()).idle()
        assertNotNull(vm.error.value)
        assertEquals(1, repository.attempts)
        repository.fail = false
        vm.retryFailedAction()
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(2, repository.attempts)
        assertTrue(repository.preferences.value.light)
        assertNull(vm.error.value)
    }

    /**
     * Verifies input retry persists pending snapshot; unexpected behaviour fails the test visibly.
     *
     * @return Unit; assertion failures identify regressions and are never swallowed.
     *   Assertion/time-out failures propagate; the failure fixture deliberately throws IOException
     *   while enabled.
     */
    @Test
    fun inputRetryPersistsPendingSnapshot() {
        val repository = FailingRepository()
        val vm = AppViewModel(RuntimeEnvironment.getApplication(), repository, kotlinx.coroutines.Dispatchers.Unconfined)
        vm.values("c4", mapOf("v" to "24"))
        vm.flushInputs()
        shadowOf(Looper.getMainLooper()).idle()
        assertNotNull(vm.error.value)
        repository.fail = false
        vm.retryFailedAction()
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals("24", repository.preferences.value.values["c4"]?.get("v"))
        assertNull(vm.error.value)
    }

    /**
     * Verifies a staged edit is visible to a fresh ViewModel despite a failed main-record write.
     *
     * @return Unit; assertion failures expose a lost-input regression.
     */
    @Test
    fun freshViewModelRecoversStagedInputsBeforeDataStoreCommit() {
        val repository = FailingRepository()
        val first = AppViewModel(RuntimeEnvironment.getApplication(), repository, kotlinx.coroutines.Dispatchers.Unconfined)
        shadowOf(Looper.getMainLooper()).idle()
        first.values("c4", mapOf("v" to "37"))
        assertEquals("37", repository.stagedInputs()["c4"]?.get("v"))
        val restored = AppViewModel(RuntimeEnvironment.getApplication(), repository, kotlinx.coroutines.Dispatchers.Unconfined)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals("37", restored.preferences.value.values["c4"]?.get("v"))
    }
    /** @return Unit; a successful input transaction must not hide a failed asset load. */
    @Test
    fun inputSuccessDoesNotDismissLoadFailure() {
        val repository = FailingRepository().apply { fail = false; failLoad = true }
        val vm = AppViewModel(RuntimeEnvironment.getApplication(), repository, kotlinx.coroutines.Dispatchers.Unconfined)
        shadowOf(Looper.getMainLooper()).idle()
        val loadError = vm.error.value
        assertNotNull(loadError)
        vm.values("c4", mapOf("v" to "24"))
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(loadError, vm.error.value)
        repository.failLoad = false
        vm.retryFailedAction()
        shadowOf(Looper.getMainLooper()).idle()
        assertTrue(vm.loaded.value)
    }

    /** @return Unit; actual production dispatcher must move blocking journal work off main. */
    @Test
    fun productionJournalRunsOffMainThread() {
        val repository = FailingRepository().apply { fail = false }
        val vm = AppViewModel(RuntimeEnvironment.getApplication(), repository)
        vm.values("c4", mapOf("v" to "24"))
        val deadline = System.nanoTime() + 5_000_000_000L
        while (repository.stageOnMain == null && System.nanoTime() < deadline) {
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(5)
        }
        assertEquals(false, repository.stageOnMain)
    }

}
