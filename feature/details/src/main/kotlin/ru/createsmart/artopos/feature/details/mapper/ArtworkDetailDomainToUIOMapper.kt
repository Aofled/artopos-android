package ru.createsmart.artopos.feature.details.mapper

import kotlinx.collections.immutable.toImmutableList
import ru.createsmart.artopos.core.designsystem.components.UiText
import ru.createsmart.artopos.core.model.ArtworkDetails
import ru.createsmart.artopos.core.model.CreationDate
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

fun ArtworkDetails.toDetailUi(
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

        val displayDate = when (val date = baseArtwork.creationDate) {
            is CreationDate.ExactYear -> date.year.toString()
            is CreationDate.TextOnly -> date.text
            is CreationDate.Unknown -> null
        }

        addIfNotNull(R.string.details_label_date, displayDate)
        addIfNotNull(R.string.details_label_period, period)
        addIfNotNull(R.string.details_label_style, style)
        addIfNotNull(R.string.details_label_medium, medium ?: technique, isWide = true)
        addIfNotNull(R.string.details_label_dimensions, dimensions.cleanHarvardApiText(), isWide = true)
        addIfNotNull(R.string.details_label_gallery, galleryLocation, isWide = true)
        addIfNotNull(R.string.details_label_credit_line, creditLine, isWide = true)
        addIfNotNull(R.string.details_label_provenance, provenance, isWide = true)
    }.toImmutableList()

    return ArtworkDetailUi(
        id = baseArtwork.id,
        title = baseArtwork.title,
        artist = baseArtwork.artist,
        imageUrl = baseArtwork.imageUrl,
        description = description.cleanHarvardApiText(),
        webUrl = url,
        details = detailsList,
        images = images.map {
            GalleryImageUi(it.url, it.aspectRatio)
        }.toImmutableList(),
        copyright = copyright,
        classification = classification,
        century = century,
        culture = culture,
        isFavorite = baseArtwork.isFavorite,
        isTranslated = isTranslated,
        canBeTranslated = canBeTranslated,
        isTranslationPending = isTranslationPending,
    )
}
