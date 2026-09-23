package com.lapoushko.core_network

/**
 * Один HTTP-обмен с сервером ПАК "Лазерный биатлон", захваченный [NetworkTrafficInterceptor].
 */
data class NetworkTrafficEntry(
    val timestampEpochMillis: Long,
    val method: String,
    val url: String,
    val requestHeaders: String,
    val requestBody: String?,
    val responseCode: Int?,
    val responseHeaders: String?,
    val responseBody: String?,
    val durationMillis: Long,
    val errorMessage: String?
)
