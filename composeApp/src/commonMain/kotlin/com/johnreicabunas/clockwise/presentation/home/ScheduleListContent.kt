package com.johnreicabunas.clockwise.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.johnreicabunas.clockwise.domain.model.HomeMode
import com.johnreicabunas.clockwise.domain.model.ScheduledItem
import com.johnreicabunas.clockwise.domain.model.ScheduledItemType
import com.johnreicabunas.clockwise.domain.time.relativeDayLabel
import com.johnreicabunas.clockwise.presentation.theme.ClockwiseTheme
import com.johnreicabunas.clockwise.presentation.theme.ListBottomPadding
import com.johnreicabunas.clockwise.presentation.theme.Spacing
import com.johnreicabunas.clockwise.presentation.theme.tabular
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

@Composable
internal fun ScheduleListContent(
    schedules: List<ScheduledItem>,
    deviceZone: TimeZone,
    now: Instant,
    onClockList: () -> Unit,
    onScheduleList: () -> Unit,
    onCreateAlarm: () -> Unit,
    onCreateMeeting: () -> Unit,
    onPlanner: () -> Unit,
    onEdit: (ScheduledItem) -> Unit,
    onDelete: (ScheduledItem) -> Unit
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
                selected = HomeMode.SCHEDULES,
                scheduleCount = schedules.size,
                onClockList = onClockList,
                onScheduleList = onScheduleList,
                onPlanner = onPlanner
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Button(
                    onClick = onCreateAlarm,
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(Spacing.sm))
                    Text("Alarm")
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

        if (schedules.isEmpty()) {
            item {
                StatusCard(
                    title = "No alarms or meeting reminders yet.",
                    body = "Create one from a timezone card or the buttons above."
                )
            }
        } else {
            items(schedules, key = { it.id }) { item ->
                ScheduleListItem(
                    item = item,
                    deviceZone = deviceZone,
                    now = now,
                    onEdit = { onEdit(item) },
                    onDelete = { onDelete(item) },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Composable
private fun ScheduleListItem(
    item: ScheduledItem,
    deviceZone: TimeZone,
    now: Instant,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val targetZone = runCatching { TimeZone.of(item.targetZoneId) }.getOrNull()
    val targetLocal = runCatching { LocalDateTime.parse(item.targetLocalDateTime) }.getOrNull()
    val resolvedInstant = runCatching { Instant.parse(item.resolvedInstant) }.getOrNull()
    val deviceLocal = resolvedInstant?.toLocalDateTime(deviceZone)
    val group = deviceLocal?.let { relativeDayLabel(it.date, now.toLocalDateTime(deviceZone).date) }.orEmpty()
    val isAlarm = item.type == ScheduledItemType.ALARM
    val typeLabel = if (isAlarm) "Alarm" else "Meeting reminder"
    val accent = if (isAlarm) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    val zoneLabel = item.targetZoneId.substringAfterLast("/").replace("_", " ")

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(accent.copy(alpha = 0.14f), MaterialTheme.shapes.small),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isAlarm) Icons.Default.Alarm else Icons.Default.Event,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(item.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text(typeLabel, style = MaterialTheme.typography.bodySmall, color = accent)
                    }
                }
                Text(
                    group,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Target time", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        targetLocal?.formatTime().orEmpty(),
                        style = MaterialTheme.typography.titleSmall.tabular(),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(zoneLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Your time", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        deviceLocal?.formatTime().orEmpty(),
                        style = MaterialTheme.typography.titleSmall.tabular(),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        deviceLocal?.formatDate().orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${item.repeatRule.frequency.label()} • ${reminderLabel(item.reminderOffsetMinutes)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit ${item.title}", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete ${item.title}",
                            tint = ClockwiseTheme.colors.error
                        )
                    }
                }
            }

            if (targetZone == null) {
                Text(
                    "Timezone unavailable. Choose a valid timezone before rescheduling.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ClockwiseTheme.colors.error
                )
            }
        }
    }
}
