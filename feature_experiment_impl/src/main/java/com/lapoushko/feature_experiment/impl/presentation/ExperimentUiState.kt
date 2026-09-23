package com.lapoushko.feature_experiment.impl.presentation

import com.lapoushko.feature_experiment.api.domain.ExperimentEvent
import com.lapoushko.feature_experiment.api.domain.ExperimentSession
import com.lapoushko.feature_experiment.api.domain.ExperimentSummary

data class ExperimentUiState(
    val sessions: List<ExperimentSession> = emptyList(),
    val activeSessionId: Long? = null,
    val selectedSessionId: Long? = null,
    val events: List<ExperimentEvent> = emptyList(),
    val summary: ExperimentSummary = ExperimentSummary(emptyList(), 0, 0),
    val titleInput: String = "",
    val checkpointInput: String = "",
    val hasLocationPermission: Boolean = false
) {
    val isRunning: Boolean get() = activeSessionId != null

    val selectedSession: ExperimentSession?
        get() = sessions.firstOrNull { it.id == selectedSessionId }

    val currentRssiDbm: Int?
        get() = events.asReversed().firstNotNullOfOrNull { event ->
            when (event) {
                is ExperimentEvent.Ping -> event.rssiDbm
                is ExperimentEvent.Checkpoint -> event.rssiDbm
            }
        }
}
