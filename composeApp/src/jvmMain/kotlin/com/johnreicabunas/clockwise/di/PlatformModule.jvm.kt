package com.johnreicabunas.clockwise.di

import com.johnreicabunas.clockwise.data.local.JvmScheduleStorage
import com.johnreicabunas.clockwise.data.local.ScheduleStorage
import com.johnreicabunas.clockwise.platform.AlertScheduler
import com.johnreicabunas.clockwise.platform.NoOpAlertScheduler
import org.koin.dsl.module

actual fun platformModule() = module {
    single<ScheduleStorage> { JvmScheduleStorage() }
    single<AlertScheduler> { NoOpAlertScheduler() }
}
