package com.johnreicabunas.clockwise.data.local

import android.content.Context

class AndroidScheduleStorage(context: Context) : ScheduleStorage {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override suspend fun loadSchedulesJson(): String? {
        return preferences.getString(SCHEDULES_KEY, null)
    }

    override suspend fun saveSchedulesJson(json: String) {
        preferences.edit().putString(SCHEDULES_KEY, json).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "clockwise_schedules"
        const val SCHEDULES_KEY = "scheduled_items"
    }
}
