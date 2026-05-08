package ru.createsmart.artopos.feature.favorites.ui

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import ru.createsmart.artopos.core.designsystem.components.UiText
import ru.createsmart.artopos.core.designsystem.theme.ArtoposDimens
import ru.createsmart.artopos.core.designsystem.theme.ArtoposTheme
import ru.createsmart.artopos.core.uicomponents.components.CustomCircularProgressIndicator
import ru.createsmart.artopos.core.uicomponents.notifiers.LocalBottomBarVisibility
import ru.createsmart.artopos.feature.favorites.FavoritesUiState
import ru.createsmart.artopos.feature.favorites.FavoritesViewModel
import ru.createsmart.artopos.feature.favorites.model.FavoritesIntent
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

    FavoritesScreen(
        state = state,
        contentVersion = contentVersion,
        effectFlow = viewModel.uiEffect,
        onArtworkClick = onArtworkClick,
        onIntent = viewModel::onIntent,
    )
}

@Composable
private fun FavoritesScreen(
    state: FavoritesUiState,
    contentVersion: Int,
    effectFlow: Flow<UiText>?,
    onArtworkClick: (Int) -> Unit,
    onIntent: (FavoritesIntent) -> Unit,
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
            ArtoposDimens.BottomBarHeight + 4.dp
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
            innerPadding = innerPadding,
            onIntent = onIntent,
            onArtworkClick = onArtworkClick,
            onShowMessage = { onShowSnackbar(it) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoritesScreenContent(
    state: FavoritesUiState,
    contentVersion: Int,
    innerPadding: PaddingValues,
    onArtworkClick: (Int) -> Unit,
    onIntent: (FavoritesIntent) -> Unit,
    onShowMessage: (UiText) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
    ) {
        when (state) {
            is FavoritesUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CustomCircularProgressIndicator()
                }
            }

            is FavoritesUiState.Empty -> {
                EmptyFavoritesView()
            }

            is FavoritesUiState.Success -> {
                FavoritesView(
                    artworks = state.artworks,
                    contentVersion = contentVersion,
                    onIntent = onIntent,
                    onArtworkClick = onArtworkClick,
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
            effectFlow = null,
            onArtworkClick = { },
            onIntent = { intent -> println("Intent triggered: $intent") },
        )
    }
}
