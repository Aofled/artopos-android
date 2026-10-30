package ru.createsmart.artopos.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import ru.createsmart.artopos.core.database.converters.ImagesConverter
import ru.createsmart.artopos.core.database.dao.ArtworkDao
import ru.createsmart.artopos.core.database.dao.ArtworkDetailDao
import ru.createsmart.artopos.core.database.dao.ArtworkRemoteKeysDao
import ru.createsmart.artopos.core.database.dao.FavoriteDao
import ru.createsmart.artopos.core.database.dao.FilterItemDao
import ru.createsmart.artopos.core.database.model.ArtworkDBO
import ru.createsmart.artopos.core.database.model.ArtworkDetailsDBO
import ru.createsmart.artopos.core.database.model.ArtworkRemoteKeysDBO
import ru.createsmart.artopos.core.database.model.DiscoverFeedItemDBO
import ru.createsmart.artopos.core.database.model.FavoriteDBO
import ru.createsmart.artopos.core.database.model.FilterItemDBO

@Database(
    entities = [
        ArtworkDBO::class,
        ArtworkRemoteKeysDBO::class,
        ArtworkDetailsDBO::class,
        FilterItemDBO::class,
        FavoriteDBO::class,
        DiscoverFeedItemDBO::class,
    ],
    version = 11,
    exportSchema = true, // Connects to Gradle config 'schemaDirectory' for Auto-Migrations
    autoMigrations = [
        AutoMigration(from = 7, to = 8),
        AutoMigration(from = 8, to = 9),
        AutoMigration(from = 9, to = 10),
        AutoMigration(
            from = 10,
            to = 11,
            spec = HarvardDatabase.MigrationSpec10To11::class,
        ),
    ],
)
@TypeConverters(ImagesConverter::class)
abstract class HarvardDatabase : RoomDatabase() {
    abstract fun artworkDao(): ArtworkDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun artworkDetailDao(): ArtworkDetailDao
    abstract fun artworkRemoteKeysDao(): ArtworkRemoteKeysDao
    abstract fun filterItemDao(): FilterItemDao

    // AutoMigration specification for removing redundant columns from artworks table
    @DeleteColumn(tableName = "artworks", columnName = "sorting_index")
    @DeleteColumn(tableName = "artworks", columnName = "in_discover_feed")
    class MigrationSpec10To11 : AutoMigrationSpec
}
