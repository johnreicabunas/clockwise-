package com.johnreicabunas.clockwise.presentation.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.johnreicabunas.clockwise.presentation.theme.ClockFaceStyle
import com.johnreicabunas.clockwise.presentation.theme.ClockSize
import com.johnreicabunas.clockwise.presentation.theme.LocalClockFaceStyle
import com.johnreicabunas.clockwise.presentation.theme.Spacing
import com.johnreicabunas.clockwise.presentation.theme.tabular
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun ClockFace(
    hour: Int,
    minute: Int,
    label: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    size: Dp = ClockSize,
    accent: Color = MaterialTheme.colorScheme.primary,
    second: Int? = null,
    showGlow: Boolean = false,
    labelVerticalOffset: Dp = (-36).dp
) {
    val style = LocalClockFaceStyle.current
    val faceColor = MaterialTheme.colorScheme.surfaceVariant
    val handColor = MaterialTheme.colorScheme.onSurface
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant
    val secondColor = MaterialTheme.colorScheme.tertiary

    val textMeasurer = rememberTextMeasurer()
    val romanStyle = MaterialTheme.typography.labelSmall
    val romanLayouts: List<TextLayoutResult>? = if (style == ClockFaceStyle.ROMAN) {
        remember(textMeasurer, romanStyle) {
            listOf("XII", "III", "VI", "IX").map {
                textMeasurer.measure(AnnotatedString(it), romanStyle)
            }
        }
    } else {
        null
    }

    // Animate the second hand by a relative +delta each tick so the
    // 354 -> 0 degree wraparound never sweeps backwards.
    val secondAngle = if (second != null) {
        val anim = remember { Animatable(second * 6f) }
        LaunchedEffect(second) {
            val current = ((anim.value % 360f) + 360f) % 360f
            var delta = second * 6f - current
            if (delta < 0f) delta += 360f
            if (delta > 0f) {
                anim.animateTo(anim.value + delta, tween(550, easing = FastOutSlowInEasing))
            }
        }
        anim
    } else {
        null
    }

    Box(
        modifier = modifier
            .size(size)
            .background(faceColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = this.size.minDimension / 2f

            if (showGlow) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.10f), Color.Transparent),
                        center = center,
                        radius = radius
                    ),
                    radius = radius,
                    center = center
                )
            }

            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = radius - 1.dp.toPx(),
                style = Stroke(width = 1.dp.toPx())
            )

            when (style) {
                ClockFaceStyle.CLASSIC -> repeat(60) { tick ->
                    val angle = (tick * 6f - 90f).toRadians()
                    val isHour = tick % 5 == 0
                    val outer = radius - 14.dp.toPx()
                    val inner = outer - if (isHour) 8.dp.toPx() else 3.dp.toPx()
                    val color = if (isHour) handColor.copy(alpha = 0.5f) else mutedColor.copy(alpha = 0.2f)
                    drawLine(
                        color = color,
                        start = center + Offset(cos(angle) * inner, sin(angle) * inner),
                        end = center + Offset(cos(angle) * outer, sin(angle) * outer),
                        strokeWidth = if (isHour) 1.5.dp.toPx() else 1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
                ClockFaceStyle.MINIMAL -> repeat(4) { quarter ->
                    val angle = (quarter * 90f - 90f).toRadians()
                    val outer = radius - 14.dp.toPx()
                    val inner = outer - 10.dp.toPx()
                    drawLine(
                        color = handColor.copy(alpha = 0.55f),
                        start = center + Offset(cos(angle) * inner, sin(angle) * inner),
                        end = center + Offset(cos(angle) * outer, sin(angle) * outer),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
                ClockFaceStyle.ROMAN -> romanLayouts?.forEachIndexed { index, layout ->
                    val angle = (index * 90f - 90f).toRadians()
                    val position = center + Offset(
                        cos(angle) * (radius - 22.dp.toPx()),
                        sin(angle) * (radius - 22.dp.toPx())
                    )
                    drawText(
                        textLayoutResult = layout,
                        color = mutedColor,
                        topLeft = position - Offset(
                            layout.size.width / 2f,
                            layout.size.height / 2f
                        )
                    )
                }
            }

            val hourAngle = (((hour % 12) + minute / 60f) * 30f - 90f).toRadians()
            val minuteAngle = (minute * 6f - 90f).toRadians()
            drawLine(
                color = handColor,
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
            if (secondAngle != null) {
                val angle = (secondAngle.value - 90f).toRadians()
                drawLine(
                    color = secondColor,
                    start = center - Offset(cos(angle) * radius * 0.12f, sin(angle) * radius * 0.12f),
                    end = center + Offset(cos(angle) * radius * 0.62f, sin(angle) * radius * 0.62f),
                    strokeWidth = 1.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            drawCircle(color = accent, radius = 5.dp.toPx(), center = center)
        }

        Text(
            text = label,
            color = handColor,
            style = MaterialTheme.typography.displaySmall.tabular(),
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = labelVerticalOffset),
            maxLines = 1
        )
        Text(
            text = subtitle,
            color = mutedColor,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = Spacing.xxl, vertical = Spacing.xxl)
        )
    }
}

private fun Float.toRadians(): Float = (this * PI / 180f).toFloat()
