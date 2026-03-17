package ru.createsmart.artopos.core.network.image

import android.content.Context
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import ru.createsmart.artopos.core.domain.repository.ImageCacheRepository
import ru.createsmart.artopos.core.network.di.ImageClient
import java.io.File
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

    override suspend fun getCacheSize(): Long = withContext(Dispatchers.IO) {
        var totalSize = 0L

        // 1. Calculating the size of the http_cache folder (our custom OkHttp)
        val httpCacheDir = File(context.cacheDir, "http_cache")
        if (httpCacheDir.exists()) {
            totalSize += getFolderSize(httpCacheDir)
        }

        // 2. Calculate the size of the image_cache folder (the default Coil folder)
        val coilCacheDir = File(context.cacheDir, "image_cache")
        if (coilCacheDir.exists()) {
            totalSize += getFolderSize(coilCacheDir)
        }

        return@withContext totalSize
    }

    private fun getFolderSize(folder: File): Long {
        var size = 0L
        val files = folder.listFiles() ?: return 0L
        for (file in files) {
            size += if (file.isDirectory) {
                getFolderSize(file)
            } else {
                file.length()
            }
        }
        return size
    }
}
