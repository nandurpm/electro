package com.electrokit.app.update

import org.junit.Assert.*
import org.junit.Test

class UpdatePolicyTests {
    @Test fun patchesAreOptional() {
        assertEquals(UpdatePriority.OPTIONAL_PATCH, UpdatePolicy.priority("4.3.0", "4.3.1"))
        assertEquals(UpdatePriority.OPTIONAL_PATCH, UpdatePolicy.priority("4.3.1", "v4.3.2"))
    }
    @Test fun featureReleasesAreRequired() {
        assertEquals(UpdatePriority.REQUIRED_FEATURE, UpdatePolicy.priority("4.2.0", "4.3.0"))
        assertEquals(UpdatePriority.REQUIRED_FEATURE, UpdatePolicy.priority("4.3.2", "4.4.0"))
        assertEquals(UpdatePriority.REQUIRED_FEATURE, UpdatePolicy.priority("4.3.2", "5.0.0"))
    }
    @Test fun equalOlderOrInvalidReleasesDoNotPrompt() {
        assertEquals(UpdatePriority.NONE, UpdatePolicy.priority("4.3.0", "4.3.0"))
        assertEquals(UpdatePriority.NONE, UpdatePolicy.priority("4.3.0", "4.2.9"))
        assertEquals(UpdatePriority.NONE, UpdatePolicy.priority("4.3.0", "v4.3.0-rc1"))
        assertEquals(UpdatePriority.NONE, UpdatePolicy.priority("4.3.0", "bad"))
    }
    @Test fun numericVersionOrder() {
        assertEquals(UpdatePriority.REQUIRED_FEATURE, UpdatePolicy.priority("4.9.0", "4.10.0"))
        assertEquals(UpdatePriority.OPTIONAL_PATCH, UpdatePolicy.priority("4.3.9", "4.3.10"))
        assertNull(ElectroVersion.parse("4.3"))
    }
}
