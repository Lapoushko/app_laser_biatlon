package com.lapoushko.feature_connection.api.domain.usecase

import com.lapoushko.feature_connection.api.domain.ConnectionRepository
import javax.inject.Inject

class DisconnectFromServerUseCase @Inject constructor(
    private val repository: ConnectionRepository
) {
    suspend operator fun invoke() = repository.disconnect()
}
