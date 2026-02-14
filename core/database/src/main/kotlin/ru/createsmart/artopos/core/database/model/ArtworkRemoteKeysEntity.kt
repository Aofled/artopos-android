package ru.createsmart.artopos.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artwork_remote_keys")
data class ArtworkRemoteKeysEntity(
    @PrimaryKey val artworkId: Int,
    val prevKey: Int?,
    val nextKey: Int?,
)
