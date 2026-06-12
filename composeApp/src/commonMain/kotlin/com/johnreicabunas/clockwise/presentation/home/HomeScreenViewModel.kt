package com.johnreicabunas.clockwise.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.johnreicabunas.clockwise.common.Response
import com.johnreicabunas.clockwise.data.repository.AppearanceRepository
import com.johnreicabunas.clockwise.domain.model.DstResolution
import com.johnreicabunas.clockwise.domain.model.HomeMode
import com.johnreicabunas.clockwise.domain.model.RepeatFrequency
import com.johnreicabunas.clockwise.domain.model.RepeatRule
import com.johnreicabunas.clockwise.domain.model.ScheduleDay
import com.johnreicabunas.clockwise.domain.model.ScheduleEditorState
import com.johnreicabunas.clockwise.domain.model.ScheduledItem
import com.johnreicabunas.clockwise.domain.model.ScheduledItemType
import com.johnreicabunas.clockwise.domain.model.SyncStatus
import com.johnreicabunas.clockwise.domain.model.WorldClockState
import com.johnreicabunas.clockwise.domain.repository.ScheduledItemRepository
import com.johnreicabunas.clockwise.domain.repository.BillingRepository
import com.johnreicabunas.clockwise.domain.time.ScheduleResolutionStatus
import com.johnreicabunas.clockwise.domain.time.nextOccurrenceAfter
import com.johnreicabunas.clockwise.domain.time.resolveScheduleLocalDateTime
import com.johnreicabunas.clockwise.domain.usecase.GetTimeZonesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

