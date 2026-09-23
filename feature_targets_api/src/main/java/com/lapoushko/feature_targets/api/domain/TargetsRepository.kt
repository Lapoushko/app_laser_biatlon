package com.lapoushko.feature_targets.api.domain

import kotlinx.coroutines.flow.Flow

interface TargetsRepository {
    fun observeTargets(): Flow<List<Target>>
}
