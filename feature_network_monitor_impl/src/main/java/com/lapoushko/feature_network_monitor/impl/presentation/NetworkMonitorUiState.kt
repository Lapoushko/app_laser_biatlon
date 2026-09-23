package com.lapoushko.feature_network_monitor.impl.presentation

import com.lapoushko.feature_network_monitor.api.domain.ConnectionQuality
import com.lapoushko.feature_network_monitor.api.domain.ConnectionStatus
import com.lapoushko.feature_network_monitor.api.domain.NetworkLogEntry

data class NetworkMonitorUiState(
    val logs: List<NetworkLogEntry> = emptyList(),
    val expandedLogId: Long? = null,
    val connectionQuality: ConnectionQuality = ConnectionQuality(
        status = ConnectionStatus.UNKNOWN,
        averageLatencyMillis = null,
        lossPercent = 0,
        sampleSize = 0,
        latencySeries = emptyList()
    )
)
