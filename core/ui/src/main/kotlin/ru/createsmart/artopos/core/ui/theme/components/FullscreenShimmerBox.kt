package ru.createsmart.artopos.core.ui.theme.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver

private const val SHIMMER_DARK_HIGHLIGHT_ALPHA = 0.15f
private const val SHIMMER_LIGHT_HIGHLIGHT_ALPHA = 0.6f

private const val SHIMMER_DURATION_MILLIS = 1500
private const val SHIMMER_WAVE_SIZE = 1.5f

@Composable
fun FullscreenShimmerBox(modifier: Modifier = Modifier) {
    val isDark = isSystemInDarkTheme()
    val baseColor = MaterialTheme.colorScheme.surfaceVariant

    val shimmerColors = remember(baseColor, isDark) {
        val edgeColor = baseColor.copy(alpha = 1f)

        val highlightColor = if (isDark) {
            Color.White.copy(alpha = SHIMMER_DARK_HIGHLIGHT_ALPHA).compositeOver(baseColor)
        } else {
            Color.White.copy(alpha = SHIMMER_LIGHT_HIGHLIGHT_ALPHA).compositeOver(baseColor)
        }

        listOf(
            edgeColor,
            highlightColor,
            highlightColor,
            highlightColor,
            edgeColor,
        )
    }

    val transition = rememberInfiniteTransition(label = "shimmer")

    val progress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = SHIMMER_DURATION_MILLIS,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_progress",
    )

    Box(
        modifier = modifier
            .drawBehind {
                val waveSize = size.width * SHIMMER_WAVE_SIZE

                val distance = waveSize + size.height
                val currentOffset = distance * progress.value

                val startOffset = Offset(
                    x = currentOffset - waveSize,
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
