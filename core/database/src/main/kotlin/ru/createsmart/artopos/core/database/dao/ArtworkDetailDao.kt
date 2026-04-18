package ru.createsmart.artopos.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ru.createsmart.artopos.core.database.model.ArtworkDetailsDBO
import ru.createsmart.artopos.core.database.model.ArtworkDetailsWithFavoriteFlagDBO

@Dao
interface ArtworkDetailDao {
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
