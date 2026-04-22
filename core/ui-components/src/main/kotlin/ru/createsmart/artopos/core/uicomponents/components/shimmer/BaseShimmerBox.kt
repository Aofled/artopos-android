package ru.createsmart.artopos.core.uicomponents.components.shimmer

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
internal fun BaseShimmerBox(
    shimmerColors: List<Color>,
    modifier: Modifier = Modifier,
    waveSizeMultiplier: Float = 1.0f,
    durationMillis: Int = 1500,
) {
    val transition = rememberInfiniteTransition(label = "shimmer")

    val progress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
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
                val waveSize = size.width * waveSizeMultiplier

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
