package com.johnreicabunas.clockwise.presentation.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.johnreicabunas.clockwise.domain.model.HomeMode
import com.johnreicabunas.clockwise.domain.model.ScheduledItemType
import com.johnreicabunas.clockwise.presentation.components.AdBanner
import com.johnreicabunas.clockwise.presentation.support.SupportScreenRoot
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

// Banner ads are temporarily disabled; flip to true to bring them back.
private const val ADS_ENABLED = false

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Homescreen(
    viewModel: HomeScreenViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val appearance by viewModel.appearance.collectAsStateWithLifecycle()
    val deviceZone = TimeZone.currentSystemDefault()

    var now by remember { mutableStateOf(Clock.System.now()) }
    var searchQuery by remember { mutableStateOf("") }
    var timeTravelHours by remember { mutableStateOf(0) }
    val displayNow = now + timeTravelHours.hours

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Clockwise",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            when (state.mode) {
                                HomeMode.SCHEDULES -> "Timezone-aligned alerts"
                                HomeMode.SUPPORT -> "Support development and unlock Pro"
                                HomeMode.PLANNER -> "Find the perfect meeting time"
                                HomeMode.PERSONALIZE -> "Make Clockwise yours"
                                else -> "World clocks and meeting reminders"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::showPersonalize) {
                        Icon(
                            Icons.Default.Palette,
                            contentDescription = "Personalize themes and clock faces",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    IconButton(onClick = viewModel::showSupport) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Support Clockwise",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (ADS_ENABLED && !state.isAdsRemoved && state.mode != HomeMode.SUPPORT) {
                AdBanner()
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = state.mode == HomeMode.CLOCKS || state.mode == HomeMode.SCHEDULES,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = { viewModel.startCreate(ScheduledItemType.ALARM) },
                    shape = MaterialTheme.shapes.extraLarge,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create scheduled item")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            AnimatedContent(
                targetState = state.mode,
                transitionSpec = {
                    (fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 20 })
                        .togetherWith(fadeOut(tween(120)))
                },
                label = "homeMode"
            ) { mode ->
                when (mode) {
                    HomeMode.CLOCKS -> ClockListContent(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        filteredZones = filteredZones,
                        isLoading = state.isLoading,
                        error = state.error,
                        expandedZone = state.expandedZone,
                        deviceZone = deviceZone,
                        now = displayNow,
                        scheduleCount = state.schedules.size,
                        timeTravelHours = timeTravelHours,
                        onTimeTravelChange = { timeTravelHours = it },
                        isProUnlocked = state.isProUnlocked,
                        onUnlockPro = viewModel::showSupport,
                        onClockList = viewModel::showClockList,
                        onScheduleList = viewModel::showScheduleList,
                        onPlanner = viewModel::showPlanner,
                        onZoneClicked = viewModel::onZoneClicked,
                        onSetAlarm = { viewModel.startCreate(ScheduledItemType.ALARM, it.zoneId) },
                        onCreateMeeting = { viewModel.startCreate(ScheduledItemType.MEETING_REMINDER, it.zoneId) }
                    )
                    HomeMode.SCHEDULES -> ScheduleListContent(
                        schedules = state.schedules,
                        deviceZone = deviceZone,
                        now = now,
                        onClockList = viewModel::showClockList,
                        onScheduleList = viewModel::showScheduleList,
                        onCreateAlarm = { viewModel.startCreate(ScheduledItemType.ALARM) },
                        onCreateMeeting = { viewModel.startCreate(ScheduledItemType.MEETING_REMINDER) },
                        onPlanner = viewModel::showPlanner,
                        onEdit = viewModel::startEdit,
                        onDelete = { viewModel.deleteSchedule(it.id) }
                    )
                    HomeMode.SUPPORT -> SupportScreenRoot(
                        onBack = viewModel::showClockList
                    )
                    HomeMode.EDITOR -> ScheduleEditorContent(
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
                    HomeMode.PLANNER -> PlannerContent(
                        zones = state.zones,
                        deviceZone = deviceZone,
                        now = now,
                        scheduleCount = state.schedules.size,
                        isProUnlocked = state.isProUnlocked,
                        onClockList = viewModel::showClockList,
                        onScheduleList = viewModel::showScheduleList,
                        onPlanner = viewModel::showPlanner,
                        onUnlockPro = viewModel::showSupport,
                        onPickSlot = viewModel::startCreateMeetingAt
                    )
                    HomeMode.PERSONALIZE -> PersonalizeContent(
                        appearance = appearance,
                        isProUnlocked = state.isProUnlocked,
                        onSelectPalette = viewModel::setPalette,
                        onSelectClockFace = viewModel::setClockFace,
                        onUnlockPro = viewModel::showSupport,
                        onBack = viewModel::showClockList
                    )
                    HomeMode.TIMEZONE_PICKER -> TimeZonePickerContent(
                        zones = state.zones,
                        deviceZone = deviceZone,
                        now = now,
                        onBack = { viewModel.selectTimezone(state.editor?.targetZoneId ?: deviceZone.id) },
                        onSelectZone = viewModel::selectTimezone
                    )
                }
            }
        }
    }
}
