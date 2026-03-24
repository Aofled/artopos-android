package ru.createsmart.artopos.feature.details.mapper

import UiText
import ru.createsmart.artopos.core.common.util.clearText
import ru.createsmart.artopos.core.model.Artwork
import ru.createsmart.artopos.feature.details.R
import ru.createsmart.artopos.feature.details.model.ArtworkDetailUi
import ru.createsmart.artopos.feature.details.model.DetailItem
import ru.createsmart.artopos.feature.details.model.GalleryImageUi

fun Artwork.toDetailUi(isTranslated: Boolean, canBeTranslated: Boolean = false): ArtworkDetailUi {
    val detailsList = buildList {
        fun addIfNotNull(labelResId: Int, value: String?, isWide: Boolean = false) {
            if (!value.isNullOrBlank()) {
                add(DetailItem(UiText.StringResource(labelResId), value, isWide))
            }
        }

        addIfNotNull(R.string.details_date, date)
        addIfNotNull(R.string.details_period, period)
        addIfNotNull(R.string.details_style, style)
        addIfNotNull(R.string.details_medium, medium ?: technique, isWide = true)
        addIfNotNull(R.string.details_dimensions, dimensions.clearText(), isWide = true)
        addIfNotNull(R.string.details_gallery, galleryLocation, isWide = true)
        addIfNotNull(R.string.details_credit_line, creditLine, isWide = true)
        addIfNotNull(R.string.details_provenance, provenance, isWide = true)
    }

    return ArtworkDetailUi(
        id = id,
        title = title,
        artist = artist,
        imageUrl = imageUrl,
        description = description.clearText(),
        webUrl = url,
        details = detailsList,
        images = images.map {
            GalleryImageUi(it.url, it.aspectRatio)
        },
        copyright = copyright,
        classification = classification,
        century = century,
        culture = culture,
        isTranslated = isTranslated,
        canBeTranslated = canBeTranslated,
    )
}
