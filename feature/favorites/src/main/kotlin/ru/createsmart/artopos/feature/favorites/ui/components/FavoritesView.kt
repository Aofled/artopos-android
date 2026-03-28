package ru.createsmart.artopos.feature.favorites.ui.components

import UiText
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.createsmart.artopos.core.designsystem.theme.ArtoposTheme
import ru.createsmart.artopos.feature.artworkcard.model.ArtworkListItem
import ru.createsmart.artopos.feature.artworkcard.ui.components.ArtworkCard
import ru.createsmart.artopos.feature.favorites.R
import ru.createsmart.artopos.feature.favorites.model.FavoritesActions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesView(
    artworks: List<ArtworkListItem>,
    contentVersion: Int,
    isRefreshing: Boolean,
    actions: FavoritesActions,
    onShowMessage: (UiText) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pullState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = actions.onRefresh,
        state = pullState,
        modifier = modifier,
    ) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            contentPadding = PaddingValues(
                top = 8.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 48.dp,
                start = 16.dp,
                end = 16.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalItemSpacing = 16.dp,
            modifier = Modifier.fillMaxSize(),
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                FavoritesHeader()
            }

            items(
                items = artworks,
                key = { it.id },
            ) { artwork ->
                ArtworkCard(
                    artwork = artwork,
                    contentVersion = contentVersion,
                    onClick = { actions.onArtworkClick(artwork.id) },
                    onFavoriteClick = { actions.onToggleFavorite(artwork.id) },
                    onShowMessage = onShowMessage,
                )
            }
        }
    }
}

@Composable
private fun FavoritesHeader() {
    Text(
        text = stringResource(R.string.label_favorites),
        style = MaterialTheme.typography.displaySmall,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Preview(
    showBackground = true,
    name = "Favorites",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Composable
private fun FavoritesViewPreview() {
    val mockArtworks = listOf(
        ArtworkListItem(1, "Art 1", "Artist 1", "", 1f, "2020", true),
        ArtworkListItem(2, "Art 2", "Artist 2", "", 2f, "2021", true),
    )

    ArtoposTheme {
        FavoritesView(
            artworks = mockArtworks,
            contentVersion = 0,
            isRefreshing = false,
            onShowMessage = { },
            actions = FavoritesActions(
                onRefresh = { },
                onArtworkClick = { },
                onToggleFavorite = { },
            ),
        )
    }
}
