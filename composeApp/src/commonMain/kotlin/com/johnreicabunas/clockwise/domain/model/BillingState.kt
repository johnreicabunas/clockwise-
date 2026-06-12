package com.johnreicabunas.clockwise.domain.model

data class BillingState(
    val products: List<SupportProduct> = emptyList(),
    val isLoading: Boolean = true,
    val isAvailable: Boolean = false,
    val isAdsRemoved: Boolean = false,
    val isProUnlocked: Boolean = false,
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
    const val PRO = "clockwise_pro"

    val all = listOf(PRO, COFFEE, PIZZA, CHICKEN, REMOVE_ADS)
    val donations = setOf(COFFEE, PIZZA, CHICKEN)
}
