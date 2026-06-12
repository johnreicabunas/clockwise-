package com.johnreicabunas.clockwise.presentation.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.johnreicabunas.clockwise.domain.model.DstResolution
import com.johnreicabunas.clockwise.domain.model.RepeatFrequency
import com.johnreicabunas.clockwise.domain.model.ScheduleEditorState
import com.johnreicabunas.clockwise.domain.model.ScheduledItemType
import com.johnreicabunas.clockwise.domain.model.TimeZoneModel
import com.johnreicabunas.clockwise.domain.time.ScheduleResolutionStatus
import com.johnreicabunas.clockwise.domain.time.relativeDayLabel
import com.johnreicabunas.clockwise.domain.time.resolveScheduleLocalDateTime
import com.johnreicabunas.clockwise.presentation.theme.ClockwiseTheme
import com.johnreicabunas.clockwise.presentation.theme.ListBottomPadding
import com.johnreicabunas.clockwise.presentation.theme.Spacing
import com.johnreicabunas.clockwise.presentation.theme.tabular
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScheduleEditorContent(
    editor: ScheduleEditorState?,
    zones: List<TimeZoneModel>,
    deviceZone: TimeZone,
    error: String?,
    onClose: () -> Unit,
    onSave: () -> Unit,
    onPickTimezone: () -> Unit,
    onTypeChange: (ScheduledItemType) -> Unit,
    onTitleChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onRepeatChange: (RepeatFrequency) -> Unit,
    onReminderOffsetChange: (Int) -> Unit,
    onDurationChange: (Int) -> Unit,
    onDstResolutionChange: (DstResolution) -> Unit
) {
    if (editor == null) {
        return
    }

    val targetLocalDateTime = parseEditorDateTime(editor)
    val targetZone = runCatching { TimeZone.of(editor.targetZoneId) }.getOrNull()
    val zoneName = zones.firstOrNull { it.zoneId == editor.targetZoneId }?.name
        ?: editor.targetZoneId.substringAfterLast("/").replace("_", " ")
    val resolution = targetLocalDateTime?.let {
        resolveScheduleLocalDateTime(it, editor.targetZoneId, editor.dstResolution)
    }
    val resolvedInstant = resolution?.instant
    val deviceLocal = resolvedInstant?.toLocalDateTime(deviceZone)
    val targetLocal = if (resolution?.status == ScheduleResolutionStatus.GAP &&
        editor.dstResolution == DstResolution.GAP_SHIFT_FORWARD
    ) {
        resolution.resolvedLocalDateTime
    } else {
        targetLocalDateTime
    }

    val parsedDate = runCatching { LocalDate.parse(editor.targetDate.trim()) }.getOrNull()
    val timeParts = editor.targetTime.trim().split(":")
    val parsedHour = timeParts.getOrNull(0)?.toIntOrNull()?.takeIf { it in 0..23 }
    val parsedMinute = timeParts.getOrNull(1)?.toIntOrNull()?.takeIf { it in 0..59 }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = parsedDate
                ?.atStartOfDayIn(TimeZone.UTC)
                ?.toEpochMilliseconds()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onDateChange(
                                Instant.fromEpochMilliseconds(millis)
                                    .toLocalDateTime(TimeZone.UTC)
                                    .date
                                    .toString()
                            )
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = parsedHour ?: 9,
            initialMinute = parsedMinute ?: 0,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Set time", style = MaterialTheme.typography.titleMedium) },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val hour = timePickerState.hour.toString().padStart(2, '0')
                        val minute = timePickerState.minute.toString().padStart(2, '0')
                        onTimeChange("$hour:$minute")
                        showTimePicker = false
                    }
                ) {
                    Text("OK", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
        contentPadding = PaddingValues(top = Spacing.lg, bottom = ListBottomPadding)
    ) {
        item {
            Column {
                Text(
                    if (editor.itemId == null) "Create schedule" else "Edit schedule",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Timezone-aligned alert",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.sm),
                contentAlignment = Alignment.Center
            ) {
                ClockFace(
                    hour = targetLocalDateTime?.hour ?: 0,
                    minute = targetLocalDateTime?.minute ?: 0,
                    label = targetLocalDateTime?.formatTime().orEmpty(),
                    subtitle = zoneName.uppercase(),
                    accent = if (editor.type == ScheduledItemType.ALARM) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                ToggleButton(
                    selected = editor.type == ScheduledItemType.ALARM,
                    label = "Alarm",
                    onClick = { onTypeChange(ScheduledItemType.ALARM) }
                )
                ToggleButton(
                    selected = editor.type == ScheduledItemType.MEETING_REMINDER,
                    label = "Meeting",
                    onClick = { onTypeChange(ScheduledItemType.MEETING_REMINDER) }
                )
            }
        }

        item {
            OutlinedTextField(
                value = editor.title,
                onValueChange = onTitleChange,
                label = { Text("Title") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
                colors = clockwiseTextFieldColors()
            )
        }

        item {
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPickTimezone() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.lg),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Target timezone",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            zoneName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            editor.targetZoneId,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.size(Spacing.md))
                    Text(
                        targetZone?.let { "UTC${formatUtcOffset(it, Clock.System.now())}" }.orEmpty(),
                        style = MaterialTheme.typography.labelLarge.tabular(),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                PickerField(
                    label = "Target date",
                    value = parsedDate?.formatDateShort()
                        ?: editor.targetDate.ifBlank { "Pick a date" },
                    icon = Icons.Default.CalendarMonth,
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f)
                )
                PickerField(
                    label = "Target time",
                    value = if (parsedHour != null && parsedMinute != null) {
                        formatTimeDisplay(parsedHour, parsedMinute)
                    } else {
                        editor.targetTime.ifBlank { "Pick a time" }
                    },
                    icon = Icons.Default.Schedule,
                    onClick = { showTimePicker = true },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            ConversionPreview(
                targetLocal = targetLocal,
                deviceLocal = deviceLocal,
                targetZone = targetZone,
                deviceZone = deviceZone,
                zoneName = zoneName
            )
        }

        if (resolution?.status == ScheduleResolutionStatus.GAP) {
            item {
                DstWarningCard(
                    message = "This time does not exist in ${editor.targetZoneId} because of daylight saving time.",
                    actionLabel = resolution.resolvedLocalDateTime?.let { "Use ${it.toEditorTime()}" },
                    onAction = {
                        resolution.resolvedLocalDateTime?.let {
                            onDateChange(it.date.toString())
                            onTimeChange(it.toEditorTime())
                            onDstResolutionChange(DstResolution.GAP_SHIFT_FORWARD)
                        }
                    }
                )
            }
        }

        if (resolution?.status == ScheduleResolutionStatus.OVERLAP) {
            item {
                DstOverlapCard(
                    selected = editor.dstResolution,
                    onSelect = onDstResolutionChange
                )
            }
        }

        if (editor.type == ScheduledItemType.ALARM) {
            item {
                OptionSection(title = "Repeat") {
                    RepeatFrequency.entries.forEach { frequency ->
                        ToggleButton(
                            selected = editor.repeatFrequency == frequency,
                            label = frequency.label(),
                            onClick = { onRepeatChange(frequency) }
                        )
                    }
                }
            }
        } else {
            item {
                OptionSection(title = "Duration") {
                    listOf(15, 30, 45, 60).forEach { minutes ->
                        ToggleButton(
                            selected = editor.durationMinutes == minutes,
                            label = "${minutes}m",
                            onClick = { onDurationChange(minutes) }
                        )
                    }
                }
            }
        }

        item {
            OptionSection(title = "Reminder") {
                listOf(0, 5, 10, 30, 60).forEach { minutes ->
                    ToggleButton(
                        selected = editor.reminderOffsetMinutes == minutes,
                        label = if (minutes == 0) "At time" else "${minutes}m",
                        onClick = { onReminderOffsetChange(minutes) }
                    )
                }
            }
        }

        if (error != null) {
            item {
                Text(
                    error,
                    color = ClockwiseTheme.colors.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ClockwiseTheme.colors.errorContainer, MaterialTheme.shapes.small)
                        .padding(Spacing.md)
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Button(
                    onClick = onSave,
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Save", style = MaterialTheme.typography.labelLarge)
                }
                TextButton(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun PickerField(
    label: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                value,
                style = MaterialTheme.typography.titleSmall.tabular(),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ConversionPreview(
    targetLocal: LocalDateTime?,
    deviceLocal: LocalDateTime?,
    targetZone: TimeZone?,
    deviceZone: TimeZone,
    zoneName: String
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text("Conversion preview", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Target time", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        targetLocal?.formatTime().orEmpty(),
                        style = MaterialTheme.typography.titleSmall.tabular(),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "${targetLocal?.formatDate().orEmpty()}, $zoneName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        if (targetZone != null && targetLocal != null) {
                            "UTC${formatUtcOffset(targetZone, targetLocal.toInstant(targetZone))}"
                        } else {
                            ""
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                    Text(
                        deviceLocal?.let { relativeDayLabel(it.date, targetLocal?.date ?: it.date) }.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        deviceLocal?.let { "UTC${formatUtcOffset(deviceZone, it.toInstant(deviceZone))}" }.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DstWarningCard(
    message: String,
    actionLabel: String?,
    onAction: () -> Unit
) {
    val colors = ClockwiseTheme.colors
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colors.warningContainer),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Text(message, color = colors.onWarningContainer, style = MaterialTheme.typography.bodyMedium)
            if (actionLabel != null) {
                Button(
                    onClick = onAction,
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.warning.copy(alpha = 0.16f),
                        contentColor = colors.warning
                    )
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
private fun DstOverlapCard(
    selected: DstResolution,
    onSelect: (DstResolution) -> Unit
) {
    val colors = ClockwiseTheme.colors
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colors.infoContainer),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Text(
                "This time happens twice in the selected timezone.",
                color = colors.onInfoContainer,
                style = MaterialTheme.typography.bodyMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                ToggleButton(
                    selected = selected != DstResolution.OVERLAP_SECOND,
                    label = "First occurrence",
                    onClick = { onSelect(DstResolution.OVERLAP_FIRST) }
                )
                ToggleButton(
                    selected = selected == DstResolution.OVERLAP_SECOND,
                    label = "Second occurrence",
                    onClick = { onSelect(DstResolution.OVERLAP_SECOND) }
                )
            }
        }
    }
}
