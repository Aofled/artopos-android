package ru.createsmart.artopos.feature.search.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import ru.createsmart.artopos.core.artworkcard.components.ArtworkCard
import ru.createsmart.artopos.core.artworkcard.model.ArtworkListItem
import ru.createsmart.artopos.core.designsystem.components.UiText
import ru.createsmart.artopos.core.designsystem.theme.ArtoposDimens
import ru.createsmart.artopos.core.navigation.SearchRoute
import ru.createsmart.artopos.core.uicomponents.notifiers.BottomBarCommand
import ru.createsmart.artopos.core.uicomponents.notifiers.LocalBottomBarStateNotifier
import ru.createsmart.artopos.core.uicomponents.notifiers.LocalTabReselectFlow
import ru.createsmart.artopos.feature.search.model.SearchEvent
import ru.createsmart.artopos.feature.search.model.SearchIntent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsGrid(
    artworks: LazyPagingItems<ArtworkListItem>,
    favoriteArtworkIds: Set<Int>,
    contentVersion: Int,
    scrollUp: Flow<SearchEvent>,
    onArtworkClick: (Int) -> Unit,
    onShowMessage: (UiText) -> Unit,
    onIntent: (SearchIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyStaggeredGridState()
    val bottomBarNotifier = LocalBottomBarStateNotifier.current
    val tabReselectFlow = LocalTabReselectFlow.current
    val coroutineScope = rememberCoroutineScope()
    val pullToRefreshState = rememberPullToRefreshState()

    val isRefreshing = artworks.loadState.refresh is LoadState.Loading

    var pendingScrollToTop by remember { mutableStateOf(false) }

    val isAtBottom by remember {
        derivedStateOf { !listState.canScrollForward }
    }

    LaunchedEffect(tabReselectFlow) {
        tabReselectFlow.collect { route ->
            if (route == SearchRoute) {
                bottomBarNotifier(BottomBarCommand.SHOW)
                listState.animateScrollToItem(0)
            }
        }
    }

    LaunchedEffect(isAtBottom) {
        if (isAtBottom) {
            bottomBarNotifier(BottomBarCommand.LOCK_AT_BOTTOM)
        } else {
            bottomBarNotifier(BottomBarCommand.UNLOCK)
        }
    }

    LaunchedEffect(scrollUp) {
        scrollUp.collect {
            pendingScrollToTop = true
        }
    }

    LaunchedEffect(artworks.loadState.refresh) {
        if (artworks.loadState.refresh is LoadState.NotLoading && pendingScrollToTop) {
            bottomBarNotifier(BottomBarCommand.SHOW)
            listState.scrollToItem(0)
            pendingScrollToTop = false
        }
    }

    val canScrollUp by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }
    BackHandler(enabled = canScrollUp) {
        coroutineScope.launch {
            bottomBarNotifier(BottomBarCommand.SHOW)
            listState.scrollToItem(0)
        }
    }

    val navBarsHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val gridPadding = PaddingValues(
        top = 8.dp,
        bottom = navBarsHeight + ArtoposDimens.BottomBarHeight + 16.dp,
        start = 16.dp,
        end = 16.dp,
    )

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { onIntent(SearchIntent.Refresh) },
        state = pullToRefreshState,
        modifier = modifier.fillMaxSize(),
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
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            state = listState,
            contentPadding = gridPadding,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalItemSpacing = 16.dp,
            modifier = Modifier.fillMaxSize(),
        ) {
            items(
                count = artworks.itemCount,
                key = artworks.itemKey { it.id },
                contentType = artworks.itemContentType { "artwork" },
            ) { index ->
                val artwork = artworks[index]
                if (artwork != null) {
                    // Check if artwork is favorite
                    val isFavorite = artwork.id in favoriteArtworkIds

                    ArtworkCard(
                        artwork = artwork.copy(isFavorite = isFavorite),
                        contentVersion = contentVersion,
                        onClick = { onArtworkClick(artwork.id) },
                        onFavoriteClick = { onIntent(SearchIntent.ToggleFavorite(artwork.id)) },
                        onShowMessage = onShowMessage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(),
                    )
                }
            }

            searchPagingFooters(
                loadState = artworks.loadState.append,
                onRetry = { onIntent(SearchIntent.Retry) },
            )
        }
    }
}
