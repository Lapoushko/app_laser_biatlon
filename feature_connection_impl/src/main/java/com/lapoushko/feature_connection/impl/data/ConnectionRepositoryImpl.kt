package com.lapoushko.feature_connection.impl.data

import com.lapoushko.core_common.DispatcherProvider
import com.lapoushko.core_network.RetrofitFactory
import com.lapoushko.core_network.ServerAddressRepository
import com.lapoushko.feature_connection.api.domain.ConnectionRepository
import com.lapoushko.feature_connection.api.domain.ConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private fun normalizeAddress(rawAddress: String): String {
    val trimmed = rawAddress.trim()
    return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
        trimmed
    } else {
        "http://$trimmed"
    }
}

@Singleton
class ConnectionRepositoryImpl @Inject constructor(
    private val serverAddressRepository: ServerAddressRepository,
    private val preferences: ConnectionPreferences,
    private val dispatchers: DispatcherProvider
) : ConnectionRepository {

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState

    override val lastKnownAddress: String?
        get() = preferences.lastAddress

    override suspend fun connect(address: String): Result<Unit> = withContext(dispatchers.io) {
        _connectionState.value = ConnectionState.Connecting
        val baseUrl = normalizeAddress(address)
        runCatching {
            val api = RetrofitFactory.create(baseUrl, ConnectionApiService::class.java)
            val response = api.checkStatus()
            check(response.isSuccessful) { "Сервер ответил кодом ${response.code()}" }
        }.onSuccess {
            serverAddressRepository.setAddress(baseUrl)
            preferences.lastAddress = baseUrl
            _connectionState.value = ConnectionState.Connected(baseUrl)
        }.onFailure { error ->
            _connectionState.value = ConnectionState.Error(error.message ?: "Не удалось подключиться")
        }
    }

    override suspend fun disconnect() {
        serverAddressRepository.clear()
        _connectionState.value = ConnectionState.Disconnected
    }
}
