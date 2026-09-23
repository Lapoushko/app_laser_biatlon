package com.lapoushko.feature_experiment.api.domain

data class ExperimentSession(
    val id: Long,
    val title: String,
    val startedAtEpochMillis: Long,
    val endedAtEpochMillis: Long?
) {
    val isRunning: Boolean get() = endedAtEpochMillis == null
}
