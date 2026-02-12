package com.johnreicabunas.clockwise.data.repository

import clockwise.composeapp.generated.resources.Res
import com.johnreicabunas.clockwise.data.model.TimeZoneDto
import com.johnreicabunas.clockwise.domain.model.TimeZoneModel
import com.johnreicabunas.clockwise.domain.repository.TimeZoneRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class TimeZoneRepositoryImpl() : TimeZoneRepository {

    override suspend fun getTimeZones(): List<TimeZoneModel> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonString = Res.readBytes("files/timezones.json").decodeToString()

                val timeZones = Json.decodeFromString<List<TimeZoneDto>>(jsonString)

                timeZones.map {
                    TimeZoneModel(
                        name = it.name,
                        country = it.country,
                        zoneId = it.zoneId
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
}