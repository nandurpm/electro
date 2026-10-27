package com.electrokit.app.viewmodel

import com.electrokit.app.data.InputJournal
import java.io.File
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Exercises actual atomic-file persistence rather than an in-memory staging fixture. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class InputJournalTests {
    /** Private temporary files isolate journal recovery from application preferences. */
    @get:Rule val folder = TemporaryFolder()

    /**
     * A completed staging call must be readable by a newly constructed store.
     *
     * @return Unit; disk/read-back mismatches fail the test.
     */
    @Test
    fun stagedInputsSurviveNewJournalInstance() {
        val file = File(folder.root, "inputs.json")
        InputJournal(file).put("c4", mapOf("v" to "24", "unit_r" to "3"))
        assertEquals(mapOf("v" to "24", "unit_r" to "3"), InputJournal(file).read()["c4"])
    }

    /**
     * Acknowledging an older DataStore write must not delete a newer staged edit.
     *
     * @return Unit; assertions detect lost updates or failed acknowledgement cleanup.
     */
    @Test
    fun acknowledgementRetainsNewerEdits() {
        val journal = InputJournal(File(folder.root, "inputs.json"))
        journal.put("c4", mapOf("v" to "12"))
        val old = journal.read()
        journal.put("c4", mapOf("v" to "24"))
        journal.clear(old)
        assertEquals("24", journal.read()["c4"]?.get("v"))
        journal.clear(journal.read())
        assertTrue(journal.read().isEmpty())
    }

    /**
     * Damaged backup text must produce an error instead of silently discarding unsaved work.
     *
     * @return Unit; expected IOException confirms corruption is surfaced.
     */
    @Test(expected = java.io.IOException::class)
    fun corruptJournalIsNotSilentlyReplaced() {
        val file = File(folder.root, "inputs.json")
        file.writeText("broken")
        InputJournal(file).read()
    }
    /** @return Unit; damaged JSON survives quarantine and a later explicit retry can save again. */
    @Test
    fun corruptJournalIsPreservedAndRetryCanContinue() {
        val path = File(folder.root, "broken.json")
        path.writeText("{broken")
        val journal = InputJournal(path)
        try { journal.read(); fail("Expected visible corruption error") } catch (_: java.io.IOException) { }
        assertTrue(folder.root.listFiles()!!.any { it.name.startsWith("broken.json.corrupt-") && it.readText() == "{broken" })
        journal.put("c4", mapOf("v" to "12"))
        assertEquals("12", journal.read()["c4"]?.get("v"))
    }

}
