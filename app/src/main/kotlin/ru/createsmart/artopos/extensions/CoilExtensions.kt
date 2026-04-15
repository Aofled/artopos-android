package ru.createsmart.artopos.extensions

import android.content.Context
import coil.ImageLoader
import coil.util.DebugLogger
import okhttp3.OkHttpClient

fun Context.createCoilImageLoader(
    okHttpClient: OkHttpClient,
    isDebug: Boolean,
): ImageLoader {
    return ImageLoader.Builder(this)
        .okHttpClient(okHttpClient)
        .apply {
            if (isDebug) {
                logger(DebugLogger())
            }
        }
        // Important: We already handle cache headers manually in Interceptor.
        // But setting this to 'false' forces Coil to ignore server's 'Cache-Control'
        // if it conflicts with our logic.
        .respectCacheHeaders(false)
        .crossfade(true)
        .build()
}
