package com.johnreicabunas.clockwise

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.johnreicabunas.clockwise.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ClockwiseApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        MobileAds.initialize(this) {}

        startKoin {
            androidContext(this@ClockwiseApplication)
            modules(appModule)
        }
    }
}