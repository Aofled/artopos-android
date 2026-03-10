package ru.createsmart.artopos.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.createsmart.artopos.core.database.converters.ImagesConverter
import ru.createsmart.artopos.core.database.dao.ArtworkDao
import ru.createsmart.artopos.core.database.dao.ArtworkRemoteKeysDao
import ru.createsmart.artopos.core.database.dao.FilterItemDao
import ru.createsmart.artopos.core.database.model.ArtworkDBO
import ru.createsmart.artopos.core.database.model.ArtworkDetailsDBO
import ru.createsmart.artopos.core.database.model.ArtworkRemoteKeysEntity
import ru.createsmart.artopos.core.database.model.FilterItemDBO

@Database(
    entities = [
        ArtworkDBO::class,
        ArtworkRemoteKeysEntity::class,
        ArtworkDetailsDBO::class,
        FilterItemDBO::class,
    ],
    version = 5,
    exportSchema = true, // Connects to Gradle config 'schemaDirectory' for Auto-Migrations
)
@TypeConverters(ImagesConverter::class)
abstract class HarvardDatabase : RoomDatabase() {
    abstract fun artworkDao(): ArtworkDao
    abstract fun artworkRemoteKeysDao(): ArtworkRemoteKeysDao
    abstract fun filterItemDao(): FilterItemDao
}
