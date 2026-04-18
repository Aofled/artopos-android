package ru.createsmart.artopos.core.database.dao

import androidx.annotation.VisibleForTesting
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import ru.createsmart.artopos.core.database.model.ArtworkDBO
import ru.createsmart.artopos.core.database.model.ArtworkWithFavoriteFlagDBO

@Dao
interface ArtworkDao {
    // Reactive stream: Updates UI automatically when DB changes
    // EXISTS - Looking for the first match
    // Take only those pictures that relate to the CURRENT feed
    @Query(
        """
        SELECT *, 
               EXISTS(SELECT 1 FROM favorites WHERE favorites.id = artworks.id) AS isFavorite 
        FROM artworks 
        WHERE in_discover_feed = 1 
        ORDER BY sorting_index ASC
    """,
    )
    fun getArtworksWithFavoriteFlags(): PagingSource<Int, ArtworkWithFavoriteFlagDBO>

    @Query("SELECT * FROM artworks WHERE id = :id")
    suspend fun getArtworkSnapshot(id: Int): ArtworkDBO?

    // Cache strategy: Overwrite old data with new data from API
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtworks(artwork: List<ArtworkDBO>)

    // --- CLEANUP LOGIC ---

    // Reset the ribbon flag for ALL paintings
    @Query("UPDATE artworks SET in_discover_feed = 0")
    suspend fun resetDiscoverFeedFlag()

    // Remove "orphans": those that are no longer in the feed and are not in favorites
    @Query("DELETE FROM artworks WHERE in_discover_feed = 0 AND id NOT IN (SELECT id FROM favorites)")
    suspend fun deleteOrphanedArtworks()

    // 1. For automatic cleaning (Garbage Collector).
    // Remove painting parts that are no longer physically present in the artworks table.
    @Query("DELETE FROM artwork_details WHERE id NOT IN (SELECT id FROM artworks)")
    suspend fun deleteOrphanedDetails()

    // 2. For manual cleaning from Settings.
    // Delete ALL details, leaving only those that are linked to Favorites
    @Query("DELETE FROM artwork_details WHERE id NOT IN (SELECT id FROM favorites)")
    suspend fun clearDetailsCacheFromSettings(): Int

    @Transaction
    suspend fun clearArtworks() {
        resetDiscoverFeedFlag()
        deleteOrphanedArtworks()
        deleteOrphanedDetails()
    }

    // --- TEST LOGIC ---

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @Query("SELECT * FROM artworks ORDER BY sorting_index ASC") // For ArtworkRemoteMediatorTest
    suspend fun getAllArtworksForTest(): List<ArtworkDBO>
}
