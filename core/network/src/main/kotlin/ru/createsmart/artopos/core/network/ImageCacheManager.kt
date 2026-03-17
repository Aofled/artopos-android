package ru.createsmart.artopos.core.network

import android.content.Context
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import ru.createsmart.artopos.core.domain.repository.ImageCacheRepository
import ru.createsmart.artopos.core.network.di.ImageClient
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageCacheManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @ImageClient private val imageOkHttpClient: OkHttpClient,
) : ImageCacheRepository {
    /**
     * The current strategy does not provide for a default coil cache.
     */
    @OptIn(ExperimentalCoilApi::class)
    override suspend fun clearCache(): Long = withContext(Dispatchers.IO) {
        var freedSpace = 0L

        // 1. Clear our custom OkHttp cache (folder "http_cache")
        val okHttpCache = imageOkHttpClient.cache
        if (okHttpCache != null) {
            try {
                freedSpace += okHttpCache.size()
                okHttpCache.evictAll()
            } catch (ignored: IOException) {
            }
        }

        // 2. Clear the default Coil caches (just in case)
        val coilImageLoader = context.imageLoader
        coilImageLoader.diskCache?.let {
            freedSpace += it.size
            it.clear()
        }
        coilImageLoader.memoryCache?.clear() // Clear RAM

        return@withContext freedSpace
    }
}
