package ru.createsmart.artopos.extensions

import android.content.Context
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import coil3.util.DebugLogger
import okhttp3.OkHttpClient

fun Context.createCoilImageLoader(
    okHttpClient: OkHttpClient,
    isDebug: Boolean,
): ImageLoader {
    return ImageLoader.Builder(this)
        .components {
            // Important: We already handle cache headers manually in Interceptor.
            // But setting this to 'false' forces Coil to ignore server's 'Cache-Control'
            // if it conflicts with our logic.
            add(
                OkHttpNetworkFetcherFactory(
                    callFactory = { okHttpClient },
                ),
            )
        }
        .apply {
            if (isDebug) {
                logger(DebugLogger())
            }
        }
        .crossfade(true)
        .build()
}
