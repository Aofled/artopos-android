package ru.createsmart.artopos.core.uicomponents.components.shimmer

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import ru.createsmart.artopos.core.designsystem.theme.LocalAppThemeIsDark

private const val SHIMMER_DARK_HIGHLIGHT_ALPHA = 0.15f
private const val SHIMMER_LIGHT_HIGHLIGHT_ALPHA = 0.6f
private const val SHIMMER_WAVE_SIZE = 1.5f

/**
 * Shared color calculation for fullscreen shimmer effects.
 */
@Composable
private fun rememberFullscreenShimmerColors(): ShimmerColors {
    val isDark = LocalAppThemeIsDark.current
    val baseColor = MaterialTheme.colorScheme.surfaceVariant

    return remember(baseColor, isDark) {
        val edgeColor = baseColor.copy(alpha = 1f)
        val highlightColor = if (isDark) {
            Color.White.copy(alpha = SHIMMER_DARK_HIGHLIGHT_ALPHA).compositeOver(baseColor)
        } else {
            Color.White.copy(alpha = SHIMMER_LIGHT_HIGHLIGHT_ALPHA).compositeOver(baseColor)
        }

        ShimmerColors(
            listOf(
                edgeColor,
                highlightColor,
                highlightColor,
                highlightColor,
                edgeColor,
            ),
        )
    }
}

@Composable
fun Modifier.fullscreenShimmerEffect(
    enabled: Boolean = true,
): Modifier {
    if (!enabled) return this
    val shimmerColors = rememberFullscreenShimmerColors()
    return baseShimmer(
        shimmerColors = shimmerColors,
        enabled = enabled,
        waveSizeMultiplier = SHIMMER_WAVE_SIZE,
    )
}

@Composable
fun FullscreenShimmerBox(modifier: Modifier = Modifier) {
    BaseShimmerBox(
        shimmerColors = rememberFullscreenShimmerColors(),
        modifier = modifier,
        waveSizeMultiplier = SHIMMER_WAVE_SIZE,
    )
}
