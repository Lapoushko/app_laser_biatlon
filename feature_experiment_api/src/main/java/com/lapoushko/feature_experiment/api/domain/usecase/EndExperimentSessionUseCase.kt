package com.lapoushko.feature_experiment.api.domain.usecase

import com.lapoushko.feature_experiment.api.domain.ExperimentRepository
import javax.inject.Inject

class EndExperimentSessionUseCase @Inject constructor(
    private val repository: ExperimentRepository
) {
    suspend operator fun invoke(sessionId: Long) = repository.endSession(sessionId)
}
