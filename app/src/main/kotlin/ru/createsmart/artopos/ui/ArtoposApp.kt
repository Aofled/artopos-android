package ru.createsmart.artopos.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import ru.createsmart.artopos.core.designsystem.theme.ArtoposDimens
import ru.createsmart.artopos.core.navigation.DiscoverRoute
import ru.createsmart.artopos.core.navigation.FavoritesRoute
import ru.createsmart.artopos.core.uicomponents.notifiers.BottomBarCommand
import ru.createsmart.artopos.core.uicomponents.notifiers.GlobalUiEvent
import ru.createsmart.artopos.core.uicomponents.notifiers.GlobalUiEventBus
import ru.createsmart.artopos.core.uicomponents.notifiers.LocalBottomBarStateNotifier
import ru.createsmart.artopos.core.uicomponents.notifiers.LocalBottomBarVisibility
import ru.createsmart.artopos.core.uicomponents.notifiers.LocalGlobalUiEventBus
import ru.createsmart.artopos.navigation.AppNavGraph
import ru.createsmart.artopos.navigation.ArtoposAppState
import ru.createsmart.artopos.navigation.rememberArtoposAppState

private const val BOTTOM_BAR_ALPHA = 0.85f
private const val BOTTOM_BAR_ANIMATION = 400
private const val SCROLL_THRESHOLD = 10f

@Composable
fun ArtoposApp(
    appState: ArtoposAppState = rememberArtoposAppState(),
) {
    var isBottomBarVisible by remember { mutableStateOf(true) }
    var isLockedAtBottom by remember { mutableStateOf(false) }

    // To move to the top of the list by clicking on the navigation icon
    val globalUiEventBus = remember { GlobalUiEventBus() }

    // When you go back (PopBackStack) or switch to a new tab the menu will always be in place
    val currentDestination = appState.currentDestination
    LaunchedEffect(currentDestination) {
        isBottomBarVisible = true
    }

    val canHideBottomBar = currentDestination?.hasRoute(DiscoverRoute::class) == true ||
        currentDestination?.hasRoute(FavoritesRoute::class) == true

    // To listen to swipes
    val nestedScrollConnection = remember(canHideBottomBar, isLockedAtBottom) {
        object : NestedScrollConnection {
            @Suppress("SameReturnValue")
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (!canHideBottomBar) return Offset.Zero
                // If we swipe UP (the content moves down, dy > 0) -> Show the menu
                if (available.y > SCROLL_THRESHOLD) {
                    isBottomBarVisible = true
                }
                // If we swipe DOWN (content moves up, dy < 0) -> Hide the menu
                else if (available.y < -SCROLL_THRESHOLD) {
                    // Hide the menu ONLY if we are not blocked at the end of the list
                    if (!isLockedAtBottom) {
                        isBottomBarVisible = false
                    }
                }
                return Offset.Zero
            }
        }
    }

    /**
     * Updates the bottom bar state based on the command.
     * [BottomBarCommand.SHOW] - makes the bottom bar visible.
     * [BottomBarCommand.LOCK_AT_BOTTOM] - locks the bar at the bottom and shows it.
     * [BottomBarCommand.UNLOCK] - unlocks the bar so it can be hidden by scrolling.
     */
    val bottomBarNotifier: (BottomBarCommand) -> Unit = { command ->
        when (command) {
            BottomBarCommand.SHOW -> {
                isBottomBarVisible = true
            }

            BottomBarCommand.LOCK_AT_BOTTOM -> {
                isLockedAtBottom = true
                isBottomBarVisible = true
            }

            BottomBarCommand.UNLOCK -> {
                isLockedAtBottom = false
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            val showBar = appState.shouldShowBottomBar && isBottomBarVisible

            AnimatedVisibility(
                visible = showBar,
                enter = slideInVertically(
                    animationSpec = tween(
                        durationMillis = BOTTOM_BAR_ANIMATION,
                        easing = FastOutSlowInEasing,
                    ),
                ) { it },
                exit = slideOutVertically(
                    animationSpec = tween(
                        durationMillis = BOTTOM_BAR_ANIMATION,
                        easing = FastOutSlowInEasing,
                    ),
                ) { it },
            ) {
                ArtoposBottomBar(appState = appState, eventBus = globalUiEventBus)
            }
        },
    ) { _ -> // contentPadding for lists we will pass later via CompositionLocal
        CompositionLocalProvider(
            LocalBottomBarStateNotifier provides bottomBarNotifier,
            LocalBottomBarVisibility provides isBottomBarVisible,
            LocalGlobalUiEventBus provides globalUiEventBus,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AppNavGraph(appState = appState)
            }
        }
    }
}

@Composable
private fun ArtoposBottomBar(
    appState: ArtoposAppState,
    eventBus: GlobalUiEventBus,
) {
    val glassColor = MaterialTheme.colorScheme.surface.copy(alpha = BOTTOM_BAR_ALPHA)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(glassColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(ArtoposDimens.BottomBarHeight)
                .selectableGroup(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val currentDestination = appState.currentDestination

            appState.topLevelDestinations.forEach { destination ->
                val routeString = destination.route::class.qualifiedName ?: ""
                val selected = currentDestination?.hasRoute(destination.route::class) == true

                IconButton(
                    onClick = {
                        if (selected) {
                            // If the tab is ALREADY selected, send a "Scroll up" event for this screen
                            eventBus.sendEvent(GlobalUiEvent.ScrollToTop(routeString))
                        } else {
                            // If not selected, just go to it
                            appState.navigateToTopLevelDestination(destination)
                        }
                    },
                    modifier = Modifier.size(ArtoposDimens.BottomBarHeight),
                ) {
                    val iconRes =
                        if (selected) destination.iconSelected else destination.iconUnselected

                    val tint = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = stringResource(id = destination.titleTextId),
                        tint = tint,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier.align(Alignment.TopCenter),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            thickness = 0.5.dp,
        )
    }
}
