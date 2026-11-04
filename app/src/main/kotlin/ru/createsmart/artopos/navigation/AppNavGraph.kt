package ru.createsmart.artopos.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ru.createsmart.artopos.core.designsystem.components.UiText
import ru.createsmart.artopos.core.navigation.DetailsRoute
import ru.createsmart.artopos.core.navigation.DiscoverRoute
import ru.createsmart.artopos.core.navigation.FavoritesRoute
import ru.createsmart.artopos.core.navigation.SearchRoute
import ru.createsmart.artopos.core.navigation.SettingsRoute
import ru.createsmart.artopos.feature.details.ui.DetailsScreenRoute
import ru.createsmart.artopos.feature.discover.ui.DiscoverRoute
import ru.createsmart.artopos.feature.favorites.ui.FavoritesRoute
import ru.createsmart.artopos.feature.search.ui.SearchRoute
import ru.createsmart.artopos.feature.settings.ui.SettingsRoute

@Composable
fun AppNavGraph(
    appState: ArtoposAppState,
    onShowSnackbar: (UiText) -> Unit,
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
                onShowSnackbar = onShowSnackbar,
                onUpdateFabVisibility = { isVisible ->
                    appState.updateFabVisibility(isVisible)
                },
            )
        }

        // 2. SEARCH Screen
        composable<SearchRoute> {
            SearchRoute(
                onArtworkClick = { id ->
                    appState.navigateToArtworkDetails(id)
                },
                onShowSnackbar = onShowSnackbar,
            )
        }

        // 3. FAVORITES Screen
        composable<FavoritesRoute> {
            FavoritesRoute(
                onArtworkClick = { id ->
                    appState.navigateToArtworkDetails(id)
                },
                onShowSnackbar = onShowSnackbar,
            )
        }

        // 4. SETTINGS Screen
        composable<SettingsRoute> {
            SettingsRoute(
                onShowSnackbar = onShowSnackbar,
            )
        }

        // 5. DETAILS Screen (NESTED SCREEN)
        composable<DetailsRoute> {
            DetailsScreenRoute(
                onBackClick = { navController.popBackStack() },
                onShowSnackbar = onShowSnackbar,
            )
        }
    }
}
