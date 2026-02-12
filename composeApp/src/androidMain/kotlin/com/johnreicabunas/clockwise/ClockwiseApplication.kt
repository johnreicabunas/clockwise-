package com.johnreicabunas.clockwise

import android.app.Application
import com.johnreicabunas.clockwise.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ClockwiseApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@ClockwiseApplication)
            modules(appModule)
        }


    }

}