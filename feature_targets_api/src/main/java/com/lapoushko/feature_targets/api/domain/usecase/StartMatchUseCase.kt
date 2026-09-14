package com.lapoushko.feature_targets.api.domain.usecase

import com.lapoushko.feature_targets.api.domain.TargetsRepository
import javax.inject.Inject

class StartMatchUseCase @Inject constructor(
    private val repository: TargetsRepository
) {
    suspend operator fun invoke(isInfinite: Boolean, durationSeconds: Int): Result<Unit> =
        repository.startMatch(isInfinite, durationSeconds)
}
