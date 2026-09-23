package com.lapoushko.feature_experiment.api.domain.usecase

import com.lapoushko.feature_experiment.api.domain.ExperimentRepository
import javax.inject.Inject

class RecordCheckpointUseCase @Inject constructor(
    private val repository: ExperimentRepository
) {
    suspend operator fun invoke(sessionId: Long, label: String, distanceMeters: Double? = null) =
        repository.recordCheckpoint(sessionId, label, distanceMeters)
}
