package com.lapoushko.feature_connection.api.domain

import kotlinx.coroutines.flow.StateFlow

interface ConnectionRepository {
    val connectionState: StateFlow<ConnectionState>
    val lastKnownAddress: String?

    suspend fun connect(address: String): Result<Unit>
    suspend fun disconnect()
}
