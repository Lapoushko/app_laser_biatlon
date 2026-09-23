package com.lapoushko.feature_experiment.api.domain.usecase

import com.lapoushko.feature_experiment.api.domain.ExperimentEvent
import com.lapoushko.feature_experiment.api.domain.ExperimentSegment
import com.lapoushko.feature_experiment.api.domain.ExperimentSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val FIRST_SEGMENT_LABEL = "До первой метки"

class ObserveExperimentSummaryUseCase @Inject constructor(
    private val observeEvents: ObserveExperimentEventsUseCase
) {
    operator fun invoke(sessionId: Long): Flow<ExperimentSummary> =
        observeEvents(sessionId).map { it.toSummary() }
}

/** [this] упорядочен по времени (см. ExperimentDao.observeEvents — ORDER BY id ASC). */
private fun List<ExperimentEvent>.toSummary(): ExperimentSummary {
    val segments = mutableListOf<ExperimentSegment>()
    var label = FIRST_SEGMENT_LABEL
    var segmentStart = firstOrNull()?.timestampEpochMillis ?: 0L
    var pings = mutableListOf<ExperimentEvent.Ping>()

    fun flushSegment() {
        if (pings.isNotEmpty()) {
            val rssiValues = pings.mapNotNull { it.rssiDbm }
            val rttValues = pings.map { it.rttMillis }
            segments += ExperimentSegment(
                label = label,
                startedAtEpochMillis = segmentStart,
                pingCount = pings.size,
                successCount = pings.count { it.success },
                averageRttMillis = rttValues.average().toLong(),
                averageRssiDbm = rssiValues.takeIf { it.isNotEmpty() }?.let { it.sum() / it.size },
                minRssiDbm = rssiValues.minOrNull(),
                maxRssiDbm = rssiValues.maxOrNull()
            )
        }
    }

    for (event in this) {
        when (event) {
            is ExperimentEvent.Checkpoint -> {
                flushSegment()
                pings = mutableListOf()
                label = event.label
                segmentStart = event.timestampEpochMillis
            }
            is ExperimentEvent.Ping -> pings.add(event)
        }
    }
    flushSegment()

    val allPings = filterIsInstance<ExperimentEvent.Ping>()
    return ExperimentSummary(
        segments = segments,
        totalPings = allPings.size,
        totalSuccess = allPings.count { it.success }
    )
}
