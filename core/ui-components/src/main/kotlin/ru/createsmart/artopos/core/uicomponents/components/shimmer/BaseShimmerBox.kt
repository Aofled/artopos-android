package ru.createsmart.artopos.core.uicomponents.components.shimmer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * High-performance shimmer based on Modifier.Node.
 * Does not use deprecated Modifier.composed.
 * Memory is allocated once, and animation runs strictly in the Draw phase.
 */
internal fun Modifier.baseShimmer(
    shimmerColors: ShimmerColors,
    enabled: Boolean = true,
    waveSizeMultiplier: Float = 1.0f,
    durationMillis: Int = 1500,
): Modifier {
    return if (enabled) {
        this.then(
            ShimmerElement(
                shimmerColors = shimmerColors,
                waveSizeMultiplier = waveSizeMultiplier,
                durationMillis = durationMillis,
            ),
        )
    } else {
        this
    }
}

private data class ShimmerElement(
    val shimmerColors: ShimmerColors,
    val waveSizeMultiplier: Float,
    val durationMillis: Int,
) : ModifierNodeElement<ShimmerNode>() {

    override fun create(): ShimmerNode = ShimmerNode(
        shimmerColors = shimmerColors,
        waveSizeMultiplier = waveSizeMultiplier,
        durationMillis = durationMillis,
    )

    override fun update(node: ShimmerNode) {
        node.update(
            shimmerColors = shimmerColors,
            waveSizeMultiplier = waveSizeMultiplier,
            durationMillis = durationMillis,
        )
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "baseShimmer"
        properties["shimmerColors"] = shimmerColors
        properties["waveSizeMultiplier"] = waveSizeMultiplier
        properties["durationMillis"] = durationMillis
    }
}

private class ShimmerNode(
    var shimmerColors: ShimmerColors,
    var waveSizeMultiplier: Float,
    var durationMillis: Int,
) : Modifier.Node(), DrawModifierNode {

    private val progress = Animatable(0f)
    private var animationJob: Job? = null

    override fun onAttach() {
        startAnimation()
    }

    override fun onDetach() {
        animationJob?.cancel()
    }

    fun update(
        shimmerColors: ShimmerColors,
        waveSizeMultiplier: Float,
        durationMillis: Int,
    ) {
        val durationChanged = this.durationMillis != durationMillis
        this.shimmerColors = shimmerColors
        this.waveSizeMultiplier = waveSizeMultiplier
        this.durationMillis = durationMillis

        if (durationChanged) {
            startAnimation()
        }
    }

    private fun startAnimation() {
        animationJob?.cancel()
        animationJob = coroutineScope.launch {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = durationMillis,
                        easing = LinearEasing,
                    ),
                    repeatMode = RepeatMode.Restart,
                ),
            )
        }
    }

    override fun ContentDrawScope.draw() {
        // Draw shimmer background gradient
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
            colors = shimmerColors.colors,
            start = startOffset,
            end = endOffset,
        )

        drawRect(brush = brush)

        // Draw content on top if present
        drawContent()
    }
}

@Composable
internal fun BaseShimmerBox(
    shimmerColors: ShimmerColors,
    modifier: Modifier = Modifier,
    waveSizeMultiplier: Float = 1.0f,
    durationMillis: Int = 1500,
) {
    Box(
        modifier = modifier.baseShimmer(
            shimmerColors = shimmerColors,
            waveSizeMultiplier = waveSizeMultiplier,
            durationMillis = durationMillis,
        ),
    )
}

@Immutable
@JvmInline
internal value class ShimmerColors(val colors: List<Color>)
