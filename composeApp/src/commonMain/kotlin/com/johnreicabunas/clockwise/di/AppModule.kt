package com.johnreicabunas.clockwise.di

import com.johnreicabunas.clockwise.data.repository.AppearanceRepository
import com.johnreicabunas.clockwise.data.repository.ScheduledItemRepositoryImpl
import com.johnreicabunas.clockwise.data.repository.TimeZoneRepositoryImpl
import com.johnreicabunas.clockwise.domain.repository.ScheduledItemRepository
import com.johnreicabunas.clockwise.domain.repository.TimeZoneRepository
import com.johnreicabunas.clockwise.domain.usecase.GetTimeZonesUseCase
import com.johnreicabunas.clockwise.presentation.home.HomeScreenViewModel
import com.johnreicabunas.clockwise.presentation.support.SupportScreenViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {

    singleOf(::TimeZoneRepositoryImpl).bind<TimeZoneRepository>()
    singleOf(::AppearanceRepository)
    singleOf(::ScheduledItemRepositoryImpl).bind<ScheduledItemRepository>()

    factory { GetTimeZonesUseCase(get()) }

    viewModelOf(::HomeScreenViewModel)
    viewModelOf(::SupportScreenViewModel)
}
