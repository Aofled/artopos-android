package ru.createsmart.artopos.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import kotlin.time.Duration.Companion.days

private val MAX_AGE_SECONDS = 7.days.inWholeSeconds

internal class CacheControlInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        // Cache Strategy: Force cache images for 7 days
        val maxAge = MAX_AGE_SECONDS

        return response.newBuilder()
            // Override server headers. We trust our cache more than the server here.
            .header("Cache-Control", "public, max-age=$maxAge")
            .removeHeader("Pragma")
            .build()
    }
}
