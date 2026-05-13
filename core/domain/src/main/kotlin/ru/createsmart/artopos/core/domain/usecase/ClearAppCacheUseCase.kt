package ru.createsmart.artopos.core.domain.usecase

import ru.createsmart.artopos.core.domain.repository.ArtworkRepository
import ru.createsmart.artopos.core.domain.repository.ImageCacheRepository
import javax.inject.Inject

public class ClearAppCacheUseCase @Inject constructor(
    private val imageCacheRepository: ImageCacheRepository,
    private val artworkRepository: ArtworkRepository,
) {
    /**
     * @return Number of freed megabytes (from images only).
     */
    public suspend operator fun invoke(): Long {
        // 1. Clearing the file cache (images)
        val freedBytes = imageCacheRepository.clearCache()

        // 2. Cleaning the database (details table)
        artworkRepository.clearDatabaseCache()

        return freedBytes
    }
}
