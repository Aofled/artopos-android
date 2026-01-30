package ru.createsmart.artopos.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.createsmart.artopos.core.database.model.ArtworkDBO

@Dao
interface ArtworkDao {
    @Query("SELECT * FROM artworks")
    fun getArtworks(): Flow<List<ArtworkDBO>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtworks(artwork: List<ArtworkDBO>)

    @Query("DELETE FROM artworks")
    suspend fun clearArtworks()
}
