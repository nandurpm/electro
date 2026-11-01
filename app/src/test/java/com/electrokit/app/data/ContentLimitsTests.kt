package com.electrokit.app.data

import org.junit.Assert.*
import org.junit.Test

/** Proves UTF-8 budgets retain whole recent snapshots and reject oversized content. */
class ContentLimitsTests {
    @Test
    fun rejectsEncodedOversizeRatherThanTruncatingResults() {
        val text = "தமிழ்".repeat(ContentLimits.SNAPSHOT_BYTES / 5)
        assertTrue(text.length <= ContentLimits.SNAPSHOT_BYTES)
        assertThrows(IllegalArgumentException::class.java) {
            ContentLimits.prepend(text, emptyList())
        }
    }

    @Test
    fun evictsOldSnapshotsWithinTheTotalBudget() {
        val entry = "x".repeat(ContentLimits.SNAPSHOT_BYTES)
        val history = ContentLimits.prepend("new", List(50) { entry })
        assertEquals("new", history.first())
        assertTrue(
            history.sumOf { it.toByteArray(Charsets.UTF_8).size } <= ContentLimits.HISTORY_BYTES
        )
        assertTrue(history.drop(1).all { it == entry })
    }
}
