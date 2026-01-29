package ru.createsmart.artopos.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import ru.createsmart.artopos.core.network.BuildConfig
import ru.createsmart.artopos.core.network.api.HarvardAPI
import ru.createsmart.artopos.core.network.interceptor.HarvardApiKeyInterceptor
import javax.inject.Singleton

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
