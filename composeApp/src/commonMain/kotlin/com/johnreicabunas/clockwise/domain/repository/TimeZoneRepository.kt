package com.johnreicabunas.clockwise.domain.repository

import com.johnreicabunas.clockwise.domain.model.TimeZoneModel

interface TimeZoneRepository {
    suspend fun getTimeZones(): List<TimeZoneModel>
}