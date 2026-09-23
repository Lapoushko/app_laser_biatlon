package com.lapoushko.feature_statistics.impl.presentation

import com.lapoushko.feature_statistics.api.domain.MatchStatistics

sealed interface StatisticsUiState {
    data object Loading : StatisticsUiState

    /** Матч ещё ни разу не запускался (или сервер недоступен и кэша тоже нет). */
    data object NotStarted : StatisticsUiState

    data class Content(val statistics: MatchStatistics) : StatisticsUiState
}
