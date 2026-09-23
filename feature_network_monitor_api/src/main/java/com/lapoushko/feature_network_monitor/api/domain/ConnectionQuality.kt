package com.lapoushko.feature_network_monitor.api.domain

enum class ConnectionStatus {
    /** Данных ещё нет — соединение не оценивалось. */
    UNKNOWN,
    GOOD,
    DEGRADED,
    LOST
}

/** Точка на графике задержки: один HTTP-обмен из [com.lapoushko.feature_network_monitor.api.domain.NetworkLogEntry]. */
data class LatencyPoint(
    val timestampEpochMillis: Long,
    val durationMillis: Long,
    val isError: Boolean
)

/**
 * Агрегированная оценка качества связи с сервером ПАК по последним запросам —
 * задержка, доля потерь и итоговый статус для быстрой индикации "всё хорошо / есть проблемы".
 */
data class ConnectionQuality(
    val status: ConnectionStatus,
    val averageLatencyMillis: Long?,
    val lossPercent: Int,
    val sampleSize: Int,
    val latencySeries: List<LatencyPoint>
)
