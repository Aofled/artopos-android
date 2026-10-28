package ru.createsmart.artopos.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "artwork_details",
    foreignKeys = [
        ForeignKey(
            entity = ArtworkDBO::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class ArtworkDetailsDBO(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "provenance") val provenance: String?,
    @ColumnInfo(name = "credit_line") val creditLine: String?,
    @ColumnInfo(name = "classification") val classification: String?,
    @ColumnInfo(name = "century") val century: String?,
    @ColumnInfo(name = "culture") val culture: String?,
    @ColumnInfo(name = "medium") val medium: String?,
    @ColumnInfo(name = "period") val period: String?,
    @ColumnInfo(name = "style") val style: String?,
    @ColumnInfo(name = "dimensions") val dimensions: String?,
    @ColumnInfo(name = "copyright") val copyright: String?,
    @ColumnInfo(name = "gallery_location") val galleryLocation: String?,
)
