package ru.createsmart.artopos.core.uicomponents.components.shimmer

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import ru.createsmart.artopos.core.designsystem.theme.LocalAppThemeIsDark

private const val SHIMMER_DARK_EDGE_ALPHA = 0.6f
private const val SHIMMER_DARK_HIGHLIGHT_ALPHA = 0.4f

private const val SHIMMER_LIGHT_EDGE_ALPHA = 0.95f
private const val SHIMMER_LIGHT_HIGHLIGHT_ALPHA = 0.4f

@Composable
fun ShimmerBox(modifier: Modifier = Modifier) {
    val isDark = LocalAppThemeIsDark.current
    val baseColor = MaterialTheme.colorScheme.surfaceVariant

    // Visual Tweak: Dark theme needs lower opacity (subtle effect).
    // Light theme needs higher opacity to be visible on white background.
    // Optimization: Cache colors to avoid re-allocating List on every frame
    val shimmerColors = remember(baseColor, isDark) {
        val (edgeAlpha, highlightAlpha) = if (isDark) {
            SHIMMER_DARK_EDGE_ALPHA to SHIMMER_DARK_HIGHLIGHT_ALPHA
        } else {
            SHIMMER_LIGHT_EDGE_ALPHA to SHIMMER_LIGHT_HIGHLIGHT_ALPHA
        }
        listOf(
            baseColor.copy(alpha = edgeAlpha),
            baseColor.copy(alpha = highlightAlpha),
            baseColor.copy(alpha = edgeAlpha),
        )
    }

    BaseShimmerBox(
        shimmerColors = shimmerColors,
        modifier = modifier,
        waveSizeMultiplier = 1.0f,
    )
}
