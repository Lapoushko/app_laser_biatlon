package com.lapoushko.feature_experiment.impl.data

import com.lapoushko.database.ExperimentEventEntity
import com.lapoushko.database.ExperimentSessionEntity
import com.lapoushko.feature_experiment.api.domain.ExperimentEvent
import com.lapoushko.feature_experiment.api.domain.ExperimentSession

fun ExperimentSessionEntity.toDomain(): ExperimentSession = ExperimentSession(
    id = id,
    title = title,
    startedAtEpochMillis = startedAtEpochMillis,
    endedAtEpochMillis = endedAtEpochMillis
)

fun ExperimentEventEntity.toDomain(): ExperimentEvent? = when (kind) {
    ExperimentEventEntity.KIND_PING -> ExperimentEvent.Ping(
        timestampEpochMillis = timestampEpochMillis,
        success = success ?: false,
        rttMillis = rttMillis ?: 0L,
        rssiDbm = rssiDbm
    )
    ExperimentEventEntity.KIND_CHECKPOINT -> ExperimentEvent.Checkpoint(
        timestampEpochMillis = timestampEpochMillis,
        label = label.orEmpty(),
        rssiDbm = rssiDbm
    )
    else -> null
}
