package ru.createsmart.artopos.feature.details.mapper

import ru.createsmart.artopos.core.model.Artwork
import ru.createsmart.artopos.feature.details.R
import ru.createsmart.artopos.feature.details.model.ArtworkDetailUi
import ru.createsmart.artopos.feature.details.model.DetailItem
import ru.createsmart.artopos.feature.details.model.GalleryImageUi

fun Artwork.toDetailUi(): ArtworkDetailUi {
    val detailsList = buildList {
        fun addIfNotNull(labelResId: Int, value: String?, isWide: Boolean = false) {
            if (!value.isNullOrBlank()) {
                add(DetailItem(UiText.StringResource(labelResId), value, isWide))
            }
        }

        addIfNotNull(R.string.details_date, date)
        addIfNotNull(R.string.details_culture, culture)

        addIfNotNull(R.string.details_period, period)
        addIfNotNull(R.string.details_style, style)

        addIfNotNull(R.string.details_classification, classification)
        addIfNotNull(R.string.details_century, century)

        addIfNotNull(R.string.details_medium, medium ?: technique, isWide = true)

        val cleanDimensions = dimensions
            ?.replace("\r\n", " ")
            ?.replace(Regex("painting proper:"), "")
            ?.trim()

        addIfNotNull(R.string.details_dimensions, cleanDimensions, isWide = true)
        addIfNotNull(R.string.details_gallery, galleryLocation, isWide = true)
        addIfNotNull(R.string.details_credit_line, creditLine, isWide = true)
        addIfNotNull(R.string.details_provenance, provenance, isWide = true)
    }

    return ArtworkDetailUi(
        id = id,
        title = title,
        artist = artist,
        imageUrl = imageUrl,
        description = description,
        webUrl = url,
        details = detailsList,
        images = images.map {
            GalleryImageUi(it.url, it.aspectRatio)
        },
        copyright = copyright,
        date = date,
        culture = culture,
    )
}
