package ru.createsmart.artopos.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Junction table for the Discover feed.
 * Stores artwork ID and its sorting index for current pagination session.
 */
@Entity(
    tableName = "discover_feed",
    foreignKeys = [
        ForeignKey(
            entity = ArtworkDBO::class,
            parentColumns = ["id"],
            childColumns = ["artwork_id"],
            onDelete = ForeignKey.CASCADE, // Automatically remove from feed when artwork is deleted
        ),
    ],
    indices = [
        Index(value = ["sorting_index"]),
    ],
)
data class DiscoverFeedItemDBO(
    @PrimaryKey
    @ColumnInfo(name = "artwork_id")
    val artworkId: Int,

    @ColumnInfo(name = "sorting_index")
    val sortingIndex: Int,
)
