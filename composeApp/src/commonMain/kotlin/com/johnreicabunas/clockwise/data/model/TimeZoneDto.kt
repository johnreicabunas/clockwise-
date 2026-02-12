package com.johnreicabunas.clockwise.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TimeZoneDto(
    val name: String,
    val country: String,
    val zoneId: String
)