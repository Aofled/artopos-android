package ru.createsmart.artopos.feature.details.ui.components

import UiText
import android.content.res.Configuration
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.createsmart.artopos.core.ui.theme.ArtoposTheme
import ru.createsmart.artopos.core.ui.theme.FontFamilySerif
import ru.createsmart.artopos.core.ui.theme.components.ExpandableDetailsSection
import ru.createsmart.artopos.feature.details.model.ArtworkDetailUi
import ru.createsmart.artopos.feature.details.ui.preview.artworkPreview

const val SCROLL_RESET_DURATION = 400
const val HEIGHT_ANIMATION_DURATION = 750
const val HEIGHT_ANIMATION_DELAY = 300

@Composable
fun DetailsContent(
    artwork: ArtworkDetailUi,
    contentVersion: Int,
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
            )
        }
    }
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

private fun LazyListScope.detailsItems(
    artwork: ArtworkDetailUi,
    pagerState: PagerState,
    isScrolledDown: Boolean,
    contentVersion: Int,
) {
    // 1. Gallery
    item {
        GalleryHeader(artwork.images, pagerState, isScrolledDown, contentVersion)
    }

    // 2. Artist and title
    item { ArtistAndTitle(artwork) }

    // 3. Description
    if (!artwork.description.isNullOrBlank()) {
        item { Description(artwork.description) }
    }

    item {
        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
        Spacer(modifier = Modifier.height(16.dp))
    }

    // 4. Details Table
    item {
        ExpandableDetailsSection(title = "Specifications") {
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
private fun ArtistAndTitle(
    artwork: ArtworkDetailUi,
) {
    Column(modifier = Modifier.padding(24.dp)) {
        Text(
            text = artwork.artist,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = artwork.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun Description(
    description: String,
) {
    Text(
        text = description,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontFamily = FontFamilySerif,
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 30.sp,
        modifier = Modifier.padding(horizontal = 24.dp),
    )
    Spacer(modifier = Modifier.height(32.dp))
}

@Composable
private fun DetailsTable(
    artwork: ArtworkDetailUi,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        val details = artwork.details
        var i = 0

        while (i < details.size) {
            val current = details[i]
            val next = details.getOrNull(i + 1)

            if (!current.isWide && next != null && !next.isWide) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    DetailItemRow(current.label, current.value, Modifier.weight(1f))
                    DetailItemRow(next.label, next.value, Modifier.weight(1f))
                }
                i += 2
            } else {
                DetailItemRow(current.label, current.value, Modifier.fillMaxWidth())
                i++
            }
        }
    }
}

@Composable
private fun DetailItemRow(
    label: UiText,
    value: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val labelString = label.asString(context)

    Column(
        modifier = modifier
            .padding(vertical = 12.dp),
    ) {
        Text(
            text = labelString.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
            letterSpacing = 1.2.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun CopyrightFooter(
    copyright: String,
) {
    Text(
        text = copyright,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
    )
}

@Preview(showBackground = true, name = "Details", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun ArtworksViewPreview() {
    ArtoposTheme {
        DetailsContent(
            artwork = artworkPreview,
            contentVersion = 1,
            onRefresh = { },
            isRefreshing = true,
        )
    }
}
