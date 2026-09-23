package com.lapoushko.feature_network_monitor.api.domain.usecase

import com.lapoushko.feature_network_monitor.api.domain.ConnectionQuality
import com.lapoushko.feature_network_monitor.api.domain.ConnectionStatus
import com.lapoushko.feature_network_monitor.api.domain.LatencyPoint
import com.lapoushko.feature_network_monitor.api.domain.NetworkLogEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Выше этой средней задержки успешных запросов соединение считается деградировавшим. */
private const val SLOW_LATENCY_THRESHOLD_MS = 1_000L

class ObserveConnectionQualityUseCase @Inject constructor(
    private val observeNetworkLogs: ObserveNetworkLogsUseCase
) {
    operator fun invoke(sampleSize: Int = DEFAULT_SAMPLE_SIZE): Flow<ConnectionQuality> =
        observeNetworkLogs().map { logs -> logs.take(sampleSize).toConnectionQuality() }

    companion object {
        const val DEFAULT_SAMPLE_SIZE = 40
    }
}

/** [logs] приходят от новых к старым (см. NetworkLogDao.observeRecent). */
private fun List<NetworkLogEntry>.toConnectionQuality(): ConnectionQuality {
    if (isEmpty()) {
        return ConnectionQuality(
            status = ConnectionStatus.UNKNOWN,
            averageLatencyMillis = null,
            lossPercent = 0,
            sampleSize = 0,
            latencySeries = emptyList()
        )
    }

    val errorCount = count { it.isError }
    val lossPercent = errorCount * 100 / size
    val successDurations = mapNotNull { entry -> entry.durationMillis.takeIf { !entry.isError } }
    val averageLatencyMillis = if (successDurations.isEmpty()) null else successDurations.average().toLong()

    val status = when {
        lossPercent >= 50 -> ConnectionStatus.LOST
        lossPercent > 0 || (averageLatencyMillis != null && averageLatencyMillis > SLOW_LATENCY_THRESHOLD_MS) ->
            ConnectionStatus.DEGRADED
        else -> ConnectionStatus.GOOD
    }

    return ConnectionQuality(
        status = status,
        averageLatencyMillis = averageLatencyMillis,
        lossPercent = lossPercent,
        sampleSize = size,
        latencySeries = asReversed().map { entry ->
            LatencyPoint(
                timestampEpochMillis = entry.timestampEpochMillis,
                durationMillis = entry.durationMillis,
                isError = entry.isError
            )
        }
    )
}
