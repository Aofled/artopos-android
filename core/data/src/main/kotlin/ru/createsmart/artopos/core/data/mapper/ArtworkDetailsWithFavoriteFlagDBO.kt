package ru.createsmart.artopos.core.data.mapper

import ru.createsmart.artopos.core.database.model.ArtworkDetailsWithFavoriteFlagDBO
import ru.createsmart.artopos.core.model.Artwork

fun ArtworkDetailsWithFavoriteFlagDBO.toDomain(): Artwork {
    val base = this.artworkWithDetails.artwork.toDomain().copy(
        isFavorite = this.isFavorite,
    )

    val details = this.artworkWithDetails.details ?: return base

    return base.copy(
        provenance = details.provenance,
        creditLine = details.creditLine,
        classification = details.classification,
        century = details.century,
        culture = details.culture,
        medium = details.medium,
        period = details.period,
        style = details.style,
        dimensions = details.dimensions,
        copyright = details.copyright,
        galleryLocation = details.galleryLocation,
    )
}
