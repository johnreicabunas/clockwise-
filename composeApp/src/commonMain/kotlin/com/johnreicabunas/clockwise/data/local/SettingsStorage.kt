package com.johnreicabunas.clockwise.data.local

/** Tiny synchronous key-value store for app preferences (theme, clock face). */
interface SettingsStorage {
    fun read(key: String): String?
    fun write(key: String, value: String)
}
