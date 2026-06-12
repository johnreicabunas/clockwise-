package com.johnreicabunas.clockwise.presentation.support

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.johnreicabunas.clockwise.domain.model.BillingState
import com.johnreicabunas.clockwise.domain.model.SupportProductIds
import com.johnreicabunas.clockwise.presentation.theme.ListBottomPadding
import com.johnreicabunas.clockwise.presentation.theme.Spacing
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import clockwise.composeapp.generated.resources.Res
import clockwise.composeapp.generated.resources.chicken_one_time_product_icon_clean
import clockwise.composeapp.generated.resources.coffee_one_time_product_icon_clean
import clockwise.composeapp.generated.resources.pizza_one_time_product_icon_clean
import clockwise.composeapp.generated.resources.remove_ads_one_time_product_icon_clean
import clockwise.composeapp.generated.resources.support_ads_removed
import clockwise.composeapp.generated.resources.support_back
import clockwise.composeapp.generated.resources.support_chicken_body
import clockwise.composeapp.generated.resources.support_chicken_title
import clockwise.composeapp.generated.resources.support_coffee_body
import clockwise.composeapp.generated.resources.support_coffee_title
import clockwise.composeapp.generated.resources.support_intro
import clockwise.composeapp.generated.resources.support_not_available
import clockwise.composeapp.generated.resources.support_pizza_body
import clockwise.composeapp.generated.resources.support_pro_body
import clockwise.composeapp.generated.resources.support_pro_title
import clockwise.composeapp.generated.resources.support_pro_unlocked
import clockwise.composeapp.generated.resources.support_pizza_title
import clockwise.composeapp.generated.resources.support_remove_ads_body
import clockwise.composeapp.generated.resources.support_remove_ads_title
import clockwise.composeapp.generated.resources.support_title

private const val SHOW_REMOVE_ADS = false

@Composable
fun SupportScreenRoot(
    onBack: () -> Unit,
    viewModel: SupportScreenViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SupportScreen(
        state = state,
        onBack = onBack,
        onPurchase = viewModel::purchase
    )
}

@Composable
fun SupportScreen(
    state: BillingState,
    onBack: () -> Unit,
    onPurchase: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
        contentPadding = PaddingValues(top = Spacing.lg, bottom = ListBottomPadding)
    ) {
        item {
            Text(
                text = stringResource(Res.string.support_title),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = stringResource(Res.string.support_intro),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            ProHeroCard(
                title = stringResource(Res.string.support_pro_title),
                body = if (state.isProUnlocked) {
                    stringResource(Res.string.support_pro_unlocked)
                } else {
                    stringResource(Res.string.support_pro_body)
                },
                price = state.priceFor(SupportProductIds.PRO),
                isUnlocked = state.isProUnlocked,
                enabled = !state.isProUnlocked && state.canPurchase(SupportProductIds.PRO),
                onClick = { onPurchase(SupportProductIds.PRO) }
            )
        }

        item {
            SupportOptionCard(
                icon = Res.drawable.coffee_one_time_product_icon_clean,
                title = stringResource(Res.string.support_coffee_title),
                body = stringResource(Res.string.support_coffee_body),
                price = state.priceFor(SupportProductIds.COFFEE),
                enabled = state.canPurchase(SupportProductIds.COFFEE),
                onClick = { onPurchase(SupportProductIds.COFFEE) }
            )
        }
        item {
            SupportOptionCard(
                icon = Res.drawable.pizza_one_time_product_icon_clean,
                title = stringResource(Res.string.support_pizza_title),
                body = stringResource(Res.string.support_pizza_body),
                price = state.priceFor(SupportProductIds.PIZZA),
                enabled = state.canPurchase(SupportProductIds.PIZZA),
                onClick = { onPurchase(SupportProductIds.PIZZA) }
            )
        }
        item {
            SupportOptionCard(
                icon = Res.drawable.chicken_one_time_product_icon_clean,
                title = stringResource(Res.string.support_chicken_title),
                body = stringResource(Res.string.support_chicken_body),
                price = state.priceFor(SupportProductIds.CHICKEN),
                enabled = state.canPurchase(SupportProductIds.CHICKEN),
                onClick = { onPurchase(SupportProductIds.CHICKEN) }
            )
        }
        // Remove-ads product hidden while ads are disabled; existing owners keep their
        // entitlement via billing state. Re-enable by flipping SHOW_REMOVE_ADS.
        if (SHOW_REMOVE_ADS) {
            item {
                SupportOptionCard(
                    icon = Res.drawable.remove_ads_one_time_product_icon_clean,
                    title = stringResource(Res.string.support_remove_ads_title),
                    body = if (state.isAdsRemoved) {
                        stringResource(Res.string.support_ads_removed)
                    } else {
                        stringResource(Res.string.support_remove_ads_body)
                    },
                    price = state.priceFor(SupportProductIds.REMOVE_ADS),
                    enabled = !state.isAdsRemoved && state.canPurchase(SupportProductIds.REMOVE_ADS),
                    onClick = { onPurchase(SupportProductIds.REMOVE_ADS) },
                    accent = MaterialTheme.colorScheme.secondary,
                    highlighted = true
                )
            }
        }

        if (state.isLoading) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        state.message?.let { message ->
            item {
                Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (!state.isLoading && !state.isAvailable) {
            item {
                Text(
                    text = stringResource(Res.string.support_not_available),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = onBack) {
                    Text(stringResource(Res.string.support_back), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun ProHeroCard(
    title: String,
    body: String,
    price: String,
    isUnlocked: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f),
                            MaterialTheme.shapes.small
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        "ONE-TIME PURCHASE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Text(
                body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!isUnlocked) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    listOf(
                        "Unlimited alarms & reminders",
                        "Meeting Time Finder",
                        "Time Travel slider",
                        "Premium themes & clock faces",
                        "Home-screen widget",
                        "No ads, ever"
                    ).forEach { feature ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                feature,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            Button(
                onClick = onClick,
                enabled = enabled,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    if (isUnlocked) "Unlocked" else price.ifBlank { "Unlock Pro" },
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun SupportOptionCard(
    icon: DrawableResource,
    title: String,
    body: String,
    price: String,
    enabled: Boolean,
    onClick: () -> Unit,
    accent: Color = MaterialTheme.colorScheme.primary,
    highlighted: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = if (highlighted) BorderStroke(1.dp, accent.copy(alpha = 0.3f)) else null
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(icon),
                    contentDescription = title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(Modifier.size(Spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
                    Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
            Button(
                onClick = onClick,
                enabled = enabled,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = Color.White
                )
            ) {
                Text(price.ifBlank { title }, style = MaterialTheme.typography.labelLarge)
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
        state = BillingState(isLoading = false, isAvailable = true),
        onBack = {},
        onPurchase = {}
    )
}
