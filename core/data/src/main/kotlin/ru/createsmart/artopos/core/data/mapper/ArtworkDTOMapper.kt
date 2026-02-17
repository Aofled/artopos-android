package ru.createsmart.artopos.core.data.mapper

import ru.createsmart.artopos.core.database.model.ArtworkDBO
import ru.createsmart.artopos.core.model.Coordinates
import ru.createsmart.artopos.core.model.ImageDimensions
import ru.createsmart.artopos.core.network.model.ArtworkDTO

private val YEAR_REGEX = Regex("\\d{4}")

fun ArtworkDTO.toDBO(): ArtworkDBO {
    // Logic: Extract only "Artist" role, join names
    val artistName = artists
        ?.filter { it.role == "Artist" }
        ?.mapNotNull { it.name }
        ?.joinToString(", ")
        .takeIf { !it.isNullOrBlank() }
        ?: "Unknown Artist"

    // Priority: Use "images" array (better quality) -> fallback to "primaryimageurl"
    val bestImage = images?.firstOrNull()
    val finalUrl = bestImage?.url ?: imageUrl ?: ""

    val dimensions = if (bestImage != null && bestImage.width > 0 && bestImage.height > 0) {
        ImageDimensions(
            width = bestImage.width,
            height = bestImage.height,
        )
    } else {
        null
    }

    // Parsing: Extract year from string (e.g., "Circa 1880" -> 1880) for sorting
    val parsedYear = date?.let {
        YEAR_REGEX.find(it)?.value?.toIntOrNull()
    }

    val coordinates: Coordinates? = null // TODO(Coordinate): Implement coordinate parsing later

    return ArtworkDBO(
        id = id,
        title = title.ifBlank { "Untitled" },
        artist = artistName,
        imageUrl = finalUrl,
        imageDimensions = dimensions,
        date = date,
        yearInt = parsedYear,
        technique = technique,
        coordinates = coordinates,
        description = description,
        url = webUrl,
    )
}
