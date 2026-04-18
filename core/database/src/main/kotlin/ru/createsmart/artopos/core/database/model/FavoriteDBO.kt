package ru.createsmart.artopos.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteDBO(
    @PrimaryKey val id: Int, // artworkId
    @ColumnInfo(name = "saved_at_timestamp") val savedAtTimestamp: Long = System.currentTimeMillis(),
)
