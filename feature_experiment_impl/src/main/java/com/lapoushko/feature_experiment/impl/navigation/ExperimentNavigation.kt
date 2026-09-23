package com.lapoushko.feature_experiment.impl.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.lapoushko.feature_experiment.impl.presentation.ExperimentRoute

object ExperimentDestination {
    const val ROUTE = "experiment"
}

fun NavGraphBuilder.experimentScreen() {
    composable(ExperimentDestination.ROUTE) {
        ExperimentRoute()
    }
}
