package com.electrokit.app.domain

import org.junit.Assert.*
import org.junit.Test

/** Verifies the configured local code and rejects malformed and incorrect candidates. */
class LocalPinTests {
    /** @return Unit; the initial code succeeds without exposing it in a UI component. */
    @Test fun initialCodeVerifies() { assertTrue(LocalPin.verify("789")) }
    /** @return Unit; empty, malformed, padded and incorrect candidates must fail. */
    @Test fun invalidCandidatesFail() {
        listOf("", "000", "788", "7890", " 789", "abc", "７８９").forEach { assertFalse(it, LocalPin.verify(it)) }
    }
}
