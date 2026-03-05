package ru.createsmart.artopos.feature.details.ui.components

import UiText
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import ru.createsmart.artopos.core.ui.theme.components.RetryDecision
import ru.createsmart.artopos.core.ui.theme.components.RetryPlaceholder
import ru.createsmart.artopos.core.ui.theme.components.ShimmerBox
import ru.createsmart.artopos.core.ui.theme.components.analyzeRetry
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

    val imageRequest = remember(imageUrl, globalVersion, localRetry) {
        ImageRequest.Builder(context)
            .data(imageUrl)
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
                            onShowMessage(UiText.StringResource(UiR.string.error_load_image))
                        }
                    }
                },
            )
            .build()
    }

    SubcomposeAsyncImage(
        model = imageRequest,
        contentDescription = null,
        contentScale = contentScale,
        modifier = Modifier.fillMaxSize(),
        loading = {
            ShimmerBox(
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
