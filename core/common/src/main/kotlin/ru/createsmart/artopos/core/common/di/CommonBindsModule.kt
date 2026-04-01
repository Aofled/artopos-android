package ru.createsmart.artopos.core.common.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.createsmart.artopos.core.common.util.AndroidImageDownloader
import ru.createsmart.artopos.core.domain.repository.ImageDownloader
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface CommonBindsModule {

    @Binds
    @Singleton
    fun bindImageDownloader(
        impl: AndroidImageDownloader,
    ): ImageDownloader
}
