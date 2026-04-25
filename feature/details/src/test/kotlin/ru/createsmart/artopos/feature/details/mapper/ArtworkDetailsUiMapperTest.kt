package ru.createsmart.artopos.feature.details.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.createsmart.artopos.core.designsystem.components.UiText
import ru.createsmart.artopos.core.model.Artwork
import ru.createsmart.artopos.core.model.ArtworkDetails
import ru.createsmart.artopos.core.model.ArtworkImage
import ru.createsmart.artopos.core.model.CreationDate
import ru.createsmart.artopos.feature.details.R

class ArtworkDetailsUiMapperTest {

    @Test
    fun `toDetailUi maps all non-null fields correctly and ignores nulls`() {
        // GIVEN
        val artwork = ArtworkDetails(
            baseArtwork = Artwork(
                id = 1,
                title = "Test Title",
                artist = "Test Artist",
                imageUrl = "mainUrl",
                imageDimensions = null,
                creationDate = CreationDate.ExactYear(1890),
                isFavorite = false,
            ),
            technique = null,
            medium = "Oil",
            description = "Some description",
            url = "webUrl",
            provenance = null,
            creditLine = "Gift of Smith",
            classification = "Painting",
            culture = "French",
            century = null,
            images = listOf(ArtworkImage("url1", 100, 200)),
            period = null,
            style = null,
            dimensions = null,
            copyright = null,
            galleryLocation = null,
        )

        // WHEN
        val uiModel = artwork.toDetailUi(true)

        // THEN
        assertEquals(1, uiModel.id)
        assertEquals("Test Title", uiModel.title)
        assertEquals("Painting", uiModel.classification)

        // THEN
        assertEquals(1, uiModel.images.size)
        assertEquals("url1", uiModel.images[0].url)
        assertEquals(0.5f, uiModel.images[0].aspectRatio, 0.01f) // 100/200

        // THEN: date, medium, creditLine
        val details = uiModel.details
        assertEquals(3, details.size)

        assertTrue(details.any { it.label is UiText.StringResource && it.label.resId == R.string.details_label_date })
        assertTrue(details.any { it.label is UiText.StringResource && it.label.resId == R.string.details_label_medium })
        assertTrue(
            details.any { it.label is UiText.StringResource && it.label.resId == R.string.details_label_credit_line },
        )
        assertTrue(
            details.none { it.label is UiText.StringResource && it.label.resId == R.string.details_label_provenance },
        )
    }

    @Test
    fun `toDetailUi prioritizes medium over technique`() {
        // GIVEN
        val artwork = ArtworkDetails(
            baseArtwork = Artwork(
                id = 1,
                title = "",
                artist = "",
                imageUrl = "",
                imageDimensions = null,
                creationDate = CreationDate.Unknown,
                isFavorite = false,
            ),
            technique = "Pencil",
            medium = "Oil",
            description = null,
            url = null,
            provenance = null,
            creditLine = null,
            classification = null,
            culture = null,
            century = null,
            images = emptyList(),
            period = null,
            style = null,
            dimensions = null,
            copyright = null,
            galleryLocation = null,
        )

        // WHEN
        val uiModel = artwork.toDetailUi(true)

        // THEN
        val mediumDetail = uiModel.details.find {
            it.label is UiText.StringResource && it.label.resId == R.string.details_label_medium
        }
        assertEquals("Oil", mediumDetail?.value)
    }

    @Test
    fun `toDetailUi uses technique if medium is null`() {
        // GIVEN
        val artwork = ArtworkDetails(
            baseArtwork = Artwork(
                id = 1,
                title = "",
                artist = "",
                imageUrl = "",
                imageDimensions = null,
                creationDate = CreationDate.Unknown,
                isFavorite = false,
            ),
            technique = "Pencil",
            medium = "Oil",
            description = null,
            url = null,
            provenance = null,
            creditLine = null,
            classification = null,
            culture = null,
            century = null,
            images = emptyList(),
            period = null,
            style = null,
            dimensions = null,
            copyright = null,
            galleryLocation = null,
        )

        // WHEN
        val uiModel = artwork.toDetailUi(true)

        // THEN
        val mediumDetail = uiModel.details.find {
            it.label is UiText.StringResource && it.label.resId == R.string.details_label_medium
        }
        assertEquals("Oil", mediumDetail?.value)
    }
}
