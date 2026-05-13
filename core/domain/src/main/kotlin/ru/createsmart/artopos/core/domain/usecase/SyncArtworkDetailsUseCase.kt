package ru.createsmart.artopos.core.domain.usecase

import ru.createsmart.artopos.core.domain.repository.ArtworkRepository
import javax.inject.Inject

public class SyncArtworkDetailsUseCase @Inject constructor(
    private val repository: ArtworkRepository,
) {
    public suspend operator fun invoke(id: Int): Result<Unit> {
        return repository.syncArtworkDetails(id)
    }
}
