package com.lapoushko.feature_targets.impl.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.lapoushko.feature_targets.impl.presentation.TargetsRoute

object TargetsDestination {
    const val ROUTE = "targets"
}

fun NavGraphBuilder.targetsScreen() {
    composable(TargetsDestination.ROUTE) {
        TargetsRoute()
    }
}
