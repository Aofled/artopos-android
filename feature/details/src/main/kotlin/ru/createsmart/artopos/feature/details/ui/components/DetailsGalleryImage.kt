package ru.createsmart.artopos.feature.details.ui.components

import android.widget.Toast
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

private const val RETRY_HASH = "retry_hash"

@Composable
fun DetailsGalleryImage(
    imageUrl: String,
    contentVersion: Int,
    contentScale: ContentScale,
) {
    val context = LocalContext.current
    var localRetry by remember { mutableIntStateOf(0) }
    var lastError: Throwable? by remember { mutableStateOf(null) }

    val imageRequest = remember(imageUrl, contentVersion + localRetry) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .setParameter(RETRY_HASH, localRetry, memoryCacheKey = null)
            .crossfade(true)
            .listener(
                onSuccess = { _, _ -> lastError = null },
                onError = { _, result -> lastError = result.throwable },
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
                        is RetryDecision.ShowMessage -> {
                            Toast.makeText(
                                context,
                                decision.message.asString(context),
                                Toast.LENGTH_SHORT,
                            ).show()
                        }

                        is RetryDecision.CanRetry -> localRetry++
                    }
                },
            )
        },
    )
}
