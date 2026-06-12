package com.johnreicabunas.clockwise.presentation.home

import com.johnreicabunas.clockwise.domain.model.RepeatFrequency
import com.johnreicabunas.clockwise.domain.model.ScheduleEditorState
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import kotlin.math.abs
import kotlin.time.Instant

internal fun parseEditorDateTime(editor: ScheduleEditorState): LocalDateTime? {
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

internal fun LocalDateTime.toEditorTime(): String {
    return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
}

fun LocalDateTime.formatTime(): String = formatTimeDisplay(hour, minute)

internal fun formatTimeDisplay(hour: Int, minute: Int): String {
    val hour12 = ((hour + 11) % 12) + 1
    val amPm = if (hour < 12) "AM" else "PM"

    return "${hour12.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} $amPm"
}

internal fun LocalDate.formatDateShort(): String {
    val monthName = month.name.lowercase()
        .replaceFirstChar { it.uppercase() }
        .take(3)
    return "$monthName $day, $year"
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

internal fun RepeatFrequency.label(): String {
    return when (this) {
        RepeatFrequency.NONE -> "Never"
        RepeatFrequency.DAILY -> "Daily"
        RepeatFrequency.WEEKDAYS -> "Weekdays"
        RepeatFrequency.WEEKLY -> "Weekly"
    }
}

internal fun reminderLabel(minutes: Int): String {
    return if (minutes == 0) "At time" else "$minutes min before"
}
