package com.johnreicabunas.clockwise.domain.model

data class BillingState(
    val products: List<SupportProduct> = emptyList(),
    val isLoading: Boolean = true,
    val isAvailable: Boolean = false,
    val isAdsRemoved: Boolean = false,
    val message: String? = null
)

data class SupportProduct(
    val id: String,
    val formattedPrice: String
)

object SupportProductIds {
    const val COFFEE = "support_coffee"
    const val PIZZA = "support_pizza"
    const val CHICKEN = "support_chicken"
    const val REMOVE_ADS = "remove_ads"

    val all = listOf(COFFEE, PIZZA, CHICKEN, REMOVE_ADS)
    val donations = setOf(COFFEE, PIZZA, CHICKEN)
}
