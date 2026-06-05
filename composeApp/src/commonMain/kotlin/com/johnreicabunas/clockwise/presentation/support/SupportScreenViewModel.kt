package com.johnreicabunas.clockwise.presentation.support

import androidx.lifecycle.ViewModel
import com.johnreicabunas.clockwise.domain.repository.BillingRepository

class SupportScreenViewModel(
    private val billingRepository: BillingRepository
) : ViewModel() {
    val state = billingRepository.state

    fun purchase(productId: String) {
        billingRepository.purchase(productId)
    }
}
