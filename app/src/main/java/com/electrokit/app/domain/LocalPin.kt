package com.electrokit.app.domain

import java.security.MessageDigest
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Verifies the initial local access code without storing its clear text in UI or preferences.
 * Uses Android API-24-compatible PBKDF2-HMAC-SHA1. This three-digit offline restriction cannot resist APK analysis or exhaustive guessing.
 */
object LocalPin {
    /** Salted PBKDF2 verification record for the initial local code; never a strong secret. */
    private val expected = "001a6f2e2dfa22673ef59adb2db54bb8c7f4aeeb9736118f6047c43c3e188bef"
        .chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    /** Derives and compares a candidate; invalid lengths never reach the expensive KDF.
 * @param candidate Untrusted code string; only three ASCII digits are eligible.
 * @return True only when the constant-time derived-key comparison succeeds.
 */
    fun verify(candidate: String): Boolean {
        if (!candidate.matches(Regex("[0-9]{3}"))) return false
        val spec = PBEKeySpec(candidate.toCharArray(), "ELECTRO-local-access-v1".toByteArray(), 120000, 256)
        return try {
            MessageDigest.isEqual(expected, SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).encoded)
        } finally { spec.clearPassword() }
    }
}
