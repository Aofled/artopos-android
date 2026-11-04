package ru.createsmart.artopos.feature.search.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.Flow
import ru.createsmart.artopos.core.artworkcard.model.ArtworkListItem
import ru.createsmart.artopos.core.designsystem.components.UiText
import ru.createsmart.artopos.core.filter.FilterBottomSheet
import ru.createsmart.artopos.core.uicomponents.components.CustomCircularProgressIndicator
import ru.createsmart.artopos.feature.search.SearchViewModel
import ru.createsmart.artopos.feature.search.model.SearchEvent
import ru.createsmart.artopos.feature.search.model.SearchIntent
import ru.createsmart.artopos.feature.search.model.SearchUiState
import ru.createsmart.artopos.feature.search.ui.components.ActiveFiltersRow
import ru.createsmart.artopos.feature.search.ui.components.SearchFullScreenError
import ru.createsmart.artopos.feature.search.ui.components.SearchInitialView
import ru.createsmart.artopos.feature.search.ui.components.SearchNotFoundView
import ru.createsmart.artopos.feature.search.ui.components.SearchResultsGrid
import ru.createsmart.artopos.feature.search.ui.components.SearchTopBar

@Composable
fun SearchRoute(
    viewModel: SearchViewModel = hiltViewModel(),
    onArtworkClick: (Int) -> Unit,
    onShowSnackbar: (UiText) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagingItems = viewModel.artworksFlow.collectAsLazyPagingItems()

    SearchScreen(
        uiState = uiState,
        pagingItems = pagingItems,
        effectFlow = viewModel.uiEffect,
        scrollUp = viewModel.actions,
        onArtworkClick = onArtworkClick,
        onShowSnackbar = onShowSnackbar,
        onIntent = { intent ->
            when (intent) {
                is SearchIntent.Refresh -> {
                    pagingItems.refresh()
                    viewModel.onIntent(intent)
                }
                is SearchIntent.Retry -> {
                    pagingItems.retry()
                    viewModel.onIntent(intent)
                }
                else -> viewModel.onIntent(intent)
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScreen(
    uiState: SearchUiState,
    pagingItems: LazyPagingItems<ArtworkListItem>,
    effectFlow: Flow<UiText>?,
    scrollUp: Flow<SearchEvent>,
    onArtworkClick: (Int) -> Unit,
    onShowSnackbar: (UiText) -> Unit,
    onIntent: (SearchIntent) -> Unit,
) {
    var showFilterSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Handle snackbars and pagination errors
    SearchScreenEffects(
        effectFlow = effectFlow,
        pagingItems = pagingItems,
        onShowSnackbar = onShowSnackbar,
        onIntent = onIntent,
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.statusBars,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Top search bar and filter button
            SearchTopBar(
                query = uiState.searchQuery,
                hasActiveFilters = uiState.hasActiveFilters,
                onQueryChanged = { onIntent(SearchIntent.QueryChanged(it)) },
                onClearQuery = { onIntent(SearchIntent.ClearQuery) },
                onFilterClick = {
                    onIntent(SearchIntent.FilterOpen)
                    showFilterSheet = true
                },
            )

            // Active filter chips
            if (uiState.hasActiveFilters) {
                ActiveFiltersRow(
                    activeFilters = uiState.activeFilters,
                    onRemoveFilter = { onIntent(SearchIntent.RemoveActiveFilter(it)) },
                )
            }

            // Screen state switcher
            SearchScreenContent(
                uiState = uiState,
                pagingItems = pagingItems,
                scrollUp = scrollUp,
                onArtworkClick = onArtworkClick,
                onShowSnackbar = onShowSnackbar,
                onIntent = onIntent,
                modifier = Modifier.weight(1f),
            )
        }

        // Filter bottom sheet
        if (showFilterSheet) {
            FilterBottomSheet(
                sheetState = sheetState,
                filtersState = uiState.filtersUiState,
                onFilterSelected = { type, value ->
                    onIntent(SearchIntent.FilterSelected(type, value))
                },
                onSearchQueryChanged = { onIntent(SearchIntent.FilterSearchQueryChanged(it)) },
                onReset = { onIntent(SearchIntent.FilterReset) },
                onToggleSort = { onIntent(SearchIntent.ToggleFilterSort) },
                onDismiss = {
                    showFilterSheet = false
                    onIntent(SearchIntent.FilterSearchQueryChanged(""))
                    onIntent(SearchIntent.FilterApply)
                },
            )
        }
    }
}

@Composable
private fun SearchScreenContent(
    uiState: SearchUiState,
    pagingItems: LazyPagingItems<ArtworkListItem>,
    scrollUp: Flow<SearchEvent>,
    onArtworkClick: (Int) -> Unit,
    onShowSnackbar: (UiText) -> Unit,
    onIntent: (SearchIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isQueryBlank = uiState.searchQuery.isBlank()
    val refreshState = pagingItems.loadState.refresh
    val itemCount = pagingItems.itemCount

    val isFullScreenLoading = refreshState is LoadState.Loading && itemCount == 0
    val isFullScreenError = refreshState is LoadState.Error && itemCount == 0
    val isEmptyResult = itemCount == 0 && refreshState is LoadState.NotLoading && !isQueryBlank

    Box(modifier = modifier.fillMaxSize()) {
        when {
            // 1. Initial state: empty query
            isQueryBlank -> {
                SearchInitialView()
            }

            // 2. Initial loading
            isFullScreenLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CustomCircularProgressIndicator()
                }
            }

            // 3. Network error
            isFullScreenError -> {
                SearchFullScreenError(onRetry = { onIntent(SearchIntent.Retry) })
            }

            // 4. Empty search result
            isEmptyResult -> {
                SearchNotFoundView(query = uiState.searchQuery)
            }

            // 5. Results grid
            else -> {
                SearchResultsGrid(
                    artworks = pagingItems,
                    favoriteArtworkIds = uiState.favoriteArtworkIds,
                    contentVersion = uiState.contentVersion,
                    scrollUp = scrollUp,
                    onArtworkClick = onArtworkClick,
                    onShowMessage = onShowSnackbar,
                    onIntent = onIntent,
                )
            }
        }
    }
}

@Composable
private fun SearchScreenEffects(
    effectFlow: Flow<UiText>?,
    pagingItems: LazyPagingItems<ArtworkListItem>,
    onShowSnackbar: (UiText) -> Unit,
    onIntent: (SearchIntent) -> Unit,
) {
    LaunchedEffect(effectFlow) {
        effectFlow?.collect { message -> onShowSnackbar(message) }
    }

    LaunchedEffect(pagingItems.loadState) {
        val state = pagingItems.loadState
        val refreshError = (state.refresh as? LoadState.Error)?.error
        val appendError = (state.append as? LoadState.Error)?.error

        refreshError?.let { onIntent(SearchIntent.ErrorOccurred(it)) }
        appendError?.let { onIntent(SearchIntent.ErrorOccurred(it)) }
    }
}
