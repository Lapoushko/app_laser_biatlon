package com.lapoushko.feature_targets.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lapoushko.feature_targets.api.domain.usecase.ObserveTargetsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class TargetsViewModel @Inject constructor(
    observeTargets: ObserveTargetsUseCase
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
}
