package ru.createsmart.artopos.feature.details.mapper

import ru.createsmart.artopos.core.model.Artwork
import ru.createsmart.artopos.feature.details.R
import ru.createsmart.artopos.feature.details.model.ArtworkDetailUi
import ru.createsmart.artopos.feature.details.model.DetailItem
import ru.createsmart.artopos.feature.details.model.GalleryImageUi

private fun String?.cleanHarvardApiText(): String? {
    return this
        ?.replace("\r\n", " ")
        ?.replace("painting proper:", "", ignoreCase = true)
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
}

fun Artwork.toDetailUi(
    isTranslated: Boolean,
    canBeTranslated: Boolean = false,
    isTranslationPending: Boolean = false,
): ArtworkDetailUi {
    val detailsList = buildList {
        fun addIfNotNull(labelResId: Int, value: String?, isWide: Boolean = false) {
            if (!value.isNullOrBlank()) {
                add(DetailItem(UiText.StringResource(labelResId), value, isWide))
            }
        }

        addIfNotNull(R.string.details_label_date, date)
        addIfNotNull(R.string.details_label_period, period)
        addIfNotNull(R.string.details_label_style, style)
        addIfNotNull(R.string.details_label_medium, medium ?: technique, isWide = true)
        addIfNotNull(R.string.details_label_dimensions, dimensions.cleanHarvardApiText(), isWide = true)
        addIfNotNull(R.string.details_label_gallery, galleryLocation, isWide = true)
        addIfNotNull(R.string.details_label_credit_line, creditLine, isWide = true)
        addIfNotNull(R.string.details_label_provenance, provenance, isWide = true)
    }

    return ArtworkDetailUi(
        id = id,
        title = title,
        artist = artist,
        imageUrl = imageUrl,
        description = description.cleanHarvardApiText(),
        webUrl = url,
        details = detailsList,
        images = images.map {
            GalleryImageUi(it.url, it.aspectRatio)
        },
        copyright = copyright,
        classification = classification,
        century = century,
        culture = culture,
        isFavorite = isFavorite,
        isTranslated = isTranslated,
        canBeTranslated = canBeTranslated,
        isTranslationPending = isTranslationPending,
    )
}
