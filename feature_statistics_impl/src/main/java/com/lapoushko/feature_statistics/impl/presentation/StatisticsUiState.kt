package com.lapoushko.feature_statistics.impl.presentation

import com.lapoushko.feature_statistics.api.domain.MatchStatistics

sealed interface StatisticsUiState {
    data object Loading : StatisticsUiState
    data class Content(val statistics: MatchStatistics) : StatisticsUiState
}
