package ru.createsmart.artopos.feature.discover.ui.components

import UiText
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.network.HttpException
import coil.request.ImageRequest
import ru.createsmart.artopos.core.common.util.isNetworkAvailable
import ru.createsmart.artopos.feature.discover.R
import ru.createsmart.artopos.feature.discover.model.ArtworkListItem

@Composable
internal fun ArtworkImage(
    artwork: ArtworkListItem,
    globalVersion: Int,
    onShowMessage: (UiText) -> Unit,
) {
    val context = LocalContext.current
    var localRetry by remember { mutableIntStateOf(0) }
    var lastError: Throwable? by remember { mutableStateOf(null) }

    val imageRequest = remember(artwork.imageUrl, globalVersion, localRetry) {
        ImageRequest.Builder(context)
            .data(artwork.imageUrl)
            // Combine Global (Pull-to-Refresh) and Local (Tap) signals to force reload
            .setParameter("retry_hash", globalVersion + localRetry, memoryCacheKey = null)
            .crossfade(true)
            .listener(
                onSuccess = { _, _ -> lastError = null },
                onError = { _, result ->
                    lastError = result.throwable
                    if (localRetry > 0) onShowMessage(UiText.StringResource(R.string.error_load_image))
                },
            )
            .build()
    }

    // Note: We use SubcomposeAsyncImage to support Custom Composables (Shimmer)
    SubcomposeAsyncImage(
        model = imageRequest,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(artwork.aspectRatio)
            .clip(RoundedCornerShape(12.dp)),
        loading = { ShimmerBox(modifier = Modifier.fillMaxSize()) }, // Show animation while downloading

        error = {
            ArtworkImageErrorState(
                onRetry = {
                    if (!context.isNetworkAvailable()) {
                        onShowMessage(UiText.StringResource(R.string.error_no_internet))
                    } else if (lastError is HttpException && (lastError as HttpException).response.code == 404) {
                        onShowMessage(UiText.StringResource(R.string.error_not_found))
                    } else {
                        localRetry++ // Tap to retry
                    }
                },
            )
        },
    )
}

@Composable
private fun ArtworkImageErrorState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.errorContainer)
            .clickable { onRetry() },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = R.drawable.refresh),
                contentDescription = stringResource(R.string.btn_retry),
                tint = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                text = stringResource(R.string.btn_retry),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
