package ru.createsmart.artopos.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ru.createsmart.artopos.core.navigation.ArtoposAppState
import ru.createsmart.artopos.core.navigation.DetailsRoute
import ru.createsmart.artopos.core.navigation.DiscoverRoute
import ru.createsmart.artopos.core.navigation.FavoritesRoute
import ru.createsmart.artopos.core.navigation.SettingsRoute
import ru.createsmart.artopos.feature.details.ui.DetailsScreenRoute
import ru.createsmart.artopos.feature.discover.ui.DiscoverRoute
import ru.createsmart.artopos.feature.favorites.ui.FavoritesRoute
import ru.createsmart.artopos.feature.settings.ui.SettingsRoute

@Composable
fun AppNavGraph(
    appState: ArtoposAppState,
) {
    val navController = appState.navController

    NavHost(
        navController = navController,
        startDestination = DiscoverRoute,
    ) {
        // 1. Discover Screen
        composable<DiscoverRoute> {
            DiscoverRoute(
                onArtworkClick = { id ->
                    appState.navigateToArtworkDetails(id)
                },
            )
        }

        // 2. FAVORITES Screen
        composable<FavoritesRoute> {
            FavoritesRoute(
                onArtworkClick = { id ->
                    appState.navigateToArtworkDetails(id)
                },
            )
        }

        // 3. SETTINGS Screen
        composable<SettingsRoute> {
            SettingsRoute()
        }

        // 4. DETAILS Screen (NESTED SCREEN)
        composable<DetailsRoute> { backStackEntry ->
            DetailsScreenRoute(
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}
