package ru.createsmart.artopos.core.data.mapper

import ru.createsmart.artopos.core.database.model.ArtworkDetailsDBO
import ru.createsmart.artopos.core.network.model.ArtworkDTO

fun ArtworkDTO.toDetailsDBO(): ArtworkDetailsDBO {
    return ArtworkDetailsDBO(
        id = id,
        provenance = provenance,
        creditLine = creditLine,
        classification = classification,
        century = century,
        culture = culture,
        medium = medium,
        period = period,
        style = style,
        dimensions = dimensions,
        copyright = copyright,
        galleryLocation = gallery?.name,
    )
}
