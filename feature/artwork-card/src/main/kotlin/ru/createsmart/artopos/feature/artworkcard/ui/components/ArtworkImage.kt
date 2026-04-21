package ru.createsmart.artopos.feature.artworkcard.ui.components

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
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
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import ru.createsmart.artopos.core.designsystem.components.UiText
import ru.createsmart.artopos.core.uicomponents.components.RetryDecision
import ru.createsmart.artopos.core.uicomponents.components.RetryPlaceholder
import ru.createsmart.artopos.core.uicomponents.components.analyzeRetry
import ru.createsmart.artopos.core.uicomponents.components.shimmer.ShimmerBox
import ru.createsmart.artopos.feature.artworkcard.model.ArtworkListItem
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

    val imageRequest = remember(artwork.imageUrl, globalVersion, localRetry) {
        ImageRequest.Builder(context)
            .data(artwork.imageUrl)
            // Combine Global (Pull-to-Refresh) and Local (Tap) signals to force reload
            .setParameter(RETRY_HASH, globalVersion + localRetry, memoryCacheKey = null)
            .crossfade(true)
            .listener(
                onSuccess = { _, _ -> lastError = null },
                onError = { _, result ->
                    lastError = result.throwable

                    // UX Logic: Don't show toast on first load failure (silent fail).
                    // Only show specific error if USER explicitly tapped "Retry".
                    if (localRetry > 0) {
                        val decision = lastError.analyzeRetry(context)
                        if (decision is RetryDecision.ShowMessage) {
                            onShowMessage(decision.message)
                        } else {
                            // Fallback: The error is transient (e.g. timeout), but since the manual
                            // retry failed, we must provide generic feedback to the user.
                            onShowMessage(UiText.StringResource(DSR.string.core_error_load_image))
                        }
                    }
                },
            )
            .build()
    }

    // Note: We use SubcomposeAsyncImage to support Custom Composables (Shimmer)
    SubcomposeAsyncImage(
        model = imageRequest,
        contentDescription = stringResource(DSR.string.core_cd_image),
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(artwork.aspectRatio)
            .clip(RoundedCornerShape(12.dp)),
        loading = { ShimmerBox(modifier = Modifier.fillMaxSize()) }, // Show animation while downloading

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
