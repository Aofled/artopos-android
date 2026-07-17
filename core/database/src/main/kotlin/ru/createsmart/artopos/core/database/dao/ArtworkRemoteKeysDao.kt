package ru.createsmart.artopos.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.createsmart.artopos.core.database.model.ArtworkRemoteKeysDBO

@Dao
interface ArtworkRemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<ArtworkRemoteKeysDBO>)

    @Query("SELECT * FROM artwork_remote_keys WHERE artworkId= :artworkId")
    suspend fun remoteKeyArtworkId(artworkId: Int): ArtworkRemoteKeysDBO?

    @Query("DELETE FROM artwork_remote_keys")
    suspend fun clearRemoteKeys()
}
