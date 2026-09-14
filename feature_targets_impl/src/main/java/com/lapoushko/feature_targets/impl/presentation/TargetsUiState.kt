package com.lapoushko.feature_targets.impl.presentation

import com.lapoushko.feature_targets.api.domain.Target

data class TargetsUiState(
    val targets: List<Target> = emptyList(),
    val isLoading: Boolean = true,
    val isInfiniteMode: Boolean = true,
    val durationInput: String = "60",
    val actionError: String? = null
)
