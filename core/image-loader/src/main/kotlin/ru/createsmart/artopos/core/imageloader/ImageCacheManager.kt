package ru.createsmart.artopos.core.imageloader

import android.content.Context
import coil3.SingletonImageLoader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageCacheManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun clearCache(): Long = withContext(Dispatchers.IO) {
        var freedSpace = 0L
        val coilImageLoader = SingletonImageLoader.get(context)

        coilImageLoader.diskCache?.let { diskCache ->
            freedSpace = diskCache.size
            diskCache.clear()
        }

        coilImageLoader.memoryCache?.clear()

        freedSpace
    }

    suspend fun getCacheSize(): Long = withContext(Dispatchers.IO) {
        val coilImageLoader = SingletonImageLoader.get(context)

        // Get cache size directly from Coil without scanning disk folders.
        coilImageLoader.diskCache?.size ?: 0L
    }
}
