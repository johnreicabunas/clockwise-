package com.johnreicabunas.clockwise

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import com.johnreicabunas.clockwise.data.repository.AppearanceRepository
import com.johnreicabunas.clockwise.presentation.home.Homescreen
import com.johnreicabunas.clockwise.presentation.theme.ClockFaceStyle
import com.johnreicabunas.clockwise.presentation.theme.ClockwiseTheme
import com.johnreicabunas.clockwise.presentation.theme.paletteFor
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    val appearanceRepository = koinInject<AppearanceRepository>()
    val appearance by appearanceRepository.settings.collectAsState()
    ClockwiseTheme(
        palette = paletteFor(appearance.paletteId),
        clockFaceStyle = ClockFaceStyle.fromId(appearance.clockFaceId)
    ) {
        Homescreen()
    }
}
