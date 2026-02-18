package ru.createsmart.artopos.feature.discover.ui

import UiText
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import ru.createsmart.artopos.core.ui.theme.ArtoposTheme
import ru.createsmart.artopos.core.ui.theme.components.FilterFloatingActionButton
import ru.createsmart.artopos.feature.discover.DiscoverViewModel
import ru.createsmart.artopos.feature.discover.model.ArtworkListItem
import ru.createsmart.artopos.feature.discover.ui.components.ArtworksView
import ru.createsmart.artopos.feature.discover.ui.components.ErrorView
import ru.createsmart.artopos.feature.discover.ui.components.LoadingView
import ru.createsmart.artopos.feature.discover.ui.preview.DiscoverStateProvider

@Composable
fun DiscoverRoute(
    viewModel: DiscoverViewModel = hiltViewModel(),
    onArtworkClick: (Int) -> Unit,
) {
    // Lifecycle-aware collection. Pauses when app is in background.
    val pagingItems = viewModel.artworksFlow.collectAsLazyPagingItems()

    val contentVersion by viewModel.contentVersion.collectAsStateWithLifecycle()

    DiscoverScreen(
        pagingItems = pagingItems,
        contentVersion = contentVersion,
        onRefresh = {
            if (viewModel.onRefresh()) {
                pagingItems.refresh()
            }
        },
        onRetry = {
            if (viewModel.onRetryAction()) {
                pagingItems.retry()
            }
        },
        onArtworkClick = onArtworkClick,
        effectFlow = viewModel.uiEffect,
        onError = viewModel::onError,
        onFilterClick = {
            // TODO(BottomSheet): Open BottomSheet
        },
    )
}

@Composable
fun DiscoverScreen(
    pagingItems: LazyPagingItems<ArtworkListItem>,
    contentVersion: Int,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onArtworkClick: (Int) -> Unit,
    effectFlow: Flow<UiText>? = null,
    onError: (Throwable) -> Unit,
    onFilterClick: () -> Unit,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()

    fun showSnackbar(message: UiText) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val text = message.asString(context)
            snackbarHostState.showSnackbar(text)
        }
    }

    LaunchedEffect(effectFlow) {
        effectFlow?.collect { message ->
            showSnackbar(message)
        }
    }

    LaunchedEffect(pagingItems.loadState) { // Global Error Handling
        val state = pagingItems.loadState

        (state.refresh as? LoadState.Error)?.error?.let { onError(it) }

        (state.append as? LoadState.Error)?.error?.let { onError(it) }
    }

    // Scaffold handles system bars (Status/Nav Bar) automatically via contentWindowInsets
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.systemBars,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
            )
        },

        floatingActionButton = {
            val showFab = pagingItems.loadState.refresh !is LoadState.Loading

            AnimatedVisibility(
                visible = showFab,
                enter = scaleIn(),
                exit = scaleOut(),
            ) {
                FilterFloatingActionButton(onFilterClick)
            }
        },

    ) { innerPadding ->
        DiscoverScreenContent(
            pagingItems = pagingItems,
            contentVersion = contentVersion,
            innerPadding = innerPadding,
            onRefresh = onRefresh,
            onRetry = onRetry,
            onArtworkClick = onArtworkClick,
            onShowMessage = { showSnackbar(it) },
        )
    }
}

@Composable
private fun DiscoverScreenContent(
    pagingItems: LazyPagingItems<ArtworkListItem>,
    contentVersion: Int,
    innerPadding: PaddingValues,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onArtworkClick: (Int) -> Unit,
    onShowMessage: (UiText) -> Unit,
) {
    val refreshState = pagingItems.loadState.refresh
    val isListEmpty = pagingItems.itemCount == 0

    when {
        !isListEmpty -> {
            val isRefreshing = refreshState is LoadState.Loading

            ArtworksView(
                artworks = pagingItems,
                contentVersion = contentVersion,
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                onRetry = onRetry,
                onArtworkClick = onArtworkClick,
                contentPadding = innerPadding,
                onShowMessage = onShowMessage,
            )
        }

        refreshState is LoadState.Error && isListEmpty -> {
            Box(modifier = Modifier.padding(innerPadding)) {
                ErrorView(onRetry = onRetry)
            }
        }

        else -> {
            Box(modifier = Modifier.padding(innerPadding)) {
                LoadingView()
            }
        }
    }
}

@Preview(showBackground = true, name = "Discover States", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun DiscoverScreenPreview(
    @PreviewParameter(DiscoverStateProvider::class) pagingFlow: Flow<PagingData<ArtworkListItem>>,
) {
    val pagingItems = pagingFlow.collectAsLazyPagingItems()

    ArtoposTheme {
        DiscoverScreen(
            pagingItems = pagingItems,
            onRefresh = { },
            onArtworkClick = { id -> println("Clicked id: $id") },
            contentVersion = 0,
            onRetry = { },
            onError = { },
            onFilterClick = { },
        )
    }
}
