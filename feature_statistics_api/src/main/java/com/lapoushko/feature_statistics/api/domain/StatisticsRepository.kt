package com.lapoushko.feature_statistics.api.domain

import kotlinx.coroutines.flow.Flow

interface StatisticsRepository {
    /**
     * Бесконечный поток снимков статистики текущего матча, обновляемый не реже 1 раза в секунду.
     * null — данных ещё нет и нечем заполнить экран даже из кэша (матч ни разу не запускался).
     */
    fun observeMatchStatistics(): Flow<MatchStatistics?>
}
