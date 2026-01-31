package ru.createsmart.artopos.core.domain.usecase

import ru.createsmart.artopos.core.domain.repository.ArtworkRepository
import javax.inject.Inject

class SyncArtworksUseCase @Inject constructor(
    private val repository: ArtworkRepository,
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.refreshArtwork()
    }
}
