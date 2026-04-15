package ru.createsmart.artopos.core.data.repository

import ru.createsmart.artopos.core.domain.repository.ImageCacheRepository
import ru.createsmart.artopos.core.imageloader.ImageCacheManager
import javax.inject.Inject

class ImageCacheRepositoryImpl @Inject constructor(
    private val cacheManager: ImageCacheManager,
) : ImageCacheRepository {

    override suspend fun clearCache(): Long {
        return cacheManager.clearCache()
    }

    override suspend fun getCacheSize(): Long {
        return cacheManager.getCacheSize()
    }
}
