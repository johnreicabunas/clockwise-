package com.johnreicabunas.clockwise.di

import com.johnreicabunas.clockwise.data.local.AndroidScheduleStorage
import com.johnreicabunas.clockwise.data.local.ScheduleStorage
import com.johnreicabunas.clockwise.platform.AlertScheduler
import com.johnreicabunas.clockwise.platform.AndroidAlertScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual fun platformModule() = module {
    single<ScheduleStorage> { AndroidScheduleStorage(androidContext()) }
    single<AlertScheduler> { AndroidAlertScheduler(androidContext()) }
}
