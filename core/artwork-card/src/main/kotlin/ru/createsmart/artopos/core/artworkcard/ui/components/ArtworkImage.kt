package ru.createsmart.artopos.core.artworkcard.ui.components

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import ru.createsmart.artopos.core.artworkcard.model.ArtworkListItem
import ru.createsmart.artopos.core.designsystem.components.UiText
import ru.createsmart.artopos.core.uicomponents.components.RetryDecision
import ru.createsmart.artopos.core.uicomponents.components.RetryPlaceholder
import ru.createsmart.artopos.core.uicomponents.components.analyzeRetry
import ru.createsmart.artopos.core.uicomponents.components.shimmer.shimmerEffect
import ru.createsmart.artopos.core.designsystem.R as DSR

@Composable
internal fun ArtworkImage(
    artwork: ArtworkListItem,
    globalVersion: Int,
    onShowMessage: (UiText) -> Unit,
) {
    val context = LocalContext.current
    var localRetry by remember { mutableIntStateOf(0) }
    var lastError: Throwable? by remember { mutableStateOf(null) }
    var errorTrigger by remember { mutableIntStateOf(0) }

    var isSuccess by remember(artwork.imageUrl) { mutableStateOf(false) }
    var isLoading by remember(artwork.imageUrl) { mutableStateOf(!isSuccess) }
    var isError by remember(artwork.imageUrl) { mutableStateOf(false) }

    LaunchedEffect(globalVersion) {
        if (!isSuccess && isError) {
            isError = false
            isLoading = true
        }
    }

    ErrorMessageHandler(
        errorTrigger = errorTrigger,
        lastError = lastError,
        onShowMessage = onShowMessage,
    )

    val retryAttempt = if (isSuccess) 0 else (globalVersion + localRetry)
    val imageRequest = rememberArtworkImageRequest(
        context = context,
        imageUrl = artwork.imageUrl,
        retryAttempt = retryAttempt,
    )

    Box(
        modifier = Modifier
            .aspectRatio(artwork.aspectRatio)
            .clip(RoundedCornerShape(12.dp)),
    ) {
        ArtworkImageContainer(
            imageRequest = imageRequest,
            isLoading = isLoading,
            isSuccess = isSuccess,
            onLoading = {
                if (!isSuccess) {
                    isLoading = true
                }
                isError = false
            },
            onSuccess = {
                isLoading = false
                isSuccess = true
                isError = false
                lastError = null
            },
            onError = { throwable ->
                isLoading = false
                isSuccess = false
                isError = true
                lastError = throwable
                if (localRetry > 0) {
                    errorTrigger++
                }
            },
        )

        if (isError) {
            ArtworkRetryOverlay(
                lastError = lastError,
                onShowMessage = onShowMessage,
                onRetryConfirmed = {
                    isError = false
                    isLoading = true
                    localRetry++
                },
            )
        }
    }
}

@Composable
private fun ArtworkImageContainer(
    imageRequest: ImageRequest,
    isLoading: Boolean,
    isSuccess: Boolean,
    onLoading: () -> Unit,
    onSuccess: () -> Unit,
    onError: (Throwable) -> Unit,
) {
    AsyncImage(
        model = imageRequest,
        contentDescription = stringResource(DSR.string.core_cd_image),
        contentScale = ContentScale.Crop,
        onLoading = { onLoading() },
        onSuccess = { onSuccess() },
        onError = { state -> onError(state.result.throwable) },
        modifier = Modifier
            .fillMaxSize()
            .shimmerEffect(enabled = isLoading && !isSuccess),
    )
}

@Composable
private fun ErrorMessageHandler(
    errorTrigger: Int,
    lastError: Throwable?,
    onShowMessage: (UiText) -> Unit,
) {
    val context = LocalContext.current
    LaunchedEffect(errorTrigger) {
        if (errorTrigger > 0 && lastError != null) {
            val decision = lastError.analyzeRetry(context)
            val message = if (decision is RetryDecision.ShowMessage) {
                decision.message
            } else {
                UiText.StringResource(DSR.string.core_error_load_image)
            }
            onShowMessage(message)
        }
    }
}

@Composable
private fun rememberArtworkImageRequest(
    context: Context,
    imageUrl: String,
    retryAttempt: Int,
): ImageRequest {
    return remember(imageUrl, retryAttempt) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .apply {
                if (retryAttempt > 0) {
                    // Bypass memory cache on retry to fetch a fresh image.
                    memoryCacheKey("${imageUrl}_retry_$retryAttempt")
                }
            }
            .crossfade(true)
            .build()
    }
}

@Composable
private fun ArtworkRetryOverlay(
    lastError: Throwable?,
    onShowMessage: (UiText) -> Unit,
    onRetryConfirmed: () -> Unit,
) {
    val context = LocalContext.current
    RetryPlaceholder(
        onRetry = {
            when (val decision = lastError.analyzeRetry(context)) {
                is RetryDecision.ShowMessage -> onShowMessage(decision.message)
                is RetryDecision.CanRetry -> onRetryConfirmed()
            }
        },
        modifier = Modifier.fillMaxSize(),
    )
}
