package com.johnreicabunas.clockwise.domain.repository

import com.johnreicabunas.clockwise.domain.model.BillingState
import kotlinx.coroutines.flow.StateFlow

interface BillingRepository {
    val state: StateFlow<BillingState>

    fun connect()
    fun purchase(productId: String)
}
