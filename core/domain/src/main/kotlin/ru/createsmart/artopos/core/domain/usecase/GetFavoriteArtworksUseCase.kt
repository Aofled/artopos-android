package ru.createsmart.artopos.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.createsmart.artopos.core.domain.repository.ArtworkRepository
import ru.createsmart.artopos.core.model.Artwork
import javax.inject.Inject

public class GetFavoriteArtworksUseCase @Inject constructor(
    private val repository: ArtworkRepository,
) {
    public operator fun invoke(): Flow<List<Artwork>> {
        return repository.getFavoriteArtworks()
    }
}
