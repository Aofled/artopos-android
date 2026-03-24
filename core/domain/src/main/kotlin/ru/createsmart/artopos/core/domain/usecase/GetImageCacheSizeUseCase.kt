package ru.createsmart.artopos.core.domain.usecase

import ru.createsmart.artopos.core.domain.repository.ImageCacheRepository
import javax.inject.Inject

class GetImageCacheSizeUseCase @Inject constructor(
    private val repository: ImageCacheRepository,
) {
    suspend operator fun invoke(): Long {
        return repository.getCacheSize()
    }
}
