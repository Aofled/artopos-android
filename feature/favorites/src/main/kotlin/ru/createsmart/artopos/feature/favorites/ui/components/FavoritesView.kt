package ru.createsmart.artopos.feature.favorites.ui.components

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.createsmart.artopos.core.designsystem.components.UiText
import ru.createsmart.artopos.core.designsystem.theme.ArtoposTheme
import ru.createsmart.artopos.core.uicomponents.notifiers.BottomBarCommand
import ru.createsmart.artopos.core.uicomponents.notifiers.LocalBottomBarStateNotifier
import ru.createsmart.artopos.feature.artworkcard.model.ArtworkListItem
import ru.createsmart.artopos.feature.artworkcard.ui.components.ArtworkCard
import ru.createsmart.artopos.feature.favorites.model.FavoritesIntent
import ru.createsmart.artopos.core.designsystem.R as DSR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FavoritesView(
    artworks: List<ArtworkListItem>,
    contentVersion: Int,
    isRefreshing: Boolean,
    onIntent: (FavoritesIntent) -> Unit,
    onShowMessage: (UiText) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pullState = rememberPullToRefreshState()

    val listState = rememberLazyStaggeredGridState()
    val bottomBarNotifier = LocalBottomBarStateNotifier.current
    val coroutineScope = rememberCoroutineScope()

    val isAtBottom by remember(artworks.size) {
        derivedStateOf {
            !listState.canScrollForward
        }
    }

    // We don't hide the navigation if the list has reached the end or the screen is not scrollable.
    LaunchedEffect(isAtBottom) {
        if (isAtBottom) {
            bottomBarNotifier(BottomBarCommand.LOCK_AT_BOTTOM)
        } else {
            bottomBarNotifier(BottomBarCommand.UNLOCK)
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

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { onIntent(FavoritesIntent.Refresh) },
        state = pullState,
        modifier = modifier,
    ) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            state = listState,
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
                    onClick = { onIntent(FavoritesIntent.ArtworkClick(artwork.id)) },
                    onFavoriteClick = { onIntent(FavoritesIntent.ToggleFavorite(artwork.id)) },
                    onShowMessage = onShowMessage,
                )
            }
        }
    }
}

@Composable
private fun FavoritesHeader() {
    Text(
        text = stringResource(DSR.string.core_title_favorites),
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
        ArtworkListItem(1, UiText.DynamicString("Art 1"), UiText.DynamicString("Artist 1"), "", 1f, "2020", true),
        ArtworkListItem(2, UiText.DynamicString("Art 2"), UiText.DynamicString("Artist 2"), "", 2f, "2021", true),
    )

    ArtoposTheme {
        FavoritesView(
            artworks = mockArtworks,
            contentVersion = 0,
            isRefreshing = false,
            onShowMessage = { },
            onIntent = { },
        )
    }
}
