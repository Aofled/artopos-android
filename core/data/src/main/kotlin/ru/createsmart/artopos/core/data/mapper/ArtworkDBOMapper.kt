package ru.createsmart.artopos.core.data.mapper

import ru.createsmart.artopos.core.database.model.ArtworkDBO
import ru.createsmart.artopos.core.model.Artwork
import ru.createsmart.artopos.core.model.ImageDimensions

fun ArtworkDBO.toDomain(): Artwork {
    return Artwork(
        id = id,
        title = title,
        artist = artist,
        imageUrl = imageUrl,
        imageDimensions = imageDimensions ?: ImageDimensions(1, 1),
        date = date ?: "",
        yearInt = yearInt,
        technique = technique,
        coordinates = coordinates,
        description = description,
        url = url,
    )
}
