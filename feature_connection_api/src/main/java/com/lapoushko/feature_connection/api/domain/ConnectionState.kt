package com.lapoushko.feature_connection.api.domain

sealed interface ConnectionState {
    data object Disconnected : ConnectionState
    data object Connecting : ConnectionState
    data class Connected(val serverAddress: String) : ConnectionState
    data class Error(val message: String) : ConnectionState
}
