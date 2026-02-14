package ru.createsmart.artopos.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.createsmart.artopos.core.database.dao.ArtworkDao
import ru.createsmart.artopos.core.database.dao.ArtworkRemoteKeysDao
import ru.createsmart.artopos.core.database.model.ArtworkDBO
import ru.createsmart.artopos.core.database.model.ArtworkRemoteKeysEntity

@Database(
    entities = [ArtworkDBO::class, ArtworkRemoteKeysEntity::class],
    version = 2,
    exportSchema = true, // Connects to Gradle config 'schemaDirectory' for Auto-Migrations
)
abstract class HarvardDatabase : RoomDatabase() {
    abstract fun artworkDao(): ArtworkDao
    abstract fun artworkRemoteKeysDao(): ArtworkRemoteKeysDao
}
