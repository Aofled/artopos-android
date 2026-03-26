package ru.createsmart.artopos.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.createsmart.artopos.core.database.HarvardDatabase
import ru.createsmart.artopos.core.database.dao.ArtworkDao
import ru.createsmart.artopos.core.database.dao.ArtworkRemoteKeysDao
import ru.createsmart.artopos.core.database.dao.FavoriteDao
import ru.createsmart.artopos.core.database.dao.FilterItemDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideHarvardDatabase(
        @ApplicationContext context: Context,
    ): HarvardDatabase {
        return Room.databaseBuilder(
            context,
            HarvardDatabase::class.java,
            "harvard-database",
        )
            // DANGER: Wipes data on version change. Use only in Dev!
            // TODO(Release): Remove before Release and implement Migration strategies
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    fun provideArtworkDao(database: HarvardDatabase): ArtworkDao {
        return database.artworkDao()
    }

    @Provides
    fun provideFavoriteDao(database: HarvardDatabase): FavoriteDao {
        return database.favoriteDao()
    }

    @Provides
    fun provideArtworkRemoteKeysDao(database: HarvardDatabase): ArtworkRemoteKeysDao {
        return database.artworkRemoteKeysDao()
    }

    @Provides
    fun provideFilterDao(database: HarvardDatabase): FilterItemDao {
        return database.filterItemDao()
    }
}
