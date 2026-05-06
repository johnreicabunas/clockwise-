package com.johnreicabunas.clockwise.domain.model

data class WorldClockState(
    val zones: List<TimeZoneModel> = emptyList(),
    val schedules: List<ScheduledItem> = emptyList(),
    val expandedZone: String? = null,
    val mode: HomeMode = HomeMode.CLOCKS,
    val editor: ScheduleEditorState? = null,
    val editorError: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

enum class HomeMode {
    CLOCKS,
    SCHEDULES,
    EDITOR,
    TIMEZONE_PICKER
}

data class ScheduleEditorState(
    val itemId: String? = null,
    val type: ScheduledItemType = ScheduledItemType.ALARM,
    val title: String = "",
    val targetZoneId: String,
    val targetDate: String,
    val targetTime: String,
    val repeatFrequency: RepeatFrequency = RepeatFrequency.NONE,
    val durationMinutes: Int = 30,
    val reminderOffsetMinutes: Int = 0,
    val dstResolution: DstResolution = DstResolution.NONE,
    val createdAt: String? = null
)
