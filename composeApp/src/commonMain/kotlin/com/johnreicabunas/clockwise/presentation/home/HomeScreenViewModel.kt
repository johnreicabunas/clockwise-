package com.johnreicabunas.clockwise.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.johnreicabunas.clockwise.common.Response
import com.johnreicabunas.clockwise.domain.model.WorldClockState
import com.johnreicabunas.clockwise.domain.usecase.GetTimeZonesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class HomeScreenViewModel(
    private val getTimeZonesUseCase: GetTimeZonesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(WorldClockState())
    val state = _state.asStateFlow()

    init {
        loadZones()
    }

    private fun loadZones() {
        getTimeZonesUseCase().onEach { response ->
                when (response) {
                    is Response.Loading -> {
                        _state.update {
                            it.copy(isLoading = true)
                        }
                    }
                    is Response.Success -> {
                        _state.update {
                            it.copy(
                                zones = response.data ?: emptyList(),
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                    is Response.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = response.message
                            )
                        }
                    }
                }
            }.launchIn(viewModelScope)
    }


    fun onZoneClicked(zoneId: String) {
        _state.update {
            it.copy(
                expandedZone = if (it.expandedZone == zoneId) null else zoneId
            )
        }
    }
}