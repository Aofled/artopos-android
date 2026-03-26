package ru.createsmart.artopos.feature.details.ui.components

import UiText
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.createsmart.artopos.core.uicomponents.components.FavoriteButton
import ru.createsmart.artopos.feature.details.model.GalleryImageUi

@Composable
fun GalleryHeader(
    images: List<GalleryImageUi>,
    pagerState: PagerState,
    isScrolledDown: Boolean,
    contentVersion: Int,
    isFavorite: Boolean,
    onShowMessage: (UiText) -> Unit,
    onFavoriteClick: () -> Unit,
) {
    if (images.isEmpty()) return

    val density = LocalDensity.current
    val screenWidth = with(density) {
        LocalWindowInfo.current.containerSize.width.toDp()
    }

    val currentImage = images[pagerState.currentPage]

    val ratio = if (currentImage.aspectRatio > 0) currentImage.aspectRatio else 1f
    val targetHeight = screenWidth / ratio
    val finalHeight = animatedHeight(targetHeight, isScrolledDown)

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
                onShowMessage = onShowMessage,
                contentScale = ContentScale.Crop,
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

        FavoriteButton(
            isFavorite = isFavorite,
            onClick = onFavoriteClick,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp), // Upper left bottom corner
            isFullScreen = true,
        )
    }
}

@Composable
private fun animatedHeight( // Image resizing animation
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
