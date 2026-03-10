package ru.createsmart.artopos.feature.details

import ru.createsmart.artopos.feature.details.model.ArtworkDetailUi

sealed interface ArtworkDetailUiState {
    data object Loading : ArtworkDetailUiState
    data object Error : ArtworkDetailUiState
    data class Success(val artwork: ArtworkDetailUi) : ArtworkDetailUiState
}
