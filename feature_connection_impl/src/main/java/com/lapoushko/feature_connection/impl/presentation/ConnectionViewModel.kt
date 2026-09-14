package com.lapoushko.feature_connection.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lapoushko.feature_connection.api.domain.ConnectionState
import com.lapoushko.feature_connection.api.domain.usecase.ConnectToServerUseCase
import com.lapoushko.feature_connection.api.domain.usecase.DisconnectFromServerUseCase
import com.lapoushko.feature_connection.api.domain.usecase.ObserveConnectionStateUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class ConnectionViewModel @Inject constructor(
    private val connectToServer: ConnectToServerUseCase,
    private val disconnectFromServer: DisconnectFromServerUseCase,
    observeConnectionState: ObserveConnectionStateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ConnectionUiState(connectionState = observeConnectionState().value)
    )
    val uiState: StateFlow<ConnectionUiState> = _uiState

    init {
        viewModelScope.launch {
            observeConnectionState().collect { state ->
                _uiState.update { it.copy(connectionState = state) }
            }
        }
    }

    fun onAddressChange(value: String) {
        _uiState.update { it.copy(addressInput = value) }
    }

    fun connect() {
        val address = _uiState.value.addressInput
        if (address.isBlank()) return
        viewModelScope.launch {
            connectToServer(address)
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            disconnectFromServer()
        }
    }
}
