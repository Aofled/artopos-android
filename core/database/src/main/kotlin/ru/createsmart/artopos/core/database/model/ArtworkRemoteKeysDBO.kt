package ru.createsmart.artopos.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artwork_remote_keys")
data class ArtworkRemoteKeysDBO(
    @PrimaryKey val artworkId: Int,
    val prevKey: Int?,
    val nextKey: Int?,
)
