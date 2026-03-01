package ru.createsmart.artopos.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.createsmart.artopos.core.domain.repository.ArtworkRepository
import ru.createsmart.artopos.core.model.Artwork
import javax.inject.Inject

class GetArtworkDetailsUseCase @Inject constructor(
    private val repository: ArtworkRepository,
) {
    operator fun invoke(id: Int): Flow<Artwork?> {
        return repository.getArtwork(id)
    }
}
