package com.lapoushko.feature_statistics.api.domain

import kotlinx.coroutines.flow.Flow

interface StatisticsRepository {
    /** Бесконечный поток снимков статистики текущего матча, обновляемый не реже 1 раза в секунду. */
    fun observeMatchStatistics(): Flow<MatchStatistics>
}
