package com.johnreicabunas.clockwise

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.johnreicabunas.clockwise.di.appModule
import com.johnreicabunas.clockwise.di.platformModule
import org.koin.core.context.startKoin

fun main() = application {

    startKoin {
        modules(
            appModule,
            platformModule()
        )
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = "Clockwise",
    ) {
        App()
    }
}
