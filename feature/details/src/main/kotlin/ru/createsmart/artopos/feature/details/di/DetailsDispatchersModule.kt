package ru.createsmart.artopos.feature.details.di

import android.app.ActivityManager
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MlKitDispatcher

private const val LOW_THREADS = 3
private const val LARGE_THREADS = 8

@Module
@InstallIn(SingletonComponent::class)
object DetailsDispatchersModule {

    /**
     * Dynamic parallelism limit for ML Kit.
     * On budget devices (low RAM), we strictly limit the number of
     * simultaneously running heavy C++ threads to 3 to avoid OOMs.
     * On standard/flagship devices, we set the limit to 8,
     * to utilize all high-performance processor cores and translate text instantly.
     */

    @OptIn(ExperimentalCoroutinesApi::class)
    @Provides
    @MlKitDispatcher
    fun provideMlKitDispatcher(@ApplicationContext context: Context): CoroutineDispatcher {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val threadLimit = if (activityManager.isLowRamDevice) LOW_THREADS else LARGE_THREADS
        return Dispatchers.IO.limitedParallelism(threadLimit)
    }
}
