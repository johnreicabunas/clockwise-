package com.johnreicabunas.clockwise.domain.model

data class WorldClockState(
    val zones: List<TimeZoneModel> = emptyList(),
    val expandedZone: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)