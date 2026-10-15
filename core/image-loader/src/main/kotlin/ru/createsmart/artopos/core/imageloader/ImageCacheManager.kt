package ru.createsmart.artopos.core.imageloader

import android.content.Context
import android.util.Log
import coil3.SingletonImageLoader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import ru.createsmart.artopos.core.imageloader.di.ImageClient
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageCacheManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @ImageClient private val imageOkHttpClient: OkHttpClient,
) {
    /**
     * The current strategy does not provide for a default coil cache.
     */
    suspend fun clearCache(): Long = withContext(Dispatchers.IO) {
        var freedSpace = 0L

        val okHttpCache = imageOkHttpClient.cache
        if (okHttpCache != null) {
            try {
                freedSpace += okHttpCache.size()
                okHttpCache.evictAll()
            } catch (e: IOException) {
                Log.w("ImageCacheManager", "Failed to clear OkHttp cache", e)
            }
        }

        val coilImageLoader = SingletonImageLoader.get(context)

        coilImageLoader.diskCache?.let { diskCache ->
            freedSpace += diskCache.size
            diskCache.clear()
        }
        coilImageLoader.memoryCache?.clear()

        return@withContext freedSpace
    }

    suspend fun getCacheSize(): Long = withContext(Dispatchers.IO) {
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

    private fun getFolderSize(folder: File): Long =
        folder.walkTopDown().filter { it.isFile }.sumOf { it.length() }
}
