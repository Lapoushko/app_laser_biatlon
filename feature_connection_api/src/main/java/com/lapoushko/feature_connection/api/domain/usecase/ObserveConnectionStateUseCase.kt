package com.lapoushko.feature_connection.api.domain.usecase

import com.lapoushko.feature_connection.api.domain.ConnectionRepository
import com.lapoushko.feature_connection.api.domain.ConnectionState
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveConnectionStateUseCase @Inject constructor(
    private val repository: ConnectionRepository
) {
    operator fun invoke(): StateFlow<ConnectionState> = repository.connectionState
}
