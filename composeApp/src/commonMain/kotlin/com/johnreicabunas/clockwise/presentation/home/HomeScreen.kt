package com.johnreicabunas.clockwise.presentation.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.johnreicabunas.clockwise.domain.model.DstResolution
import com.johnreicabunas.clockwise.domain.model.HomeMode
import com.johnreicabunas.clockwise.domain.model.RepeatFrequency
import com.johnreicabunas.clockwise.domain.model.ScheduleEditorState
import com.johnreicabunas.clockwise.domain.model.ScheduledItem
import com.johnreicabunas.clockwise.domain.model.ScheduledItemType
import com.johnreicabunas.clockwise.domain.model.TimeZoneModel
import com.johnreicabunas.clockwise.domain.time.ScheduleResolutionStatus
import com.johnreicabunas.clockwise.domain.time.relativeDayLabel
import com.johnreicabunas.clockwise.domain.time.resolveScheduleLocalDateTime
import com.johnreicabunas.clockwise.presentation.components.AdBanner
import com.johnreicabunas.clockwise.presentation.support.SupportScreenRoot
import kotlinx.coroutines.delay
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.abs
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Homescreen(
    viewModel: HomeScreenViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val deviceZone = TimeZone.currentSystemDefault()

    var now by remember { mutableStateOf(Clock.System.now()) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            now = Clock.System.now()
            delay(1000)
        }
    }

    val filteredZones = remember(state.zones, searchQuery) {
        if (searchQuery.isBlank()) {
            state.zones
        } else {
            state.zones.filter {
                it.name.contains(searchQuery, true) ||
                    it.country.contains(searchQuery, true) ||
                    it.zoneId.contains(searchQuery, true)
            }
        }
    }

    Scaffold(
        containerColor = ClockwiseBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Clockwise",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = ClockwiseText
                        )
                        Text(
                            when (state.mode) {
                                HomeMode.SCHEDULES -> "Timezone-aligned alerts"
                                HomeMode.SUPPORT -> "Support development and remove ads"
                                else -> "World clocks and meeting reminders"
                            },
                            fontSize = 12.sp,
                            color = ClockwiseMuted
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::showSupport) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Support Clockwise",
                            tint = ClockwiseViolet
                        )
                    }
                    if (state.mode != HomeMode.SUPPORT) {
                        IconButton(onClick = { viewModel.startCreate(ScheduledItemType.ALARM) }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Create alarm or meeting reminder",
                                tint = ClockwiseCoral
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ClockwiseBackground
                )
            )
        },
        bottomBar = {
            if (!state.isAdsRemoved && state.mode != HomeMode.SUPPORT) {
                AdBanner()
            }
        },
        floatingActionButton = {
            if (state.mode == HomeMode.CLOCKS || state.mode == HomeMode.SCHEDULES) {
                FloatingActionButton(
                    onClick = { viewModel.startCreate(ScheduledItemType.ALARM) },
                    containerColor = ClockwiseCoral,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create scheduled item")
                }
            }
        }
    ) { padding ->
        when (state.mode) {
            HomeMode.CLOCKS -> ClockListContent(
                padding = padding,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                filteredZones = filteredZones,
                isLoading = state.isLoading,
                error = state.error,
                expandedZone = state.expandedZone,
                deviceZone = deviceZone,
                now = now,
                scheduleCount = state.schedules.size,
                onClockList = viewModel::showClockList,
                onScheduleList = viewModel::showScheduleList,
                onZoneClicked = viewModel::onZoneClicked,
                onSetAlarm = { viewModel.startCreate(ScheduledItemType.ALARM, it.zoneId) },
                onCreateMeeting = { viewModel.startCreate(ScheduledItemType.MEETING_REMINDER, it.zoneId) }
            )
            HomeMode.SCHEDULES -> ScheduleListContent(
                padding = padding,
                schedules = state.schedules,
                deviceZone = deviceZone,
                now = now,
                onClockList = viewModel::showClockList,
                onScheduleList = viewModel::showScheduleList,
                onCreateAlarm = { viewModel.startCreate(ScheduledItemType.ALARM) },
                onCreateMeeting = { viewModel.startCreate(ScheduledItemType.MEETING_REMINDER) },
                onEdit = viewModel::startEdit,
                onDelete = { viewModel.deleteSchedule(it.id) }
            )
            HomeMode.SUPPORT -> SupportScreenRoot(
                padding = padding,
                onBack = viewModel::showClockList
            )
            HomeMode.EDITOR -> ScheduleEditorContent(
                padding = padding,
                editor = state.editor,
                zones = state.zones,
                deviceZone = deviceZone,
                error = state.editorError,
                onClose = viewModel::closeEditor,
                onSave = viewModel::saveEditor,
                onPickTimezone = viewModel::showTimezonePicker,
                onTypeChange = viewModel::updateEditorType,
                onTitleChange = viewModel::updateEditorTitle,
                onDateChange = viewModel::updateEditorDate,
                onTimeChange = viewModel::updateEditorTime,
                onRepeatChange = viewModel::updateRepeatFrequency,
                onReminderOffsetChange = viewModel::updateReminderOffset,
                onDurationChange = viewModel::updateDuration,
                onDstResolutionChange = viewModel::updateDstResolution
            )
            HomeMode.TIMEZONE_PICKER -> TimeZonePickerContent(
                padding = padding,
                zones = state.zones,
                deviceZone = deviceZone,
                now = now,
                onBack = { viewModel.selectTimezone(state.editor?.targetZoneId ?: deviceZone.id) },
                onSelectZone = viewModel::selectTimezone
            )
        }
    }
}

