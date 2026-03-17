package ru.createsmart.artopos.core.network.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.createsmart.artopos.core.domain.repository.ImageCacheRepository
import ru.createsmart.artopos.core.network.ImageCacheManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface NetworkBindsModule {

    @Binds
    @Singleton
    fun bindImageCacheRepository(
        impl: ImageCacheManager,
    ): ImageCacheRepository
}
