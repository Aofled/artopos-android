package ru.createsmart.artopos.feature.favorites

import kotlinx.collections.immutable.ImmutableList
import ru.createsmart.artopos.core.artworkcard.model.ArtworkListItem

sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState
    data object Empty : FavoritesUiState
    data class Success(val artworks: ImmutableList<ArtworkListItem>) : FavoritesUiState
}
