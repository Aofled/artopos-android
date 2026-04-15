package ru.createsmart.artopos.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.createsmart.artopos.core.data.repository.ImageCacheRepositoryImpl
import ru.createsmart.artopos.core.data.repository.ImageDownloaderImpl
import ru.createsmart.artopos.core.data.repository.OfflineFirstArtworkRepository
import ru.createsmart.artopos.core.data.repository.OfflineFirstFilterRepository
import ru.createsmart.artopos.core.data.repository.SettingsRepositoryImpl
import ru.createsmart.artopos.core.data.repository.TextTranslatorImpl
import ru.createsmart.artopos.core.domain.repository.ArtworkRepository
import ru.createsmart.artopos.core.domain.repository.FilterRepository
import ru.createsmart.artopos.core.domain.repository.ImageCacheRepository
import ru.createsmart.artopos.core.domain.repository.ImageDownloader
import ru.createsmart.artopos.core.domain.repository.SettingsRepository
import ru.createsmart.artopos.core.domain.translation.TextTranslator
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    @Singleton
    fun bindArtworkRepository(
        impl: OfflineFirstArtworkRepository,
    ): ArtworkRepository

    @Binds
    @Singleton
    fun bindFilterRepository(
        impl: OfflineFirstFilterRepository,
    ): FilterRepository

    @Binds
    @Singleton
    fun bindImageDownloader(
        impl: ImageDownloaderImpl,
    ): ImageDownloader

    @Binds
    @Singleton
    fun bindImageCacheRepository(
        impl: ImageCacheRepositoryImpl,
    ): ImageCacheRepository

    @Binds
    @Singleton
    fun bindSettingsRepository(
        impl: SettingsRepositoryImpl,
    ): SettingsRepository

    @Binds
    @Singleton
    fun bindTextTranslator(
        impl: TextTranslatorImpl,
    ): TextTranslator
}
