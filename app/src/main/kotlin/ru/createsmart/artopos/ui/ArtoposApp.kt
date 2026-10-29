package ru.createsmart.artopos.ui

import android.annotation.SuppressLint
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import kotlinx.coroutines.flow.MutableSharedFlow
import ru.createsmart.artopos.core.designsystem.theme.ArtoposDimens
import ru.createsmart.artopos.core.navigation.DiscoverRoute
import ru.createsmart.artopos.core.navigation.FavoritesRoute
import ru.createsmart.artopos.core.uicomponents.notifiers.BottomBarCommand
import ru.createsmart.artopos.core.uicomponents.notifiers.LocalBottomBarStateNotifier
import ru.createsmart.artopos.core.uicomponents.notifiers.LocalBottomBarVisibility
import ru.createsmart.artopos.core.uicomponents.notifiers.LocalTabReselectFlow
import ru.createsmart.artopos.navigation.AppNavGraph
import ru.createsmart.artopos.navigation.ArtoposAppState
import ru.createsmart.artopos.navigation.rememberArtoposAppState

private const val BOTTOM_BAR_ALPHA = 0.85f
private const val ANIMATION_DURATION_MS = 400
private val SCROLL_THRESHOLD_DP = 10.dp

private val FAB_HEIGHT_WITH_PADDING = 74.dp
private val SNACKBAR_PADDING_BASE = 4.dp

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ArtoposApp(
    appState: ArtoposAppState = rememberArtoposAppState(),
) {
    val context = LocalContext.current
    var isBottomBarVisible by remember { mutableStateOf(true) }
    var isLockedAtBottom by remember { mutableStateOf(false) }

    val tabReselectFlow = remember { MutableSharedFlow<Any>(extraBufferCapacity = 1) }

    // When you go back (PopBackStack) or switch to a new tab the menu will always be in place
    val currentDestination = appState.currentDestination
    LaunchedEffect(currentDestination) {
        isBottomBarVisible = true
    }

    val canHideBottomBar = currentDestination.canHideBottomBar()

    val density = LocalDensity.current
    val scrollThresholdPx = remember(density) {
        with(density) { SCROLL_THRESHOLD_DP.toPx() }
    }

    val nestedScrollConnection = rememberBottomBarNestedScrollConnection(
        canHideBottomBar = canHideBottomBar,
        isLockedAtBottom = isLockedAtBottom,
        scrollThresholdPx = scrollThresholdPx,
        onVisibilityChange = { isBottomBarVisible = it },
    )

    val bottomBarNotifier: (BottomBarCommand) -> Unit = remember {
        {
                command ->
            handleBottomBarCommand(
                command = command,
                onVisibilityChange = { isBottomBarVisible = it },
                onLockChange = { isLockedAtBottom = it },
            )
        }
    }

    // Whether the bottom bar is allowed on the current destination
    val hasBottomBarOnScreen = appState.shouldShowBottomBar

    // Bottom bar is currently active and not hidden by scroll
    val isBarShowingNow = hasBottomBarOnScreen && isBottomBarVisible
    val isFabActive = appState.isFabVisible

    val navBarBottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // 1. Bottom bar slide animation offset
    val bottomBarOffset by animateDpAsState(
        targetValue = if (isBarShowingNow) 0.dp else (ArtoposDimens.BottomBarHeight + navBarBottomInset),
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION_MS,
            easing = FastOutSlowInEasing,
        ),
        label = "bottomBarOffset",
    )

    // 2. Snackbar bottom padding calculation
    val snackbarBottomPadding by animateDpAsState(
        targetValue = calculateSnackbarBottomPadding(
            isFabActive = isFabActive,
            isBarShowingNow = isBarShowingNow,
        ),
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION_MS,
            easing = FastOutSlowInEasing,
        ),
        label = "rootSnackbarPadding",
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { _ ->
        CompositionLocalProvider(
            LocalBottomBarStateNotifier provides bottomBarNotifier,
            LocalBottomBarVisibility provides isBottomBarVisible,
            LocalTabReselectFlow provides tabReselectFlow,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AppNavGraph(
                    appState = appState,
                    onShowSnackbar = { uiText ->
                        appState.showSnackbar(uiText.asString(context))
                    },
                )

                if (hasBottomBarOnScreen) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = bottomBarOffset),
                    ) {
                        ArtoposBottomBar(
                            appState = appState,
                            onTabReselected = { route -> tabReselectFlow.tryEmit(route) },
                        )
                    }
                }

                SnackbarHost(
                    hostState = appState.snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(bottom = snackbarBottomPadding)
                        .padding(horizontal = 16.dp),
                ) { snackbarData ->
                    Snackbar(
                        snackbarData = snackbarData,
                        shape = RoundedCornerShape(12.dp),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

private fun NavDestination?.canHideBottomBar(): Boolean =
    this?.hasRoute(DiscoverRoute::class) == true || this?.hasRoute(FavoritesRoute::class) == true

@Composable
private fun rememberBottomBarNestedScrollConnection(
    canHideBottomBar: Boolean,
    isLockedAtBottom: Boolean,
    scrollThresholdPx: Float,
    onVisibilityChange: (Boolean) -> Unit,
): NestedScrollConnection {
    return remember(canHideBottomBar, isLockedAtBottom, scrollThresholdPx) {
        object : NestedScrollConnection {
            @Suppress("SameReturnValue")
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (!canHideBottomBar) return Offset.Zero
                if (available.y > scrollThresholdPx) {
                    onVisibilityChange(true)
                } else if (available.y < -scrollThresholdPx && !isLockedAtBottom) {
                    onVisibilityChange(false)
                }
                return Offset.Zero
            }
        }
    }
}

private fun handleBottomBarCommand(
    command: BottomBarCommand,
    onVisibilityChange: (Boolean) -> Unit,
    onLockChange: (Boolean) -> Unit,
) {
    when (command) {
        BottomBarCommand.SHOW -> onVisibilityChange(true)
        BottomBarCommand.LOCK_AT_BOTTOM -> {
            onLockChange(true)
            onVisibilityChange(true)
        }
        BottomBarCommand.UNLOCK -> onLockChange(false)
    }
}

private fun calculateSnackbarBottomPadding(
    isFabActive: Boolean,
    isBarShowingNow: Boolean,
) = when {
    isFabActive && isBarShowingNow -> FAB_HEIGHT_WITH_PADDING + ArtoposDimens.BottomBarHeight
    isFabActive && !isBarShowingNow -> FAB_HEIGHT_WITH_PADDING
    !isFabActive && isBarShowingNow -> ArtoposDimens.BottomBarHeight + SNACKBAR_PADDING_BASE
    else -> SNACKBAR_PADDING_BASE
}

@Composable
private fun ArtoposBottomBar(
    appState: ArtoposAppState,
    onTabReselected: (Any) -> Unit,
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
                val selected = currentDestination?.hasRoute(destination.route::class) == true

                IconButton(
                    onClick = {
                        if (selected) {
                            onTabReselected(destination.route)
                        } else {
                            appState.navigateToTopLevelDestination(destination)
                        }
                    },
                    modifier = Modifier.size(ArtoposDimens.BottomBarHeight),
                ) {
                    val iconRes = if (selected) destination.iconSelected else destination.iconUnselected
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
