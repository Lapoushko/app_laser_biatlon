package com.lapoushko.feature_connection.impl.presentation

import com.lapoushko.feature_connection.api.domain.ConnectionState

data class ConnectionUiState(
    val addressInput: String = "",
    val connectionState: ConnectionState = ConnectionState.Disconnected
)
