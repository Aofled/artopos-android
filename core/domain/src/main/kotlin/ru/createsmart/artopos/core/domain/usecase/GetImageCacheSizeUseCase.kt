package ru.createsmart.artopos.core.domain.usecase

import ru.createsmart.artopos.core.domain.repository.ImageCacheRepository
import javax.inject.Inject

public class GetImageCacheSizeUseCase @Inject constructor(
    private val repository: ImageCacheRepository,
) {
    public suspend operator fun invoke(): Long {
        return repository.getCacheSize()
    }
}
