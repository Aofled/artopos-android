package ru.createsmart.artopos.core.datastore.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.createsmart.artopos.core.datastore.DataStoreSettingsRepository
import ru.createsmart.artopos.core.datastore.SettingsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataStoreModule {

    @Binds
    @Singleton
    fun bindSettingsRepository(
        impl: DataStoreSettingsRepository,
    ): SettingsRepository
}
