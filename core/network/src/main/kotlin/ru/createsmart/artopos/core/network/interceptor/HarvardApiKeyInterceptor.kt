package ru.createsmart.artopos.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

// Centralized logic to add "?apikey=ABC" query param to all requests
internal class HarvardApiKeyInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalHttpUrl = originalRequest.url

        val newUrl = originalHttpUrl.newBuilder()
            .addQueryParameter("apikey", apiKey)
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}
