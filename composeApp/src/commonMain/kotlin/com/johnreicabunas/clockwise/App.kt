package com.johnreicabunas.clockwise

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.johnreicabunas.clockwise.presentation.home.Homescreen

@Composable
@Preview
fun App() {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFFF5A72),
            secondary = Color(0xFF8D7CFF),
            tertiary = Color(0xFF55D6E8),
            background = Color(0xFF121116),
            surface = Color(0xFF1B1A21),
            onPrimary = Color.White,
            onBackground = Color(0xFFF8F7FB),
            onSurface = Color(0xFFF8F7FB)
        )
    ) {
        Homescreen()
    }
}
