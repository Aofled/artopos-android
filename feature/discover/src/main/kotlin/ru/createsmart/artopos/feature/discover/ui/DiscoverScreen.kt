package ru.createsmart.artopos.feature.discover.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import ru.createsmart.artopos.core.artworkcard.model.ArtworkListItem
import ru.createsmart.artopos.core.designsystem.components.UiText
import ru.createsmart.artopos.core.designsystem.theme.ArtoposDimens
import ru.createsmart.artopos.core.designsystem.theme.ArtoposTheme
import ru.createsmart.artopos.core.model.FilterParams
import ru.createsmart.artopos.core.uicomponents.components.FilterFloatingActionButton
import ru.createsmart.artopos.core.uicomponents.notifiers.LocalBottomBarVisibility
import ru.createsmart.artopos.feature.discover.DiscoverViewModel
import ru.createsmart.artopos.feature.discover.model.DiscoverEvent
import ru.createsmart.artopos.feature.discover.model.DiscoverIntent
import ru.createsmart.artopos.feature.discover.model.FilterListItem
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
    onShowSnackbar: (UiText) -> Unit,
    onUpdateFabVisibility: (Boolean) -> Unit,
) {
    val pagingItems = viewModel.artworksFlow.collectAsLazyPagingItems()
    val contentVersion by viewModel.contentVersion.collectAsStateWithLifecycle()
    val filtersState by viewModel.filtersUiState.collectAsStateWithLifecycle()
    val activeParams by viewModel.activeFilterParams.collectAsStateWithLifecycle()

    // 1. Check if screen is in full screen error or initial loading state
    val isListEmpty = pagingItems.itemCount == 0
    val isFullScreenError = pagingItems.loadState.refresh is LoadState.Error && isListEmpty
    val isFullScreenLoading = pagingItems.loadState.refresh is LoadState.Loading && isListEmpty

    // 2. FAB is active only when filterable data is present
    val showFab = filtersState.isAvailable && !isFullScreenError && !isFullScreenLoading

    LaunchedEffect(showFab) {
        onUpdateFabVisibility(showFab)
    }

    DiscoverScreen(
        pagingItems = pagingItems,
        filtersState = filtersState,
        contentVersion = contentVersion,
        effectFlow = viewModel.uiEffect,
        filterParams = activeParams,
        scrollUp = viewModel.actions,
        showFab = showFab,
        onArtworkClick = onArtworkClick,
        onShowSnackbar = onShowSnackbar,
        onIntent = { intent ->
            when (intent) {
                is DiscoverIntent.Refresh -> {
                    pagingItems.refresh()
                    viewModel.onIntent(intent)
                }
                is DiscoverIntent.Retry -> {
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
private fun DiscoverScreen(
    pagingItems: LazyPagingItems<ArtworkListItem>,
    filtersState: FiltersUiState,
    contentVersion: Int,
    effectFlow: Flow<UiText>?,
    filterParams: FilterParams,
    scrollUp: Flow<DiscoverEvent>,
    showFab: Boolean,
    onArtworkClick: (Int) -> Unit,
    onShowSnackbar: (UiText) -> Unit,
    onIntent: (DiscoverIntent) -> Unit,
) {
    var showFilterSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    DiscoverScreenEffects(
        effectFlow = effectFlow,
        pagingItems = pagingItems,
        onShowSnackbar = onShowSnackbar,
        onIntent = onIntent,
    )

    val isBottomBarVisible = LocalBottomBarVisibility.current

    // Animate FAB vertical offset to avoid overlapping with the BottomBar
    val fabBottomPadding by animateDpAsState(
        targetValue = if (isBottomBarVisible) ArtoposDimens.BottomBarHeight else 0.dp,
        animationSpec = tween(
            durationMillis = ArtoposDimens.BOTTOM_BAR_ANIMATION_DURATION,
            easing = FastOutSlowInEasing,
        ),
        label = "fabPadding",
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.statusBars,
            floatingActionButton = {
                DiscoverFloatingButton(
                    visible = showFab,
                    onFilterClick = {
                        onIntent(DiscoverIntent.FilterOpen)
                        showFilterSheet = true
                    },
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(bottom = fabBottomPadding),
                )
            },
        ) { innerPadding ->
            DiscoverScreenContent(
                pagingItems = pagingItems,
                contentVersion = contentVersion,
                innerPadding = innerPadding,
                onShowMessage = onShowSnackbar,
                filterParams = filterParams,
                scrollUp = scrollUp,
                onArtworkClick = onArtworkClick,
                onIntent = onIntent,
            )
        }

        if (showFilterSheet) {
            FilterBottomSheet(
                sheetState = sheetState,
                filtersState = filtersState,
                onFilterSelected = { type, value ->
                    onIntent(DiscoverIntent.FilterSelected(type, value))
                },
                onSearchQueryChanged = { query -> onIntent(DiscoverIntent.SearchQueryChanged(query)) },
                onReset = { onIntent(DiscoverIntent.FilterReset) },
                onToggleSort = { onIntent(DiscoverIntent.ToggleFilterSort) },
                onDismiss = {
                    showFilterSheet = false
                    onIntent(DiscoverIntent.SearchQueryChanged(""))
                    onIntent(DiscoverIntent.FilterApply)
                },
            )
        }
    }
}

@Composable
private fun DiscoverScreenEffects(
    effectFlow: Flow<UiText>?,
    pagingItems: LazyPagingItems<ArtworkListItem>,
    onShowSnackbar: (UiText) -> Unit,
    onIntent: (DiscoverIntent) -> Unit,
) {
    LaunchedEffect(effectFlow) {
        effectFlow?.collect { message -> onShowSnackbar(message) }
    }

    LaunchedEffect(pagingItems.loadState) {
        val state = pagingItems.loadState
        val refreshError = (state.refresh as? LoadState.Error)?.error
        val appendError = (state.append as? LoadState.Error)?.error

        refreshError?.let { onIntent(DiscoverIntent.ErrorOccurred(it)) }
        appendError?.let { onIntent(DiscoverIntent.ErrorOccurred(it)) }
    }
}

@Composable
private fun DiscoverFloatingButton(
    visible: Boolean,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(),
        exit = scaleOut(),
        modifier = modifier,
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
    scrollUp: Flow<DiscoverEvent>,
    onArtworkClick: (Int) -> Unit,
    onIntent: (DiscoverIntent) -> Unit,
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
                scrollUp = scrollUp,
                onArtworkClick = onArtworkClick,
                onIntent = onIntent,
            )
        }

        refreshState is LoadState.Error -> {
            Box(modifier = Modifier.padding(innerPadding)) {
                ErrorView(onRetry = { onIntent(DiscoverIntent.Retry) })
            }
        }
        hasActiveFilters -> {
            ArtworksView(
                artworks = pagingItems,
                contentVersion = contentVersion,
                isRefreshing = refreshState is LoadState.Loading,
                contentPadding = innerPadding,
                onShowMessage = onShowMessage,
                filterParams = filterParams,
                scrollUp = scrollUp,
                onArtworkClick = onArtworkClick,
                onIntent = onIntent,
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
private fun DiscoverScreenPreview(
    @PreviewParameter(DiscoverStateProvider::class) pagingFlow: Flow<PagingData<ArtworkListItem>>,
) {
    val pagingItems = pagingFlow.collectAsLazyPagingItems()
    ArtoposTheme {
        DiscoverScreen(
            pagingItems = pagingItems,
            contentVersion = 0,
            effectFlow = null,
            filtersState = FiltersUiState(
                classifications = persistentListOf<FilterListItem>(),
                centuries = persistentListOf<FilterListItem>(),
                cultures = persistentListOf<FilterListItem>(),
                searchQuery = "",
                isAvailable = false,
            ),
            filterParams = FilterParams(),
            showFab = false,
            onArtworkClick = { },
            onShowSnackbar = { },
            onIntent = { },
            scrollUp = emptyFlow(),
        )
    }
}
