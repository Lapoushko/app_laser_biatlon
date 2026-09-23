package com.lapoushko.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lapoushko.feature_connection.impl.navigation.ConnectionDestination
import com.lapoushko.feature_connection.impl.navigation.connectionScreen
import com.lapoushko.feature_experiment.impl.navigation.ExperimentDestination
import com.lapoushko.feature_experiment.impl.navigation.experimentScreen
import com.lapoushko.feature_network_monitor.impl.navigation.NetworkMonitorDestination
import com.lapoushko.feature_network_monitor.impl.navigation.networkMonitorScreen
import com.lapoushko.feature_statistics.impl.navigation.StatisticsDestination
import com.lapoushko.feature_statistics.impl.navigation.statisticsScreen
import com.lapoushko.feature_targets.impl.navigation.TargetsDestination
import com.lapoushko.feature_targets.impl.navigation.targetsScreen

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute == StatisticsDestination.ROUTE ||
        currentRoute == TargetsDestination.ROUTE ||
        currentRoute == NetworkMonitorDestination.ROUTE ||
        currentRoute == ExperimentDestination.ROUTE

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                MainBottomBar(currentRoute = currentRoute, navController = navController)
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = ConnectionDestination.ROUTE,
            modifier = Modifier.padding(padding)
        ) {
            connectionScreen(
                onConnected = {
                    navController.navigate(StatisticsDestination.ROUTE) {
                        popUpTo(ConnectionDestination.ROUTE) { inclusive = true }
                    }
                }
            )
            statisticsScreen()
            targetsScreen()
            networkMonitorScreen()
            experimentScreen()
        }
    }
}

@Composable
private fun MainBottomBar(currentRoute: String?, navController: NavHostController) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == StatisticsDestination.ROUTE,
            onClick = { navController.navigateToTab(StatisticsDestination.ROUTE) },
            icon = { Text("📊") },
            label = { NavItemLabel("Статистика") }
        )
        NavigationBarItem(
            selected = currentRoute == TargetsDestination.ROUTE,
            onClick = { navController.navigateToTab(TargetsDestination.ROUTE) },
            icon = { Text("🎯") },
            label = { NavItemLabel("Мишени") }
        )
        NavigationBarItem(
            selected = currentRoute == NetworkMonitorDestination.ROUTE,
            onClick = { navController.navigateToTab(NetworkMonitorDestination.ROUTE) },
            icon = { Text("🌐") },
            label = { NavItemLabel("Трафик") }
        )
        NavigationBarItem(
            selected = currentRoute == ExperimentDestination.ROUTE,
            onClick = { navController.navigateToTab(ExperimentDestination.ROUTE) },
            icon = { Text("🧪") },
            label = { NavItemLabel("Эксперимент") }
        )
    }
}

/**
 * На узких экранах с 5 вкладками подпись в стандартный NavigationBarItem не помещается
 * в одну строку и переносится посередине слова ("Экспериме" / "нт"). Фиксируем одну строку
 * и уменьшаем шрифт вместо переноса; если совсем не влезает — обрезаем многоточием, а не рвём слово.
 */
@Composable
private fun NavItemLabel(text: String) {
    Text(
        text = text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.labelSmall
    )
}

private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(StatisticsDestination.ROUTE) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
