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
}
