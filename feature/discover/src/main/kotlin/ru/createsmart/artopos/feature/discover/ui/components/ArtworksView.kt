package ru.createsmart.artopos.feature.discover.ui.components

import UiText
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import ru.createsmart.artopos.core.model.FilterParams
import ru.createsmart.artopos.core.model.FilterType
import ru.createsmart.artopos.core.ui.theme.ArtoposTheme
import ru.createsmart.artopos.core.ui.theme.components.CustomCircularProgressIndicator
import ru.createsmart.artopos.core.ui.theme.components.CustomInputChip
import ru.createsmart.artopos.core.ui.theme.util.FilterNameHelper
import ru.createsmart.artopos.feature.discover.R
import ru.createsmart.artopos.feature.discover.model.ArtworkListItem
import ru.createsmart.artopos.feature.discover.model.DiscoverActions
import ru.createsmart.artopos.feature.discover.model.DiscoverEvent
import ru.createsmart.artopos.core.ui.R as UiR

@Composable
fun ArtworksView(
    artworks: LazyPagingItems<ArtworkListItem>,
    contentVersion: Int,
    isRefreshing: Boolean,
    contentPadding: PaddingValues,
    onShowMessage: (UiText) -> Unit,
    filterParams: FilterParams,
    actions: DiscoverActions,
    scrollUp: Flow<DiscoverEvent>,
) {
    val pullToRefreshState = rememberPullToRefreshState()

    // Material 3 Pull-to-Refresh container
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = actions.onRefresh,
        state = pullToRefreshState,
        modifier = Modifier.fillMaxSize(),
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullToRefreshState,
                isRefreshing = isRefreshing,
                containerColor = MaterialTheme.colorScheme.surface,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        },
    ) {
        ArtworksGrid(
            artworks = artworks,
            contentVersion = contentVersion,
            contentPadding = contentPadding,
            onShowMessage = onShowMessage,
            filterParams = filterParams,
            actions = actions,
            scrollUp = scrollUp,
        )
    }
}

@Composable
private fun ArtworksGrid(
    artworks: LazyPagingItems<ArtworkListItem>,
    contentVersion: Int,
    contentPadding: PaddingValues,
    onShowMessage: (UiText) -> Unit,
    filterParams: FilterParams,
    actions: DiscoverActions,
    scrollUp: Flow<DiscoverEvent>,
) {
    val listState = rememberLazyStaggeredGridState()
    val isEmptyResult = artworks.itemCount == 0 && artworks.loadState.refresh !is LoadState.Loading

    LaunchedEffect(artworks.loadState.refresh) { // Auto-scroll up when data is updated
        scrollUp.collect { action ->
            when (action) {
                is DiscoverEvent.ScrollToTop -> {
                    listState.scrollToItem(0)
                }
            }
        }
    }

    val gridPadding = PaddingValues( // Add System Bars padding + extra spacing for design
        top = contentPadding.calculateTopPadding() + 8.dp,
        bottom = contentPadding.calculateBottomPadding() + 8.dp,
        start = 16.dp,
        end = 16.dp,
    )

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        state = listState,
        contentPadding = gridPadding,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalItemSpacing = 16.dp,
        modifier = Modifier.fillMaxSize(),
    ) {
        item(span = StaggeredGridItemSpan.FullLine) {
            TopAppBar(filterParams = filterParams, onRemoveFilter = actions.onRemoveFilter)
        }

        if (isEmptyResult) {
            item(span = StaggeredGridItemSpan.FullLine) { EmptyDataView() }
        } else {
            artworkItems(
                artworks = artworks,
                contentVersion = contentVersion,
                onArtworkClick = actions.onArtworkClick,
                onShowMessage = onShowMessage,
            )
        }

        pagingFooters(
            loadState = artworks.loadState.append,
            onRetry = actions.onRetry,
        )
    }
}

@Composable
private fun TopAppBar(
    filterParams: FilterParams,
    onRemoveFilter: (FilterType) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.label_discover),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp),
        ) {
            if (filterParams.classification != null) {
                item {
                    ActiveFilterChip(
                        rawName = filterParams.classification!!,
                        onRemove = { onRemoveFilter(FilterType.CLASSIFICATION) },
                    )
                }
            }

            if (filterParams.century != null) {
                item {
                    ActiveFilterChip(
                        rawName = filterParams.century!!,
                        onRemove = { onRemoveFilter(FilterType.CENTURY) },
                    )
                }
            }

            if (filterParams.culture != null) {
                item {
                    ActiveFilterChip(
                        rawName = filterParams.culture!!,
                        onRemove = { onRemoveFilter(FilterType.CULTURE) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyDataView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(UiR.string.label_no_results),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(UiR.string.incorrect_filters),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun LazyStaggeredGridScope.artworkItems(
    artworks: LazyPagingItems<ArtworkListItem>,
    contentVersion: Int,
    onArtworkClick: (Int) -> Unit,
    onShowMessage: (UiText) -> Unit,
) {
    items(
        count = artworks.itemCount,
        // Optimization: Stable keys prevent unnecessary recompositions when new pages load.
        key = artworks.itemKey { it.id },
        contentType = artworks.itemContentType { "artwork" },
    ) { index ->
        val artwork = artworks[index]
        if (artwork != null) {
            ArtworkCard(
                artwork = artwork,
                contentVersion = contentVersion,
                onClick = { onArtworkClick(artwork.id) },
                onShowMessage = onShowMessage,
            )
        }
    }
}

private fun LazyStaggeredGridScope.pagingFooters(
    loadState: LoadState,
    onRetry: () -> Unit,
) {
    if (loadState is LoadState.Loading) {
        item(span = StaggeredGridItemSpan.FullLine) {
            BottomProgress()
        }
    }

    if (loadState is LoadState.Error) {
        item(span = StaggeredGridItemSpan.FullLine) {
            ErrorFooter(onRetry = onRetry)
        }
    }
}

@Composable
private fun BottomProgress() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        CustomCircularProgressIndicator()
    }
}

@Composable
private fun ErrorFooter(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Button(
            onClick = onRetry,
        ) {
            Text(
                text = stringResource(UiR.string.description_btn_retry),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveFilterChip(
    rawName: String,
    onRemove: () -> Unit,
) {
    val context = LocalContext.current

    val localizedName = remember(rawName) {
        FilterNameHelper.getLocalizedName(context, rawName)
    }

    CustomInputChip(
        selected = true,
        onClick = onRemove,
        label = { Text(localizedName) },
    )
}

@Preview(showBackground = true, name = "Discover", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun ArtworksViewPreview() {
    val mockData = listOf(
        ArtworkListItem(1, "Art 1", "Artist 1", "", 1f, "2020"),
        ArtworkListItem(2, "Art 2", "Artist 2", "", 2f, "2021"),
    )

    val artworks = flowOf(PagingData.from(mockData)).collectAsLazyPagingItems()

    ArtoposTheme {
        ArtworksView(
            artworks = artworks,
            contentVersion = 0,
            isRefreshing = false,
            contentPadding = PaddingValues(0.dp),
            onShowMessage = { },
            filterParams = FilterParams(),
            actions = DiscoverActions(
                onRefresh = {},
                onRetry = {},
                onArtworkClick = {},
                onError = {},
                onFilterSelected = { _, _ -> },
                onFilterApply = {},
                onFilterReset = {},
                onFilterOpen = {},
                onRemoveFilter = { _ -> },
                onToggleFilterSort = {},
                onSearchQueryChanged = { },
            ),
            scrollUp = emptyFlow(),
        )
    }
}
