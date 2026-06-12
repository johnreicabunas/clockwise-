package com.johnreicabunas.clockwise.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import clockwise.composeapp.generated.resources.Res
import clockwise.composeapp.generated.resources.outfit_bold
import clockwise.composeapp.generated.resources.outfit_medium
import clockwise.composeapp.generated.resources.outfit_regular
import clockwise.composeapp.generated.resources.outfit_semibold
import org.jetbrains.compose.resources.Font

/** Tabular figures so times don't jitter as digits tick over. */
internal fun TextStyle.tabular(): TextStyle = copy(fontFeatureSettings = "tnum")

@Composable
private fun outfitFamily() = FontFamily(
    Font(Res.font.outfit_regular, FontWeight.Normal),
    Font(Res.font.outfit_medium, FontWeight.Medium),
    Font(Res.font.outfit_semibold, FontWeight.SemiBold),
    Font(Res.font.outfit_bold, FontWeight.Bold)
)

@Composable
internal fun clockwiseTypography(): Typography {
    val outfit = outfitFamily()
    val base = Typography()
    return Typography(
        displayLarge = base.displayLarge.copy(fontFamily = outfit),
        displayMedium = base.displayMedium.copy(fontFamily = outfit),
        displaySmall = base.displaySmall.copy(
            fontFamily = outfit,
            fontSize = 34.sp,
            lineHeight = 40.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.5).sp
        ),
        headlineLarge = base.headlineLarge.copy(fontFamily = outfit),
        headlineMedium = base.headlineMedium.copy(fontFamily = outfit),
        headlineSmall = base.headlineSmall.copy(
            fontFamily = outfit,
            fontSize = 24.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.SemiBold
        ),
        titleLarge = base.titleLarge.copy(
            fontFamily = outfit,
            fontSize = 20.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.SemiBold
        ),
        titleMedium = base.titleMedium.copy(
            fontFamily = outfit,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.SemiBold
        ),
        titleSmall = base.titleSmall.copy(
            fontFamily = outfit,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium
        ),
        bodyLarge = base.bodyLarge.copy(fontFamily = outfit, fontSize = 16.sp, lineHeight = 24.sp),
        bodyMedium = base.bodyMedium.copy(fontFamily = outfit, fontSize = 14.sp, lineHeight = 20.sp),
        bodySmall = base.bodySmall.copy(fontFamily = outfit, fontSize = 12.sp, lineHeight = 16.sp),
        labelLarge = base.labelLarge.copy(
            fontFamily = outfit,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium
        ),
        labelMedium = base.labelMedium.copy(
            fontFamily = outfit,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium
        ),
        labelSmall = base.labelSmall.copy(
            fontFamily = outfit,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.2.sp
        )
    )
}
