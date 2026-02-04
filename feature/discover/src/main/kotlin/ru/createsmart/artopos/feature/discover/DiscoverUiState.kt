package ru.createsmart.artopos.feature.discover

import ru.createsmart.artopos.feature.discover.model.ArtworkListItem

sealed interface DiscoverUiState {
    data object Loading : DiscoverUiState
    data object Error : DiscoverUiState
    data class Success(
        val artworks: List<ArtworkListItem>,
        val isRefreshing: Boolean = false,
        val contentVersion: Int = 0, // Used to force-reload images when Pull-to-Refresh is triggered
    ) : DiscoverUiState
}
