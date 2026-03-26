package ru.createsmart.artopos.core.data.mapper

import ru.createsmart.artopos.core.database.model.ArtworkFavoriteWithDetailsDBO
import ru.createsmart.artopos.core.model.Artwork

fun ArtworkFavoriteWithDetailsDBO.toDomain(): Artwork {
    val base = favoriteArtwork.toDomain()

    val details = this.details ?: return base

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
