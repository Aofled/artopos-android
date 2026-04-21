package ru.createsmart.artopos.feature.artworkcard.model

import androidx.compose.runtime.Immutable
import ru.createsmart.artopos.core.designsystem.components.UiText

// Optimization: Tells Compose that this object is stable.
// Prevents unnecessary recompositions (flickering/lag) during scroll.
@Immutable
data class ArtworkListItem(
    val id: Int,
    val title: UiText,
    val artist: UiText,
    val imageUrl: String,
    val aspectRatio: Float,
    val year: String,
    val isFavorite: Boolean,
)
