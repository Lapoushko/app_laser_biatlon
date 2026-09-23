package com.lapoushko.feature_statistics.impl.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.lapoushko.feature_statistics.impl.presentation.StatisticsRoute

object StatisticsDestination {
    const val ROUTE = "statistics"
}

fun NavGraphBuilder.statisticsScreen() {
    composable(StatisticsDestination.ROUTE) {
        StatisticsRoute()
    }
}
