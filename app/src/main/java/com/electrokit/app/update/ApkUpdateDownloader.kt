package com.electrokit.app.update

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/** Download and verify the official update into app-private cache before Android installation. */
object ApkUpdateDownloader {
    private const val MAX_BYTES = 50L * 1024 * 1024

    /** @param context App context. @param release Freshly verified GitHub release metadata. @param onProgress Progress 0-100 on the UI thread. @return Signed APK from the app's private cache. */
    suspend fun download(context: Context, release: UpdateRelease, onProgress: (Int) -> Unit): File =
        withContext(Dispatchers.IO) {
            require(release.size in 1..MAX_BYTES) { "Invalid APK size" }
            require(ElectroVersion.parse(release.version) != null) { "Invalid version" }
            val expectedName = "ELECTRO-" + release.version + ".apk"
            val original = "https://github.com/nandurpm/electro/releases/download/v" +
                release.version + "/" + expectedName
            require(release.apkUrl == original) { "Untrusted APK URL" }
            val directory = File(context.cacheDir, "electro_updates").apply {
                if (!isDirectory && !mkdirs()) error("Cannot create updater cache")
            }
            val destination = File(directory, expectedName)
            val partial = File(directory, expectedName + ".part")
            try {
                partial.delete()
                if (destination.length() == release.size) {
                    try {
                        validateInstalledIdentity(context, destination, release.version)
                        onProgress(100)
                        return@withContext destination
                    } catch (_: Exception) {
                        destination.delete()
                    }
                }
                val connection = openHttpsRelease(original)
                try {
                    if (connection.contentLengthLong > MAX_BYTES) error("APK is too large")
                    var total = 0L
                    var lastPercent = -1
                    connection.inputStream.use { input ->
                        FileOutputStream(partial).use { output ->
                            val buffer = ByteArray(64 * 1024)
                            while (true) {
                                currentCoroutineContext().ensureActive()
                                val read = input.read(buffer)
                                if (read < 0) break
                                total += read
                                if (total > release.size || total > MAX_BYTES) error("APK exceeds release size")
                                output.write(buffer, 0, read)
                                val percent = (total * 100L / release.size).toInt().coerceIn(0, 100)
                                if (percent != lastPercent) {
                                    lastPercent = percent
                                    withContext(Dispatchers.Main) { onProgress(percent) }
                                }
                            }
                            output.fd.sync()
                        }
                    }
                    if (total != release.size) error("APK download incomplete")
                } finally {
                    connection.disconnect()
                }
                if (!partial.renameTo(destination)) error("Cannot finalize APK download")
                validateInstalledIdentity(context, destination, release.version)
                withContext(Dispatchers.Main) { onProgress(100) }
                destination
            } catch (error: Throwable) {
                partial.delete()
                destination.delete()
                throw error
            }
        }

    /** @param requested Official release URL. @return Successful HTTPS connection, after bounded trusted redirects. */
    private fun openHttpsRelease(requested: String): HttpURLConnection {
        var url = URL(requested)
        repeat(6) {
            val hostname = url.host.lowercase()
            require(url.protocol == "https" &&
                (hostname == "github.com" || hostname == "release-assets.githubusercontent.com" ||
                    hostname == "objects.githubusercontent.com" || hostname.endsWith(".githubusercontent.com"))
            ) { "Untrusted download redirect" }
            val connection = url.openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 15_000
            connection.readTimeout = 30_000
            connection.setRequestProperty("User-Agent", "ELECTRO-Android-Updater")
            val status = connection.responseCode
            if (status == HttpURLConnection.HTTP_OK) return connection
            if (status in 300..399) {
                val location = connection.getHeaderField("Location")
                connection.disconnect()
                if (location.isNullOrBlank()) error("Missing download redirect")
                url = URL(url, location)
            } else {
                connection.disconnect()
                error("GitHub download unavailable (HTTP " + status + ")")
            }
        }
        error("Too many GitHub download redirects")
    }

    /** @param context Installed ELECTRO package. @param apk Candidate APK. @param version Exact expected release version. @return Unit after name, version and signing checks. */
    @Suppress("DEPRECATION")
    fun validateInstalledIdentity(context: Context, apk: File, version: String) {
        require(apk.isFile && apk.length() > 100_000) { "Invalid APK archive" }
        apk.inputStream().use { input ->
            val header = ByteArray(4)
            if (input.read(header) != 4 ||
                !header.contentEquals(byteArrayOf(0x50, 0x4B, 0x03, 0x04))
            ) error("APK archive header is invalid")
        }
        val flags = if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES
            else PackageManager.GET_SIGNATURES
        val manager = context.packageManager
        val candidate = manager.getPackageArchiveInfo(apk.absolutePath, flags)
            ?: error("APK package information unavailable")
        val current = manager.getPackageInfo(context.packageName, flags)
        require(candidate.packageName == context.packageName && candidate.versionName == version) {
            "Update does not match installed ELECTRO package and version"
        }
        val candidateSignatures = if (Build.VERSION.SDK_INT >= 28)
            candidate.signingInfo?.apkContentsSigners else candidate.signatures
        val installedSignatures = if (Build.VERSION.SDK_INT >= 28)
            current.signingInfo?.apkContentsSigners else current.signatures
        require(!candidateSignatures.isNullOrEmpty() && !installedSignatures.isNullOrEmpty() &&
            candidateSignatures.size == installedSignatures.size &&
            candidateSignatures.all { candidateSignature ->
                installedSignatures.any { installed -> installed == candidateSignature }
            }
        ) { "Update signing certificate does not match installed ELECTRO" }
    }
}
