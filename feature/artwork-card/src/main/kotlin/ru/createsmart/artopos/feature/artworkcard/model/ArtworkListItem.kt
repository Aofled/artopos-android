package ru.createsmart.artopos.feature.artworkcard.model

import androidx.compose.runtime.Immutable

// Optimization: Tells Compose that this object is stable.
// Prevents unnecessary recompositions (flickering/lag) during scroll.
@Immutable
data class ArtworkListItem(
    val id: Int,
    val title: String,
    val artist: String,
    val imageUrl: String,
    val aspectRatio: Float,
    val year: String,
)
