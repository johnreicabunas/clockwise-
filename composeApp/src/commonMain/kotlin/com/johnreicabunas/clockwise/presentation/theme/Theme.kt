package com.johnreicabunas.clockwise.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

internal val LocalClockFaceStyle = staticCompositionLocalOf { ClockFaceStyle.CLASSIC }

private fun clockwiseColorScheme(palette: ClockwisePalette): ColorScheme = darkColorScheme(
    primary = palette.accent,
    onPrimary = Color.White,
    primaryContainer = lerp(palette.surface, palette.accent, 0.18f),
    onPrimaryContainer = lerp(palette.accent, Color.White, 0.45f),
    secondary = palette.accentSecondary,
    onSecondary = Color.White,
    secondaryContainer = lerp(palette.surface, palette.accentSecondary, 0.18f),
    onSecondaryContainer = lerp(palette.accentSecondary, Color.White, 0.45f),
    tertiary = palette.accentTertiary,
    onTertiary = lerp(palette.accentTertiary, Color.Black, 0.7f),
    background = palette.background,
    onBackground = palette.text,
    surface = palette.surface,
    onSurface = palette.text,
    surfaceVariant = palette.surfaceRaised,
    onSurfaceVariant = palette.muted,
    surfaceContainerLowest = palette.background,
    surfaceContainerLow = palette.surface,
    surfaceContainer = palette.surface,
    surfaceContainerHigh = palette.surfaceRaised,
    surfaceContainerHighest = lerp(palette.surfaceRaised, Color.White, 0.04f),
    outline = ClockwiseOutline,
    outlineVariant = Color.White.copy(alpha = 0.05f),
    error = Color(0xFFFF7A85),
    onError = Color(0xFF3A1218),
    errorContainer = Color(0xFF2E1A1E),
    onErrorContainer = Color(0xFFFFB3BA)
)

/** Accessor for the extended (non-Material) semantic colors: `ClockwiseTheme.colors.success`. */
internal object ClockwiseTheme {
    val colors: ClockwiseColors
        @Composable get() = LocalClockwiseColors.current
}

@Composable
fun ClockwiseTheme(
    palette: ClockwisePalette = CoralNightPalette,
    clockFaceStyle: ClockFaceStyle = ClockFaceStyle.CLASSIC,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalClockwiseColors provides ClockwiseColors(),
        LocalClockFaceStyle provides clockFaceStyle
    ) {
        MaterialTheme(
            colorScheme = clockwiseColorScheme(palette),
            typography = clockwiseTypography(),
            shapes = ClockwiseShapes,
            content = content
        )
    }
}
