package com.johnreicabunas.clockwise.presentation.support

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.johnreicabunas.clockwise.domain.model.BillingState
import com.johnreicabunas.clockwise.domain.model.SupportProductIds
import com.johnreicabunas.clockwise.presentation.home.ClockwiseBackground
import com.johnreicabunas.clockwise.presentation.home.ClockwiseCoral
import com.johnreicabunas.clockwise.presentation.home.ClockwiseMuted
import com.johnreicabunas.clockwise.presentation.home.ClockwiseSurface
import com.johnreicabunas.clockwise.presentation.home.ClockwiseSurfaceRaised
import com.johnreicabunas.clockwise.presentation.home.ClockwiseText
import com.johnreicabunas.clockwise.presentation.home.ClockwiseViolet
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import clockwise.composeapp.generated.resources.Res
import clockwise.composeapp.generated.resources.support_ads_removed
import clockwise.composeapp.generated.resources.support_back
import clockwise.composeapp.generated.resources.support_chicken_body
import clockwise.composeapp.generated.resources.support_chicken_title
import clockwise.composeapp.generated.resources.support_coffee_body
import clockwise.composeapp.generated.resources.support_coffee_title
import clockwise.composeapp.generated.resources.support_intro
import clockwise.composeapp.generated.resources.support_not_available
import clockwise.composeapp.generated.resources.support_pizza_body
import clockwise.composeapp.generated.resources.support_pizza_title
import clockwise.composeapp.generated.resources.support_remove_ads_body
import clockwise.composeapp.generated.resources.support_remove_ads_title
import clockwise.composeapp.generated.resources.support_title

@Composable
fun SupportScreenRoot(
    padding: PaddingValues,
    onBack: () -> Unit,
    viewModel: SupportScreenViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SupportScreen(
        padding = padding,
        state = state,
        onBack = onBack,
        onPurchase = viewModel::purchase
    )
}

@Composable
fun SupportScreen(
    padding: PaddingValues,
    state: BillingState,
    onBack: () -> Unit,
    onPurchase: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(ClockwiseBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        item {
            Text(
                text = stringResource(Res.string.support_title),
                color = ClockwiseText,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(Res.string.support_intro),
                color = ClockwiseMuted,
                fontSize = 14.sp
            )
        }

        item {
            SupportOptionCard(
                icon = Icons.Default.Coffee,
                title = stringResource(Res.string.support_coffee_title),
                body = stringResource(Res.string.support_coffee_body),
                price = state.priceFor(SupportProductIds.COFFEE),
                enabled = state.canPurchase(SupportProductIds.COFFEE),
                onClick = { onPurchase(SupportProductIds.COFFEE) }
            )
        }
        item {
            SupportOptionCard(
                icon = Icons.Default.LocalPizza,
                title = stringResource(Res.string.support_pizza_title),
                body = stringResource(Res.string.support_pizza_body),
                price = state.priceFor(SupportProductIds.PIZZA),
                enabled = state.canPurchase(SupportProductIds.PIZZA),
                onClick = { onPurchase(SupportProductIds.PIZZA) }
            )
        }
        item {
            SupportOptionCard(
                icon = Icons.Default.Restaurant,
                title = stringResource(Res.string.support_chicken_title),
                body = stringResource(Res.string.support_chicken_body),
                price = state.priceFor(SupportProductIds.CHICKEN),
                enabled = state.canPurchase(SupportProductIds.CHICKEN),
                onClick = { onPurchase(SupportProductIds.CHICKEN) }
            )
        }
        item {
            SupportOptionCard(
                icon = Icons.Default.Block,
                title = stringResource(Res.string.support_remove_ads_title),
                body = if (state.isAdsRemoved) {
                    stringResource(Res.string.support_ads_removed)
                } else {
                    stringResource(Res.string.support_remove_ads_body)
                },
                price = state.priceFor(SupportProductIds.REMOVE_ADS),
                enabled = !state.isAdsRemoved && state.canPurchase(SupportProductIds.REMOVE_ADS),
                onClick = { onPurchase(SupportProductIds.REMOVE_ADS) },
                accent = ClockwiseViolet
            )
        }

        if (state.isLoading) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = ClockwiseCoral)
                }
            }
        }

        state.message?.let { message ->
            item {
                Text(message, color = ClockwiseMuted, fontSize = 13.sp)
            }
        }

        if (!state.isLoading && !state.isAvailable) {
            item {
                Text(
                    text = stringResource(Res.string.support_not_available),
                    color = ClockwiseMuted,
                    fontSize = 13.sp
                )
            }
        }

        item {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, ClockwiseSurfaceRaised)
            ) {
                Text(stringResource(Res.string.support_back), color = ClockwiseMuted)
            }
        }
    }
}

@Composable
private fun SupportOptionCard(
    icon: ImageVector,
    title: String,
    body: String,
    price: String,
    enabled: Boolean,
    onClick: () -> Unit,
    accent: Color = ClockwiseCoral
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ClockwiseSurface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(28.dp))
                Spacer(Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = ClockwiseText, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text(body, color = ClockwiseMuted, fontSize = 13.sp)
                }
            }
            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = accent)
            ) {
                Text(price.ifBlank { title })
            }
        }
    }
}

private fun BillingState.priceFor(productId: String): String =
    products.firstOrNull { it.id == productId }?.formattedPrice.orEmpty()

private fun BillingState.canPurchase(productId: String): Boolean =
    isAvailable && products.any { it.id == productId }

@Preview
@Composable
private fun SupportScreenPreview() {
    SupportScreen(
        padding = PaddingValues(),
        state = BillingState(isLoading = false, isAvailable = true),
        onBack = {},
        onPurchase = {}
    )
}