class HomeScreenViewModel(
    private val getTimeZonesUseCase: GetTimeZonesUseCase,
    private val scheduledItemRepository: ScheduledItemRepository,
    private val billingRepository: BillingRepository,
    private val appearanceRepository: AppearanceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WorldClockState())
    val state = _state.asStateFlow()

    val appearance = appearanceRepository.settings

    init {
        loadZones()
        loadSchedules()
        observeSchedules()
        observeBilling()
    }

    private fun observeBilling() {
        billingRepository.state.onEach { billingState ->
            _state.update {
                it.copy(
                    isAdsRemoved = billingState.isAdsRemoved || billingState.isProUnlocked,
                    isProUnlocked = billingState.isProUnlocked
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun loadZones() {
        getTimeZonesUseCase().onEach { response ->
                when (response) {
                    is Response.Loading -> {
                        _state.update {
                            it.copy(isLoading = true)
                        }
                    }
                    is Response.Success -> {
                        _state.update {
                            it.copy(
                                zones = response.data ?: emptyList(),
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                    is Response.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = response.message
                            )
                        }
                    }
                }
            }.launchIn(viewModelScope)
    }

    private fun loadSchedules() {
        viewModelScope.launch {
            scheduledItemRepository.load()
        }
    }

    private fun observeSchedules() {
        scheduledItemRepository.items.onEach { schedules ->
            _state.update { it.copy(schedules = schedules) }
        }.launchIn(viewModelScope)
    }


    fun onZoneClicked(zoneId: String) {
        _state.update {
            it.copy(
                expandedZone = if (it.expandedZone == zoneId) null else zoneId
            )
        }
    }

    fun showClockList() {
        _state.update { it.copy(mode = HomeMode.CLOCKS, editorError = null) }
    }

    fun showScheduleList() {
        _state.update { it.copy(mode = HomeMode.SCHEDULES, editorError = null) }
    }

    fun showSupport() {
        _state.update { it.copy(mode = HomeMode.SUPPORT, editorError = null) }
    }

    fun showPlanner() {
        _state.update { it.copy(mode = HomeMode.PLANNER, editorError = null) }
    }

    fun showPersonalize() {
        _state.update { it.copy(mode = HomeMode.PERSONALIZE, editorError = null) }
    }

    fun setPalette(paletteId: String) {
        appearanceRepository.setPalette(paletteId)
    }

    fun setClockFace(clockFaceId: String) {
        appearanceRepository.setClockFace(clockFaceId)
    }

    /** Opens the meeting editor prefilled with a slot picked in the planner (device-zone local time). */
    fun startCreateMeetingAt(dateTime: LocalDateTime) {
        val deviceZone = TimeZone.currentSystemDefault()
        _state.update {
            it.copy(
                mode = HomeMode.EDITOR,
                editorError = null,
                editor = ScheduleEditorState(
                    type = ScheduledItemType.MEETING_REMINDER,
                    title = "Meeting reminder",
                    targetZoneId = deviceZone.id,
                    targetDate = dateTime.date.toString(),
                    targetTime = dateTime.toFormTime()
                )
            )
        }
    }

    fun startCreate(type: ScheduledItemType, zoneId: String? = null) {
        val now = Clock.System.now()
        val deviceZone = TimeZone.currentSystemDefault()
        val targetZoneId = zoneId ?: deviceZone.id
        val targetZone = runCatching { TimeZone.of(targetZoneId) }.getOrDefault(deviceZone)
        val targetLocal = now.toLocalDateTime(targetZone)

        _state.update {
            it.copy(
                mode = HomeMode.EDITOR,
                editorError = null,
                editor = ScheduleEditorState(
                    type = type,
                    title = if (type == ScheduledItemType.ALARM) "Alarm" else "Meeting reminder",
                    targetZoneId = targetZoneId,
                    targetDate = targetLocal.date.toString(),
                    targetTime = targetLocal.toFormTime()
                )
            )
        }
    }

    fun startEdit(item: ScheduledItem) {
        val targetLocal = runCatching { LocalDateTime.parse(item.targetLocalDateTime) }.getOrNull()
        _state.update {
            it.copy(
                mode = HomeMode.EDITOR,
                editorError = null,
                editor = ScheduleEditorState(
                    itemId = item.id,
                    type = item.type,
                    title = item.title,
                    targetZoneId = item.targetZoneId,
                    targetDate = targetLocal?.date?.toString().orEmpty(),
                    targetTime = targetLocal?.toFormTime().orEmpty(),
                    repeatFrequency = item.repeatRule.frequency,
                    durationMinutes = item.durationMinutes ?: 30,
                    reminderOffsetMinutes = item.reminderOffsetMinutes,
                    dstResolution = item.dstResolution,
                    createdAt = item.createdAt
                )
            )
        }
    }

    fun closeEditor() {
        _state.update {
            it.copy(
                mode = HomeMode.CLOCKS,
                editor = null,
                editorError = null
            )
        }
    }

    fun showTimezonePicker() {
        _state.update { it.copy(mode = HomeMode.TIMEZONE_PICKER, editorError = null) }
    }

    fun selectTimezone(zoneId: String) {
        _state.update { state ->
            state.copy(
                mode = HomeMode.EDITOR,
                editor = state.editor?.copy(targetZoneId = zoneId),
                editorError = null
            )
        }
    }

    fun updateEditorTitle(title: String) {
        updateEditor { it.copy(title = title) }
    }

    fun updateEditorType(type: ScheduledItemType) {
        updateEditor {
            it.copy(
                type = type,
                repeatFrequency = if (type == ScheduledItemType.MEETING_REMINDER) RepeatFrequency.NONE else it.repeatFrequency,
                title = if (it.title.isBlank() || it.title == "Alarm" || it.title == "Meeting reminder") {
                    if (type == ScheduledItemType.ALARM) "Alarm" else "Meeting reminder"
                } else {
                    it.title
                }
            )
        }
    }

    fun updateEditorDate(date: String) {
        updateEditor { it.copy(targetDate = date) }
    }

    fun updateEditorTime(time: String) {
        updateEditor { it.copy(targetTime = time) }
    }

    fun updateRepeatFrequency(frequency: RepeatFrequency) {
        updateEditor { it.copy(repeatFrequency = frequency) }
    }

    fun updateReminderOffset(minutes: Int) {
        updateEditor { it.copy(reminderOffsetMinutes = minutes) }
    }

    fun updateDuration(minutes: Int) {
        updateEditor { it.copy(durationMinutes = minutes) }
    }

    fun updateDstResolution(resolution: DstResolution) {
        updateEditor { it.copy(dstResolution = resolution) }
    }

    fun saveEditor() {
        val editor = _state.value.editor ?: return
        val current = _state.value
        if (editor.itemId == null && !current.isProUnlocked &&
            current.schedules.size >= FREE_SCHEDULE_LIMIT
        ) {
            return setEditorError(
                "The free plan supports $FREE_SCHEDULE_LIMIT schedules. Unlock Clockwise Pro for unlimited alarms and reminders."
            )
        }
        val localDateTime = parseEditorLocalDateTime(editor)
            ?: return setEditorError("Enter the target date as YYYY-MM-DD and time as HH:MM.")

        if (editor.title.isBlank()) {
            return setEditorError("Title is required.")
        }

        val resolution = resolveScheduleLocalDateTime(
            requestedLocalDateTime = localDateTime,
            zoneId = editor.targetZoneId,
            dstResolution = editor.dstResolution
        )

        when (resolution.status) {
            ScheduleResolutionStatus.INVALID_ZONE -> {
                return setEditorError("Choose a valid timezone.")
            }
            ScheduleResolutionStatus.GAP -> {
                if (editor.dstResolution != DstResolution.GAP_SHIFT_FORWARD) {
                    return setEditorError("This time does not exist in ${editor.targetZoneId} because of daylight saving time. Use the suggested valid time or choose another time.")
                }
            }
            ScheduleResolutionStatus.OVERLAP -> {
                if (editor.dstResolution == DstResolution.NONE) {
                    updateEditor { it.copy(dstResolution = DstResolution.OVERLAP_FIRST) }
                }
            }
            ScheduleResolutionStatus.VALID -> Unit
        }

        val resolvedLocalDateTime = if (
            resolution.status == ScheduleResolutionStatus.GAP &&
            editor.dstResolution == DstResolution.GAP_SHIFT_FORWARD
        ) {
            resolution.resolvedLocalDateTime ?: localDateTime
        } else {
            localDateTime
        }

        val finalResolution = resolveScheduleLocalDateTime(
            requestedLocalDateTime = resolvedLocalDateTime,
            zoneId = editor.targetZoneId,
            dstResolution = editor.dstResolution
        )
        val instant = finalResolution.instant ?: return setEditorError("Unable to resolve this schedule time.")
        val now = Clock.System.now()
        val isOneTimeSchedule = editor.type == ScheduledItemType.MEETING_REMINDER ||
            editor.repeatFrequency == RepeatFrequency.NONE
        if (isOneTimeSchedule && instant - editor.reminderOffsetMinutes.minutes <= now) {
            return setEditorError("Choose a future time for this alert.")
        }

        val id = editor.itemId ?: newId()
        val createdAt = editor.createdAt ?: now.toString()
        val repeatRule = RepeatRule(
            frequency = if (editor.type == ScheduledItemType.ALARM) editor.repeatFrequency else RepeatFrequency.NONE,
            daysOfWeek = weeklyDaysFor(resolvedLocalDateTime)
        )
        val baseItem = ScheduledItem(
            id = id,
            type = editor.type,
            title = editor.title.trim(),
            targetZoneId = editor.targetZoneId,
            createdInZoneId = TimeZone.currentSystemDefault().id,
            targetLocalDateTime = resolvedLocalDateTime.toString(),
            resolvedInstant = instant.toString(),
            dstResolution = editor.dstResolution,
            repeatRule = repeatRule,
            durationMinutes = if (editor.type == ScheduledItemType.MEETING_REMINDER) editor.durationMinutes else null,
            reminderOffsetMinutes = editor.reminderOffsetMinutes,
            enabled = true,
            createdAt = createdAt,
            updatedAt = now.toString(),
            syncStatus = if (editor.itemId == null) SyncStatus.PENDING_CREATE else SyncStatus.PENDING_UPDATE
        )
        val item = if (repeatRule.frequency == RepeatFrequency.NONE) {
            baseItem
        } else {
            val next = nextOccurrenceAfter(baseItem, now + editor.reminderOffsetMinutes.minutes) ?: instant
            baseItem.copy(resolvedInstant = next.toString())
        }

        viewModelScope.launch {
            scheduledItemRepository.save(item)
            _state.update {
                it.copy(
                    mode = HomeMode.SCHEDULES,
                    editor = null,
                    editorError = null
                )
            }
        }
    }

    fun deleteSchedule(itemId: String) {
        viewModelScope.launch {
            scheduledItemRepository.delete(itemId)
        }
    }

    private fun updateEditor(block: (ScheduleEditorState) -> ScheduleEditorState) {
        _state.update { state ->
            state.copy(
                editor = state.editor?.let(block),
                editorError = null
            )
        }
    }

    private fun setEditorError(message: String) {
        _state.update { it.copy(editorError = message) }
    }

    private fun parseEditorLocalDateTime(editor: ScheduleEditorState): LocalDateTime? {
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

    private fun LocalDateTime.toFormTime(): String {
        return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
    }

    private fun weeklyDaysFor(localDateTime: LocalDateTime): List<ScheduleDay> {
        return when (localDateTime.dayOfWeek) {
            DayOfWeek.MONDAY -> listOf(ScheduleDay.MONDAY)
            DayOfWeek.TUESDAY -> listOf(ScheduleDay.TUESDAY)
            DayOfWeek.WEDNESDAY -> listOf(ScheduleDay.WEDNESDAY)
            DayOfWeek.THURSDAY -> listOf(ScheduleDay.THURSDAY)
            DayOfWeek.FRIDAY -> listOf(ScheduleDay.FRIDAY)
            DayOfWeek.SATURDAY -> listOf(ScheduleDay.SATURDAY)
            DayOfWeek.SUNDAY -> listOf(ScheduleDay.SUNDAY)
        }
    }

    private fun newId(): String {
        return "${Clock.System.now().toEpochMilliseconds()}-${Random.nextLong().toString(16)}"
    }

    companion object {
        const val FREE_SCHEDULE_LIMIT = 3
    }
}
