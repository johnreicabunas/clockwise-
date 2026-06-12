package com.johnreicabunas.clockwise.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * A complete color identity for the app. The default Coral Night palette is free;
 * the rest are part of Clockwise Pro.
 */
@Immutable
data class ClockwisePalette(
    val id: String,
    val name: String,
    val isPro: Boolean,
    val accent: Color,
    val accentSecondary: Color,
    val accentTertiary: Color,
    val background: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val text: Color = ClockwiseText,
    val muted: Color = ClockwiseMuted
)

internal val CoralNightPalette = ClockwisePalette(
    id = "coral",
    name = "Coral Night",
    isPro = false,
    accent = ClockwiseCoral,
    accentSecondary = ClockwiseViolet,
    accentTertiary = ClockwiseCyan,
    background = ClockwiseBackground,
    surface = ClockwiseSurface,
    surfaceRaised = ClockwiseSurfaceRaised
)

internal val ClockwisePalettes: List<ClockwisePalette> = listOf(
    CoralNightPalette,
    ClockwisePalette(
        id = "solar",
        name = "Solar Gold",
        isPro = true,
        accent = Color(0xFFFFB454),
        accentSecondary = Color(0xFFFF8A5C),
        accentTertiary = Color(0xFFFFE08A),
        background = Color(0xFF141210),
        surface = Color(0xFF1D1A16),
        surfaceRaised = Color(0xFF272219)
    ),
    ClockwisePalette(
        id = "emerald",
        name = "Emerald",
        isPro = true,
        accent = Color(0xFF4ADE80),
        accentSecondary = Color(0xFF2DD4BF),
        accentTertiary = Color(0xFFA3E635),
        background = Color(0xFF101413),
        surface = Color(0xFF171D1B),
        surfaceRaised = Color(0xFF1F2724)
    ),
    ClockwisePalette(
        id = "ocean",
        name = "Ocean",
        isPro = true,
        accent = Color(0xFF38BDF8),
        accentSecondary = Color(0xFF818CF8),
        accentTertiary = Color(0xFF5EEAD4),
        background = Color(0xFF0F1217),
        surface = Color(0xFF161B22),
        surfaceRaised = Color(0xFF1E242E)
    ),
    ClockwisePalette(
        id = "midnight",
        name = "Midnight AMOLED",
        isPro = true,
        accent = ClockwiseCoral,
        accentSecondary = ClockwiseViolet,
        accentTertiary = ClockwiseCyan,
        background = Color(0xFF000000),
        surface = Color(0xFF0B0B0E),
        surfaceRaised = Color(0xFF16161B)
    )
)

internal fun paletteFor(id: String): ClockwisePalette =
    ClockwisePalettes.firstOrNull { it.id == id } ?: CoralNightPalette

/** Dial rendering styles for [com.johnreicabunas.clockwise.presentation.home.ClockFace]. */
enum class ClockFaceStyle(val id: String, val displayName: String, val isPro: Boolean) {
    CLASSIC("classic", "Classic", false),
    MINIMAL("minimal", "Minimal", true),
    ROMAN("roman", "Roman", true);

    companion object {
        fun fromId(id: String): ClockFaceStyle = entries.firstOrNull { it.id == id } ?: CLASSIC
    }
}
