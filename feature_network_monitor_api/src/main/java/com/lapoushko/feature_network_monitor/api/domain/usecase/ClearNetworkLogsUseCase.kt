package com.lapoushko.feature_network_monitor.api.domain.usecase

import com.lapoushko.feature_network_monitor.api.domain.NetworkLogRepository
import javax.inject.Inject

class ClearNetworkLogsUseCase @Inject constructor(
    private val repository: NetworkLogRepository
) {
    suspend operator fun invoke() = repository.clear()
}
