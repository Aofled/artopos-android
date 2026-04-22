package ru.createsmart.artopos.feature.favorites

import ru.createsmart.artopos.feature.artworkcard.model.ArtworkListItem

sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState
    data object Empty : FavoritesUiState
    data class Success(val artworks: List<ArtworkListItem>) : FavoritesUiState
}
