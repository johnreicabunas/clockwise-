package com.johnreicabunas.clockwise.data.local

import java.util.prefs.Preferences

class JvmScheduleStorage : ScheduleStorage {
    private val preferences = Preferences.userRoot().node("clockwise")

    override suspend fun loadSchedulesJson(): String? {
        return preferences.get(SCHEDULES_KEY, null)
    }

    override suspend fun saveSchedulesJson(json: String) {
        preferences.put(SCHEDULES_KEY, json)
    }

    private companion object {
        const val SCHEDULES_KEY = "scheduled_items"
    }
}
