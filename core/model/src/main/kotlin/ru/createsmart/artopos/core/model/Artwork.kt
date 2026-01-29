package ru.createsmart.artopos.core.model

data class Artwork(
    val id: Int,
    val title: String,
    val artist: String,
    val imageUrl: String,
    val imageDimensions: ImageDimensions?,
    val date: String?,
    val yearInt: Int?,
    val technique: String?,
    val coordinates: Coordinates?,
    val description: String?,
    val url: String?,
)

data class ImageDimensions(
    val width: Int,
    val height: Int,
)

data class Coordinates(
    val lat: Double,
    val lon: Double,
)
