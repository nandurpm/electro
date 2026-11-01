package com.electrokit.app.update

import org.junit.Assert.*
import org.junit.Test

/** Plain-text notes keep links readable and never leave a split UTF-16 surrogate. */
class ReleaseNotesTests {
    /** @return Unit; common GitHub Markdown becomes readable dialog prose. */
    @Test
    fun normalizesMarkdown() {
        assertEquals(
            "Changes\n• New features\nRead more",
            ReleaseNotes.plainText(
                "## Changes\n- **New features**\n[Read more](https://example.com)"
            ),
        )
    }

    /** @return Unit; truncation respects whole code points and a nearby word boundary. */
    @Test
    fun truncatesWithoutSplittingEmoji() {
        val notes = ReleaseNotes.plainText("😀".repeat(700))
        assertEquals(651, notes.codePointCount(0, notes.length))
        assertTrue(notes.endsWith("…"))
        assertEquals("one two…", ReleaseNotes.plainText("one two three four", 10))
        assertFalse(Character.isHighSurrogate(notes[notes.length - 2]))
    }
}
