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

@Composable
fun FullscreenShimmerBox(modifier: Modifier = Modifier) {
    val isDark = LocalAppThemeIsDark.current
    val baseColor = MaterialTheme.colorScheme.surfaceVariant

    // Visual Tweak: Dark theme needs lower opacity (subtle effect).
    // Light theme needs higher opacity to be visible on white background.
    // Optimization: Cache colors to avoid re-allocating List on every frame
    val shimmerColors = remember(baseColor, isDark) {
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

    BaseShimmerBox(
        shimmerColors = shimmerColors,
        modifier = modifier,
        waveSizeMultiplier = SHIMMER_WAVE_SIZE,
    )
}
