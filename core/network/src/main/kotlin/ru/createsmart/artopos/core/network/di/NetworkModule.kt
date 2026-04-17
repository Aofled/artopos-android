package ru.createsmart.artopos.core.network.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import ru.createsmart.artopos.core.network.BuildConfig
import ru.createsmart.artopos.core.network.api.HarvardAPI
import ru.createsmart.artopos.core.network.interceptor.CacheControlInterceptor
import ru.createsmart.artopos.core.network.interceptor.HarvardApiKeyInterceptor
import java.io.File
import javax.inject.Singleton

private const val CACHE_SIZE_MB = 200L
private const val BYTES_IN_KB = 1024L

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true // Stability: Don't crash if API adds new fields
        coerceInputValues = true // Stability: Convert nulls/errors to default values (safe parsing)
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()

        // Auth: Auto-inject API Key into every request
        builder.addInterceptor(HarvardApiKeyInterceptor(BuildConfig.API_KEY))

        if (BuildConfig.DEBUG) {
            // Security: Log body only in Debug. NEVER in Release (Performance + Privacy)
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }
        return builder.build()
    }

    @Provides
    @Singleton
    @ImageClient // Use a specific Qualifier to inject THIS client into Coil, not the API client
    fun provideImageOkHttpClient(
        @ApplicationContext context: Context,
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()

        // Dedicated Cache: 200MB on disk.
        // Separated from API cache to prevent images from pushing out small JSON responses.
        val cacheDir = File(context.cacheDir, "http_cache")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        val cache = Cache(cacheDir, CACHE_SIZE_MB * BYTES_IN_KB * BYTES_IN_KB)

        builder.cache(cache)

        // Force cache headers even if server says "no-cache"
        builder.addNetworkInterceptor(CacheControlInterceptor())

        if (BuildConfig.DEBUG) {
            // Security: Log body only in Debug. NEVER in Release (Performance + Privacy)
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.NONE
            }
            builder.addInterceptor(loggingInterceptor)
        }
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.HARVARD_API_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideHarvardApi(retrofit: Retrofit): HarvardAPI {
        return retrofit.create(HarvardAPI::class.java)
    }
}
