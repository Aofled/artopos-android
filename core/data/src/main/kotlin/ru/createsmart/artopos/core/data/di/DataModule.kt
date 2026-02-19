package ru.createsmart.artopos.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.createsmart.artopos.core.data.repository.OfflineFirstArtworkRepository
import ru.createsmart.artopos.core.data.repository.OfflineFirstFilterRepository
import ru.createsmart.artopos.core.domain.repository.ArtworkRepository
import ru.createsmart.artopos.core.domain.repository.FilterRepository
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
}
