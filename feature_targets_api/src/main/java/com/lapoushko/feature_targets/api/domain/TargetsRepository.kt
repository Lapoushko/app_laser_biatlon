package com.lapoushko.feature_targets.api.domain

import kotlinx.coroutines.flow.Flow

interface TargetsRepository {
    fun observeTargets(): Flow<List<Target>>

    suspend fun startMatch(isInfinite: Boolean, durationSeconds: Int): Result<Unit>
    suspend fun stopMatch(remainingSeconds: Int): Result<Unit>
}
