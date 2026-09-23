package com.lapoushko.core_network

import javax.inject.Inject
import javax.inject.Singleton

class NotConnectedException : IllegalStateException("Нет подключения к серверу")

/**
 * Строит Retrofit-сервисы по текущему адресу сервера. Пересобирает клиента только
 * при смене адреса, чтобы не плодить сокеты на каждый poll-запрос.
 */
@Singleton
class RetrofitServiceFactory @Inject constructor(
    private val serverAddressRepository: ServerAddressRepository
) {
    private var cachedBaseUrl: String? = null
    private val cache = mutableMapOf<Class<*>, Any>()

    fun <T> create(serviceClass: Class<T>): T {
        val baseUrl = serverAddressRepository.address.value ?: throw NotConnectedException()
        if (baseUrl != cachedBaseUrl) {
            cache.clear()
            cachedBaseUrl = baseUrl
        }
        @Suppress("UNCHECKED_CAST")
        return cache.getOrPut(serviceClass) {
            RetrofitFactory.create(baseUrl, serviceClass) as Any
        } as T
    }
}
