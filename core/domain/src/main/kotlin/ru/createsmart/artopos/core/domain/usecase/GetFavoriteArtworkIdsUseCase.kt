package ru.createsmart.artopos.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.createsmart.artopos.core.domain.repository.ArtworkRepository
import javax.inject.Inject

public class GetFavoriteArtworkIdsUseCase @Inject constructor(
    private val repository: ArtworkRepository,
) {
    public operator fun invoke(): Flow<Set<Int>> {
        return repository.getFavoriteArtworkIds()
    }
}
