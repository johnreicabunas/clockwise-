package com.johnreicabunas.clockwise.presentation.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal val ClockwiseBackground = Color(0xFF121116)
internal val ClockwiseSurface = Color(0xFF1B1A21)
internal val ClockwiseSurfaceRaised = Color(0xFF24232B)
internal val ClockwiseCoral = Color(0xFFFF5A72)
internal val ClockwiseViolet = Color(0xFF8D7CFF)
internal val ClockwiseCyan = Color(0xFF55D6E8)
internal val ClockwiseText = Color(0xFFF8F7FB)
internal val ClockwiseMuted = Color(0xFF9C99A8)

@Composable
internal fun ClockFace(
    hour: Int,
    minute: Int,
    label: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    size: Dp = 230.dp,
    accent: Color = ClockwiseCoral,
    labelFontSize: TextUnit = 28.sp,
    labelVerticalOffset: Dp = (-22).dp
) {
    Box(
        modifier = modifier
            .size(size)
            .background(ClockwiseSurfaceRaised, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = this.size.minDimension / 2f

            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = radius - 1.dp.toPx(),
                style = Stroke(width = 1.dp.toPx())
            )

            repeat(60) { tick ->
                val angle = (tick * 6f - 90f).toRadians()
                val isHour = tick % 5 == 0
                val outer = radius - 14.dp.toPx()
                val inner = outer - if (isHour) 8.dp.toPx() else 3.dp.toPx()
                val color = if (isHour) ClockwiseText.copy(alpha = 0.5f) else ClockwiseMuted.copy(alpha = 0.2f)
                drawLine(
                    color = color,
                    start = center + Offset(cos(angle) * inner, sin(angle) * inner),
                    end = center + Offset(cos(angle) * outer, sin(angle) * outer),
                    strokeWidth = if (isHour) 1.5.dp.toPx() else 1.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            val hourAngle = (((hour % 12) + minute / 60f) * 30f - 90f).toRadians()
            val minuteAngle = (minute * 6f - 90f).toRadians()
            drawLine(
                color = ClockwiseText,
                start = center,
                end = center + Offset(cos(hourAngle) * radius * 0.34f, sin(hourAngle) * radius * 0.34f),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = accent,
                start = center,
                end = center + Offset(cos(minuteAngle) * radius * 0.55f, sin(minuteAngle) * radius * 0.55f),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(color = accent, radius = 5.dp.toPx(), center = center)
        }

        Text(
            text = label,
            color = ClockwiseText,
            fontWeight = FontWeight.Bold,
            fontSize = labelFontSize,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = labelVerticalOffset),
            maxLines = 1
        )
        Text(
            text = subtitle,
            color = ClockwiseMuted,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 32.dp, vertical = 34.dp)
        )
    }
}

private fun Float.toRadians(): Float = (this * PI / 180f).toFloat()
