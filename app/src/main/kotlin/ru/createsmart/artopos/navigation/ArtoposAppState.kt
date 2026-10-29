package ru.createsmart.artopos.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import ru.createsmart.artopos.core.navigation.DetailsRoute

@Composable
fun rememberArtoposAppState(
    navController: NavHostController = rememberNavController(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): ArtoposAppState {
    return remember(navController, snackbarHostState, coroutineScope) {
        ArtoposAppState(
            navController = navController,
            snackbarHostState = snackbarHostState,
            coroutineScope = coroutineScope,
        )
    }
}

@Stable
class ArtoposAppState(
    val navController: NavHostController,
    val snackbarHostState: SnackbarHostState,
    private val coroutineScope: CoroutineScope,
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

    // Flag indicating if FAB is present on current screen (used for snackbar padding)
    var isFabVisible by mutableStateOf(false)
        private set

    fun updateFabVisibility(visible: Boolean) {
        isFabVisible = visible
    }

    // Stream for active tab reselection (scroll-to-top)
    private val _reselectedTab = MutableSharedFlow<TopLevelDestination>(extraBufferCapacity = 1)
    val reselectedTab: SharedFlow<TopLevelDestination> = _reselectedTab.asSharedFlow()

    fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
        // Reset FAB state on tab transition
        isFabVisible = false

        navController.navigate(topLevelDestination.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun onTopLevelDestinationReselected(topLevelDestination: TopLevelDestination) {
        _reselectedTab.tryEmit(topLevelDestination)
    }

    fun navigateToArtworkDetails(artworkId: Int) {
        isFabVisible = false
        navController.navigate(DetailsRoute(artworkId))
    }

    /**
     * Shows a system snackbar at application level.
     * Persists across screen transitions.
     */
    fun showSnackbar(message: String) {
        coroutineScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }
}
