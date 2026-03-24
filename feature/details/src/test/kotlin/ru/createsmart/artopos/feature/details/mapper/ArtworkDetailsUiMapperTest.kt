package ru.createsmart.artopos.feature.details.mapper

import UiText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.createsmart.artopos.core.model.Artwork
import ru.createsmart.artopos.core.model.ArtworkImage
import ru.createsmart.artopos.feature.details.R

class ArtworkDetailsUiMapperTest {

    @Test
    fun `toDetailUi maps all non-null fields correctly and ignores nulls`() {
        // GIVEN
        val artwork = Artwork(
            id = 1,
            title = "Test Title",
            artist = "Test Artist",
            imageUrl = "mainUrl",
            imageDimensions = null,
            date = "1890",
            yearInt = 1890,
            technique = null,
            medium = "Oil",
            description = "Some description",
            url = "webUrl",
            provenance = null,
            creditLine = "Gift of Smith",
            classification = "Painting",
            culture = "French",
            images = listOf(
                ArtworkImage("url1", 100, 200),
            ),
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

        assertTrue(details.any { it.label is UiText.StringResource && it.label.resId == R.string.details_date })
        assertTrue(details.any { it.label is UiText.StringResource && it.label.resId == R.string.details_medium })
        assertTrue(details.any { it.label is UiText.StringResource && it.label.resId == R.string.details_credit_line })
        assertTrue(details.none { it.label is UiText.StringResource && it.label.resId == R.string.details_provenance })
    }

    @Test
    fun `toDetailUi prioritizes medium over technique`() {
        // GIVEN
        val artwork = Artwork(
            id = 1, title = "", artist = "", imageUrl = "", imageDimensions = null, date = null, yearInt = null,
            technique = "Pencil",
            medium = "Oil",
            description = null, url = null, provenance = null, creditLine = null, classification = null, culture = null,
        )

        // WHEN
        val uiModel = artwork.toDetailUi(true)

        // THEN
        val mediumDetail = uiModel.details.find {
            it.label is UiText.StringResource && it.label.resId == R.string.details_medium
        }
        assertEquals("Oil", mediumDetail?.value)
    }

    @Test
    fun `toDetailUi uses technique if medium is null`() {
        // GIVEN
        val artwork = Artwork(
            id = 1, title = "", artist = "", imageUrl = "", imageDimensions = null, date = null, yearInt = null,
            technique = "Pencil",
            medium = null,
            description = null, url = null, provenance = null, creditLine = null, classification = null, culture = null,
        )

        // WHEN
        val uiModel = artwork.toDetailUi(true)

        // THEN
        val mediumDetail = uiModel.details.find {
            it.label is UiText.StringResource && it.label.resId == R.string.details_medium
        }
        assertEquals("Pencil", mediumDetail?.value)
    }
}
