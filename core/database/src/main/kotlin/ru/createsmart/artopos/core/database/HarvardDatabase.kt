package ru.createsmart.artopos.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.createsmart.artopos.core.database.dao.ArtworkDao
import ru.createsmart.artopos.core.database.dao.ArtworkRemoteKeysDao
import ru.createsmart.artopos.core.database.dao.FilterItemDao
import ru.createsmart.artopos.core.database.model.ArtworkDBO
import ru.createsmart.artopos.core.database.model.ArtworkRemoteKeysEntity
import ru.createsmart.artopos.core.database.model.FilterItemDBO

@Database(
    entities = [
        ArtworkDBO::class,
        ArtworkRemoteKeysEntity::class,
        FilterItemDBO::class,
    ],
    version = 3,
    exportSchema = true, // Connects to Gradle config 'schemaDirectory' for Auto-Migrations
)
abstract class HarvardDatabase : RoomDatabase() {
    abstract fun artworkDao(): ArtworkDao
    abstract fun artworkRemoteKeysDao(): ArtworkRemoteKeysDao
    abstract fun filterItemDao(): FilterItemDao
}
