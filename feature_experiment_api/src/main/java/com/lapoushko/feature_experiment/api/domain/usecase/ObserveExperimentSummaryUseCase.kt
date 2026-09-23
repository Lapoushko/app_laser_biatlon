package com.lapoushko.feature_experiment.api.domain.usecase

import com.lapoushko.feature_experiment.api.domain.ExperimentCorrelation
import com.lapoushko.feature_experiment.api.domain.ExperimentEvent
import com.lapoushko.feature_experiment.api.domain.ExperimentSegment
import com.lapoushko.feature_experiment.api.domain.ExperimentSummary
import com.lapoushko.feature_experiment.api.domain.fitPathLossModel
import com.lapoushko.feature_experiment.api.domain.pearsonCorrelation
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
    var distanceMeters: Double? = null
    var segmentStart = firstOrNull()?.timestampEpochMillis ?: 0L
    var pings = mutableListOf<ExperimentEvent.Ping>()

    fun flushSegment() {
        if (pings.isNotEmpty()) {
            val rssiValues = pings.mapNotNull { it.rssiDbm }
            val rttValues = pings.map { it.rttMillis }
            segments += ExperimentSegment(
                label = label,
                startedAtEpochMillis = segmentStart,
                distanceMeters = distanceMeters,
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
                distanceMeters = event.distanceMeters
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
        totalSuccess = allPings.count { it.success },
        correlation = segments.toCorrelation(),
        pathLossModel = segments.toPathLossModel()
    )
}

private fun List<ExperimentSegment>.toCorrelation(): ExperimentCorrelation {
    val withRssi = filter { it.averageRssiDbm != null }
    val rssiVsRtt = pearsonCorrelation(
        xs = withRssi.map { it.averageRssiDbm!!.toDouble() },
        ys = withRssi.map { it.averageRttMillis?.toDouble() ?: 0.0 }
    )
    val rssiVsLoss = pearsonCorrelation(
        xs = withRssi.map { it.averageRssiDbm!!.toDouble() },
        ys = withRssi.map { it.lossPercent.toDouble() }
    )
    return ExperimentCorrelation(rssiVsRtt = rssiVsRtt, rssiVsLoss = rssiVsLoss)
}

private fun List<ExperimentSegment>.toPathLossModel() =
    fitPathLossModel(
        points = mapNotNull { segment ->
            val distance = segment.distanceMeters
            val rssi = segment.averageRssiDbm
            if (distance != null && rssi != null) distance to rssi.toDouble() else null
        }
    )
