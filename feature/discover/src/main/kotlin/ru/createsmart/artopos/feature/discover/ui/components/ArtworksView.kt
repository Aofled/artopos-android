package ru.createsmart.artopos.feature.discover.ui.components

import UiText
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import kotlinx.coroutines.flow.flowOf
import ru.createsmart.artopos.core.ui.theme.ArtoposTheme
import ru.createsmart.artopos.feature.discover.R
import ru.createsmart.artopos.feature.discover.model.ArtworkListItem
import ru.createsmart.artopos.core.ui.R as UiR

@Composable
fun ArtworksView(
    artworks: LazyPagingItems<ArtworkListItem>,
    contentVersion: Int,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onArtworkClick: (Int) -> Unit,
    contentPadding: PaddingValues,
    onShowMessage: (UiText) -> Unit,
) {
    val pullToRefreshState = rememberPullToRefreshState()

    // Material 3 Pull-to-Refresh container
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = pullToRefreshState,
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            contentPadding = PaddingValues(
                // Add System Bars padding + extra spacing for design
                top = contentPadding.calculateTopPadding() + 8.dp,
                bottom = contentPadding.calculateBottomPadding() + 8.dp,
                start = 16.dp,
                end = 16.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalItemSpacing = 16.dp,
            modifier = Modifier.fillMaxSize(),
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                TopAppBar()
            }
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

            if (artworks.loadState.append is LoadState.Loading) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    BottomProgress()
                }
            }

            if (artworks.loadState.append is LoadState.Error) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    ErrorFooter(onRetry = onRetry)
                }
            }
        }
    }
}

@Composable
private fun TopAppBar() {
    Text(
        text = stringResource(R.string.label_discover),
        style = MaterialTheme.typography.displaySmall,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
private fun BottomProgress() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Composable
private fun ErrorFooter(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Button(
            onClick = onRetry,
        ) {
            Text(
                text = stringResource(UiR.string.btn_retry),
            )
        }
    }
}

@Preview(showBackground = true, name = "Discover States", uiMode = Configuration.UI_MODE_NIGHT_NO)
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
            onRefresh = { },
            onRetry = { },
            onArtworkClick = { },
            onShowMessage = { },
            contentPadding = PaddingValues(0.dp),
        )
    }
}
