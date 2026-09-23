package com.lapoushko.feature_network_monitor.api.domain

import kotlinx.coroutines.flow.Flow

interface NetworkLogRepository {
    fun observeLogs(): Flow<List<NetworkLogEntry>>
    suspend fun clear()
}
