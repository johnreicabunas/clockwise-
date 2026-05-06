package com.johnreicabunas.clockwise

import com.johnreicabunas.clockwise.domain.model.DstResolution
import com.johnreicabunas.clockwise.domain.model.RepeatFrequency
import com.johnreicabunas.clockwise.domain.model.RepeatRule
import com.johnreicabunas.clockwise.domain.model.ScheduledItem
import com.johnreicabunas.clockwise.domain.model.ScheduledItemType
import com.johnreicabunas.clockwise.domain.time.ScheduleResolutionStatus
import com.johnreicabunas.clockwise.domain.time.nextOccurrenceAfter
import com.johnreicabunas.clockwise.domain.time.resolveScheduleLocalDateTime
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class ScheduleTimeResolverTest {

    @Test
    fun resolvesNewYorkTimeToManilaEquivalentInstant() {
        val resolution = resolveScheduleLocalDateTime(
            requestedLocalDateTime = LocalDateTime.parse("2026-06-01T09:00:00"),
            zoneId = "America/New_York"
        )

        assertEquals(ScheduleResolutionStatus.VALID, resolution.status)
        assertEquals("2026-06-01T13:00:00Z", resolution.instant.toString())
    }

    @Test
    fun detectsSpringForwardGap() {
        val resolution = resolveScheduleLocalDateTime(
            requestedLocalDateTime = LocalDateTime.parse("2026-03-08T02:30:00"),
            zoneId = "America/New_York"
        )

        assertEquals(ScheduleResolutionStatus.GAP, resolution.status)
        assertNotNull(resolution.resolvedLocalDateTime)
        assertTrue(resolution.resolvedLocalDateTime.toString() >= "2026-03-08T03:00")
    }

    @Test
    fun detectsFallBackOverlap() {
        val resolution = resolveScheduleLocalDateTime(
            requestedLocalDateTime = LocalDateTime.parse("2026-11-01T01:30:00"),
            zoneId = "America/New_York"
        )

        assertEquals(ScheduleResolutionStatus.OVERLAP, resolution.status)
        assertEquals(2, resolution.matchingInstants.size)
    }

    @Test
    fun weeklyRepeatingAlarmUsesNextTargetZoneWallClockDate() {
        val item = ScheduledItem(
            id = "test",
            type = ScheduledItemType.ALARM,
            title = "Weekly standup",
            targetZoneId = "America/New_York",
            createdInZoneId = "Asia/Manila",
            targetLocalDateTime = "2026-06-01T09:00:00",
            resolvedInstant = "2026-06-01T13:00:00Z",
            dstResolution = DstResolution.NONE,
            repeatRule = RepeatRule(frequency = RepeatFrequency.WEEKLY),
            createdAt = "2026-05-07T00:00:00Z",
            updatedAt = "2026-05-07T00:00:00Z"
        )

        val next = nextOccurrenceAfter(item, Instant.parse("2026-06-02T00:00:00Z"))

        assertEquals("2026-06-08T13:00:00Z", next.toString())
    }
}
