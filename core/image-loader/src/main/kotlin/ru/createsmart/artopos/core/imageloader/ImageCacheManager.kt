package ru.createsmart.artopos.core.imageloader

import coil3.ImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageCacheManager @Inject constructor(
    private val imageLoader: ImageLoader,
) {
    suspend fun clearCache(): Long = withContext(Dispatchers.IO) {
        var freedSpace = 0L

        imageLoader.diskCache?.let { diskCache ->
            freedSpace = diskCache.size
            diskCache.clear()
        }

        imageLoader.memoryCache?.clear()

        freedSpace
    }

    suspend fun getCacheSize(): Long = withContext(Dispatchers.IO) {
        // Get cache size directly from Coil without scanning disk folders.
        imageLoader.diskCache?.size ?: 0L
    }
}
