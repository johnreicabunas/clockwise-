package com.johnreicabunas.clockwise.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScheduledItem(
    val id: String,
    val type: ScheduledItemType,
    val title: String,
    val targetZoneId: String,
    val createdInZoneId: String,
    val targetLocalDateTime: String,
    val resolvedInstant: String,
    val dstResolution: DstResolution = DstResolution.NONE,
    val repeatRule: RepeatRule = RepeatRule(),
    val durationMinutes: Int? = null,
    val reminderOffsetMinutes: Int = 0,
    val enabled: Boolean = true,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String? = null,
    val syncStatus: SyncStatus = SyncStatus.LOCAL
)

@Serializable
enum class ScheduledItemType {
    @SerialName("alarm")
    ALARM,

    @SerialName("meeting_reminder")
    MEETING_REMINDER
}

@Serializable
enum class DstResolution {
    @SerialName("none")
    NONE,

    @SerialName("gap_shift_forward")
    GAP_SHIFT_FORWARD,

    @SerialName("overlap_first")
    OVERLAP_FIRST,

    @SerialName("overlap_second")
    OVERLAP_SECOND
}

@Serializable
data class RepeatRule(
    val frequency: RepeatFrequency = RepeatFrequency.NONE,
    val daysOfWeek: List<ScheduleDay> = emptyList()
)

@Serializable
enum class RepeatFrequency {
    @SerialName("none")
    NONE,

    @SerialName("daily")
    DAILY,

    @SerialName("weekdays")
    WEEKDAYS,

    @SerialName("weekly")
    WEEKLY
}

@Serializable
enum class ScheduleDay {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY
}

@Serializable
enum class SyncStatus {
    @SerialName("local")
    LOCAL,

    @SerialName("synced")
    SYNCED,

    @SerialName("pending_create")
    PENDING_CREATE,

    @SerialName("pending_update")
    PENDING_UPDATE,

    @SerialName("pending_delete")
    PENDING_DELETE,

    @SerialName("failed")
    FAILED
}
