package com.lapoushko.feature_targets.api.domain.usecase

import com.lapoushko.feature_targets.api.domain.TargetsRepository
import javax.inject.Inject

class StopMatchUseCase @Inject constructor(
    private val repository: TargetsRepository
) {
    suspend operator fun invoke(remainingSeconds: Int): Result<Unit> =
        repository.stopMatch(remainingSeconds)
}
