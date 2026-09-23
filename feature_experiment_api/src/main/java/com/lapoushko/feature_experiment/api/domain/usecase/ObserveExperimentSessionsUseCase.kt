package com.lapoushko.feature_experiment.api.domain.usecase

import com.lapoushko.feature_experiment.api.domain.ExperimentRepository
import com.lapoushko.feature_experiment.api.domain.ExperimentSession
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveExperimentSessionsUseCase @Inject constructor(
    private val repository: ExperimentRepository
) {
    operator fun invoke(): Flow<List<ExperimentSession>> = repository.observeSessions()
}
