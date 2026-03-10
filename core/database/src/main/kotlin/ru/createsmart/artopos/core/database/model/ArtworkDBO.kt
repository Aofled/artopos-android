package ru.createsmart.artopos.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.createsmart.artopos.core.database.converters.StoredImage
import ru.createsmart.artopos.core.model.ImageDimensions

@Entity(tableName = "artworks")
data class ArtworkDBO(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "sorting_index") val sortingIndex: Int = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "artist") val artist: String,
    @ColumnInfo(name = "image_url") val imageUrl: String,
    @Embedded(prefix = "image_dimensions_") val imageDimensions: ImageDimensions?,
    @ColumnInfo(name = "date") val date: String?,
    @ColumnInfo(name = "year_int") val yearInt: Int?,
    @ColumnInfo(name = "technique") val technique: String?,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "url") val url: String?,
    @ColumnInfo(name = "gallery_images") val galleryImages: List<StoredImage>?,
)
