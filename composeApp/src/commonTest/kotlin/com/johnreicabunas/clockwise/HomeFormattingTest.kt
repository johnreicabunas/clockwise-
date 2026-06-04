package com.johnreicabunas.clockwise

import com.johnreicabunas.clockwise.presentation.home.formatUtcOffset
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class HomeFormattingTest {

    @Test
    fun formatsPositiveAndNegativeHalfHourUtcOffsets() {
        assertEquals(
            "+05:45",
            formatUtcOffset(TimeZone.of("Asia/Kathmandu"), Instant.parse("2026-01-01T00:00:00Z"))
        )
        assertEquals(
            "-03:30",
            formatUtcOffset(TimeZone.of("America/St_Johns"), Instant.parse("2026-01-01T00:00:00Z"))
        )
    }
}
