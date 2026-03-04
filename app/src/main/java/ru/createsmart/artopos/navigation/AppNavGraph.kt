package ru.createsmart.artopos.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ru.createsmart.artopos.core.navigation.DetailsRoute
import ru.createsmart.artopos.core.navigation.DiscoverRoute
import ru.createsmart.artopos.feature.details.ui.DetailsScreenRoute
import ru.createsmart.artopos.feature.discover.ui.DiscoverRoute

@Composable
fun AppNavGraph(
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = DiscoverRoute,
    ) {
        // 1. Discover Screen
        composable<DiscoverRoute> {
            DiscoverRoute(
                onArtworkClick = { id ->
                    navController.navigate(
                        DetailsRoute(
                            artworkId = id,
                        ),
                    )
                },
            )
        }

        // 2. Details Screen
        composable<DetailsRoute> { backStackEntry ->
            DetailsScreenRoute(
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}
