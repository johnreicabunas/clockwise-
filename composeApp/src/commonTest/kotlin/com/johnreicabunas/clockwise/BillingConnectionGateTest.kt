package com.johnreicabunas.clockwise

import com.johnreicabunas.clockwise.data.repository.BillingConnectionGate
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BillingConnectionGateTest {

    @Test
    fun ignoresDuplicateConnectionRequestsUntilConnectionFinishes() {
        val gate = BillingConnectionGate()

        assertTrue(gate.tryStartConnecting())
        assertFalse(gate.tryStartConnecting())

        gate.onConnectionFinished()

        assertTrue(gate.tryStartConnecting())
    }
}
