package com.lapoushko.feature_network_monitor.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lapoushko.feature_network_monitor.api.domain.usecase.ClearNetworkLogsUseCase
import com.lapoushko.feature_network_monitor.api.domain.usecase.ObserveConnectionQualityUseCase
import com.lapoushko.feature_network_monitor.api.domain.usecase.ObserveNetworkLogsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class NetworkMonitorViewModel @Inject constructor(
    private val clearNetworkLogs: ClearNetworkLogsUseCase,
    observeNetworkLogs: ObserveNetworkLogsUseCase,
    observeConnectionQuality: ObserveConnectionQualityUseCase
) : ViewModel() {

    private val expandedLogId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<NetworkMonitorUiState> = combine(
        observeNetworkLogs(),
        expandedLogId,
        observeConnectionQuality()
    ) { logs, expandedId, connectionQuality ->
        NetworkMonitorUiState(logs = logs, expandedLogId = expandedId, connectionQuality = connectionQuality)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NetworkMonitorUiState()
    )

    fun toggleExpanded(id: Long) {
        expandedLogId.update { current -> if (current == id) null else id }
    }

    fun clear() {
        viewModelScope.launch { clearNetworkLogs() }
    }
}
