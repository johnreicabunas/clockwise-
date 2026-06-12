package com.johnreicabunas.clockwise.data.local

import java.util.prefs.Preferences

class JvmSettingsStorage : SettingsStorage {
    private val preferences = Preferences.userRoot().node("clockwise_settings")

    override fun read(key: String): String? = preferences.get(key, null)

    override fun write(key: String, value: String) {
        preferences.put(key, value)
    }
}
