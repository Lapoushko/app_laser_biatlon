package com.lapoushko.feature_statistics.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lapoushko.feature_statistics.api.domain.usecase.ObserveMatchStatisticsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

class StatisticsViewModel @Inject constructor(
    observeMatchStatistics: ObserveMatchStatisticsUseCase
) : ViewModel() {

    val uiState: StateFlow<StatisticsUiState> = observeMatchStatistics()
        .map<_, StatisticsUiState> { StatisticsUiState.Content(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = StatisticsUiState.Loading
        )
}
