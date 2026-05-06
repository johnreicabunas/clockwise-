package com.johnreicabunas.clockwise.data.local

interface ScheduleStorage {
    suspend fun loadSchedulesJson(): String?
    suspend fun saveSchedulesJson(json: String)
}
