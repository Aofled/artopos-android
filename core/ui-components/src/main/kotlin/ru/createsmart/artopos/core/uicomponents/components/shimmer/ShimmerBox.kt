package ru.createsmart.artopos.core.uicomponents.components.shimmer

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import ru.createsmart.artopos.core.designsystem.theme.LocalAppThemeIsDark

private const val SHIMMER_DARK_EDGE_ALPHA = 0.6f
private const val SHIMMER_DARK_HIGHLIGHT_ALPHA = 0.4f

private const val SHIMMER_LIGHT_EDGE_ALPHA = 0.95f
private const val SHIMMER_LIGHT_HIGHLIGHT_ALPHA = 0.4f

/**
 * Shared color calculation for standard shimmer effects.
 */
@Composable
private fun rememberShimmerColors(): ShimmerColors {
    val isDark = LocalAppThemeIsDark.current
    val baseColor = MaterialTheme.colorScheme.surfaceVariant

    return remember(baseColor, isDark) {
        val (edgeAlpha, highlightAlpha) = if (isDark) {
            SHIMMER_DARK_EDGE_ALPHA to SHIMMER_DARK_HIGHLIGHT_ALPHA
        } else {
            SHIMMER_LIGHT_EDGE_ALPHA to SHIMMER_LIGHT_HIGHLIGHT_ALPHA
        }
        ShimmerColors(
            listOf(
                baseColor.copy(alpha = edgeAlpha),
                baseColor.copy(alpha = highlightAlpha),
                baseColor.copy(alpha = edgeAlpha),
            ),
        )
    }
}

/**
 * Standard Shimmer modifier for cards and list items.
 */
fun Modifier.shimmerEffect(
    enabled: Boolean = true,
): Modifier = composed {
    if (!enabled) return@composed Modifier

    val shimmerColors = rememberShimmerColors()

    baseShimmer(
        shimmerColors = shimmerColors,
        enabled = enabled,
        waveSizeMultiplier = 1.0f,
    )
}

@Composable
fun ShimmerBox(modifier: Modifier = Modifier) {
    BaseShimmerBox(
        shimmerColors = rememberShimmerColors(),
        modifier = modifier,
        waveSizeMultiplier = 1.0f,
    )
}
