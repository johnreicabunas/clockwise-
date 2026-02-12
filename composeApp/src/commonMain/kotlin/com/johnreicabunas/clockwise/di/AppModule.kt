package com.johnreicabunas.clockwise.di

import com.johnreicabunas.clockwise.data.repository.TimeZoneRepositoryImpl
import com.johnreicabunas.clockwise.domain.repository.TimeZoneRepository
import com.johnreicabunas.clockwise.domain.usecase.GetTimeZonesUseCase
import com.johnreicabunas.clockwise.presentation.home.HomeScreenViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {

    singleOf(::TimeZoneRepositoryImpl).bind<TimeZoneRepository>()

    factory { GetTimeZonesUseCase(get()) }

    viewModelOf(::HomeScreenViewModel)
}