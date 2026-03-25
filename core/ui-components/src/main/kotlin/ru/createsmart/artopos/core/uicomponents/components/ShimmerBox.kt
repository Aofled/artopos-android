package ru.createsmart.artopos.core.uicomponents.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

private const val SHIMMER_DARK_EDGE_ALPHA = 0.6f
private const val SHIMMER_DARK_HIGHLIGHT_ALPHA = 0.4f

private const val SHIMMER_LIGHT_EDGE_ALPHA = 0.95f
private const val SHIMMER_LIGHT_HIGHLIGHT_ALPHA = 0.4f

@Composable
fun ShimmerBox(modifier: Modifier = Modifier) {
    val isDark = isSystemInDarkTheme()
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

    val transition = rememberInfiniteTransition(label = "shimmer")

    val progress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_progress",
    )

    Box(
        modifier = modifier
            // Optimization: 'drawBehind' runs only in the Draw phase.
            // Unlike 'background(brush)', it skips Composition and Layout phases,
            // which is critical for infinite animations (60/120 FPS).
            .drawBehind {
                val distance = size.width + size.height
                val currentOffset = distance * progress.value

                val startOffset = Offset(
                    x = currentOffset - size.width,
                    y = currentOffset - size.height,
                )
                val endOffset = Offset(
                    x = currentOffset,
                    y = currentOffset,
                )

                val brush = Brush.linearGradient(
                    colors = shimmerColors,
                    start = startOffset,
                    end = endOffset,
                )

                drawRect(brush = brush)
            },
    )
}
