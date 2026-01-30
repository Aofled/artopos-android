package ru.createsmart.artopos.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.createsmart.artopos.core.model.Coordinates
import ru.createsmart.artopos.core.model.ImageDimensions

@Entity(tableName = "artworks")
data class ArtworkDBO(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "artist") val artist: String,
    @ColumnInfo(name = "image_url") val imageUrl: String,
    @Embedded(prefix = "image_dimensions_") val imageDimensions: ImageDimensions?,
    @ColumnInfo(name = "date") val date: String?,
    @ColumnInfo(name = "year_int") val yearInt: Int?,
    @ColumnInfo(name = "technique") val technique: String?,
    @Embedded(prefix = "coordinates_") val coordinates: Coordinates?,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "url") val url: String?,
)
