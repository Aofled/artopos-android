package ru.createsmart.artopos.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ru.createsmart.artopos.core.database.model.ArtworkFavoriteDBO
import ru.createsmart.artopos.core.database.model.ArtworkFavoriteWithDetailsDBO

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY saved_at_timestamp DESC")
    fun getFavorites(): Flow<List<ArtworkFavoriteDBO>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(artwork: ArtworkFavoriteDBO)

    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun removeFavorite(id: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :id)")
    suspend fun isFavorite(id: Int): Boolean

    @Transaction // Required for @Relation (For data consistency)
    @Query("SELECT * FROM favorites WHERE id = :id")
    fun getArtworkFavoriteWithDetails(id: Int): Flow<ArtworkFavoriteWithDetailsDBO?>
}
