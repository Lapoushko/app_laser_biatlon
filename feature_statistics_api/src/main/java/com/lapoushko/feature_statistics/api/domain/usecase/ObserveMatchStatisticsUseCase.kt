package com.lapoushko.feature_statistics.api.domain.usecase

import com.lapoushko.feature_statistics.api.domain.MatchStatistics
import com.lapoushko.feature_statistics.api.domain.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveMatchStatisticsUseCase @Inject constructor(
    private val repository: StatisticsRepository
) {
    operator fun invoke(): Flow<MatchStatistics?> = repository.observeMatchStatistics()
}
