package com.electrokit.app.ui

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.electrokit.app.update.ReleaseChecker
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Automatic checks use validated cached metadata and respect a recent failed request. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UpdateCacheTests {
    @Test
    fun freshValidatedCacheAvoidsAnotherRequest() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val json =
            """{"tag_name":"v4.6.0","assets":[{"name":"ELECTRO-4.6.0.apk","size":1000000,"browser_download_url":"https://github.com/nandurpm/electro/releases/download/v4.6.0/ELECTRO-4.6.0.apk"}]}"""
        context
            .getSharedPreferences("electro_release_check_settings", Context.MODE_PRIVATE)
            .edit()
            .putString("cached_release", json)
            .putLong("last_success", System.currentTimeMillis())
            .commit()
        assertEquals("4.6.0", ReleaseChecker.fetchLatest(context).version)
    }

    @Test
    fun failedCheckBacksOffWithoutASecondRequest() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context
            .getSharedPreferences("electro_release_check_settings", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .putLong("last_failure", System.currentTimeMillis())
            .commit()
        val error =
            assertThrows(IllegalStateException::class.java) { ReleaseChecker.fetchLatest(context) }
        assertTrue(error.message.orEmpty().contains("temporarily paused"))
    }

    /**
     * @return Unit; Later survives a new context and affects only the selected optional version.
     */
    @Test
    fun laterIsRememberedForExactPatchOnly() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.electrokit.app.update.UpdateSettings.dismissPatch(context, "4.5.4")
        assertTrue(
            com.electrokit.app.update.UpdateSettings.patchDismissed(
                context.applicationContext,
                "4.5.4",
            )
        )
        assertFalse(com.electrokit.app.update.UpdateSettings.patchDismissed(context, "4.5.5"))
        assertFalse(com.electrokit.app.update.UpdateSettings.patchDismissed(context, "4.6.0"))
    }

    /** @return Unit; automatic checks honor Later, manual checks and a new patch can show again. */
    @Test
    fun patchDismissalSurvivesUpdaterRecreation() {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        context
            .getSharedPreferences("electro_release_check_settings", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        var version = "4.6.2"
        val fetch: (Context, Boolean) -> com.electrokit.app.update.UpdateRelease = { _, _ ->
            com.electrokit.app.update.UpdateRelease(
                "v$version",
                version,
                "https://github.com/nandurpm/electro/releases/download/v$version/ELECTRO-$version.apk",
                "Notes",
                1000,
            )
        }
        val first =
            com.electrokit.app.update.UpdateViewModel(
                context,
                kotlinx.coroutines.Dispatchers.Unconfined,
                fetch,
            )
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        first.check(false)
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        assertNotNull(first.updates.value.pending)
        first.dismissPatch()
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        assertNull(first.updates.value.pending)
        val restored =
            com.electrokit.app.update.UpdateViewModel(
                context,
                kotlinx.coroutines.Dispatchers.Unconfined,
                fetch,
            )
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        restored.check(false)
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        assertNull(restored.updates.value.pending)
        restored.check(true)
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        assertEquals(version, restored.updates.value.pending?.version)
        restored.dismissPatch()
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        version = "4.6.3"
        restored.check(false)
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        assertEquals(version, restored.updates.value.pending?.version)
    }

    /** @return Unit; a required release survives a fresh updater and rejects optional dismissal. */
    @Test
    fun requiredUpdateSurvivesRestartAndCannotUseLater() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val release =
            com.electrokit.app.update.UpdateRelease(
                "v9.0.0",
                "9.0.0",
                "https://github.com/nandurpm/electro/releases/download/v9.0.0/ELECTRO-9.0.0.apk",
                "Required",
                1000,
            )
        com.electrokit.app.update.UpdateSettings.rememberRequired(app, release)
        repeat(2) {
            val vm =
                com.electrokit.app.update.UpdateViewModel(
                    app,
                    kotlinx.coroutines.Dispatchers.Unconfined,
                ) { _, _ ->
                    error("No network expected")
                }
            org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
            assertEquals("9.0.0", vm.updates.value.pending?.version)
            vm.dismissPatch()
            org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
            assertEquals("9.0.0", vm.updates.value.pending?.version)
        }
    }
}
