package com.lapoushko.feature_experiment.api.domain.usecase

import com.lapoushko.feature_experiment.api.domain.ExperimentRepository
import javax.inject.Inject

class GetCurrentRssiUseCase @Inject constructor(
    private val repository: ExperimentRepository
) {
    operator fun invoke(): Int? = repository.currentRssiDbm()
}
