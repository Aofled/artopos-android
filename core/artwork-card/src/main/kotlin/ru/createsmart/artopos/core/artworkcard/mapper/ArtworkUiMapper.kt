package ru.createsmart.artopos.core.artworkcard.mapper

import ru.createsmart.artopos.core.artworkcard.model.ArtworkListItem
import ru.createsmart.artopos.core.designsystem.components.UiText
import ru.createsmart.artopos.core.model.Artwork
import ru.createsmart.artopos.core.model.CreationDate
import javax.inject.Inject
import ru.createsmart.artopos.core.designsystem.R as DSR

private const val DEFAULT_ASPECT_RATIO = 0.75f

class ArtworkUiMapper @Inject constructor() {

    fun mapToUi(artwork: Artwork): ArtworkListItem {
        val dims = artwork.imageDimensions
        val ratio = if (dims != null && dims.width > 0 && dims.height > 0) {
            dims.width.toFloat() / dims.height.toFloat()
        } else {
            DEFAULT_ASPECT_RATIO
        }

        val displayTitle = if (artwork.title.isBlank()) {
            UiText.StringResource(DSR.string.core_placeholder_title)
        } else {
            UiText.DynamicString(artwork.title)
        }

        val displayArtist = if (artwork.artist.isBlank()) {
            UiText.StringResource(DSR.string.core_placeholder_artist)
        } else {
            UiText.DynamicString(artwork.artist)
        }

        val displayYear = when (val date = artwork.creationDate) {
            is CreationDate.ExactYear -> UiText.DynamicString(date.year.toString())
            is CreationDate.TextOnly -> UiText.DynamicString(date.text)
            is CreationDate.Unknown -> UiText.DynamicString("")
        }

        return ArtworkListItem(
            id = artwork.id,
            title = displayTitle,
            artist = displayArtist,
            imageUrl = artwork.imageUrl,
            aspectRatio = ratio,
            year = displayYear,
            isFavorite = artwork.isFavorite,
        )
    }
}
