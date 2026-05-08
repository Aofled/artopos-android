package ru.createsmart.artopos.feature.favorites.ui.components

import android.content.res.Configuration
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
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.createsmart.artopos.core.artworkcard.model.ArtworkListItem
import ru.createsmart.artopos.core.artworkcard.ui.components.ArtworkCard
import ru.createsmart.artopos.core.designsystem.components.UiText
import ru.createsmart.artopos.core.designsystem.theme.ArtoposTheme
import ru.createsmart.artopos.core.navigation.FavoritesRoute
import ru.createsmart.artopos.core.uicomponents.notifiers.BottomBarCommand
import ru.createsmart.artopos.core.uicomponents.notifiers.GlobalUiEvent
import ru.createsmart.artopos.core.uicomponents.notifiers.LocalBottomBarStateNotifier
import ru.createsmart.artopos.core.uicomponents.notifiers.LocalGlobalUiEventBus
import ru.createsmart.artopos.feature.favorites.model.FavoritesIntent
import ru.createsmart.artopos.core.designsystem.R as DSR

private const val REFRESH_DELAY_MS = 300L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FavoritesView(
    artworks: ImmutableList<ArtworkListItem>,
    contentVersion: Int,
    onArtworkClick: (Int) -> Unit,
    onIntent: (FavoritesIntent) -> Unit,
    onShowMessage: (UiText) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyStaggeredGridState()
    val bottomBarNotifier = LocalBottomBarStateNotifier.current
    val globalEventBus = LocalGlobalUiEventBus.current
    val coroutineScope = rememberCoroutineScope()

    var isRefreshing by remember { mutableStateOf(false) }

    val isAtBottom by remember {
        derivedStateOf {
            !listState.canScrollForward
        }
    }

    // Return to the top of the list when clicking on the icon in the navigation
    LaunchedEffect(globalEventBus) {
        val targetRoute = FavoritesRoute::class.qualifiedName

        globalEventBus.events.collect { event ->
            when (event) {
                is GlobalUiEvent.ScrollToTop -> {
                    if (event.route == targetRoute) {
                        bottomBarNotifier(BottomBarCommand.SHOW)
                        listState.scrollToItem(0)
                    }
                }
            }
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

    // Return to the top of the list by pressing the back button
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
        onRefresh = {
            coroutineScope.launch {
                isRefreshing = true
                onIntent(FavoritesIntent.Refresh)
                delay(REFRESH_DELAY_MS)
                isRefreshing = false
            }
        },
        modifier = modifier,
    ) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            state = listState,
            contentPadding = PaddingValues(
                top = 8.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues()
                    .calculateBottomPadding() + 48.dp,
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
                    onClick = { onArtworkClick(artwork.id) },
                    onFavoriteClick = { onIntent(FavoritesIntent.ToggleFavorite(artwork.id)) },
                    onShowMessage = onShowMessage,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun FavoritesHeader(
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(DSR.string.core_title_favorites),
        style = MaterialTheme.typography.displaySmall,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier,
    )
}

@Preview(
    showBackground = true,
    name = "Favorites",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Composable
private fun FavoritesViewPreview() {
    val mockArtworks = persistentListOf(
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
            true,
        ),
    )

    ArtoposTheme {
        FavoritesView(
            artworks = mockArtworks,
            contentVersion = 0,
            onShowMessage = { },
            onArtworkClick = { },
            onIntent = { },
        )
    }
}
