package ru.createsmart.artopos.core.database.dao

import androidx.annotation.VisibleForTesting
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import ru.createsmart.artopos.core.database.model.ArtworkDBO
import ru.createsmart.artopos.core.database.model.ArtworkFeedWithFavoriteFlagDBO
import ru.createsmart.artopos.core.database.model.DiscoverFeedItemDBO

@Dao
interface ArtworkDao {

    // Select feed artworks via INNER JOIN on discover_feed ordered by session sorting index
    @Query(
        """
        SELECT artworks.id, artworks.title, artworks.artist, artworks.image_url, 
               artworks.image_dimensions_width, artworks.image_dimensions_height, 
               EXISTS(SELECT 1 FROM favorites WHERE favorites.id = artworks.id) AS isFavorite 
        FROM artworks 
        INNER JOIN discover_feed ON artworks.id = discover_feed.artwork_id 
        ORDER BY discover_feed.sorting_index ASC
    """,
    )
    fun getArtworksWithFavoriteFlags(): PagingSource<Int, ArtworkFeedWithFavoriteFlagDBO>

    @Query("SELECT * FROM artworks WHERE id = :id")
    suspend fun getArtworkSnapshot(id: Int): ArtworkDBO?

    // Cache strategy: Overwrite old data with new data from API
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtworks(artwork: List<ArtworkDBO>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertArtworkIfAbsent(artwork: ArtworkDBO)

    // --- CLEANUP LOGIC ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscoverFeedItems(items: List<DiscoverFeedItemDBO>)

    @Query("DELETE FROM discover_feed")
    suspend fun clearDiscoverFeed()

    // Removes artworks from catalog except favorited ones
    @Query("DELETE FROM artworks WHERE id NOT IN (SELECT id FROM favorites)")
    suspend fun deleteNonFavoriteArtworks()

    // Clears discover feed and removes non-favorite artworks on refresh
    @Transaction
    suspend fun clearArtworks() {
        clearDiscoverFeed()
        deleteNonFavoriteArtworks()
    }

    // --- TEST LOGIC ---
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @Query("SELECT * FROM artworks ORDER BY id ASC")
    suspend fun getAllArtworksForTest(): List<ArtworkDBO>

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @Query("SELECT * FROM discover_feed ORDER BY sorting_index ASC")
    suspend fun getAllDiscoverFeedItemsForTest(): List<DiscoverFeedItemDBO>
}
