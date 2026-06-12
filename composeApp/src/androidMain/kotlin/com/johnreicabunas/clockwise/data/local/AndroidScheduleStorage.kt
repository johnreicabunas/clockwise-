package com.johnreicabunas.clockwise.data.local

import android.content.Context
import com.johnreicabunas.clockwise.platform.ClockwiseWidgetProvider

class AndroidScheduleStorage(context: Context) : ScheduleStorage {
    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override suspend fun loadSchedulesJson(): String? {
        return preferences.getString(SCHEDULES_KEY, null)
    }

    override suspend fun saveSchedulesJson(json: String) {
        preferences.edit().putString(SCHEDULES_KEY, json).apply()
        ClockwiseWidgetProvider.refresh(appContext)
    }

    private companion object {
        const val PREFERENCES_NAME = "clockwise_schedules"
        const val SCHEDULES_KEY = "scheduled_items"
    }
}
