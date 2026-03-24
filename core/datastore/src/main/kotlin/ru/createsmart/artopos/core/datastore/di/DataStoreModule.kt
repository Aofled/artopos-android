package ru.createsmart.artopos.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.createsmart.artopos.core.datastore.DataStoreSettingsRepository
import ru.createsmart.artopos.core.domain.repository.SettingsRepository
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object DataStoreProvidesModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }
}

@Module
@InstallIn(SingletonComponent::class)
interface DataStoreBindsModule {

    @Binds
    @Singleton
    fun bindSettingsRepository(
        impl: DataStoreSettingsRepository,
    ): SettingsRepository
}
