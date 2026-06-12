package com.johnreicabunas.clockwise.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

internal val ClockwiseBackground = Color(0xFF121116)
internal val ClockwiseSurface = Color(0xFF1B1A21)
internal val ClockwiseSurfaceRaised = Color(0xFF24232B)
internal val ClockwiseCoral = Color(0xFFFF5A72)
internal val ClockwiseViolet = Color(0xFF8D7CFF)
internal val ClockwiseCyan = Color(0xFF55D6E8)
internal val ClockwiseText = Color(0xFFF8F7FB)
internal val ClockwiseMuted = Color(0xFF9C99A8)
internal val ClockwiseOutline = Color.White.copy(alpha = 0.08f)

/** Semantic colors tuned for the dark palette; not covered by Material slots. */
@Immutable
internal data class ClockwiseColors(
    val success: Color = Color(0xFF6FDB8B),
    val successContainer: Color = Color(0xFF16271C),
    val onSuccessContainer: Color = Color(0xFF9CE8B0),
    val warning: Color = Color(0xFFFFC85C),
    val warningContainer: Color = Color(0xFF2B2315),
    val onWarningContainer: Color = Color(0xFFFFDD9E),
    val error: Color = Color(0xFFFF7A85),
    val errorContainer: Color = Color(0xFF2E1A1E),
    val onErrorContainer: Color = Color(0xFFFFB3BA),
    val info: Color = Color(0xFF55D6E8),
    val infoContainer: Color = Color(0xFF13262B),
    val onInfoContainer: Color = Color(0xFFA8E9F4)
)

internal val LocalClockwiseColors = staticCompositionLocalOf { ClockwiseColors() }
