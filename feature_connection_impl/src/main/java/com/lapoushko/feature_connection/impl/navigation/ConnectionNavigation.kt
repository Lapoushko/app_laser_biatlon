package com.lapoushko.feature_connection.impl.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.lapoushko.feature_connection.impl.presentation.ConnectionRoute

object ConnectionDestination {
    const val ROUTE = "connection"
}

fun NavGraphBuilder.connectionScreen(onConnected: () -> Unit) {
    composable(ConnectionDestination.ROUTE) {
        ConnectionRoute(onConnected = onConnected)
    }
}
