package com.electrokit.app.update

import android.content.Context
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

/**
 * Verified release. @property tag Stable release tag. @property version Numeric release
 * version. @property apkUrl Canonical release download. @property notes Published change
 * notes. @property size APK bytes.
 */
data class UpdateRelease(
    val tag: String,
    val version: String,
    val apkUrl: String,
    val notes: String,
    val size: Long,
)

/** Only requests release metadata; the calculator, bookmark and personal data remain local. */
object ReleaseChecker {
    private const val REPOSITORY = "nandurpm/electro"
    private const val ENDPOINT = "https://api.github.com/repos/" + REPOSITORY + "/releases/latest"
    private const val MAX_REPLY = 512 * 1024
    private const val MAX_APK = 50L * 1024L * 1024L

    /**
     * Loads bounded release metadata, caching automatic checks for six hours.
     *
     * @param context Optional private cache owner; null performs an uncached check.
     * @param force True for a user-requested refresh, bypassing cache and failure backoff.
     * @return Valid official stable-release metadata, not a certificate or malware verdict.
     */
    fun fetchLatest(context: Context? = null, force: Boolean = false): UpdateRelease {
        val settings =
            context?.getSharedPreferences("electro_release_check_settings", Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        if (!force && settings != null) {
            val age = now - settings.getLong("last_success", 0)
            val cached = settings.getString("cached_release", null)
            if (age in 0..21_600_000 && cached != null) {
                runCatching { decode(JSONObject(cached)) }
                    .getOrNull()
                    ?.let {
                        return it
                    }
            }
            val failedAge = now - settings.getLong("last_failure", 0)
            if (failedAge in 0..900_000)
                error("Update checks are temporarily paused; use Check now to retry.")
        }
        val conn = URL(ENDPOINT).openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 9000
            conn.readTimeout = 9000
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            conn.setRequestProperty("User-Agent", "ELECTRO-Android-Release-Checker")
            if (conn.responseCode != HttpURLConnection.HTTP_OK) error("GitHub release unavailable")
            if (conn.contentLengthLong > MAX_REPLY) error("Release metadata too large")
            val out = ByteArrayOutputStream()
            conn.inputStream.use { stream ->
                val buffer = ByteArray(8192)
                while (true) {
                    val n = stream.read(buffer)
                    if (n < 0) break
                    if (out.size() + n > MAX_REPLY) error("Release metadata too large")
                    out.write(buffer, 0, n)
                }
            }
            val text = out.toString("UTF-8")
            val release = decode(JSONObject(text)) ?: error("No valid ELECTRO APK asset found")
            settings
                ?.edit()
                ?.putString("cached_release", text)
                ?.putLong("last_success", now)
                ?.remove("last_failure")
                ?.apply()
            return release
        } catch (failure: Exception) {
            settings?.edit()?.putLong("last_failure", now)?.apply()
            throw failure
        } finally {
            conn.disconnect()
        }
    }

    /** Enforces a published non-prerelease and an official version-matched GitHub asset. */
    /** @param data Parsed release metadata. @return Valid release or null for rejected metadata. */
    fun decode(data: JSONObject): UpdateRelease? {
        if (data.optBoolean("draft") || data.optBoolean("prerelease")) return null
        val tag = data.optString("tag_name")
        val version = ElectroVersion.parse(tag) ?: return null
        val exact = version.major.toString() + "." + version.feature + "." + version.patch
        if (tag != "v" + exact) return null
        val filename = "ELECTRO-" + exact + ".apk"
        val base = "https://github.com/" + REPOSITORY + "/releases/download/" + tag + "/"
        val assets = data.optJSONArray("assets") ?: return null
        for (i in 0 until assets.length()) {
            val asset = assets.optJSONObject(i) ?: continue
            val size = asset.optLong("size", -1)
            if (asset.optString("name") != filename) continue
            if (asset.optString("browser_download_url") != base + filename || size !in 1..MAX_APK)
                continue
            return UpdateRelease(
                tag,
                exact,
                base + filename,
                data.optString("body").take(1100).ifBlank { "See the GitHub release notes." },
                size,
            )
        }
        return null
    }
}

/** Preserve previous verified mandatory updates across restarts and offline use. */
object UpdateSettings {
    private const val NAME = "electro_release_check_settings"

    private fun store(context: Context) = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    /** @param context Private application context. @return Whether to check on launch. */
    fun autoCheck(context: Context): Boolean = store(context).getBoolean("auto_check", true)

    /** @param context App context. @param value Desired checking preference. @return Unit. */
    fun setAutoCheck(context: Context, value: Boolean) {
        store(context).edit().putBoolean("auto_check", value).apply()
    }

    /**
     * @param context App context. @param installed Current installed version. @return Previously
     *   verified requirement or null.
     */
    fun requiredRelease(context: Context, installed: String): UpdateRelease? {
        val s = store(context)
        val version = s.getString("verified_required_version", null) ?: return null
        val address = s.getString("verified_required_url", null) ?: return null
        if (UpdatePolicy.priority(installed, version) != UpdatePriority.REQUIRED_FEATURE) {
            s.edit()
                .remove("verified_required_version")
                .remove("verified_required_url")
                .remove("verified_required_notes")
                .apply()
            return null
        }
        val expected =
            "https://github.com/nandurpm/electro/releases/download/v" +
                version +
                "/ELECTRO-" +
                version +
                ".apk"
        if (address != expected) return null
        return UpdateRelease(
            "v" + version,
            version,
            address,
            s.getString("verified_required_notes", "") ?: "",
            0,
        )
    }

    /**
     * Removes a requirement withdrawn by a successfully fetched official release.
     *
     * @param context Private update-settings owner.
     * @return Unit after clearing only the requirement record.
     */
    fun clearRequired(context: Context) {
        store(context)
            .edit()
            .remove("verified_required_version")
            .remove("verified_required_url")
            .remove("verified_required_notes")
            .apply()
    }

    /**
     * Returns the latest completed network check, excluding cached reads.
     *
     * @param context Private update-settings owner.
     * @return Epoch milliseconds, or zero before the first successful check.
     */
    fun lastSuccess(context: Context): Long = store(context).getLong("last_success", 0)

    /**
     * @param context App context. @param release Verified required release. @return Unit after
     *   persistence.
     */
    fun rememberRequired(context: Context, release: UpdateRelease) {
        store(context)
            .edit()
            .putString("verified_required_version", release.version)
            .putString("verified_required_url", release.apkUrl)
            .putString("verified_required_notes", release.notes)
            .apply()
    }
}
