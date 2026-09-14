package com.lapoushko.feature_targets.api.domain.usecase

import com.lapoushko.feature_targets.api.domain.Target
import com.lapoushko.feature_targets.api.domain.TargetsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTargetsUseCase @Inject constructor(
    private val repository: TargetsRepository
) {
    operator fun invoke(): Flow<List<Target>> = repository.observeTargets()
}
