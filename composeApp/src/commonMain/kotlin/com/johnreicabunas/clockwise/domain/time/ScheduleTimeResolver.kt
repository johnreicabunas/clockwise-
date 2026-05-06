package com.johnreicabunas.clockwise.domain.time

import com.johnreicabunas.clockwise.domain.model.DstResolution
import com.johnreicabunas.clockwise.domain.model.RepeatFrequency
import com.johnreicabunas.clockwise.domain.model.ScheduleDay
import com.johnreicabunas.clockwise.domain.model.ScheduledItem
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.IllegalTimeZoneException
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

data class ScheduleResolution(
    val status: ScheduleResolutionStatus,
    val requestedLocalDateTime: LocalDateTime,
    val resolvedLocalDateTime: LocalDateTime?,
    val instant: Instant?,
    val matchingInstants: List<Instant> = emptyList()
)

enum class ScheduleResolutionStatus {
    VALID,
    GAP,
    OVERLAP,
    INVALID_ZONE
}

fun resolveScheduleLocalDateTime(
    requestedLocalDateTime: LocalDateTime,
    zoneId: String,
    dstResolution: DstResolution = DstResolution.NONE
): ScheduleResolution {
    val zone = try {
        TimeZone.of(zoneId)
    } catch (_: IllegalTimeZoneException) {
        return ScheduleResolution(
            status = ScheduleResolutionStatus.INVALID_ZONE,
            requestedLocalDateTime = requestedLocalDateTime,
            resolvedLocalDateTime = null,
            instant = null
        )
    }

    val defaultInstant = requestedLocalDateTime.toInstant(zone)
    val roundTrip = defaultInstant.toLocalDateTime(zone)

    if (roundTrip != requestedLocalDateTime) {
        val shifted = findFirstValidLocalDateTimeAfter(defaultInstant, requestedLocalDateTime, zone)
        return ScheduleResolution(
            status = ScheduleResolutionStatus.GAP,
            requestedLocalDateTime = requestedLocalDateTime,
            resolvedLocalDateTime = shifted?.first,
            instant = shifted?.second
        )
    }

    val matchingInstants = findMatchingInstants(requestedLocalDateTime, defaultInstant, zone)
    if (matchingInstants.size > 1) {
        val selectedInstant = when (dstResolution) {
            DstResolution.OVERLAP_SECOND -> matchingInstants.last()
            else -> matchingInstants.first()
        }
        return ScheduleResolution(
            status = ScheduleResolutionStatus.OVERLAP,
            requestedLocalDateTime = requestedLocalDateTime,
            resolvedLocalDateTime = requestedLocalDateTime,
            instant = selectedInstant,
            matchingInstants = matchingInstants
        )
    }

    return ScheduleResolution(
        status = ScheduleResolutionStatus.VALID,
        requestedLocalDateTime = requestedLocalDateTime,
        resolvedLocalDateTime = requestedLocalDateTime,
        instant = defaultInstant,
        matchingInstants = listOf(defaultInstant)
    )
}

fun nextOccurrenceAfter(
    item: ScheduledItem,
    afterInstant: Instant = Clock.System.now()
): Instant? {
    val zone = try {
        TimeZone.of(item.targetZoneId)
    } catch (_: IllegalTimeZoneException) {
        return null
    }
    val targetLocalDateTime = runCatching {
        LocalDateTime.parse(item.targetLocalDateTime)
    }.getOrNull() ?: return null

    val frequency = item.repeatRule.frequency
    if (frequency == RepeatFrequency.NONE) {
        return runCatching { Instant.parse(item.resolvedInstant) }
            .getOrNull()
            ?.takeIf { it > afterInstant }
    }

    val afterDate = afterInstant.toLocalDateTime(zone).date
    val firstDate = maxOfDate(afterDate, targetLocalDateTime.date)
    val targetTime = targetLocalDateTime.time

    for (dayOffset in 0..370) {
        val date = firstDate.plus(DatePeriod(days = dayOffset))
        if (!isRepeatAllowedOn(date, frequency, item.repeatRule.daysOfWeek, targetLocalDateTime.date)) {
            continue
        }

        val candidateLocal = LocalDateTime(date, targetTime)
        val resolution = resolveScheduleLocalDateTime(candidateLocal, item.targetZoneId, item.dstResolution)
        val candidateInstant = resolution.instant ?: continue
        if (candidateInstant > afterInstant) {
            return candidateInstant
        }
    }

    return null
}

