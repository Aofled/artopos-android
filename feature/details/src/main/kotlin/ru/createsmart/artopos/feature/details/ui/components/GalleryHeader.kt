package ru.createsmart.artopos.feature.details.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.createsmart.artopos.core.uicomponents.components.DownloadButton
import ru.createsmart.artopos.core.uicomponents.components.FavoriteButton
import ru.createsmart.artopos.feature.details.model.DetailsIntent
import ru.createsmart.artopos.feature.details.model.GalleryImageUi

private const val RATIO_THRESHOLD_PORTRAIT = 0.8f
private const val RATIO_THRESHOLD_SQUARE = 1.1f
private const val RATIO_THRESHOLD_SMALL_PANORAMA = 1.5f
private const val RATIO_THRESHOLD_MEDIUM_PANORAMA = 2.5f
private const val RATIO_THRESHOLD_SUPER_PANORAMA = 3.0f
private const val RATIO_THRESHOLD_EXTRA_PANORAMA = 3.5f

private const val ZOOM_MULTIPLIER_NONE = 1.0f
private const val ZOOM_MULTIPLIER_EXTRA_SMALL = 1.5f
private const val ZOOM_MULTIPLIER_SMALL = 2.0f
private const val ZOOM_MULTIPLIER_MEDIUM = 2.5f
private const val ZOOM_MULTIPLIER_LARGE = 3.0f
private const val ZOOM_MULTIPLIER_SUPER_LARGE = 3.5f
private const val ZOOM_MULTIPLIER_EXTRA_LARGE = 5.5f

@Composable
fun GalleryHeader(
    images: List<GalleryImageUi>,
    pagerState: PagerState,
    isScrolledDown: Boolean,
    contentVersion: Int,
    isFavorite: Boolean,
    artworkTitle: String,
    onIntent: (DetailsIntent) -> Unit,
) {
    if (images.isEmpty()) return

    val density = LocalDensity.current
    val screenWidth = with(density) {
        LocalWindowInfo.current.containerSize.width.toDp()
    }

    val currentImage = images[pagerState.currentPage]

    val ratio = if (currentImage.aspectRatio > 0) currentImage.aspectRatio else 1f

    val zoomedPages = remember { mutableStateMapOf<Int, Boolean>() }
    val isCurrentZoomed = zoomedPages[pagerState.currentPage] == true

    val baseTargetHeight = screenWidth / ratio

    // The wider the panorama (the larger the ratio), the more we expand the container when zooming
    val zoomMultiplier = if (isCurrentZoomed) {
        when {
            // Vertical portraits (ratio <= 0.8) are NOT stretched.
            ratio <= RATIO_THRESHOLD_PORTRAIT -> ZOOM_MULTIPLIER_NONE

            // Squares and vertical portraits (from 0.8 to 1.1:1). Stretch by 1.5 times.
            ratio <= RATIO_THRESHOLD_SQUARE -> ZOOM_MULTIPLIER_EXTRA_SMALL

            // Small panoramas (from 1.1 to 1.5:1). Stretch by 2.0 times.
            ratio <= RATIO_THRESHOLD_SMALL_PANORAMA -> ZOOM_MULTIPLIER_SMALL

            // Medium panoramas (from 1.5:1 to 2.5:1). Stretched by 2.5x.
            ratio <= RATIO_THRESHOLD_MEDIUM_PANORAMA -> ZOOM_MULTIPLIER_MEDIUM

            // large panoramas (from 2.5:1 to 3.0:1). Stretched by 3.0x.
            ratio <= RATIO_THRESHOLD_SUPER_PANORAMA -> ZOOM_MULTIPLIER_LARGE

            // large panoramas (from 3.0:1 to 3.5:1). Stretched by 3.5x.
            ratio <= RATIO_THRESHOLD_EXTRA_PANORAMA -> ZOOM_MULTIPLIER_SUPER_LARGE

            // Extremely long scrolls/panoramas (wider than 3.0:1). Stretch by 5.5x.
            else -> ZOOM_MULTIPLIER_EXTRA_LARGE
        }
    } else {
        // If there is no zoom, the container is always the base size (so that the entire image is visible)
        ZOOM_MULTIPLIER_NONE
    }

    val targetHeight = baseTargetHeight * zoomMultiplier

    val finalHeight = animatedHeight(targetHeight, isScrolledDown)

    // For the "download" button
    val loadedPages = remember { mutableStateMapOf<Int, Boolean>() }

    Box {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(finalHeight)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            verticalAlignment = Alignment.Top,
        ) { page ->
            DetailsGalleryImage(
                imageUrl = images[page].url,
                globalVersion = contentVersion,
                onShowMessage = { onIntent(DetailsIntent.ShowMessage(it)) },
                contentScale = ContentScale.Fit,
                onImageLoaded = { loaded ->
                    loadedPages[page] = loaded
                },
                onZoomStateChanged = { isZoomed ->
                    zoomedPages[page] = isZoomed
                },
            )
        }

        if (images.size > 1) { // Page Indicator
            Text(
                text = "${pagerState.currentPage + 1} / ${images.size}",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FavoriteButton(
                isFavorite = isFavorite,
                onClick = { onIntent(DetailsIntent.ToggleFavorite) },
                modifier = Modifier
                    .padding(0.dp),
                isFullScreen = true,
            )

            val isCurrentImageLoaded = loadedPages[pagerState.currentPage] == true

            AnimatedVisibility(
                visible = isCurrentImageLoaded,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                DownloadButton(
                    artworkTitle = artworkTitle,
                    currentUrl = images[pagerState.currentPage].url,
                    onDownloadClick = { url, title ->
                        onIntent(DetailsIntent.DownloadImage(url, title)) // ИНТЕНТ
                    },
                )
            }
        }
    }
}

@Composable
private fun animatedHeight(
    // Image resizing animation
    target: Dp,
    isScrolledDown: Boolean,
): Dp {
    val height by animateDpAsState(
        targetValue = target,
        animationSpec = tween(
            durationMillis = HEIGHT_ANIMATION_DURATION,
            delayMillis = if (isScrolledDown) HEIGHT_ANIMATION_DELAY else 0,
        ),
        label = "height",
    )
    return height
}
