package ru.createsmart.artopos.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.createsmart.artopos.core.database.converters.ImagesConverter
import ru.createsmart.artopos.core.database.dao.ArtworkDao
import ru.createsmart.artopos.core.database.dao.ArtworkDetailDao
import ru.createsmart.artopos.core.database.dao.ArtworkRemoteKeysDao
import ru.createsmart.artopos.core.database.dao.FavoriteDao
import ru.createsmart.artopos.core.database.dao.FilterItemDao
import ru.createsmart.artopos.core.database.model.ArtworkDBO
import ru.createsmart.artopos.core.database.model.ArtworkDetailsDBO
import ru.createsmart.artopos.core.database.model.ArtworkRemoteKeysDBO
import ru.createsmart.artopos.core.database.model.FavoriteDBO
import ru.createsmart.artopos.core.database.model.FilterItemDBO

@Database(
    entities = [
        ArtworkDBO::class,
        ArtworkRemoteKeysDBO::class,
        ArtworkDetailsDBO::class,
        FilterItemDBO::class,
        FavoriteDBO::class,
    ],
    version = 8,
    exportSchema = true, // Connects to Gradle config 'schemaDirectory' for Auto-Migrations
    autoMigrations = [
        AutoMigration(from = 7, to = 8),
    ],
)
@TypeConverters(ImagesConverter::class)
abstract class HarvardDatabase : RoomDatabase() {
    abstract fun artworkDao(): ArtworkDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun artworkDetailDao(): ArtworkDetailDao
    abstract fun artworkRemoteKeysDao(): ArtworkRemoteKeysDao
    abstract fun filterItemDao(): FilterItemDao
}
