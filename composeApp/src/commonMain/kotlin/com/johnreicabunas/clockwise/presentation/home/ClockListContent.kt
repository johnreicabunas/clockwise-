package com.johnreicabunas.clockwise.presentation.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.johnreicabunas.clockwise.domain.model.HomeMode
import com.johnreicabunas.clockwise.domain.model.TimeZoneModel
import com.johnreicabunas.clockwise.presentation.theme.ClockwiseTheme
import com.johnreicabunas.clockwise.presentation.theme.ListBottomPadding
import com.johnreicabunas.clockwise.presentation.theme.Spacing
import com.johnreicabunas.clockwise.presentation.theme.tabular
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt
import kotlin.time.Instant

@Composable
internal fun ClockListContent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    filteredZones: List<TimeZoneModel>,
    isLoading: Boolean,
    error: String?,
    expandedZone: String?,
    deviceZone: TimeZone,
    now: Instant,
    scheduleCount: Int,
    timeTravelHours: Int,
    onTimeTravelChange: (Int) -> Unit,
    isProUnlocked: Boolean,
    onUnlockPro: () -> Unit,
    onClockList: () -> Unit,
    onScheduleList: () -> Unit,
    onPlanner: () -> Unit,
    onZoneClicked: (String) -> Unit,
    onSetAlarm: (TimeZoneModel) -> Unit,
    onCreateMeeting: (TimeZoneModel) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
        contentPadding = PaddingValues(top = Spacing.lg, bottom = ListBottomPadding)
    ) {
        item {
            ModeSwitcher(
                selected = HomeMode.CLOCKS,
                scheduleCount = scheduleCount,
                onClockList = onClockList,
                onScheduleList = onScheduleList,
                onPlanner = onPlanner
            )
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Search city or country...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                shape = MaterialTheme.shapes.extraLarge,
                singleLine = true,
                colors = clockwiseSearchFieldColors()
            )
        }

        item {
            HeaderSection(deviceZone, now, timeTravelHours)
        }

        item {
            TimeTravelCard(
                timeTravelHours = timeTravelHours,
                onTimeTravelChange = onTimeTravelChange,
                isProUnlocked = isProUnlocked,
                onUnlockPro = onUnlockPro
            )
        }

        item {
            Text(
                "${filteredZones.size} TIMEZONES",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.xs)
            )
        }

        when {
            error != null -> item {
                StatusCard(
                    title = "Timezones unavailable",
                    body = "Close and reopen Clockwise, then try again."
                )
            }
            isLoading && filteredZones.isEmpty() -> item {
                StatusCard(
                    title = "Loading timezones",
                    body = "Preparing world clocks for your device."
                )
            }
            filteredZones.isEmpty() -> item {
                StatusCard(
                    title = "No matching timezones",
                    body = "Try a city, country, or timezone ID."
                )
            }
            else -> items(
                items = filteredZones,
                key = { it.zoneId }
            ) { zone ->
                WorldClockItem(
                    timeZone = zone,
                    now = now,
                    deviceZone = deviceZone,
                    isExpanded = expandedZone == zone.zoneId,
                    onClick = { onZoneClicked(zone.zoneId) },
                    onSetAlarm = { onSetAlarm(zone) },
                    onCreateMeeting = { onCreateMeeting(zone) },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Composable
internal fun HeaderSection(
    deviceZone: TimeZone,
    now: Instant,
    timeTravelHours: Int = 0
) {
    val deviceTime = now.toLocalDateTime(deviceZone)
    val deviceDate = deviceTime.formatDate()

    Card(
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surface)
                    )
                )
                .padding(vertical = Spacing.xl, horizontal = Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "YOUR TIME",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.md))
            ClockFace(
                hour = deviceTime.hour,
                minute = deviceTime.minute,
                second = if (timeTravelHours == 0) deviceTime.second else null,
                showGlow = true,
                label = deviceTime.formatTime(),
                subtitle = deviceZone.id.substringAfterLast("/").replace("_", " ").uppercase()
            )
            Spacer(Modifier.height(Spacing.lg))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        deviceZone.id.substringAfterLast("/").replace("_", " "),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        deviceDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                LiveBadge(timeTravelHours)
            }
        }
    }
}

