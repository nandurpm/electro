package com.electrokit.app.data

import org.junit.Assert.*
import org.junit.Test

/** Ensures complete Tamil lesson coverage without changing technical headings or English fallback. */
class LearningLanguageTests {
    @Test fun everyLessonHasTamilProseAndStableTechnicalHeadings() {
        learningPages.forEach { page ->
            val tamil = learningSections(page, "ta")
            assertEquals(page.sections.size, tamil.size)
            assertEquals(page.sections.map { it.first }, tamil.map { it.first })
            assertTrue(page.id, tamil.all { section -> section.second.any { it in '\u0B80'..'\u0BFF' } })
            assertEquals(page.sections, learningSections(page, "en"))
            assertEquals(page.sections, learningSections(page, "unknown"))
        }
        assertTrue(learningSections(learningPages.first(), "ta").any { it.second.contains("COM") })
    }
    @Test fun freshPreferencesAreLightAndExplicitDarkIsPreserved() {
        assertTrue(PreferenceCodec.decode(null).light)
        assertFalse(PreferenceCodec.decode("{\"light\":false,\"themeMode\":\"dark\"}").light)
        assertEquals("dark", PreferenceCodec.decode("{\"light\":false,\"themeMode\":\"dark\"}").themeMode)
    }
}
