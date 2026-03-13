package ru.createsmart.artopos.core.data.mapper

import ru.createsmart.artopos.core.database.model.ArtworkDBO
import ru.createsmart.artopos.core.model.Artwork
import ru.createsmart.artopos.core.model.ArtworkImage

fun ArtworkDBO.toDomain(): Artwork {
    val domainImages = galleryImages?.map { stored ->
        ArtworkImage(
            url = stored.url,
            width = stored.width,
            height = stored.height,
        )
    } ?: emptyList()

    return Artwork(
        id = id,
        title = title,
        artist = artist,
        imageUrl = imageUrl,
        imageDimensions = imageDimensions,
        date = date ?: "",
        yearInt = yearInt,
        technique = technique,
        description = description,
        url = url,
        images = domainImages,
        provenance = null,
        creditLine = null,
        classification = null,
        culture = null,
        medium = null,
        period = null,
        style = null,
        dimensions = null,
        copyright = null,
        galleryLocation = null,
    )
}
