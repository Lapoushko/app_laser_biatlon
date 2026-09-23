package com.lapoushko.feature_network_monitor.api.domain

data class NetworkLogEntry(
    val id: Long,
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
) {
    val isError: Boolean
        get() = errorMessage != null || (responseCode != null && responseCode >= 400)
}
