package ru.createsmart.artopos.feature.details.ui.components

import UiText
import android.content.res.Configuration
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.createsmart.artopos.core.ui.components.ExpandableDetailsSection
import ru.createsmart.artopos.core.ui.theme.ArtoposTheme
import ru.createsmart.artopos.feature.details.R
import ru.createsmart.artopos.feature.details.model.ArtworkDetailUi
import ru.createsmart.artopos.feature.details.ui.preview.artworkPreview

const val SCROLL_RESET_DURATION = 400
const val HEIGHT_ANIMATION_DURATION = 750
const val HEIGHT_ANIMATION_DELAY = 300

@Composable
fun DetailsContent(
    artwork: ArtworkDetailUi,
    contentVersion: Int,
    onShowMessage: (UiText) -> Unit,
    onRefresh: () -> Unit,
    isRefreshing: Boolean,
) {
    val scrollState = rememberLazyListState()
    val pagerState = rememberPagerState(pageCount = { artwork.images.size })

    val isScrolledDown by remember {
        derivedStateOf {
            scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 0
        }
    }

    DetailsScrollEffect(pagerState, scrollState)

    val pullRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = pullRefreshState,
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = scrollState,
        ) {
            detailsItems(
                artwork = artwork,
                pagerState = pagerState,
                isScrolledDown = isScrolledDown,
                contentVersion = contentVersion,
                onShowMessage = onShowMessage,
            )
        }
    }
}

private fun LazyListScope.detailsItems(
    artwork: ArtworkDetailUi,
    pagerState: PagerState,
    isScrolledDown: Boolean,
    contentVersion: Int,
    onShowMessage: (UiText) -> Unit,
) {
    // 1. Gallery
    item {
        GalleryHeader(artwork.images, pagerState, isScrolledDown, contentVersion, onShowMessage)
    }

    // 2. Artist and title
    item { ArtistAndTitle(artwork) }

    item {
        KeyFactsRow(artwork)
    }

    // 3. Description
    if (!artwork.description.isNullOrBlank()) {
        item {
            Description(artwork.description)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // 4. Details Table
    item {
        ExpandableDetailsSection(title = stringResource(R.string.details_specifications)) {
            DetailsTable(artwork)
        }
    }

    // 5. Copyright (Footer)
    if (!artwork.copyright.isNullOrBlank()) {
        item { CopyrightFooter(artwork.copyright) }
    }

    item { Spacer(modifier = Modifier.height(48.dp)) }
}

@Composable
private fun DetailsScrollEffect(pagerState: PagerState, scrollState: LazyListState) {
    LaunchedEffect(pagerState.currentPage) {
        if (scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 0) {
            scrollState.animateScrollBy(
                value = -scrollState.firstVisibleItemScrollOffset.toFloat(),
                animationSpec = tween(durationMillis = SCROLL_RESET_DURATION),
            )
        }
    }
}

@Preview(showBackground = true, name = "Details", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun ArtworksViewPreview() {
    ArtoposTheme {
        DetailsContent(
            artwork = artworkPreview,
            contentVersion = 1,
            onShowMessage = { },
            onRefresh = { },
            isRefreshing = true,
        )
    }
}
