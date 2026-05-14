package ru.createsmart.artopos.feature.details.ui.components

import android.app.ActivityManager
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import ru.createsmart.artopos.core.designsystem.components.UiText
import ru.createsmart.artopos.core.uicomponents.components.RetryDecision
import ru.createsmart.artopos.core.uicomponents.components.RetryPlaceholder
import ru.createsmart.artopos.core.uicomponents.components.analyzeRetry
import ru.createsmart.artopos.core.uicomponents.components.shimmer.FullscreenShimmerBox
import ru.createsmart.artopos.core.designsystem.R as DSR

private const val RETRY_HASH = "retry_hash"
private const val ZOOM_ACTIVATION_THRESHOLD = 1.25f

private const val BYTES_IN_GIGABYTE = 1024.0 * 1024.0 * 1024.0 // GB

private object GalleryHardwareConfig {
    // RAM Thresholds (GB)
    const val RAM_ULTRA_LOW = 2.0
    const val RAM_LOW = 4.0
    const val RAM_MEDIUM = 6.0
    const val RAM_HIGH = 8.0

    // Multipliers
    const val MULTIPLIER_ULTRA_LOW = 2.0f
    const val MULTIPLIER_LOW = 3.0f
    const val MULTIPLIER_MEDIUM = 4.0f
    const val MULTIPLIER_HIGH = 5.0f
    const val MULTIPLIER_ULTRA = 6.0f

    // Absolute Max Texture Sizes (px)
    const val MAX_SIZE_ULTRA_LOW = 4000
    const val MAX_SIZE_LOW = 5000
    const val MAX_SIZE_MEDIUM = 6000
    const val MAX_SIZE_HIGH = 8000
    const val MAX_SIZE_ULTRA = 10_000
}

@Composable
fun DetailsGalleryImage(
    imageUrl: String,
    globalVersion: Int,
    onShowMessage: (UiText) -> Unit,
    contentScale: ContentScale,
    onImageLoaded: (Boolean) -> Unit,
    onZoomStateChanged: (isZoomed: Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    var localRetry by remember { mutableIntStateOf(0) }
    var lastError: Throwable? by remember { mutableStateOf(null) }

    val zoomState = rememberZoomState(maxScale = 10f)
    var isImageLoaded by remember { mutableStateOf(false) }

    val imageRequest = rememberGalleryImageRequest(
        imageUrl = imageUrl,
        version = globalVersion + localRetry,
        onStateChange = { loaded, error ->
            isImageLoaded = loaded
            lastError = error
            onImageLoaded(loaded)
        },
        onErrorMessage = { if (localRetry > 0) onShowMessage(it) },
    )

    val isZoomed by remember {
        derivedStateOf { zoomState.scale > ZOOM_ACTIVATION_THRESHOLD }
    }

    LaunchedEffect(isZoomed) {
        // The image is zoomed if the scale is greater than 1.25
        onZoomStateChanged(isZoomed)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds(),
    ) {
        SubcomposeAsyncImage(
            model = imageRequest,
            contentDescription = stringResource(DSR.string.core_cd_image),
            contentScale = contentScale,
            modifier = Modifier
                .fillMaxSize()
                .then(if (isImageLoaded) Modifier.zoomable(zoomState) else Modifier),
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

    val windowInfo = LocalWindowInfo.current
    val containerSize = windowInfo.containerSize

    // Hardware-Aware Resolution
    val (targetWidth, targetHeight) = remember(containerSize, context) {
        val widthPx = containerSize.width.toFloat()
        val heightPx = containerSize.height.toFloat()

        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val totalRamGb = memoryInfo.totalMem / BYTES_IN_GIGABYTE

        // Dynamic multipliers and GPU hard limits (GL_MAX_TEXTURE_SIZE)
        val (multiplier, absoluteMax) = when {
            // 1. Ultra-budget (Android Go or < 2 GB)
            activityManager.isLowRamDevice || totalRamGb < GalleryHardwareConfig.RAM_ULTRA_LOW ->
                GalleryHardwareConfig.MULTIPLIER_ULTRA_LOW to GalleryHardwareConfig.MAX_SIZE_ULTRA_LOW

            // 2. Old/Weak devices (2 to 4 GB)
            totalRamGb < GalleryHardwareConfig.RAM_LOW ->
                GalleryHardwareConfig.MULTIPLIER_LOW to GalleryHardwareConfig.MAX_SIZE_LOW

            // 3. Middle class (4 to 6 GB)
            totalRamGb < GalleryHardwareConfig.RAM_MEDIUM ->
                GalleryHardwareConfig.MULTIPLIER_MEDIUM to GalleryHardwareConfig.MAX_SIZE_MEDIUM

            // 4. Sub-flagships (6 to 8 GB)
            totalRamGb < GalleryHardwareConfig.RAM_HIGH ->
                GalleryHardwareConfig.MULTIPLIER_HIGH to GalleryHardwareConfig.MAX_SIZE_HIGH

            // 5. Ultra-flagships (8+ GB RAM)
            else ->
                GalleryHardwareConfig.MULTIPLIER_ULTRA to GalleryHardwareConfig.MAX_SIZE_ULTRA
        }

        val safeWidth = if (widthPx > 0) (widthPx * multiplier).toInt().coerceAtMost(absoluteMax) else absoluteMax
        val safeHeight = if (heightPx > 0) (heightPx * multiplier).toInt().coerceAtMost(absoluteMax) else absoluteMax

        safeWidth to safeHeight
    }

    return remember(imageUrl, version, targetWidth, targetHeight) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .setParameter(RETRY_HASH, version, memoryCacheKey = null)
            .size(targetWidth, targetHeight)
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
                        UiText.StringResource(DSR.string.core_error_load_image)
                    }
                    onErrorMessage(message)
                },
            )
            .build()
    }
}
