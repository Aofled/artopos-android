package ru.createsmart.artopos.core.database.model

import androidx.room.ColumnInfo

data class ImageDimensionsDBO(
    @ColumnInfo(name = "width") val width: Int,
    @ColumnInfo(name = "height") val height: Int,
)
