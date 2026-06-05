package com.johnreicabunas.clockwise.data.repository

import com.johnreicabunas.clockwise.domain.model.BillingState
import com.johnreicabunas.clockwise.domain.repository.BillingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class UnavailableBillingRepository : BillingRepository {
    private val _state = MutableStateFlow(
        BillingState(
            isLoading = false,
            isAvailable = false,
            message = "Google Play purchases are available on Android."
        )
    )
    override val state = _state.asStateFlow()

    override fun connect() = Unit

    override fun purchase(productId: String) {
        _state.update { it.copy(message = "Google Play purchases are available on Android.") }
    }
}
