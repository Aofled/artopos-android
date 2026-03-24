package ru.createsmart.artopos.feature.discover.ui

import UiText
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import ru.createsmart.artopos.core.model.FilterParams
import ru.createsmart.artopos.core.model.FilterType
import ru.createsmart.artopos.core.ui.components.FilterFloatingActionButton
import ru.createsmart.artopos.core.ui.theme.ArtoposTheme
import ru.createsmart.artopos.feature.discover.DiscoverViewModel
import ru.createsmart.artopos.feature.discover.model.ArtworkListItem
import ru.createsmart.artopos.feature.discover.model.DiscoverActions
import ru.createsmart.artopos.feature.discover.model.DiscoverEvent
import ru.createsmart.artopos.feature.discover.model.FiltersUiState
import ru.createsmart.artopos.feature.discover.ui.components.ArtworksView
import ru.createsmart.artopos.feature.discover.ui.components.ErrorView
import ru.createsmart.artopos.feature.discover.ui.components.FilterBottomSheet
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

    val filtersState by viewModel.filtersUiState.collectAsStateWithLifecycle()

    val activeParams by viewModel.activeFilterParams.collectAsStateWithLifecycle()

    DiscoverScreen(
        pagingItems = pagingItems,
        filtersState = filtersState,
        contentVersion = contentVersion,
        effectFlow = viewModel.uiEffect,
        filterParams = activeParams,
        actions = DiscoverActions(
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
            onError = viewModel::onError,
            onFilterSelected = viewModel::onFilterSelect,
            onFilterReset = viewModel::onFilterReset,
            onFilterApply = viewModel::onFilterApply,
            onFilterOpen = viewModel::onFilterOpen,
            onRemoveFilter = viewModel::onRemoveFilter,
            onToggleFilterSort = viewModel::onToggleFilterSort,
            onSearchQueryChanged = viewModel::onSearchQueryChanged,
        ),
        scrollUp = viewModel.actions,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    pagingItems: LazyPagingItems<ArtworkListItem>,
    filtersState: FiltersUiState,
    contentVersion: Int,
    effectFlow: Flow<UiText>? = null,
    filterParams: FilterParams,
    actions: DiscoverActions,
    scrollUp: Flow<DiscoverEvent>,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showFilterSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val scope = rememberCoroutineScope()

    val onShowSnackbar: (UiText) -> Unit = { message ->
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message.asString(context))
        }
    }

    DiscoverScreenEffects(
        effectFlow = effectFlow,
        pagingItems = pagingItems,
        onShowSnackbar = onShowSnackbar,
        onError = actions.onError,
    )

    val isListReady = pagingItems.loadState.refresh !is LoadState.Loading
    val showFab = isListReady && filtersState.isAvailable

    // Scaffold handles system bars (Status/Nav Bar) automatically via contentWindowInsets
    Scaffold(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(bottom = if (showFab) 0.dp else 48.dp),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.statusBars,
        snackbarHost = { SnackbarHost(snackbarHostState) },

        floatingActionButton = {
            DiscoverFloatingButton(
                pagingItems = pagingItems,
                filtersState = filtersState,
                visible = showFab,
                onFilterClick = {
                    actions.onFilterOpen()
                    showFilterSheet = true
                },
            )
        },

    ) { innerPadding ->
        DiscoverScreenContent(
            pagingItems = pagingItems,
            contentVersion = contentVersion,
            innerPadding = innerPadding,
            onShowMessage = { onShowSnackbar(it) },
            filterParams = filterParams,
            actions = actions,
            scrollUp = scrollUp,
        )

        if (showFilterSheet) {
            FilterBottomSheet(
                sheetState = sheetState,
                filtersState = filtersState,
                onFilterSelected = actions.onFilterSelected,
                onReset = actions.onFilterReset,
                onDismiss = {
                    showFilterSheet = false
                    actions.onFilterApply()
                },
                onToggleSort = actions.onToggleFilterSort,
                onSearchQueryChanged = actions.onSearchQueryChanged,
            )
        }
    }
}

@Composable
private fun DiscoverScreenEffects(
    effectFlow: Flow<UiText>?,
    pagingItems: LazyPagingItems<ArtworkListItem>,
    onError: (Throwable) -> Unit,
    onShowSnackbar: (UiText) -> Unit,
) {
    LaunchedEffect(effectFlow) {
        effectFlow?.collect { message -> onShowSnackbar(message) }
    }

    LaunchedEffect(pagingItems.loadState) {
        val state = pagingItems.loadState
        (state.refresh as? LoadState.Error)?.error?.let { onError(it) }
        (state.append as? LoadState.Error)?.error?.let { onError(it) }
    }
}

@Composable
private fun DiscoverFloatingButton(
    pagingItems: LazyPagingItems<ArtworkListItem>,
    filtersState: FiltersUiState,
    visible: Boolean,
    onFilterClick: () -> Unit,
) {
    val isListReady = pagingItems.loadState.refresh !is LoadState.Loading
    val showFab = isListReady && filtersState.isAvailable

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(),
        exit = scaleOut(),
    ) {
        FilterFloatingActionButton(onFilterClick = onFilterClick)
    }
}

@Composable
private fun DiscoverScreenContent(
    pagingItems: LazyPagingItems<ArtworkListItem>,
    contentVersion: Int,
    innerPadding: PaddingValues,
    onShowMessage: (UiText) -> Unit,
    filterParams: FilterParams,
    actions: DiscoverActions,
    scrollUp: Flow<DiscoverEvent>,
) {
    val refreshState = pagingItems.loadState.refresh
    val isListEmpty = pagingItems.itemCount == 0

    val hasActiveFilters = filterParams.classification != null ||
        filterParams.century != null ||
        filterParams.culture != null

    when {
        !isListEmpty -> {
            ArtworksView(
                artworks = pagingItems,
                contentVersion = contentVersion,
                isRefreshing = refreshState is LoadState.Loading,
                contentPadding = innerPadding,
                onShowMessage = onShowMessage,
                filterParams = filterParams,
                actions = actions,
                scrollUp = scrollUp,
            )
        }

        refreshState is LoadState.Error -> {
            Box(modifier = Modifier.padding(innerPadding)) {
                ErrorView(onRetry = actions.onRetry)
            }
        }

        hasActiveFilters -> { // For filters if the result is empty
            ArtworksView(
                artworks = pagingItems,
                contentVersion = contentVersion,
                isRefreshing = refreshState is LoadState.Loading,
                contentPadding = innerPadding,
                onShowMessage = onShowMessage,
                filterParams = filterParams,
                actions = actions,
                scrollUp = scrollUp,
            )
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
            contentVersion = 0,
            effectFlow = null,
            filtersState = FiltersUiState(
                classifications = emptyList(),
                centuries = emptyList(),
                cultures = emptyList(),
                searchQuery = "",
                isAvailable = false,
            ),
            filterParams = FilterParams(),
            actions = DiscoverActions(
                onRefresh = { },
                onRetry = { },
                onArtworkClick = { id -> println("Clicked id: $id") },
                onError = { },
                onFilterSelected = { filterType: FilterType, query: String? -> },
                onFilterApply = { },
                onFilterReset = { },
                onFilterOpen = { },
                onRemoveFilter = { _ -> },
                onToggleFilterSort = { },
                onSearchQueryChanged = { },
            ),
            scrollUp = emptyFlow(),
        )
    }
}
