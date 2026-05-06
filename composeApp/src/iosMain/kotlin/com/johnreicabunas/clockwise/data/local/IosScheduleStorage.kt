package com.johnreicabunas.clockwise.data.local

import platform.Foundation.NSUserDefaults

class IosScheduleStorage : ScheduleStorage {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    override suspend fun loadSchedulesJson(): String? {
        return userDefaults.stringForKey(SCHEDULES_KEY)
    }

    override suspend fun saveSchedulesJson(json: String) {
        userDefaults.setObject(json, forKey = SCHEDULES_KEY)
    }

    private companion object {
        const val SCHEDULES_KEY = "clockwise_scheduled_items"
    }
}
