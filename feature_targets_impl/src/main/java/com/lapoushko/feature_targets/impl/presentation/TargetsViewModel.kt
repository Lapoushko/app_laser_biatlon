package com.lapoushko.feature_targets.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lapoushko.feature_targets.api.domain.usecase.ObserveTargetsUseCase
import com.lapoushko.feature_targets.api.domain.usecase.StartMatchUseCase
import com.lapoushko.feature_targets.api.domain.usecase.StopMatchUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class TargetsViewModel @Inject constructor(
    observeTargets: ObserveTargetsUseCase,
    private val startMatch: StartMatchUseCase,
    private val stopMatch: StopMatchUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TargetsUiState())
    val uiState: StateFlow<TargetsUiState> = _uiState

    init {
        viewModelScope.launch {
            observeTargets().collect { targets ->
                _uiState.update { it.copy(targets = targets, isLoading = false) }
            }
        }
    }

    fun onInfiniteModeChange(isInfinite: Boolean) {
        _uiState.update { it.copy(isInfiniteMode = isInfinite) }
    }

    fun onDurationChange(value: String) {
        _uiState.update { it.copy(durationInput = value.filter { c -> c.isDigit() }) }
    }

    fun onStartMatch() {
        val state = _uiState.value
        val duration = state.durationInput.toIntOrNull() ?: 0
        viewModelScope.launch {
            startMatch(state.isInfiniteMode, duration).onFailure { error ->
                _uiState.update { it.copy(actionError = error.message) }
            }
        }
    }

    fun onStopMatch() {
        viewModelScope.launch {
            stopMatch(0).onFailure { error ->
                _uiState.update { it.copy(actionError = error.message) }
            }
        }
    }
}
