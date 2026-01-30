package ru.createsmart.artopos.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.createsmart.artopos.core.database.dao.ArtworkDao
import ru.createsmart.artopos.core.database.model.ArtworkDBO

@Database(
    entities = [ArtworkDBO::class],
    version = 1,
    exportSchema = true, // Connects to Gradle config 'schemaDirectory' for Auto-Migrations
)
abstract class HarvardDatabase : RoomDatabase() {
    abstract fun artworkDao(): ArtworkDao
}
