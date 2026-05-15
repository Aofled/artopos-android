package ru.createsmart.artopos.feature.details.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.collections.immutable.ImmutableList
import ru.createsmart.artopos.core.designsystem.components.UiText
import ru.createsmart.artopos.core.uicomponents.components.DownloadButton
import ru.createsmart.artopos.core.uicomponents.components.FavoriteButton
import ru.createsmart.artopos.feature.details.R
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
    images: ImmutableList<GalleryImageUi>,
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
    val zoomMultiplier = calculateZoomMultiplier(ratio, isCurrentZoomed)

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
            PageIndicator(
                currentPage = pagerState.currentPage + 1,
                totalPages = images.size,
                modifier = Modifier.align(Alignment.BottomEnd),
            )
        }

        GalleryActionsRow(
            isFavorite = isFavorite,
            isCurrentImageLoaded = loadedPages[pagerState.currentPage] == true,
            artworkTitle = artworkTitle,
            currentUrl = images[pagerState.currentPage].url,
            onIntent = onIntent,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
        )
    }
}

/**
 * The wider the panorama (the larger the ratio), the more we expand the container when zooming.
 */
private fun calculateZoomMultiplier(ratio: Float, isZoomed: Boolean): Float {
    // If there is no zoom, the container is always the base size (so that the entire image is visible)
    if (!isZoomed) return ZOOM_MULTIPLIER_NONE

    return when {
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
}

@Composable
private fun GalleryActionsRow(
    isFavorite: Boolean,
    isCurrentImageLoaded: Boolean,
    artworkTitle: String,
    currentUrl: String,
    onIntent: (DetailsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var pendingDownload by remember { mutableStateOf<Pair<String, String>?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Let's start delayed downloading
            pendingDownload?.let { (url, title) ->
                onIntent(DetailsIntent.DownloadImage(url, title))
            }
        } else {
            onIntent(
                DetailsIntent.ShowMessage(
                    UiText.StringResource(R.string.details_msg_permission_denied),
                ),
            )
        }
        pendingDownload = null
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FavoriteButton(
            isFavorite = isFavorite,
            onClick = { onIntent(DetailsIntent.ToggleFavorite) },
            modifier = Modifier.padding(0.dp),
            isFullScreen = true,
        )

        AnimatedVisibility(
            visible = isCurrentImageLoaded,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            DownloadButton(
                artworkTitle = artworkTitle,
                currentUrl = currentUrl,
                onDownloadClick = { url, title ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        onIntent(DetailsIntent.DownloadImage(url, title))
                    } else {
                        val isGranted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        ) == PackageManager.PERMISSION_GRANTED

                        if (isGranted) {
                            onIntent(DetailsIntent.DownloadImage(url, title))
                        } else {
                            pendingDownload = url to title
                            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun PageIndicator(
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "$currentPage / $totalPages",
        color = Color.White,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .padding(16.dp)
            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
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
