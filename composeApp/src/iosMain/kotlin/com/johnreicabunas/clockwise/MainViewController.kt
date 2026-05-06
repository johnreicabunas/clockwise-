package com.johnreicabunas.clockwise

import androidx.compose.ui.window.ComposeUIViewController
import com.johnreicabunas.clockwise.di.appModule
import com.johnreicabunas.clockwise.di.platformModule
import org.koin.core.context.startKoin

private var isKoinStarted = false

fun MainViewController() = ComposeUIViewController {
    if (!isKoinStarted) {
        startKoin {
            modules(appModule, platformModule())
        }
        isKoinStarted = true
    }
    App()
}