@Composable
private fun ClockListContent(
    padding: PaddingValues,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    filteredZones: List<TimeZoneModel>,
    isLoading: Boolean,
    error: String?,
    expandedZone: String?,
    deviceZone: TimeZone,
    now: Instant,
    scheduleCount: Int,
    onClockList: () -> Unit,
    onScheduleList: () -> Unit,
    onZoneClicked: (String) -> Unit,
    onSetAlarm: (TimeZoneModel) -> Unit,
    onCreateMeeting: (TimeZoneModel) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(ClockwiseBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp)
    ) {
        item {
            ModeSwitcher(
                selected = HomeMode.CLOCKS,
                scheduleCount = scheduleCount,
                onClockList = onClockList,
                onScheduleList = onScheduleList
            )
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 8.dp),
                placeholder = {
                    Text("Search city or country...", color = ClockwiseMuted)
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, null, tint = ClockwiseMuted)
                },
                shape = RoundedCornerShape(28.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = ClockwiseText,
                    unfocusedTextColor = ClockwiseText,
                    focusedContainerColor = ClockwiseSurface,
                    unfocusedContainerColor = ClockwiseSurface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = ClockwiseCoral
                )
            )
        }

        item {
            HeaderSection(deviceZone, now)
        }

        item {
            Text(
                "${filteredZones.size} TIMEZONES",
                fontSize = 11.sp,
                color = ClockwiseMuted,
                modifier = Modifier.padding(top = 4.dp)
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
                    onCreateMeeting = { onCreateMeeting(zone) }
                )
            }
        }
    }
}

@Composable
private fun ScheduleListContent(
    padding: PaddingValues,
    schedules: List<ScheduledItem>,
    deviceZone: TimeZone,
    now: Instant,
    onClockList: () -> Unit,
    onScheduleList: () -> Unit,
    onCreateAlarm: () -> Unit,
    onCreateMeeting: () -> Unit,
    onEdit: (ScheduledItem) -> Unit,
    onDelete: (ScheduledItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(ClockwiseBackground)
            .padding(16.dp)
    ) {
        ModeSwitcher(
            selected = HomeMode.SCHEDULES,
            scheduleCount = schedules.size,
            onClockList = onClockList,
            onScheduleList = onScheduleList
        )

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onCreateAlarm,
                colors = ButtonDefaults.buttonColors(containerColor = ClockwiseCoral)
            ) {
                Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("Alarm")
            }
            OutlinedButton(
                onClick = onCreateMeeting,
                border = BorderStroke(1.dp, ClockwiseViolet)
            ) {
                Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("Meeting")
            }
        }

        Spacer(Modifier.height(16.dp))

        if (schedules.isEmpty()) {
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(0.dp),
                colors = CardDefaults.cardColors(containerColor = ClockwiseSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "No alarms or meeting reminders yet.",
                        fontWeight = FontWeight.Bold,
                        color = ClockwiseText
                    )
                    Text(
                        "Create one from a timezone card or the buttons above.",
                        color = ClockwiseMuted,
                        fontSize = 13.sp
                    )
                }
            }
            return
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            items(schedules) { item ->
                ScheduleListItem(
                    item = item,
                    deviceZone = deviceZone,
                    now = now,
                    onEdit = { onEdit(item) },
                    onDelete = { onDelete(item) }
                )
            }
        }
    }
}

