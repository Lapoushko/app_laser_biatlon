package com.lapoushko.feature_experiment.api.domain.usecase

import com.lapoushko.feature_experiment.api.domain.ExperimentEvent
import com.lapoushko.feature_experiment.api.domain.ExperimentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveExperimentEventsUseCase @Inject constructor(
    private val repository: ExperimentRepository
) {
    operator fun invoke(sessionId: Long): Flow<List<ExperimentEvent>> = repository.observeEvents(sessionId)
}
