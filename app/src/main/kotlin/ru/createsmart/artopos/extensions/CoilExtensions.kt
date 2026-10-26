package ru.createsmart.artopos.extensions

import android.content.Context
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import coil3.util.DebugLogger
import okhttp3.OkHttpClient

private const val COIL_DISK_CACHE_MB = 512L
private const val BYTES_IN_MB = 1024L * 1024L

fun Context.createCoilImageLoader(
    okHttpClient: OkHttpClient,
    isDebug: Boolean,
): ImageLoader {
    return ImageLoader.Builder(this)
        .components {
            add(
                OkHttpNetworkFetcherFactory(
                    callFactory = { okHttpClient },
                ),
            )
        }
        // Single disk cache for images.
        .diskCache {
            DiskCache.Builder()
                .directory(cacheDir.resolve("image_cache"))
                .maxSizeBytes(COIL_DISK_CACHE_MB * BYTES_IN_MB)
                .build()
        }
        .apply {
            if (isDebug) {
                logger(DebugLogger())
            }
        }
        .crossfade(true)
        .build()
}
