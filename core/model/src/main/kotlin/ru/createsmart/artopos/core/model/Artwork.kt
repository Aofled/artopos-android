package ru.createsmart.artopos.core.model

/**
 * Lightweight model for feeds (Discover / Favorites).
 * Contains ONLY the data necessary to render an artwork card.
 */
data class Artwork(
    val id: Int,
    val title: String,
    val artist: String,
    val imageUrl: String,
    val imageDimensions: ImageDimensions?,
    val creationDate: CreationDate,
    val isFavorite: Boolean = false,
)

/**
 * Heavyweight model for the Details screen.
 * Contains the base [Artwork] plus all deep metadata and gallery images.
 */
data class ArtworkDetails(
    val baseArtwork: Artwork,
    val technique: String?,
    val description: String?,
    val url: String?,
    val provenance: String?,
    val creditLine: String?,
    val classification: String?,
    val century: String?,
    val culture: String?,
    val images: List<ArtworkImage> = emptyList(),
    val medium: String?,
    val period: String?,
    val style: String?,
    val dimensions: String?,
    val copyright: String?,
    val galleryLocation: String?,
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
