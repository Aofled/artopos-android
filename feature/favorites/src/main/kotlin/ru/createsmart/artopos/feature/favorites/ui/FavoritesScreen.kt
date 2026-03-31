package ru.createsmart.artopos.feature.favorites.ui

import UiText
import android.content.res.Configuration
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import ru.createsmart.artopos.core.designsystem.theme.ArtoposDimens
import ru.createsmart.artopos.core.designsystem.theme.ArtoposTheme
import ru.createsmart.artopos.core.uicomponents.notifiers.LocalBottomBarVisibility
import ru.createsmart.artopos.feature.favorites.FavoritesUiState
import ru.createsmart.artopos.feature.favorites.FavoritesViewModel
import ru.createsmart.artopos.feature.favorites.model.FavoritesActions
import ru.createsmart.artopos.feature.favorites.ui.components.EmptyFavoritesView
import ru.createsmart.artopos.feature.favorites.ui.components.FavoritesView
import ru.createsmart.artopos.feature.favorites.ui.preview.FavoritesStateProvider

@Composable
fun FavoritesRoute(
    viewModel: FavoritesViewModel = hiltViewModel(),
    onArtworkClick: (Int) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val contentVersion by viewModel.contentVersion.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    FavoritesScreen(
        state = state,
        contentVersion = contentVersion,
        isRefreshing = isRefreshing,
        effectFlow = viewModel.uiEffect,
        actions = FavoritesActions(
            onRefresh = viewModel::onRefresh,
            onArtworkClick = onArtworkClick,
            onToggleFavorite = viewModel::onToggleFavorite,
        ),
    )
}

@Composable
fun FavoritesScreen(
    state: FavoritesUiState,
    contentVersion: Int,
    isRefreshing: Boolean,
    effectFlow: Flow<UiText>?,
    actions: FavoritesActions,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val onShowSnackbar: (UiText) -> Unit = { message ->
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message.asString(context))
        }
    }

    LaunchedEffect(effectFlow) {
        effectFlow?.collect { message -> onShowSnackbar(message) }
    }

    val isBottomBarVisible = LocalBottomBarVisibility.current

    val snackbarBottomPadding by animateDpAsState(
        targetValue = if (isBottomBarVisible) {
            ArtoposDimens.SnackbarPaddingWithMenu
        } else {
            0.dp
        }, // NAV is visible, else NAV not visible.
        animationSpec = tween(
            durationMillis = ArtoposDimens.BOTTOM_BAR_ANIMATION_DURATION,
            easing = FastOutSlowInEasing,
        ),
        label = "snackbarPadding",
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.statusBars,

        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(bottom = snackbarBottomPadding),
            )
        },
    ) { innerPadding ->

        FavoritesScreenContent(
            state = state,
            contentVersion = contentVersion,
            isRefreshing = isRefreshing,
            innerPadding = innerPadding,
            actions = actions,
            onShowMessage = { onShowSnackbar(it) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoritesScreenContent(
    state: FavoritesUiState,
    contentVersion: Int,
    isRefreshing: Boolean,
    innerPadding: PaddingValues,
    actions: FavoritesActions,
    onShowMessage: (UiText) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
    ) {
        when (state) {
            is FavoritesUiState.Empty -> {
                EmptyFavoritesView()
            }

            is FavoritesUiState.Success -> {
                FavoritesView(
                    artworks = state.artworks,
                    contentVersion = contentVersion,
                    isRefreshing = isRefreshing,
                    actions = actions,
                    onShowMessage = onShowMessage,
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Discover States", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun FavoritesScreenPreview(
    @PreviewParameter(FavoritesStateProvider::class) state: FavoritesUiState,
) {
    ArtoposTheme {
        FavoritesScreen(
            state = state,
            contentVersion = 1,
            isRefreshing = false,
            effectFlow = null,
            actions = FavoritesActions(
                onRefresh = { println("Refreshed") },
                onArtworkClick = { id -> println("Artwork clicked: $id") },
                onToggleFavorite = { id -> println("Favorite clicked: $id") },
            ),
        )
    }
}
