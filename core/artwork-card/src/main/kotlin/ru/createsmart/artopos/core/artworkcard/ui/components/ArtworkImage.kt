package ru.createsmart.artopos.core.artworkcard.ui.components

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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ru.createsmart.artopos.core.artworkcard.model.ArtworkListItem
import ru.createsmart.artopos.core.designsystem.components.UiText
import ru.createsmart.artopos.core.uicomponents.components.RetryDecision
import ru.createsmart.artopos.core.uicomponents.components.RetryPlaceholder
import ru.createsmart.artopos.core.uicomponents.components.analyzeRetry
import ru.createsmart.artopos.core.uicomponents.components.shimmer.shimmerEffect
import ru.createsmart.artopos.core.designsystem.R as DSR

private const val RETRY_HASH = "retry_hash"

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

    var isLoading by remember(artwork.imageUrl, globalVersion, localRetry) { mutableStateOf(true) }
    var isError by remember(artwork.imageUrl, globalVersion, localRetry) { mutableStateOf(false) }

    val imageRequest = remember(artwork.imageUrl, globalVersion, localRetry) {
        ImageRequest.Builder(context)
            .data(artwork.imageUrl)
            // Combine Global (Pull-to-Refresh) and Local (Tap) signals to force reload
            .setParameter(RETRY_HASH, globalVersion + localRetry, memoryCacheKey = null)
            .crossfade(true)
            .build()
    }

    LaunchedEffect(errorTrigger) {
        if (errorTrigger > 0 && lastError != null) {
            val decision = lastError.analyzeRetry(context)
            if (decision is RetryDecision.ShowMessage) {
                onShowMessage(decision.message)
            } else {
                onShowMessage(UiText.StringResource(DSR.string.core_error_load_image))
            }
        }
    }

    Box(
        modifier = Modifier
            .aspectRatio(artwork.aspectRatio)
            .clip(RoundedCornerShape(12.dp)),
    ) {
        AsyncImage(
            model = imageRequest,
            contentDescription = stringResource(DSR.string.core_cd_image),
            contentScale = ContentScale.Crop,
            onLoading = {
                isLoading = true
                isError = false
            },
            onSuccess = {
                isLoading = false
                isError = false
                lastError = null
            },
            onError = { state ->
                isLoading = false
                isError = true
                lastError = state.result.throwable
                if (localRetry > 0) {
                    errorTrigger++
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .shimmerEffect(enabled = isLoading),
        )

        if (isError) {
            RetryPlaceholder(
                onRetry = {
                    when (val decision = lastError.analyzeRetry(context)) {
                        is RetryDecision.ShowMessage -> onShowMessage(decision.message)
                        is RetryDecision.CanRetry -> {
                            isError = false
                            isLoading = true
                            localRetry++
                        }
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
