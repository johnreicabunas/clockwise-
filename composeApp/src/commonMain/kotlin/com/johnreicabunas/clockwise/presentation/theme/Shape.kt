package com.johnreicabunas.clockwise.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// extraSmall: chips and pills, small: icon tiles and inline blocks,
// medium: text fields and secondary cards, large: standard cards,
// extraLarge: hero card and search pill.
internal val ClockwiseShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
