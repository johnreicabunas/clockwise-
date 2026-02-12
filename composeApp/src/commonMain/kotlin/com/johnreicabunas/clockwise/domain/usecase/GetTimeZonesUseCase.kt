package com.johnreicabunas.clockwise.domain.usecase

import com.johnreicabunas.clockwise.common.Response
import com.johnreicabunas.clockwise.domain.model.TimeZoneModel
import com.johnreicabunas.clockwise.domain.repository.TimeZoneRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetTimeZonesUseCase(
    private val repository: TimeZoneRepository
) {
    operator fun invoke(): Flow<Response<List<TimeZoneModel>>> = flow {
        emit(Response.Loading())
        runCatching {
            repository.getTimeZones()
        }.onSuccess {
            emit(Response.Success(it))
        }.onFailure {
            emit(Response.Error(it.message ?: "Unknown error"))
        }
    }
}