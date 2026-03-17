package ru.createsmart.artopos.core.domain.usecase

import ru.createsmart.artopos.core.domain.repository.ImageCacheRepository
import javax.inject.Inject

class ClearImageCacheUseCase @Inject constructor(
    private val repository: ImageCacheRepository,
) {
    suspend operator fun invoke(): Long = repository.clearCache()
}
