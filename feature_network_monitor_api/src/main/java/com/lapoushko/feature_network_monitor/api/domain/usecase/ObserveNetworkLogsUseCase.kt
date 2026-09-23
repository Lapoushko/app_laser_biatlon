package com.lapoushko.feature_network_monitor.api.domain.usecase

import com.lapoushko.feature_network_monitor.api.domain.NetworkLogEntry
import com.lapoushko.feature_network_monitor.api.domain.NetworkLogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveNetworkLogsUseCase @Inject constructor(
    private val repository: NetworkLogRepository
) {
    operator fun invoke(): Flow<List<NetworkLogEntry>> = repository.observeLogs()
}
