package ru.createsmart.artopos.feature.details.ui.components

import UiText
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import ru.createsmart.artopos.core.ui.R
import ru.createsmart.artopos.core.ui.components.FullscreenShimmerBox
import ru.createsmart.artopos.core.ui.components.RetryDecision
import ru.createsmart.artopos.core.ui.components.RetryPlaceholder
import ru.createsmart.artopos.core.ui.components.analyzeRetry
import ru.createsmart.artopos.core.ui.R as UiR

private const val RETRY_HASH = "retry_hash"

@Composable
fun DetailsGalleryImage(
    imageUrl: String,
    globalVersion: Int,
    onShowMessage: (UiText) -> Unit,
    contentScale: ContentScale,
) {
    val context = LocalContext.current
    var localRetry by remember { mutableIntStateOf(0) }
    var lastError: Throwable? by remember { mutableStateOf(null) }

    val zoomState = rememberZoomState(maxScale = 5f)
    var isImageLoaded by remember { mutableStateOf(false) }

    val imageRequest = rememberGalleryImageRequest(
        imageUrl = imageUrl,
        version = globalVersion + localRetry,
        onStateChange = { loaded, error ->
            isImageLoaded = loaded
            lastError = error
        },
        onErrorMessage = { if (localRetry > 0) onShowMessage(it) },
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .then(if (isImageLoaded) Modifier.zoomable(zoomState) else Modifier),
    ) {
        SubcomposeAsyncImage(
            model = imageRequest,
            contentDescription = stringResource(R.string.description_image),
            contentScale = contentScale,
            modifier = Modifier.fillMaxSize(),
            loading = {
                FullscreenShimmerBox(
                    modifier = Modifier.fillMaxSize(),
                )
            },
            error = {
                RetryPlaceholder(
                    onRetry = {
                        when (val decision = lastError.analyzeRetry(context)) {
                            is RetryDecision.ShowMessage -> onShowMessage(decision.message)
                            is RetryDecision.CanRetry -> localRetry++
                        }
                    },
                )
            },
        )
    }
}

@Composable
private fun rememberGalleryImageRequest(
    imageUrl: String,
    version: Int,
    onStateChange: (isLoaded: Boolean, error: Throwable?) -> Unit,
    onErrorMessage: (UiText) -> Unit,
): ImageRequest {
    val context = LocalContext.current
    return remember(imageUrl, version) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .setParameter(RETRY_HASH, version, memoryCacheKey = null)
            .crossfade(true)
            .listener(
                onStart = { onStateChange(false, null) },
                onSuccess = { _, _ -> onStateChange(true, null) },
                onError = { _, result ->
                    onStateChange(false, result.throwable)
                    val decision = result.throwable.analyzeRetry(context)
                    val message = if (decision is RetryDecision.ShowMessage) {
                        decision.message
                    } else {
                        UiText.StringResource(UiR.string.error_load_image)
                    }
                    onErrorMessage(message)
                },
            )
            .build()
    }
}
