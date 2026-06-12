package com.johnreicabunas.clockwise.data.repository

import com.johnreicabunas.clockwise.data.local.SettingsStorage
import com.johnreicabunas.clockwise.domain.model.AppearanceSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AppearanceRepository(private val storage: SettingsStorage) {

    private val _settings = MutableStateFlow(
        AppearanceSettings(
            paletteId = storage.read(KEY_PALETTE) ?: AppearanceSettings().paletteId,
            clockFaceId = storage.read(KEY_CLOCK_FACE) ?: AppearanceSettings().clockFaceId
        )
    )
    val settings = _settings.asStateFlow()

    fun setPalette(paletteId: String) {
        storage.write(KEY_PALETTE, paletteId)
        _settings.update { it.copy(paletteId = paletteId) }
    }

    fun setClockFace(clockFaceId: String) {
        storage.write(KEY_CLOCK_FACE, clockFaceId)
        _settings.update { it.copy(clockFaceId = clockFaceId) }
    }

    private companion object {
        const val KEY_PALETTE = "palette"
        const val KEY_CLOCK_FACE = "clock_face"
    }
}
