package com.johnreicabunas.clockwise.di

import com.johnreicabunas.clockwise.data.local.IosScheduleStorage
import com.johnreicabunas.clockwise.data.local.ScheduleStorage
import com.johnreicabunas.clockwise.data.repository.UnavailableBillingRepository
import com.johnreicabunas.clockwise.domain.repository.BillingRepository
import com.johnreicabunas.clockwise.platform.AlertScheduler
import com.johnreicabunas.clockwise.platform.NoOpAlertScheduler
import org.koin.dsl.module

actual fun platformModule() = module {
    single<ScheduleStorage> { IosScheduleStorage() }
    single<AlertScheduler> { NoOpAlertScheduler() }
    single<BillingRepository> { UnavailableBillingRepository() }
}
