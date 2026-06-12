package com.johnreicabunas.clockwise.data.local

import platform.Foundation.NSUserDefaults

class IosSettingsStorage : SettingsStorage {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    override fun read(key: String): String? = userDefaults.stringForKey("clockwise_settings_$key")

    override fun write(key: String, value: String) {
        userDefaults.setObject(value, forKey = "clockwise_settings_$key")
    }
}
