package ru.createsmart.artopos.feature.discover.mapper

import ru.createsmart.artopos.core.model.Artwork
import ru.createsmart.artopos.feature.discover.model.ArtworkListItem

fun Artwork.toUi(): ArtworkListItem {
    val dims = imageDimensions

    // UI Logic: Calculate ratio for Staggered Grid (Pinterest style).
    // Safety: Default to 1f (Square) to avoid DivisionByZero exception.
    val ratio = if (dims != null && dims.width > 0 && dims.height > 0) {
        dims.width.toFloat() / dims.height.toFloat()
    } else {
        1f
    }

    return ArtworkListItem(
        id = id,
        title = title,
        artist = artist,
        imageUrl = imageUrl,
        aspectRatio = ratio,
        year = date ?: "",
    )
}
