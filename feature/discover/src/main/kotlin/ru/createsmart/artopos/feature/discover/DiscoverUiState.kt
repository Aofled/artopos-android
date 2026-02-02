package ru.createsmart.artopos.feature.discover

import ru.createsmart.artopos.feature.discover.model.ArtworkListItem

sealed interface DiscoverUiState {
    data object Loading : DiscoverUiState
    data object Error : DiscoverUiState
    data class Success(
        val artworks: List<ArtworkListItem>,
        val isRefreshing: Boolean = false,
    ) : DiscoverUiState
}
