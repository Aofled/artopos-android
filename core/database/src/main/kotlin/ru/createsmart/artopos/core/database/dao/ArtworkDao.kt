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
import ru.createsmart.artopos.core.database.model.ArtworkDetailsWithFavoriteFlagDBO
import ru.createsmart.artopos.core.database.model.ArtworkWithFavoriteFlagDBO

@Dao
interface ArtworkDao {
    // Reactive stream: Updates UI automatically when DB changes
    // EXISTS - Looking for the first match
    @Query(
        """
        SELECT *, 
               EXISTS(SELECT 1 FROM favorites WHERE favorites.id = artworks.id) AS isFavorite 
        FROM artworks 
        ORDER BY sorting_index ASC
    """,
    )
    fun getArtworksWithFavoriteFlags(): PagingSource<Int, ArtworkWithFavoriteFlagDBO>

    @Query("SELECT * FROM artworks WHERE id = :id")
    suspend fun getArtworkSnapshot(id: Int): ArtworkDBO?

    // Cache strategy: Overwrite old data with new data from API
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtworks(artwork: List<ArtworkDBO>)

    @Query("DELETE FROM artworks")
    suspend fun clearArtworks()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @Query("SELECT * FROM artworks ORDER BY sorting_index ASC") // For ArtworkRemoteMediatorTest
    suspend fun getAllArtworksForTest(): List<ArtworkDBO>

    @Transaction // Required for @Relation (For data consistency)
    // EXISTS - Looking for the first match
    @Query(
        """
        SELECT *, 
               EXISTS(SELECT 1 FROM favorites WHERE favorites.id = artworks.id) AS isFavorite 
        FROM artworks 
        WHERE id = :id
    """,
    )
    fun getArtworkWithDetails(id: Int): Flow<ArtworkDetailsWithFavoriteFlagDBO?>

    // Clears artwork_details that are not associated with the Discover or Favorites.
    @Query(
        """
        DELETE FROM artwork_details 
        WHERE id NOT IN (SELECT id FROM artworks) 
          AND id NOT IN (SELECT id FROM favorites)
    """,
    )
    suspend fun clearOrphanedDetails(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetails(details: ArtworkDetailsDBO)
}
