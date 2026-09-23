package com.lapoushko.feature_experiment.api.domain.usecase

import com.lapoushko.feature_experiment.api.domain.ExperimentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RunExperimentPingLoopUseCase @Inject constructor(
    private val repository: ExperimentRepository
) {
    operator fun invoke(sessionId: Long): Flow<Unit> = repository.runPingLoop(sessionId)
}
