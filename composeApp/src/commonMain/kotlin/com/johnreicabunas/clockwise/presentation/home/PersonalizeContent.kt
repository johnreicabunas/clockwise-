package com.johnreicabunas.clockwise.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.johnreicabunas.clockwise.domain.model.AppearanceSettings
import com.johnreicabunas.clockwise.presentation.theme.ClockFaceStyle
import com.johnreicabunas.clockwise.presentation.theme.ClockwisePalette
import com.johnreicabunas.clockwise.presentation.theme.ClockwisePalettes
import com.johnreicabunas.clockwise.presentation.theme.ListBottomPadding
import com.johnreicabunas.clockwise.presentation.theme.Spacing

@Composable
internal fun PersonalizeContent(
    appearance: AppearanceSettings,
    isProUnlocked: Boolean,
    onSelectPalette: (String) -> Unit,
    onSelectClockFace: (String) -> Unit,
    onUnlockPro: () -> Unit,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
        contentPadding = PaddingValues(top = Spacing.lg, bottom = ListBottomPadding)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Personalize",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Themes and clock faces",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = onBack) {
                    Text("Back", color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        if (!isProUnlocked) {
            item {
                ProLockedCard(
                    title = "Premium themes & clock faces",
                    body = "Coral Night is free forever. Unlock Clockwise Pro for Solar Gold, Emerald, Ocean, Midnight AMOLED, and the Minimal and Roman dials.",
                    onUnlock = onUnlockPro
                )
            }
        }

        item {
            Text(
                "THEME",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.sm)
            )
        }

        items(ClockwisePalettes.size) { index ->
            val palette = ClockwisePalettes[index]
            PaletteCard(
                palette = palette,
                selected = palette.id == appearance.paletteId,
                locked = palette.isPro && !isProUnlocked,
                onClick = {
                    if (palette.isPro && !isProUnlocked) onUnlockPro() else onSelectPalette(palette.id)
                }
            )
        }

        item {
            Text(
                "CLOCK FACE",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.sm)
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                ClockFaceStyle.entries.forEach { face ->
                    val locked = face.isPro && !isProUnlocked
                    val selected = face.id == appearance.clockFaceId
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        elevation = CardDefaults.cardElevation(0.dp),
                        border = if (selected) {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        } else null,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                if (locked) onUnlockPro() else onSelectClockFace(face.id)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.md),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                face.displayName,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                            if (locked) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Requires Clockwise Pro",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .padding(start = Spacing.xs)
                                        .size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaletteCard(
    palette: ClockwisePalette,
    selected: Boolean,
    locked: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = if (selected) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        } else null,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // Swatch trio on the palette's own background so it reads as a preview.
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(palette.background, MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Swatch(palette.accent)
                    Swatch(palette.accentSecondary)
                    Swatch(palette.accentTertiary)
                }
            }
            Column(Modifier.weight(1f)) {
                Text(
                    palette.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    if (palette.isPro) "Clockwise Pro" else "Free",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            when {
                selected -> Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
                locked -> Icon(
                    Icons.Default.Lock,
                    contentDescription = "Requires Clockwise Pro",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun Swatch(color: Color) {
    Box(
        Modifier
            .size(12.dp)
            .background(color, CircleShape)
    )
}
