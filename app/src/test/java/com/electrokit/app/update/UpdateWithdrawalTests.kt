package com.electrokit.app.update

import org.junit.Assert.*
import org.junit.Test

/** Fresh metadata can revoke a broken release; failures or unchanged metadata cannot. */
class UpdateWithdrawalTests {
    @Test
    fun recognizesExplicitVersionRollback() {
        assertTrue(UpdatePolicy.withdrawn("4.6.0", "4.5.2"))
        assertFalse(UpdatePolicy.withdrawn("4.6.0", "4.6.0"))
        assertFalse(UpdatePolicy.withdrawn("4.6.0", "4.7.0"))
        assertFalse(UpdatePolicy.withdrawn("4.6.0", "invalid"))
    }
}
