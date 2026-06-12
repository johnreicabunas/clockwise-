package com.johnreicabunas.clockwise.data.local

import android.content.Context

class AndroidSettingsStorage(context: Context) : SettingsStorage {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun read(key: String): String? = preferences.getString(key, null)

    override fun write(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "clockwise_settings"
    }
}
