package ru.createsmart.artopos.core.database.dao

import androidx.annotation.VisibleForTesting
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ru.createsmart.artopos.core.database.model.ArtworkDBO
import ru.createsmart.artopos.core.database.model.ArtworkDetailsDBO
import ru.createsmart.artopos.core.database.model.ArtworkWithDetailsDBO

@Dao
interface ArtworkDao {
    // Reactive stream: Updates UI automatically when DB changes
    @Query("SELECT * FROM artworks ORDER BY sorting_index ASC")
    fun getArtworks(): PagingSource<Int, ArtworkDBO>

    // Cache strategy: Overwrite old data with new data from API
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtworks(artwork: List<ArtworkDBO>)

    @Query("DELETE FROM artworks")
    suspend fun clearArtworks()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @Query("SELECT * FROM artworks ORDER BY sorting_index ASC") // For ArtworkRemoteMediatorTest
    suspend fun getAllArtworksForTest(): List<ArtworkDBO>

    @Transaction // Required for @Relation
    @Query("SELECT * FROM artworks WHERE id = :id")
    fun getArtworkWithDetails(id: Int): Flow<ArtworkWithDetailsDBO?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetails(details: ArtworkDetailsDBO)
}