@Composable
private fun ScheduleEditorContent(
    padding: PaddingValues,
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(ClockwiseBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        if (editor.itemId == null) "Create schedule" else "Edit schedule",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = ClockwiseText
                    )
                    Text("Timezone-aligned alert", color = ClockwiseMuted, fontSize = 13.sp)
                }
                TextButton(onClick = onClose) {
                    Text("Cancel", color = ClockwiseCoral)
                }
            }
        }

        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                ClockFace(
                    hour = targetLocalDateTime?.hour ?: 0,
                    minute = targetLocalDateTime?.minute ?: 0,
                    label = targetLocalDateTime?.formatTime().orEmpty(),
                    subtitle = zoneName.uppercase(),
                    size = 250.dp,
                    accent = if (editor.type == ScheduledItemType.ALARM) ClockwiseCoral else ClockwiseViolet
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                modifier = Modifier.fillMaxWidth(),
                colors = darkTextFieldColors()
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ClockwiseSurface),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPickTimezone() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Target timezone", color = ClockwiseMuted, fontSize = 12.sp)
                        Text(
                            zoneName,
                            fontWeight = FontWeight.Bold,
                            color = ClockwiseText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            editor.targetZoneId,
                            color = ClockwiseMuted,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.size(12.dp))
                    Text(
                        targetZone?.let { "UTC${formatUtcOffset(it, Clock.System.now())}" }.orEmpty(),
                        color = ClockwiseCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = editor.targetDate,
                    onValueChange = onDateChange,
                    label = { Text("Target date") },
                    placeholder = { Text("YYYY-MM-DD") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = darkTextFieldColors()
                )
                OutlinedTextField(
                    value = editor.targetTime,
                    onValueChange = onTimeChange,
                    label = { Text("Target time") },
                    placeholder = { Text("HH:MM") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = darkTextFieldColors()
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
                    color = Color(0xFFC62828),
                    fontSize = 13.sp,
                    modifier = Modifier
                        .background(Color(0xFFFFEBEE), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                        .fillMaxWidth()
                )
            }
        }

        item {
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ClockwiseCoral)
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
private fun TimeZonePickerContent(
    padding: PaddingValues,
    zones: List<TimeZoneModel>,
    deviceZone: TimeZone,
    now: Instant,
    onBack: () -> Unit,
    onSelectZone: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val deviceZoneId = deviceZone.id
    val currentZone = TimeZoneModel(
        name = deviceZoneId.substringAfterLast("/").replace("_", " "),
        country = "Current timezone",
        zoneId = deviceZoneId
    )
    val allZones = remember(zones, deviceZoneId) {
        listOf(currentZone) + zones.filterNot { it.zoneId == deviceZoneId }
    }
    val filtered = remember(allZones, query) {
        if (query.isBlank()) {
            allZones
        } else {
            allZones.filter {
                it.name.contains(query, true) ||
                    it.country.contains(query, true) ||
                    it.zoneId.contains(query, true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(ClockwiseBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Choose timezone", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = ClockwiseText)
                Text("Search by city, country, or zone ID", color = ClockwiseMuted, fontSize = 13.sp)
            }
            TextButton(onClick = onBack) {
                Text("Back", color = ClockwiseCoral)
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search timezone...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = darkTextFieldColors()
        )

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            items(filtered) { zone ->
                val tz = runCatching { TimeZone.of(zone.zoneId) }.getOrNull()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectZone(zone.zoneId) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ClockwiseSurface),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                zone.name,
                                fontWeight = FontWeight.Bold,
                                color = ClockwiseText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                zone.country,
                                color = ClockwiseMuted,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                zone.zoneId,
                                color = ClockwiseMuted,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            tz?.let { "UTC${formatUtcOffset(it, now)}" }.orEmpty(),
                            color = ClockwiseCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeSwitcher(
    selected: HomeMode,
    scheduleCount: Int,
    onClockList: () -> Unit,
    onScheduleList: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ToggleButton(
            selected = selected == HomeMode.CLOCKS,
            label = "Clocks",
            onClick = onClockList
        )
        ToggleButton(
            selected = selected == HomeMode.SCHEDULES,
            label = "Schedules $scheduleCount",
            onClick = onScheduleList
        )
    }
}

@Composable
private fun ToggleButton(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    if (selected) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = ClockwiseCoral)
        ) {
            Text(label, maxLines = 1)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            border = BorderStroke(1.dp, ClockwiseSurfaceRaised)
        ) {
            Text(label, maxLines = 1, color = ClockwiseMuted)
        }
    }
}

@Composable
private fun OptionSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, fontWeight = FontWeight.Bold, color = ClockwiseText)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            content()
        }
    }
}

