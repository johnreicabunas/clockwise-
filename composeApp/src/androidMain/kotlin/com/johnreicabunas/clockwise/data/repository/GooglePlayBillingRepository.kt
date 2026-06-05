package com.johnreicabunas.clockwise.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.johnreicabunas.clockwise.domain.model.BillingState
import com.johnreicabunas.clockwise.domain.model.SupportProduct
import com.johnreicabunas.clockwise.domain.model.SupportProductIds
import com.johnreicabunas.clockwise.domain.repository.BillingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.lang.ref.WeakReference

class GooglePlayBillingRepository(
    context: Context
) : BillingRepository, PurchasesUpdatedListener {

    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val productDetails = mutableMapOf<String, ProductDetails>()
    private val connectionGate = BillingConnectionGate()
    private var activityReference = WeakReference<Activity>(null)

    private val _state = MutableStateFlow(
        BillingState(isAdsRemoved = preferences.getBoolean(KEY_ADS_REMOVED, false))
    )
    override val state = _state.asStateFlow()

    private val billingClient = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .enableAutoServiceReconnection()
        .build()

    fun attachActivity(activity: Activity) {
        activityReference = WeakReference(activity)
        connect()
    }

    fun detachActivity(activity: Activity) {
        if (activityReference.get() === activity) {
            activityReference.clear()
        }
    }

    override fun connect() {
        if (billingClient.isReady) {
            refresh()
            return
        }
        if (!connectionGate.tryStartConnecting()) {
            Log.d(TAG, "Ignoring duplicate billing connection request.")
            return
        }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                connectionGate.onConnectionFinished()
                logResult("Billing setup", billingResult)
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    _state.update { it.copy(isAvailable = true, message = null) }
                    refresh()
                } else {
                    showBillingError(billingResult)
                }
            }

            override fun onBillingServiceDisconnected() {
                connectionGate.onConnectionFinished()
                Log.w(TAG, "Billing service disconnected.")
                _state.update { it.copy(isAvailable = false) }
            }
        })
    }

    override fun purchase(productId: String) {
        val activity = activityReference.get()
        val details = productDetails[productId]
        if (activity == null || details == null || !billingClient.isReady) {
            _state.update { it.copy(message = "Purchases are not ready yet. Please try again.") }
            connect()
            return
        }

        val detailsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
        details.oneTimePurchaseOfferDetailsList
            ?.firstOrNull()
            ?.offerToken
            ?.let(detailsBuilder::setOfferToken)

        val result = billingClient.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(detailsBuilder.build()))
                .build()
        )
        if (result.responseCode != BillingResponseCode.OK) {
            showBillingError(result)
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingResponseCode.OK -> purchases.orEmpty().forEach(::processPurchase)
            BillingResponseCode.USER_CANCELED -> Unit
            else -> showBillingError(billingResult)
        }
    }

    private fun refresh() {
        queryProducts()
        queryPurchases()
    }

    private fun queryProducts() {
        val products = SupportProductIds.all.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(ProductType.INAPP)
                .build()
        }
        billingClient.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(products).build()
        ) { billingResult, result ->
            logResult("Product details query", billingResult)
            if (billingResult.responseCode != BillingResponseCode.OK) {
                showBillingError(billingResult)
                return@queryProductDetailsAsync
            }
            productDetails.clear()
            result.productDetailsList.forEach { productDetails[it.productId] = it }
            Log.d(TAG, "Fetched products: ${productDetails.keys.sorted()}")
            result.unfetchedProductList.forEach { unfetched ->
                Log.w(
                    TAG,
                    "Unfetched product id=${unfetched.productId}, status=${unfetched.statusCode}"
                )
            }
            _state.update {
                it.copy(
                    products = SupportProductIds.all.mapNotNull { id ->
                        productDetails[id]?.let { details ->
                            SupportProduct(
                                id = id,
                                formattedPrice = details.oneTimePurchaseOfferDetailsList
                                    ?.firstOrNull()
                                    ?.formattedPrice
                                    .orEmpty()
                            )
                        }
                    },
                    isLoading = false,
                    isAvailable = true,
                    message = if (productDetails.isEmpty()) {
                        "No Google Play products are available for this account or app install."
                    } else {
                        it.message
                    }
                )
            }
        }
    }

    private fun queryPurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(ProductType.INAPP).build()
        ) { billingResult, purchases ->
            logResult("Purchases query", billingResult)
            if (billingResult.responseCode != BillingResponseCode.OK) {
                showBillingError(billingResult)
                return@queryPurchasesAsync
            }
            val hasRemoveAds = purchases.any {
                it.purchaseState == Purchase.PurchaseState.PURCHASED &&
                    SupportProductIds.REMOVE_ADS in it.products
            }
            setAdsRemoved(hasRemoveAds)
            purchases.forEach(::processPurchase)
        }
    }

    private fun processPurchase(purchase: Purchase) {
        when (purchase.purchaseState) {
            Purchase.PurchaseState.PENDING -> {
                _state.update { it.copy(message = "Purchase pending. It will be applied after payment completes.") }
            }
            Purchase.PurchaseState.PURCHASED -> {
                when {
                    SupportProductIds.REMOVE_ADS in purchase.products -> handleRemoveAds(purchase)
                    purchase.products.any(SupportProductIds.donations::contains) -> consumeDonation(purchase)
                }
            }
            else -> Unit
        }
    }

    private fun handleRemoveAds(purchase: Purchase) {
        setAdsRemoved(true)
        if (purchase.isAcknowledged) {
            _state.update { it.copy(message = "Ads are removed. Thank you for supporting Clockwise!") }
            return
        }
        billingClient.acknowledgePurchase(
            AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        ) { result ->
            if (result.responseCode == BillingResponseCode.OK) {
                _state.update { it.copy(message = "Ads are removed. Thank you for supporting Clockwise!") }
            } else {
                showBillingError(result)
            }
        }
    }

    private fun consumeDonation(purchase: Purchase) {
        billingClient.consumeAsync(
            ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        ) { result, _ ->
            if (result.responseCode == BillingResponseCode.OK) {
                _state.update { it.copy(message = "Thank you for supporting Clockwise!") }
            } else {
                showBillingError(result)
            }
        }
    }

    private fun setAdsRemoved(removed: Boolean) {
        preferences.edit().putBoolean(KEY_ADS_REMOVED, removed).apply()
        _state.update { it.copy(isAdsRemoved = removed) }
    }

    private fun showBillingError(result: BillingResult) {
        logResult("Billing error", result)
        _state.update {
            it.copy(
                isLoading = false,
                isAvailable = false,
                message = result.debugMessage.ifBlank { "Google Play purchases are unavailable." }
            )
        }
    }

    private fun logResult(operation: String, result: BillingResult) {
        Log.d(
            TAG,
            "$operation responseCode=${result.responseCode}, debugMessage=${result.debugMessage}"
        )
    }

    private companion object {
        const val TAG = "ClockwiseBilling"
        const val PREFERENCES_NAME = "clockwise_billing"
        const val KEY_ADS_REMOVED = "ads_removed"
    }
}
