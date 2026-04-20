package ru.createsmart.artopos.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ru.createsmart.artopos.core.navigation.DetailsRoute

@Composable
fun rememberArtoposAppState(
    navController: NavHostController = rememberNavController(),
): ArtoposAppState {
    return remember(navController) {
        ArtoposAppState(navController)
    }
}

@Stable
class ArtoposAppState(
    val navController: NavHostController,
) {
    val currentDestination: NavDestination?
        @Composable get() = navController.currentBackStackEntryAsState().value?.destination

    val topLevelDestinations: List<TopLevelDestination> = TopLevelDestination.entries

    val shouldShowBottomBar: Boolean
        @Composable get() = currentDestination?.let { destination ->
            topLevelDestinations.any { topLevel ->
                destination.hasRoute(topLevel.route::class)
            }
        } ?: false

    // Avoid opening the same screen twice
    fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
        navController.navigate(topLevelDestination.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateToArtworkDetails(artworkId: Int) {
        navController.navigate(DetailsRoute(artworkId))
    }
}
