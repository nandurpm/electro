package com.electrokit.app.data

import android.util.AtomicFile
import java.io.File
import java.io.IOException
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/**
 * Stores a small atomic input backup on the persistence worker. DataStore remains the
 * main record, while this journal recovers writes interrupted before that asynchronous commit.
 *
 * @param path Private application file; callers must not share it between unrelated app instances.
 *   Construction performs no I/O; methods throw IOException for unreadable or unwritable storage.
 */
class InputJournal(path: File) {
    /** AtomicFile preserves the previous record when a write is interrupted. */
    private val file = AtomicFile(path)
    /** Exact string snapshots preserve partially typed values and selected unit scales. */
    private val codec =
        MapSerializer(String.serializer(), MapSerializer(String.serializer(), String.serializer()))
    /** No lenient decoding: corrupt backups are surfaced rather than silently discarded. */
    private val json = Json

    /**
     * Reads the recoverable snapshot; no record means no pending input.
     *
     * @return Stored calculator maps; IOException preserves a damaged original for explicit
     *   recovery.
     */
    @Synchronized
    fun read(): Map<String, Map<String, String>> {
        if (!file.baseFile.exists() && !File(file.baseFile.path + ".bak").exists())
            return emptyMap()
        return try {
            json.decodeFromString(codec, file.openRead().bufferedReader().use { it.readText() })
        } catch (e: kotlinx.serialization.SerializationException) {
            // Quarantine only malformed content, never an ordinary permission/read failure.
            val damaged = File(file.baseFile.path + ".corrupt-" + System.currentTimeMillis())
            if (file.baseFile.exists() && file.baseFile.renameTo(damaged)) file.delete()
            throw IOException("The damaged input backup was preserved; retry to continue.", e)
        } catch (e: Exception) {
            throw IOException("The local input backup could not be read.", e)
        }
    }

    /**
     * Durably stages one complete calculator snapshot on the IO worker.
     *
     * @param id Stable calculator key.
     * @param values Immutable raw fields, including unit-selector state.
     * @return Unit after an fsync and read-back check; IOException means the write was not
     *   acknowledged.
     */
    @Synchronized
    fun put(id: String, values: Map<String, String>) {
        write(read() + (id to values.toMap()))
    }

    /**
     * Clears only snapshots that actually reached DataStore; newer keystrokes must remain staged.
     *
     * @param committed Snapshot that completed the main-record transaction.
     * @return Unit after clearing matching records; IOException leaves a visible retryable failure.
     */
    @Synchronized
    fun clear(committed: Map<String, Map<String, String>>) {
        val before = read()
        val after = before.filterNot { (id, values) -> committed[id] == values }
        if (after != before) write(after)
    }

    /**
     * Explicitly resets the journal when the user resets saved preferences.
     *
     * @return Unit after an atomic empty-record commit; IOException propagates.
     */
    @Synchronized
    fun reset() {
        write(emptyMap())
    }

    /**
     * Writes atomically on the caller’s IO worker; never invoke this from the UI thread.
     *
     * @param values Whole pending-input record.
     * @return Unit after atomic replacement and verification; failures roll back the partial file.
     * @throws IOException if storage cannot acknowledge the record. Hardware failure before
     *   completion is not guaranteed.
     */
    private fun write(values: Map<String, Map<String, String>>) {
        val stream = file.startWrite()
        try {
            stream.write(json.encodeToString(codec, values).toByteArray(Charsets.UTF_8))
            stream.fd.sync()
            file.finishWrite(stream)
        } catch (e: Exception) {
            file.failWrite(stream)
            throw IOException("The local input backup could not be committed.", e)
        }
        if (read() != values)
            throw IOException("The local input backup did not match the committed snapshot.")
    }
}
