package ru.createsmart.artopos.feature.discover.ui.components

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch
import ru.createsmart.artopos.core.artworkcard.model.ArtworkListItem
import ru.createsmart.artopos.core.artworkcard.ui.components.ArtworkCard
import ru.createsmart.artopos.core.common.util.DictionaryHelper
import ru.createsmart.artopos.core.designsystem.components.UiText
import ru.createsmart.artopos.core.designsystem.theme.ArtoposDimens
import ru.createsmart.artopos.core.designsystem.theme.ArtoposTheme
import ru.createsmart.artopos.core.model.FilterParams
import ru.createsmart.artopos.core.model.FilterType
import ru.createsmart.artopos.core.uicomponents.components.CustomCircularProgressIndicator
import ru.createsmart.artopos.core.uicomponents.components.CustomInputChip
import ru.createsmart.artopos.core.uicomponents.notifiers.BottomBarCommand
import ru.createsmart.artopos.core.uicomponents.notifiers.LocalBottomBarStateNotifier
import ru.createsmart.artopos.feature.discover.model.DiscoverEvent
import ru.createsmart.artopos.feature.discover.model.DiscoverIntent
import ru.createsmart.artopos.core.designsystem.R as DSR

@Composable
internal fun ArtworksView(
    artworks: LazyPagingItems<ArtworkListItem>,
    contentVersion: Int,
    isRefreshing: Boolean,
    contentPadding: PaddingValues,
    onShowMessage: (UiText) -> Unit,
    filterParams: FilterParams,
    scrollUp: Flow<DiscoverEvent>,
    onIntent: (DiscoverIntent) -> Unit,
) {
    val pullToRefreshState = rememberPullToRefreshState()

    // Material 3 Pull-to-Refresh container
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { onIntent(DiscoverIntent.Refresh) },
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
            scrollUp = scrollUp,
            onIntent = onIntent,
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
    scrollUp: Flow<DiscoverEvent>,
    onIntent: (DiscoverIntent) -> Unit,
) {
    val listState = rememberLazyStaggeredGridState()
    val bottomBarNotifier = LocalBottomBarStateNotifier.current
    val coroutineScope = rememberCoroutineScope()

    val isEmptyResult = artworks.itemCount == 0 && artworks.loadState.refresh !is LoadState.Loading

    val navBarsHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val isAtBottom by remember {
        derivedStateOf { !listState.canScrollForward }
    }

    // We don't hide the navigation if the list has reached the end or the screen is not scrollable.
    LaunchedEffect(isAtBottom) {
        if (isAtBottom) {
            bottomBarNotifier(BottomBarCommand.LOCK_AT_BOTTOM)
        } else {
            bottomBarNotifier(BottomBarCommand.UNLOCK)
        }
    }

    LaunchedEffect(scrollUp) { // Auto-scroll up when data is updated
        scrollUp.collect { action ->
            when (action) {
                is DiscoverEvent.ScrollToTop -> {
                    bottomBarNotifier(BottomBarCommand.SHOW)
                    listState.scrollToItem(0)
                }
            }
        }
    }

    val canScrollUp by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 } // If below the first element
    }

    BackHandler(enabled = canScrollUp) {
        coroutineScope.launch {
            bottomBarNotifier(BottomBarCommand.SHOW)
            listState.scrollToItem(0)
        }
    }

    val gridPadding = PaddingValues(
        // Add System Bars padding + extra spacing for design
        top = contentPadding.calculateTopPadding() + 8.dp,
        bottom = navBarsHeight + ArtoposDimens.BottomBarHeight + 16.dp,
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
            FavoritesHeader(
                filterParams = filterParams,
                onRemoveFilter = { onIntent(DiscoverIntent.RemoveFilter(it)) },
            )
        }

        if (isEmptyResult) {
            item(span = StaggeredGridItemSpan.FullLine) { EmptyDataView() }
        } else {
            artworkItems(
                artworks = artworks,
                contentVersion = contentVersion,
                onArtworkClick = { onIntent(DiscoverIntent.ArtworkClicked(it)) },
                onFavoriteClick = { onIntent(DiscoverIntent.ToggleFavorite(it)) },
                onShowMessage = onShowMessage,
            )
        }

        pagingFooters(
            loadState = artworks.loadState.append,
            onRetry = { onIntent(DiscoverIntent.Retry) },
        )
    }
}

@Composable
private fun FavoritesHeader(
    filterParams: FilterParams,
    onRemoveFilter: (FilterType) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(DSR.string.core_title_discover),
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

private fun LazyStaggeredGridScope.artworkItems(
    artworks: LazyPagingItems<ArtworkListItem>,
    contentVersion: Int,
    onArtworkClick: (Int) -> Unit,
    onFavoriteClick: (Int) -> Unit,
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
                onFavoriteClick = { onFavoriteClick(artwork.id) },
                onShowMessage = onShowMessage,
                modifier = Modifier.fillMaxWidth(),
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
                text = stringResource(DSR.string.core_cd_retry),
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
        DictionaryHelper.getLocalizedName(context, rawName)
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
        ArtworkListItem(
            1,
            UiText.DynamicString("Art 1"),
            UiText.DynamicString("Artist 1"),
            "",
            1f,
            UiText.DynamicString("2020"),
            true,
        ),
        ArtworkListItem(
            2,
            UiText.DynamicString("Art 2"),
            UiText.DynamicString("Artist 2"),
            "",
            2f,
            UiText.DynamicString("2021"),
            false,
        ),
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
            onIntent = { },
            scrollUp = emptyFlow(),
        )
    }
}
