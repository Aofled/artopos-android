package ru.createsmart.artopos.core.imageloader.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ru.createsmart.artopos.core.imageloader.interceptor.CacheControlInterceptor
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ImageClient

private const val CACHE_SIZE_MB = 512L
private const val BYTES_IN_KB = 1024L
private const val CONNECT_TIMEOUT = 15L
private const val READ_TIMEOUT = 30L

@Module
@InstallIn(SingletonComponent::class)
object ImageLoaderModule {

    @Provides
    @Singleton
    @ImageClient
    fun provideImageOkHttpClient(
        @ApplicationContext context: Context,
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)

        // Dedicated Cache: 300MB on disk.
        val cacheDir = File(context.cacheDir, "http_cache")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        val cache = Cache(cacheDir, CACHE_SIZE_MB * BYTES_IN_KB * BYTES_IN_KB)

        builder.cache(cache)

        // Force cache headers even if server says "no-cache"
        builder.addNetworkInterceptor(CacheControlInterceptor())

        val isDebug = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebug) {
            // Security: Log body only in Debug. NEVER in Release (Performance + Privacy)
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            builder.addInterceptor(loggingInterceptor)
        }
        return builder.build()
    }
}
