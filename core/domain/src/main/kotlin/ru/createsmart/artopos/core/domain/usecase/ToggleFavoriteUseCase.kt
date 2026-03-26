package ru.createsmart.artopos.core.domain.usecase

import ru.createsmart.artopos.core.domain.repository.ArtworkRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: ArtworkRepository,
) {
    suspend operator fun invoke(artworkId: Int) {
        repository.toggleFavorite(artworkId)
    }
}
