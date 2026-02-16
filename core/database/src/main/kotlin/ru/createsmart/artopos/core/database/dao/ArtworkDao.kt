package ru.createsmart.artopos.core.database.dao

import androidx.annotation.VisibleForTesting
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.createsmart.artopos.core.database.model.ArtworkDBO

@Dao
interface ArtworkDao {
    @Query("SELECT * FROM artworks ORDER BY sorting_index ASC")
    fun getArtworks(): PagingSource<Int, ArtworkDBO>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtworks(artwork: List<ArtworkDBO>)

    @Query("DELETE FROM artworks")
    suspend fun clearArtworks()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @Query("SELECT * FROM artworks ORDER BY sorting_index ASC") // For ArtworkRemoteMediatorTest
    suspend fun getAllArtworksForTest(): List<ArtworkDBO>
}