fun ScheduledItem.notificationBody(deviceZone: TimeZone = TimeZone.currentSystemDefault()): String {
    val targetZone = runCatching { TimeZone.of(targetZoneId) }.getOrNull()
    val targetLocal = runCatching { LocalDateTime.parse(targetLocalDateTime) }.getOrNull()
    val instant = runCatching { Instant.parse(resolvedInstant) }.getOrNull()
    val targetName = targetZoneId.substringAfterLast("/").replace("_", " ")

    val targetLine = if (targetLocal != null) {
        "${targetLocal.formatClockTime()} $targetName"
    } else {
        targetName
    }

    val localLine = if (instant != null) {
        val local = instant.toLocalDateTime(deviceZone)
        "Your time: ${local.formatClockTime()} ${deviceZone.id.substringAfterLast("/").replace("_", " ")}"
    } else if (targetZone != null && targetLocal != null) {
        val local = targetLocal.toInstant(targetZone).toLocalDateTime(deviceZone)
        "Your time: ${local.formatClockTime()} ${deviceZone.id.substringAfterLast("/").replace("_", " ")}"
    } else {
        "Your time: unavailable"
    }

    return "$targetLine\n$localLine"
}

fun LocalDateTime.formatClockTime(): String {
    val hour12 = ((hour + 11) % 12) + 1
    val amPm = if (hour < 12) "AM" else "PM"
    return "${hour12.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} $amPm"
}

fun relativeDayLabel(target: LocalDate, reference: LocalDate): String {
    val targetStart = target.atStartOfDayIn(TimeZone.UTC)
    val referenceStart = reference.atStartOfDayIn(TimeZone.UTC)
    val difference = ((targetStart - referenceStart).inWholeDays).toInt()
    return when (difference) {
        -1 -> "Yesterday"
        0 -> "Today"
        1 -> "Tomorrow"
        else -> if (difference > 0) "+$difference days" else "$difference days"
    }
}

private fun findMatchingInstants(
    requestedLocalDateTime: LocalDateTime,
    defaultInstant: Instant,
    zone: TimeZone
): List<Instant> {
    val matches = mutableListOf<Instant>()
    var probe = defaultInstant - 180.minutes
    val end = defaultInstant + 180.minutes

    while (probe <= end) {
        if (probe.toLocalDateTime(zone) == requestedLocalDateTime) {
            if (matches.none { it == probe }) {
                matches += probe
            }
        }
        probe += 1.minutes
    }

    return matches
}

private fun findFirstValidLocalDateTimeAfter(
    defaultInstant: Instant,
    requestedLocalDateTime: LocalDateTime,
    zone: TimeZone
): Pair<LocalDateTime, Instant>? {
    var probe = defaultInstant - 30.minutes
    val end = defaultInstant + 360.minutes

    while (probe <= end) {
        val local = probe.toLocalDateTime(zone)
        if (local >= requestedLocalDateTime) {
            return local to probe
        }
        probe += 1.minutes
    }

    return null
}

private fun isRepeatAllowedOn(
    date: LocalDate,
    frequency: RepeatFrequency,
    daysOfWeek: List<ScheduleDay>,
    originalDate: LocalDate
): Boolean {
    return when (frequency) {
        RepeatFrequency.NONE -> date == originalDate
        RepeatFrequency.DAILY -> true
        RepeatFrequency.WEEKDAYS -> date.dayOfWeek !in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        RepeatFrequency.WEEKLY -> {
            val allowedDays = daysOfWeek.ifEmpty { listOf(originalDate.dayOfWeek.toScheduleDay()) }
            date.dayOfWeek.toScheduleDay() in allowedDays
        }
    }
}

private fun DayOfWeek.toScheduleDay(): ScheduleDay {
    return when (this) {
        DayOfWeek.MONDAY -> ScheduleDay.MONDAY
        DayOfWeek.TUESDAY -> ScheduleDay.TUESDAY
        DayOfWeek.WEDNESDAY -> ScheduleDay.WEDNESDAY
        DayOfWeek.THURSDAY -> ScheduleDay.THURSDAY
        DayOfWeek.FRIDAY -> ScheduleDay.FRIDAY
        DayOfWeek.SATURDAY -> ScheduleDay.SATURDAY
        DayOfWeek.SUNDAY -> ScheduleDay.SUNDAY
    }
}

private fun maxOfDate(first: LocalDate, second: LocalDate): LocalDate {
    return if (first >= second) first else second
}
