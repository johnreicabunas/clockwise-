package com.johnreicabunas.clockwise.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.johnreicabunas.clockwise.domain.model.HomeMode
import com.johnreicabunas.clockwise.domain.model.TimeZoneModel
import com.johnreicabunas.clockwise.presentation.theme.ClockwiseTheme
import com.johnreicabunas.clockwise.presentation.theme.ListBottomPadding
import com.johnreicabunas.clockwise.presentation.theme.Spacing
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

private const val MAX_EXTRA_ZONES = 4

private enum class HourKind { WORK, WAKE, SLEEP }

private fun kindForHour(hour: Int): HourKind = when (hour) {
    in 9..16 -> HourKind.WORK
    7, 8, in 17..21 -> HourKind.WAKE
    else -> HourKind.SLEEP
}

@Composable
internal fun PlannerContent(
    zones: List<TimeZoneModel>,
    deviceZone: TimeZone,
    now: Instant,
    scheduleCount: Int,
    isProUnlocked: Boolean,
    onClockList: () -> Unit,
    onScheduleList: () -> Unit,
    onPlanner: () -> Unit,
    onUnlockPro: () -> Unit,
    onPickSlot: (LocalDateTime) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val selectedZoneIds = remember { emptyList<String>().toMutableStateList() }

    val searchResults = remember(zones, query, selectedZoneIds.size) {
        if (query.isBlank()) {
            emptyList()
        } else {
            zones.asSequence()
                .filter { it.zoneId != deviceZone.id && it.zoneId !in selectedZoneIds }
                .filter {
                    it.name.contains(query, true) ||
                        it.country.contains(query, true) ||
                        it.zoneId.contains(query, true)
                }
                .take(5)
                .toList()
        }
    }

    val deviceToday = now.toLocalDateTime(deviceZone).date
    val baseInstant = remember(now, deviceZone) {
        // Midnight today in the device zone, as an Instant we can offset per hour.
        val midnight = LocalDateTime(deviceToday, LocalTime(0, 0))
        runCatching { midnight.toInstant(deviceZone) }.getOrDefault(now)
    }
    val currentDeviceHour = now.toLocalDateTime(deviceZone).hour

    val allRows: List<Pair<String, TimeZone>> = remember(selectedZoneIds.size, deviceZone, zones) {
        val device = deviceZone.id.substringAfterLast("/").replace("_", " ") to deviceZone
        val others = selectedZoneIds.mapNotNull { id ->
            runCatching { TimeZone.of(id) }.getOrNull()?.let { tz ->
                (zones.firstOrNull { it.zoneId == id }?.name ?: id.substringAfterLast("/")) to tz
            }
        }
        listOf(device) + others
    }

    // Hours (device time) where every selected zone is inside working hours.
    val bestHours: Set<Int> = remember(allRows, baseInstant) {
        if (allRows.size < 2) {
            emptySet()
        } else {
            (0..23).filter { hour ->
                val instant = baseInstant + hour.hours
                allRows.all { (_, tz) ->
                    kindForHour(instant.toLocalDateTime(tz).hour) == HourKind.WORK
                }
            }.toSet()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
        contentPadding = PaddingValues(top = Spacing.lg, bottom = ListBottomPadding)
    ) {
        item {
            ModeSwitcher(
                selected = HomeMode.PLANNER,
                scheduleCount = scheduleCount,
                onClockList = onClockList,
                onScheduleList = onScheduleList,
                onPlanner = onPlanner
            )
        }

        if (!isProUnlocked) {
            item {
                ProLockedCard(
                    title = "Meeting Time Finder",
                    body = "Compare working hours across up to 5 timezones, spot the perfect overlap, and create a meeting reminder with one tap.",
                    onUnlock = onUnlockPro
                )
            }
            return@LazyColumn
        }

        item {
            Column {
                Text(
                    "Meeting Time Finder",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Today in your time, across every zone. Tap an hour to schedule it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedZoneIds.size < MAX_EXTRA_ZONES,
                placeholder = {
                    Text(
                        if (selectedZoneIds.size < MAX_EXTRA_ZONES) {
                            "Add a timezone..."
                        } else {
                            "Up to $MAX_EXTRA_ZONES extra timezones"
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge,
                colors = clockwiseSearchFieldColors()
            )
        }

        items(searchResults.size) { index ->
            val zone = searchResults[index]
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedZoneIds.add(zone.zoneId)
                        query = ""
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            zone.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            zone.country,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        "Add",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    allRows.forEachIndexed { rowIndex, (name, tz) ->
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (rowIndex == 0) "$name (you)" else name,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                if (rowIndex > 0) {
                                    IconButton(
                                        onClick = { selectedZoneIds.remove(tz.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove $name",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            HourStrip(
                                timeZone = tz,
                                baseInstant = baseInstant,
                                bestHours = bestHours,
                                currentDeviceHour = currentDeviceHour,
                                onPickHour = { hour ->
                                    onPickSlot(LocalDateTime(deviceToday, LocalTime(hour, 0)))
                                }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(0, 6, 12, 18, 23).forEach { hour ->
                            val suffix = if (hour < 12) "AM" else "PM"
                            val h12 = ((hour + 11) % 12) + 1
                            Text(
                                "$h12$suffix",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    PlannerLegend()

                    if (allRows.size >= 2) {
                        Text(
                            if (bestHours.isEmpty()) {
                                "No shared working-hours overlap today. Try fewer zones or aim for waking hours."
                            } else {
                                val ranges = contiguousRanges(bestHours.sorted())
                                "Best overlap (your time): " + ranges.joinToString(", ") { (start, end) ->
                                    "${formatTimeDisplay(start, 0)} – ${formatTimeDisplay(end + 1, 0)}"
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (bestHours.isEmpty()) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                ClockwiseTheme.colors.success
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun contiguousRanges(sortedHours: List<Int>): List<Pair<Int, Int>> {
    if (sortedHours.isEmpty()) return emptyList()
    val ranges = mutableListOf<Pair<Int, Int>>()
    var start = sortedHours.first()
    var prev = start
    for (hour in sortedHours.drop(1)) {
        if (hour != prev + 1) {
            ranges += start to prev
            start = hour
        }
        prev = hour
    }
    ranges += start to prev
    return ranges
}

@Composable
private fun HourStrip(
    timeZone: TimeZone,
    baseInstant: Instant,
    bestHours: Set<Int>,
    currentDeviceHour: Int,
    onPickHour: (Int) -> Unit
) {
    val colors = ClockwiseTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        repeat(24) { hour ->
            val localHour = (baseInstant + hour.hours).toLocalDateTime(timeZone).hour
            val kind = kindForHour(localHour)
            val cellColor = when (kind) {
                HourKind.WORK -> colors.success.copy(alpha = 0.55f)
                HourKind.WAKE -> colors.warning.copy(alpha = 0.30f)
                HourKind.SLEEP -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
            val isBest = hour in bestHours
            val isNow = hour == currentDeviceHour
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(26.dp)
                    .background(cellColor, RoundedCornerShape(3.dp))
                    .then(
                        when {
                            isBest -> Modifier.border(
                                1.dp,
                                colors.success,
                                RoundedCornerShape(3.dp)
                            )
                            isNow -> Modifier.border(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                RoundedCornerShape(3.dp)
                            )
                            else -> Modifier
                        }
                    )
                    .clickable { onPickHour(hour) }
            )
        }
    }
}

@Composable
private fun PlannerLegend() {
    val colors = ClockwiseTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        LegendDot(colors.success.copy(alpha = 0.55f), "Working 9–5")
        LegendDot(colors.warning.copy(alpha = 0.30f), "Awake")
        LegendDot(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), "Sleeping")
    }
}

@Composable
private fun LegendDot(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        Box(
            Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(3.dp))
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
