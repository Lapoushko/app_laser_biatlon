package com.lapoushko.core_network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Единый источник текущего адреса сервера ПАК "Лазерный биатлон".
 * Пишет feature_connection при подключении, читают feature_statistics/feature_targets
 * для построения Retrofit-сервисов.
 */
interface ServerAddressRepository {
    val address: StateFlow<String?>
    fun setAddress(baseUrl: String)
    fun clear()
}

@Singleton
class ServerAddressRepositoryImpl @Inject constructor() : ServerAddressRepository {
    private val _address = MutableStateFlow<String?>(null)
    override val address: StateFlow<String?> = _address

    override fun setAddress(baseUrl: String) {
        _address.value = baseUrl
    }

    override fun clear() {
        _address.value = null
    }
}
