package ru.createsmart.artopos.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Embedded

/**
 * Lightweight projection of the 'artworks' table for Paging 3 (Feed).
 * Excludes heavy columns (gallery_images, description, date)
 * to avoid JSON parsing and unnecessary memory allocations when scrolling.
 */
data class ArtworkFeedProjectionDBO(
    val id: Int,
    val title: String,
    val artist: String,
    @ColumnInfo(name = "image_url") val imageUrl: String,
    @Embedded(prefix = "image_dimensions_") val imageDimensions: ImageDimensionsDBO?,
)

data class ArtworkFeedWithFavoriteFlagDBO(
    @Embedded val artwork: ArtworkFeedProjectionDBO,
    val isFavorite: Boolean,
)
