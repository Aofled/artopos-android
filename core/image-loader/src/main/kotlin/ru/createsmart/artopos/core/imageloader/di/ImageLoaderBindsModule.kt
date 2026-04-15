package ru.createsmart.artopos.core.imageloader.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.createsmart.artopos.core.domain.repository.ImageCacheRepository
import ru.createsmart.artopos.core.imageloader.ImageCacheManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface ImageLoaderBindsModule {

    @Binds
    @Singleton
    fun bindImageCacheRepository(
        impl: ImageCacheManager
    ): ImageCacheRepository
}
