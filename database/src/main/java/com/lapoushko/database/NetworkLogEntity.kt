package com.lapoushko.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Один захваченный HTTP-обмен с сервером ПАК "Лазерный биатлон".
 * Сериализация деталей запроса/ответа — забота core_network (NetworkTrafficEntry),
 * этот модуль лишь хранит уже готовые строки.
 */
@Entity(tableName = "network_logs")
data class NetworkLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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
