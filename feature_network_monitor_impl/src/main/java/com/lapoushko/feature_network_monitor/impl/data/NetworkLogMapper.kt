package com.lapoushko.feature_network_monitor.impl.data

import com.lapoushko.core_network.NetworkTrafficEntry
import com.lapoushko.database.NetworkLogEntity
import com.lapoushko.feature_network_monitor.api.domain.NetworkLogEntry

fun NetworkTrafficEntry.toEntity(): NetworkLogEntity = NetworkLogEntity(
    timestampEpochMillis = timestampEpochMillis,
    method = method,
    url = url,
    requestHeaders = requestHeaders,
    requestBody = requestBody,
    responseCode = responseCode,
    responseHeaders = responseHeaders,
    responseBody = responseBody,
    durationMillis = durationMillis,
    errorMessage = errorMessage
)

fun NetworkLogEntity.toDomain(): NetworkLogEntry = NetworkLogEntry(
    id = id,
    timestampEpochMillis = timestampEpochMillis,
    method = method,
    url = url,
    requestHeaders = requestHeaders,
    requestBody = requestBody,
    responseCode = responseCode,
    responseHeaders = responseHeaders,
    responseBody = responseBody,
    durationMillis = durationMillis,
    errorMessage = errorMessage
)
