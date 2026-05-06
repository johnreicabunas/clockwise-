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
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
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
import kotlinx.coroutines.delay
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
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
        containerColor = Color(0xFFF5F6FA),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "World Clock",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            if (state.mode == HomeMode.SCHEDULES) {
                                "Timezone-aligned alarms and reminders"
                            } else {
                                "Compare timezones worldwide"
                            },
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.startCreate(ScheduledItemType.ALARM) }) {
                        Icon(Icons.Default.Add, contentDescription = "Create alarm or meeting reminder")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF5F6FA)
                )
            )
        },
        bottomBar = {
            AdBanner()
        },
        floatingActionButton = {
            if (state.mode == HomeMode.CLOCKS || state.mode == HomeMode.SCHEDULES) {
                FloatingActionButton(
                    onClick = { viewModel.startCreate(ScheduledItemType.ALARM) },
                    containerColor = Color(0xFF5B5FC7),
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
    ) {
        ModeSwitcher(
            selected = HomeMode.CLOCKS,
            scheduleCount = scheduleCount,
            onClockList = onClockList,
            onScheduleList = onScheduleList
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            placeholder = {
                Text("Search city or country...", color = Color.Gray)
            },
            leadingIcon = {
                Icon(Icons.Default.Search, null, tint = Color.Gray)
            },
            shape = RoundedCornerShape(28.dp),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFEDEFF5),
                unfocusedContainerColor = Color(0xFFEDEFF5),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color(0xFF5B5FC7)
            )
        )

        HeaderSection(deviceZone, now)

        Spacer(Modifier.height(16.dp))

        Text(
            "${filteredZones.size} TIMEZONES",
            fontSize = 11.sp,
            color = Color.Gray
        )

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            items(filteredZones) { zone ->
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
            Button(onClick = onCreateAlarm) {
                Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("Alarm")
            }
            OutlinedButton(onClick = onCreateMeeting) {
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
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("No alarms or meeting reminders yet.", fontWeight = FontWeight.Bold)
                    Text(
                        "Create one from a timezone card or the buttons above.",
                        color = Color.Gray,
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
                        fontSize = 22.sp
                    )
                    Text("Timezone-aligned alert", color = Color.Gray, fontSize = 13.sp)
                }
                TextButton(onClick = onClose) {
                    Text("Cancel")
                }
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
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    Column {
                        Text("Target timezone", color = Color.Gray, fontSize = 12.sp)
                        Text(zoneName, fontWeight = FontWeight.Bold)
                        Text(editor.targetZoneId, color = Color.Gray, fontSize = 12.sp)
                    }
                    Text(
                        targetZone?.let { "UTC${formatUtcOffset(it, Clock.System.now())}" }.orEmpty(),
                        color = Color(0xFF5B5FC7),
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
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = editor.targetTime,
                    onValueChange = onTimeChange,
                    label = { Text("Target time") },
                    placeholder = { Text("HH:MM") },
                    singleLine = true,
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
                modifier = Modifier.fillMaxWidth()
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
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Choose timezone", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Text("Search by city, country, or zone ID", color = Color.Gray, fontSize = 13.sp)
            }
            TextButton(onClick = onBack) {
                Text("Back")
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
            shape = RoundedCornerShape(24.dp)
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
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(zone.name, fontWeight = FontWeight.Bold)
                            Text(zone.country, color = Color.Gray, fontSize = 12.sp)
                            Text(zone.zoneId, color = Color.Gray, fontSize = 11.sp)
                        }
                        Text(
                            tz?.let { "UTC${formatUtcOffset(it, now)}" }.orEmpty(),
                            color = Color(0xFF5B5FC7),
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
        Button(onClick = onClick) {
            Text(label, maxLines = 1)
        }
    } else {
        OutlinedButton(onClick = onClick) {
            Text(label, maxLines = 1)
        }
    }
}

@Composable
private fun OptionSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, fontWeight = FontWeight.Bold)
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
fun HeaderSection(
    deviceZone: TimeZone,
    now: Instant
) {
    val deviceTime = now.toLocalDateTime(deviceZone)
    val deviceDate = deviceTime.formatDate()

    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF1E2A47),
                            Color(0xFF3E3C8F)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Default.AccessTime),
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            "YOUR TIMEZONE",
                            fontSize = 11.sp,
                            color = Color.White.copy(.7f)
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        deviceZone.id.substringAfterLast("/").replace("_", " "),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        deviceDate,
                        fontSize = 13.sp,
                        color = Color.White.copy(.85f)
                    )
                }

                Text(
                    deviceTime.formatTime(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
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
            defaultElevation = if (isExpanded) 8.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded)
                Color(0xFFF8F9FF) else Color.White
        ),
        border = if (isExpanded) {
            BorderStroke(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF5B5FC7).copy(alpha = 0.3f),
                        Color(0xFF8A8ED9).copy(alpha = 0.3f)
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (isExpanded)
                                    Color(0xFF5B5FC7).copy(alpha = 0.12f)
                                else Color(0xFFEDEFF5),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Default.AccessTime),
                            contentDescription = null,
                            tint = if (isExpanded) Color(0xFF5B5FC7) else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            timeZone.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (isExpanded) Color(0xFF1E2A47) else Color.Black
                        )
                        Text(
                            timeZone.country,
                            fontSize = 12.sp,
                            color = if (isExpanded) Color(0xFF5B5FC7) else Color.Gray
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            cityTime.formatTime(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (isExpanded) Color(0xFF5B5FC7) else Color.Black
                        )
                        Text(
                            "UTC${formatUtcOffset(cityZone, now)}",
                            fontSize = 11.sp,
                            modifier = Modifier
                                .background(
                                    if (isExpanded)
                                        Color(0xFF5B5FC7).copy(alpha = 0.1f)
                                    else Color(0xFFEDEFF5),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            color = if (isExpanded) Color(0xFF5B5FC7) else Color.Gray
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
                                    Color(0xFF5B5FC7).copy(alpha = 0.2f),
                                    Color(0xFF8A8ED9).copy(alpha = 0.2f)
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
                            tint = Color(0xFF5B5FC7),
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                "Your Time",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                deviceTime.formatTime(),
                                fontWeight = FontWeight.Bold,
                                color = if (isExpanded) Color(0xFF1E2A47) else Color.Black
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
                                color = Color.Gray
                            )
                            Text(
                                cityTime.formatTime(),
                                fontWeight = FontWeight.Bold,
                                color = if (isExpanded) Color(0xFF5B5FC7) else Color.Black
                            )
                        }
                        Icon(
                            painter = rememberVectorPainter(Icons.Default.LocationOn),
                            contentDescription = null,
                            tint = Color(0xFF5B5FC7),
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
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onSetAlarm) {
                        Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text("Set alarm")
                    }
                    OutlinedButton(onClick = onCreateMeeting) {
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text(typeLabel, color = Color(0xFF5B5FC7), fontSize = 12.sp)
                }
                Text(
                    group,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Target time", color = Color.Gray, fontSize = 12.sp)
                    Text(
                        targetLocal?.formatTime().orEmpty(),
                        fontWeight = FontWeight.Bold
                    )
                    Text(zoneLabel, color = Color.Gray, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Your time", color = Color.Gray, fontSize = 12.sp)
                    Text(
                        deviceLocal?.formatTime().orEmpty(),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5B5FC7)
                    )
                    Text(
                        deviceLocal?.formatDate().orEmpty(),
                        color = Color.Gray,
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
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit ${item.title}")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete ${item.title}")
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Conversion preview", fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Target time", color = Color.Gray, fontSize = 12.sp)
                    Text(targetLocal?.formatTime().orEmpty(), fontWeight = FontWeight.Bold)
                    Text("${targetLocal?.formatDate().orEmpty()}, $zoneName", color = Color.Gray, fontSize = 12.sp)
                    Text(
                        if (targetZone != null && targetLocal != null) {
                            "UTC${formatUtcOffset(targetZone, targetLocal.toInstant(targetZone))}"
                        } else {
                            ""
                        },
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Your time", color = Color.Gray, fontSize = 12.sp)
                    Text(deviceLocal?.formatTime().orEmpty(), fontWeight = FontWeight.Bold, color = Color(0xFF5B5FC7))
                    Text(deviceLocal?.formatDate().orEmpty(), color = Color.Gray, fontSize = 12.sp)
                    Text(
                        deviceLocal?.let { relativeDayLabel(it.date, targetLocal?.date ?: it.date) }.orEmpty(),
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                    Text(
                        deviceLocal?.let { "UTC${formatUtcOffset(deviceZone, it.toInstant(deviceZone))}" }.orEmpty(),
                        color = Color.Gray,
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
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return "%+03d:%02d".replace("%+03d", if (hours >= 0) "+$hours" else "$hours")
        .replace("%02d", minutes.toString().padStart(2, '0'))
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
