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
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private const val CONNECT_TIMEOUT = 15L
private const val READ_TIMEOUT = 15L

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = NetworkJson

    @Provides
    @Singleton
    @ApiClient // Use a specific Qualifier to inject THIS client into API, not the Coil client
    fun provideOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)

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
    fun provideRetrofit(
        @ApiClient client: OkHttpClient,
        json: Json,
    ): Retrofit {
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
