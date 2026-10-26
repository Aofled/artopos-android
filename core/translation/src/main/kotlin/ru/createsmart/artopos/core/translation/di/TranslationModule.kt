package ru.createsmart.artopos.core.translation.di

import android.app.ActivityManager
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TranslationDispatcher

private const val LOW_RAM_THREADS = 2
private const val STANDARD_THREADS = 6

@Module
@InstallIn(SingletonComponent::class)
object TranslationModule {

    // Limit threads to 2 on low RAM devices to prevent OOM. Use 6 threads on standard devices.
    @Provides
    @Singleton
    @TranslationDispatcher
    fun provideTranslationDispatcher(
        @ApplicationContext context: Context,
    ): CoroutineDispatcher {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val threadLimit = if (activityManager.isLowRamDevice) LOW_RAM_THREADS else STANDARD_THREADS
        return Dispatchers.IO.limitedParallelism(threadLimit)
    }
}
