package ru.createsmart.artopos.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.createsmart.artopos.core.database.model.ArtworkWithFavoriteFlagDBO
import ru.createsmart.artopos.core.database.model.FavoriteDBO

@Dao
interface FavoriteDao {
    @Query(
        """
        SELECT artworks.*, 1 AS isFavorite 
        FROM artworks 
        INNER JOIN favorites ON artworks.id = favorites.id 
        ORDER BY favorites.saved_at_timestamp DESC
    """,
    )
    fun getFavorites(): Flow<List<ArtworkWithFavoriteFlagDBO>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteDBO)

    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun removeFavorite(id: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :id)")
    suspend fun isFavorite(id: Int): Boolean
}