@Composable
private fun StatusCard(
    title: String,
    body: String
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ClockwiseSurface),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, fontWeight = FontWeight.Bold, color = ClockwiseText)
            Text(body, color = ClockwiseMuted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun darkTextFieldColors() = TextFieldDefaults.colors(
    focusedTextColor = ClockwiseText,
    unfocusedTextColor = ClockwiseText,
    focusedLabelColor = ClockwiseCoral,
    unfocusedLabelColor = ClockwiseMuted,
    focusedPlaceholderColor = ClockwiseMuted,
    unfocusedPlaceholderColor = ClockwiseMuted,
    focusedLeadingIconColor = ClockwiseMuted,
    unfocusedLeadingIconColor = ClockwiseMuted,
    focusedContainerColor = ClockwiseSurface,
    unfocusedContainerColor = ClockwiseSurface,
    focusedIndicatorColor = ClockwiseCoral,
    unfocusedIndicatorColor = ClockwiseSurfaceRaised,
    cursorColor = ClockwiseCoral
)

@Composable
fun HeaderSection(
    deviceZone: TimeZone,
    now: Instant
) {
    val deviceTime = now.toLocalDateTime(deviceZone)
    val deviceDate = deviceTime.formatDate()

    Card(
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = ClockwiseSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "YOUR TIME",
                fontSize = 11.sp,
                color = ClockwiseMuted
            )
            Spacer(Modifier.height(14.dp))
            ClockFace(
                hour = deviceTime.hour,
                minute = deviceTime.minute,
                label = deviceTime.formatTime(),
                subtitle = deviceZone.id.substringAfterLast("/").replace("_", " ").uppercase(),
                size = 240.dp,
                labelFontSize = 25.sp,
                labelVerticalOffset = (-42).dp
            )
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        deviceZone.id.substringAfterLast("/").replace("_", " "),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ClockwiseText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        deviceDate,
                        fontSize = 13.sp,
                        color = ClockwiseMuted
                    )
                }
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = ClockwiseCoral.copy(alpha = 0.14f)
                ) {
                    Text(
                        "LIVE",
                        color = ClockwiseCoral,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WorldClockItem(
    timeZone: TimeZoneModel,
    now: Instant,
    deviceZone: TimeZone,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onSetAlarm: () -> Unit,
    onCreateMeeting: () -> Unit
) {
    val cityZone = TimeZone.of(timeZone.zoneId)
    val deviceTime = now.toLocalDateTime(deviceZone)
    val cityTime = now.toLocalDateTime(cityZone)
    val cityDate = cityTime.formatDate()

    val diffHours =
        (cityZone.offsetAt(now).totalSeconds -
            deviceZone.offsetAt(now).totalSeconds) / 3600

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded)
                ClockwiseSurfaceRaised else ClockwiseSurface
        ),
        border = if (isExpanded) {
            BorderStroke(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        ClockwiseCoral.copy(alpha = 0.5f),
                        ClockwiseViolet.copy(alpha = 0.5f)
                    )
                )
            )
        } else null
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (isExpanded)
                                    ClockwiseCoral.copy(alpha = 0.14f)
                                else ClockwiseSurfaceRaised,
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Default.AccessTime),
                            contentDescription = null,
                            tint = if (isExpanded) ClockwiseCoral else ClockwiseMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(Modifier.weight(1f)) {
                        Text(
                            timeZone.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ClockwiseText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            timeZone.country,
                            fontSize = 12.sp,
                            color = if (isExpanded) ClockwiseCoral else ClockwiseMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(Modifier.size(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            cityTime.formatTime(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (isExpanded) ClockwiseCoral else ClockwiseText
                        )
                        Text(
                            "UTC${formatUtcOffset(cityZone, now)}",
                            fontSize = 11.sp,
                            modifier = Modifier
                                .background(
                                    if (isExpanded)
                                        ClockwiseCoral.copy(alpha = 0.12f)
                                    else ClockwiseSurfaceRaised,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            color = if (isExpanded) ClockwiseCoral else ClockwiseMuted
                        )
                    }
                }
            }

            if (isExpanded) {
                Spacer(Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    ClockwiseCoral.copy(alpha = 0.35f),
                                    ClockwiseViolet.copy(alpha = 0.35f)
                                )
                            )
                        )
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Default.Schedule),
                            contentDescription = null,
                            tint = ClockwiseCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                "Your Time",
                                fontSize = 12.sp,
                                color = ClockwiseMuted
                            )
                            Text(
                                deviceTime.formatTime(),
                                fontWeight = FontWeight.Bold,
                                color = ClockwiseText
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (diffHours >= 0)
                            Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 6.dp
                            )
                        ) {
                            Icon(
                                painter = rememberVectorPainter(
                                    if (diffHours >= 0)
                                        Icons.Default.ArrowUpward
                                    else Icons.Default.ArrowDownward
                                ),
                                contentDescription = null,
                                tint = if (diffHours >= 0)
                                    Color(0xFF4CAF50)
                                else Color(0xFFF44336),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "${if (diffHours >= 0) "+" else ""}${diffHours}h",
                                fontWeight = FontWeight.Bold,
                                color = if (diffHours >= 0)
                                    Color(0xFF4CAF50)
                                else Color(0xFFF44336)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                timeZone.name,
                                fontSize = 12.sp,
                                color = ClockwiseMuted
                            )
                            Text(
                                cityTime.formatTime(),
                                fontWeight = FontWeight.Bold,
                                color = ClockwiseCoral
                            )
                        }
                        Icon(
                            painter = rememberVectorPainter(Icons.Default.LocationOn),
                            contentDescription = null,
                            tint = ClockwiseCoral,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        cityDate,
                        fontSize = 11.sp,
                        color = ClockwiseMuted,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onSetAlarm,
                        border = BorderStroke(1.dp, ClockwiseCoral)
                    ) {
                        Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text("Set alarm")
                    }
                    OutlinedButton(
                        onClick = onCreateMeeting,
                        border = BorderStroke(1.dp, ClockwiseViolet)
                    ) {
                        Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text("Meeting")
                    }
                }
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
    onDelete: () -> Unit
) {
    val targetZone = runCatching { TimeZone.of(item.targetZoneId) }.getOrNull()
    val targetLocal = runCatching { LocalDateTime.parse(item.targetLocalDateTime) }.getOrNull()
    val resolvedInstant = runCatching { Instant.parse(item.resolvedInstant) }.getOrNull()
    val deviceLocal = resolvedInstant?.toLocalDateTime(deviceZone)
    val group = deviceLocal?.let { relativeDayLabel(it.date, now.toLocalDateTime(deviceZone).date) }.orEmpty()
    val typeLabel = if (item.type == ScheduledItemType.ALARM) "Alarm" else "Meeting reminder"
    val zoneLabel = item.targetZoneId.substringAfterLast("/").replace("_", " ")

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ClockwiseSurface),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = ClockwiseText)
                    Text(
                        typeLabel,
                        color = if (item.type == ScheduledItemType.ALARM) ClockwiseCoral else ClockwiseViolet,
                        fontSize = 12.sp
                    )
                }
                Text(
                    group,
                    color = ClockwiseMuted,
                    fontSize = 12.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Target time", color = ClockwiseMuted, fontSize = 12.sp)
                    Text(
                        targetLocal?.formatTime().orEmpty(),
                        fontWeight = FontWeight.Bold,
                        color = ClockwiseText
                    )
                    Text(zoneLabel, color = ClockwiseMuted, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Your time", color = ClockwiseMuted, fontSize = 12.sp)
                    Text(
                        deviceLocal?.formatTime().orEmpty(),
                        fontWeight = FontWeight.Bold,
                        color = ClockwiseCyan
                    )
                    Text(
                        deviceLocal?.formatDate().orEmpty(),
                        color = ClockwiseMuted,
                        fontSize = 12.sp
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
                    color = ClockwiseMuted,
                    fontSize = 12.sp
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit ${item.title}", tint = ClockwiseMuted)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete ${item.title}", tint = ClockwiseCoral)
                    }
                }
            }

            if (targetZone == null) {
                Text(
                    "Timezone unavailable. Choose a valid timezone before rescheduling.",
                    color = Color(0xFFC62828),
                    fontSize = 12.sp
                )
            }
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
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ClockwiseSurface),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Conversion preview", fontWeight = FontWeight.Bold, color = ClockwiseText)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Target time", color = ClockwiseMuted, fontSize = 12.sp)
                    Text(targetLocal?.formatTime().orEmpty(), fontWeight = FontWeight.Bold, color = ClockwiseText)
                    Text("${targetLocal?.formatDate().orEmpty()}, $zoneName", color = ClockwiseMuted, fontSize = 12.sp)
                    Text(
                        if (targetZone != null && targetLocal != null) {
                            "UTC${formatUtcOffset(targetZone, targetLocal.toInstant(targetZone))}"
                        } else {
                            ""
                        },
                        color = ClockwiseMuted,
                        fontSize = 11.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Your time", color = ClockwiseMuted, fontSize = 12.sp)
                    Text(deviceLocal?.formatTime().orEmpty(), fontWeight = FontWeight.Bold, color = ClockwiseCyan)
                    Text(deviceLocal?.formatDate().orEmpty(), color = ClockwiseMuted, fontSize = 12.sp)
                    Text(
                        deviceLocal?.let { relativeDayLabel(it.date, targetLocal?.date ?: it.date) }.orEmpty(),
                        color = ClockwiseMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        deviceLocal?.let { "UTC${formatUtcOffset(deviceZone, it.toInstant(deviceZone))}" }.orEmpty(),
                        color = ClockwiseMuted,
                        fontSize = 11.sp
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
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(message, color = Color(0xFF8A5A00), fontSize = 13.sp)
            if (actionLabel != null) {
                OutlinedButton(onClick = onAction) {
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
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF3FF)),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "This time happens twice in the selected timezone.",
                color = Color(0xFF1E2A47),
                fontSize = 13.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

private fun parseEditorDateTime(editor: ScheduleEditorState): LocalDateTime? {
    val time = editor.targetTime.trim()
    val normalizedTime = when (time.count { it == ':' }) {
        1 -> "$time:00"
        2 -> time
        else -> return null
    }
    return runCatching {
        LocalDateTime.parse("${editor.targetDate.trim()}T$normalizedTime")
    }.getOrNull()
}

private fun LocalDateTime.toEditorTime(): String {
    return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
}

fun LocalDateTime.formatTime(): String {
    val hour12 = ((hour + 11) % 12) + 1
    val amPm = if (hour < 12) "AM" else "PM"

    return "${hour12.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} $amPm"
}

fun LocalDateTime.formatDate(): String {

    val monthName = when (month) {
        Month.JANUARY -> "January"
        Month.FEBRUARY -> "February"
        Month.MARCH -> "March"
        Month.APRIL -> "April"
        Month.MAY -> "May"
        Month.JUNE -> "June"
        Month.JULY -> "July"
        Month.AUGUST -> "August"
        Month.SEPTEMBER -> "September"
        Month.OCTOBER -> "October"
        Month.NOVEMBER -> "November"
        Month.DECEMBER -> "December"
    }

    val dayName = when (dayOfWeek) {
        DayOfWeek.MONDAY -> "Monday"
        DayOfWeek.TUESDAY -> "Tuesday"
        DayOfWeek.WEDNESDAY -> "Wednesday"
        DayOfWeek.THURSDAY -> "Thursday"
        DayOfWeek.FRIDAY -> "Friday"
        DayOfWeek.SATURDAY -> "Saturday"
        DayOfWeek.SUNDAY -> "Sunday"
    }

    return "$dayName, $monthName $day, $year"
}

fun formatUtcOffset(zone: TimeZone, instant: Instant): String {
    val totalSeconds = zone.offsetAt(instant).totalSeconds
    val sign = if (totalSeconds >= 0) "+" else "-"
    val absoluteSeconds = abs(totalSeconds)
    val hours = absoluteSeconds / 3600
    val minutes = (absoluteSeconds % 3600) / 60
    return "$sign${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}"
}

private fun RepeatFrequency.label(): String {
    return when (this) {
        RepeatFrequency.NONE -> "Never"
        RepeatFrequency.DAILY -> "Daily"
        RepeatFrequency.WEEKDAYS -> "Weekdays"
        RepeatFrequency.WEEKLY -> "Weekly"
    }
}

private fun reminderLabel(minutes: Int): String {
    return if (minutes == 0) "At time" else "$minutes min before"
}
