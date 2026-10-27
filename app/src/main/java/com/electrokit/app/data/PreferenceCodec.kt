package com.electrokit.app.data

import java.io.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Preserves corrupt saved records by surfacing decoding failures instead of silently replacing them
 * with defaults. Construction holds metadata/helpers; physical validation is performed by the named
 * operations, not by field assignment.
 */
object PreferenceCodec {
    /**
     * Compatibility codec accepts additive fields and writes explicit defaults. Accept additive
     * schema fields for compatibility, but surface corrupt syntax instead of replacing it.
     */
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Serialises one atomic preference snapshot with explicit defaults for schema compatibility.
     *
     * @param state Immutable preference snapshot to encode.
     * @return JSON text containing the snapshot; serialization failures propagate to the caller. No
     *   file I/O occurs here; storage ownership remains with Repository.
     */
    fun encode(state: PreferencesState): String =
        json.encodeToString(PreferencesState.serializer(), state)

    /**
     * Decodes saved preferences without silently discarding a damaged original record.
     *
     * @param raw Saved JSON text; null means no saved record and yields the documented defaults.
     * @return Decoded preferences, or defaults only when no record exists.
     * @throws IOException when malformed JSON cannot be decoded; the caller retains the original
     *   bytes.
     */
    fun decode(raw: String?): PreferencesState =
        try {
            json.decodeFromString<PreferencesState>(raw ?: "{}")
            // Substituting defaults here would let a later write destroy the retained damaged
            // original.
        } catch (e: SerializationException) {
            throw IOException(
                "Saved preferences could not be decoded; the original data has been retained.",
                e,
            )
        }
}
