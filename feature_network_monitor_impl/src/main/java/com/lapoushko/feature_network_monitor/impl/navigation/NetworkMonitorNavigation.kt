package com.lapoushko.feature_network_monitor.impl.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.lapoushko.feature_network_monitor.impl.presentation.NetworkMonitorRoute

object NetworkMonitorDestination {
    const val ROUTE = "network_monitor"
}

fun NavGraphBuilder.networkMonitorScreen() {
    composable(NetworkMonitorDestination.ROUTE) {
        NetworkMonitorRoute()
    }
}