@Composable
private fun LiveBadge(timeTravelHours: Int = 0) {
    if (timeTravelHours != 0) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f)
        ) {
            Text(
                if (timeTravelHours > 0) "+${timeTravelHours}H" else "${timeTravelHours}H",
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = Spacing.md, vertical = 6.dp)
            )
        }
        return
    }
    val pulse = rememberInfiniteTransition(label = "livePulse")
    val dotAlpha by pulse.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "liveDotAlpha"
    )
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = 6.dp)
        ) {
            Box(
                Modifier
                    .size(6.dp)
                    .graphicsLayer { alpha = dotAlpha }
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
            Text(
                "LIVE",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun TimeTravelCard(
    timeTravelHours: Int,
    onTimeTravelChange: (Int) -> Unit,
    isProUnlocked: Boolean,
    onUnlockPro: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        "TIME TRAVEL",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!isProUnlocked) {
                    TextButton(onClick = onUnlockPro) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.size(Spacing.xs))
                        Text("Pro", color = MaterialTheme.colorScheme.secondary)
                    }
                } else if (timeTravelHours != 0) {
                    TextButton(onClick = { onTimeTravelChange(0) }) {
                        Text("Reset", color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    Text(
                        "Now",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (isProUnlocked) {
                Slider(
                    value = timeTravelHours.toFloat(),
                    onValueChange = { onTimeTravelChange(it.roundToInt()) },
                    valueRange = -24f..24f,
                    steps = 47,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.tertiary,
                        activeTrackColor = MaterialTheme.colorScheme.tertiary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                        activeTickColor = Color.Transparent,
                        inactiveTickColor = Color.Transparent
                    )
                )
                Text(
                    when {
                        timeTravelHours > 0 -> "Viewing every clock $timeTravelHours hour${if (timeTravelHours == 1) "" else "s"} ahead"
                        timeTravelHours < 0 -> "Viewing every clock ${-timeTravelHours} hour${if (timeTravelHours == -1) "" else "s"} ago"
                        else -> "Drag to see all clocks at another moment"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    "Scrub forward or back to see every clock at another moment — a Clockwise Pro feature.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
internal fun WorldClockItem(
    timeZone: TimeZoneModel,
    now: Instant,
    deviceZone: TimeZone,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onSetAlarm: () -> Unit,
    onCreateMeeting: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cityZone = TimeZone.of(timeZone.zoneId)
    val deviceTime = now.toLocalDateTime(deviceZone)
    val cityTime = now.toLocalDateTime(cityZone)
    val cityDate = cityTime.formatDate()

    val diffHours =
        (cityZone.offsetAt(now).totalSeconds -
            deviceZone.offsetAt(now).totalSeconds) / 3600

    val containerColor by animateColorAsState(
        targetValue = if (isExpanded) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
        label = "clockCardColor"
    )
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "clockChevron"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.85f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = if (isExpanded) {
            BorderStroke(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    )
                )
            )
        } else null
    ) {
        Column(Modifier.padding(Spacing.lg)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (isExpanded)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                else MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.shapes.small
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Default.AccessTime),
                            contentDescription = null,
                            tint = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(Modifier.weight(1f)) {
                        Text(
                            timeZone.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            timeZone.country,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(Modifier.size(Spacing.md))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            cityTime.formatTime(),
                            style = MaterialTheme.typography.titleMedium.tabular(),
                            color = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "UTC${formatUtcOffset(cityZone, now)}",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .background(
                                    if (isExpanded)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.shapes.extraSmall
                                )
                                .padding(horizontal = Spacing.sm, vertical = 2.dp),
                            color = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(chevronRotation)
                    )
                }
            }

            if (isExpanded) {
                Spacer(Modifier.height(Spacing.lg))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)
                                )
                            )
                        )
                )

                Spacer(Modifier.height(Spacing.lg))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Default.Schedule),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                "Your Time",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                deviceTime.formatTime(),
                                style = MaterialTheme.typography.titleSmall.tabular(),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    TimeDiffChip(diffHours)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                timeZone.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                cityTime.formatTime(),
                                style = MaterialTheme.typography.titleSmall.tabular(),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Icon(
                            painter = rememberVectorPainter(Icons.Default.LocationOn),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.sm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        cityDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(Spacing.md))

                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Button(
                        onClick = onSetAlarm,
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(Spacing.sm))
                        Text("Set alarm")
                    }
                    Button(
                        onClick = onCreateMeeting,
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f),
                            contentColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(Spacing.sm))
                        Text("Meeting")
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeDiffChip(diffHours: Int) {
    val colors = ClockwiseTheme.colors
    val positive = diffHours >= 0
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (positive) colors.successContainer else colors.errorContainer,
        modifier = Modifier.padding(horizontal = Spacing.sm)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = 6.dp)
        ) {
            if (diffHours != 0) {
                Icon(
                    painter = rememberVectorPainter(
                        if (positive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
                    ),
                    contentDescription = null,
                    tint = if (positive) colors.success else colors.error,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                when {
                    diffHours > 0 -> "+${diffHours}h ahead"
                    diffHours < 0 -> "${diffHours}h behind"
                    else -> "Same time"
                },
                style = MaterialTheme.typography.labelLarge,
                color = if (positive) colors.success else colors.error
            )
        }
    }
}
