package com.johnreicabunas.clockwise

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.johnreicabunas.clockwise.presentation.home.Homescreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        Homescreen()
    }
}