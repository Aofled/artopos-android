package ru.createsmart.artopos.feature.discover.ui

import UiText
import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
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
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
            )
        },
        contentWindowInsets = WindowInsets.statusBars,
    ) { innerPadding ->
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
                    onShowMessage = { msg -> showSnackbar(msg) },
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
        )
    }
}
