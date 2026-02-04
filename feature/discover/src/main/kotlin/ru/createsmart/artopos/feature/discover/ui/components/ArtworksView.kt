package ru.createsmart.artopos.feature.discover.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.createsmart.artopos.core.ui.theme.ArtoposTheme
import ru.createsmart.artopos.feature.discover.R
import ru.createsmart.artopos.feature.discover.model.ArtworkListItem

@Composable
fun ArtworksView(
    artworks: List<ArtworkListItem>,
    contentVersion: Int,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onArtworkClick: (Int) -> Unit,
    contentPadding: PaddingValues,
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
                Text(
                    text = stringResource(R.string.label_discover),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            items(
                items = artworks,
                // Critical for Performance: Helps Compose identify moved items instead of redrawing them
                key = { it.id },
            ) { artwork ->
                ArtworkCard(
                    artwork = artwork,
                    contentVersion = contentVersion,
                    onClick = { onArtworkClick(artwork.id) },
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun ArtworksViewPreview() {
    val mockData = listOf(
        ArtworkListItem(1, "Art 1", "Artist 1", "", 1f, "2020"),
        ArtworkListItem(2, "Art 2", "Artist 2", "", 2f, "2021"),
    )
    ArtoposTheme {
        ArtworksView(
            artworks = mockData,
            contentVersion = 0,
            isRefreshing = false,
            onRefresh = {},
            onArtworkClick = {},
            contentPadding = PaddingValues(0.dp),
        )
    }
}
