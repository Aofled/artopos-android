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
    val description: String?,
    val url: String?,
    // Details
    val provenance: String? = null,
    val creditLine: String? = null,
    val classification: String? = null,
    val century: String? = null,
    val culture: String? = null,
    val images: List<ArtworkImage> = emptyList(),
    val medium: String? = null,
    val period: String? = null,
    val style: String? = null,
    val dimensions: String? = null,
    val copyright: String? = null,
    val galleryLocation: String? = null,
    val isFavorite: Boolean = false,
)

data class ImageDimensions(
    val width: Int,
    val height: Int,
)

data class ArtworkImage(
    val url: String,
    val width: Int,
    val height: Int,
) {
    val aspectRatio: Float
        get() = if (height > 0) width.toFloat() / height.toFloat() else 1f
}
