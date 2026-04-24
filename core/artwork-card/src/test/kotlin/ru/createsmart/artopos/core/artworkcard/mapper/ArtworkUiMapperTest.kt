package ru.createsmart.artopos.core.artworkcard.mapper

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.createsmart.artopos.core.designsystem.components.UiText
import ru.createsmart.artopos.core.model.Artwork
import ru.createsmart.artopos.core.model.ImageDimensions
import ru.createsmart.artopos.core.designsystem.R as DSR

class ArtworkUiMapperTest {

    private val mapper = ArtworkUiMapper()

    @Test
    fun `map Artwork to ArtworkListItem correctly`() {
        val imageDimensions = ImageDimensions(
            width = 1024,
            height = 2550,
        )

        val artwork = Artwork(
            id = 340543,
            title = "Sparrows in Bamboo",
            artist = "Watanabe Seitei 渡辺省亭",
            imageUrl = "https://nrs.harvard.edu/urn-3:HUAM:766954",
            imageDimensions = imageDimensions,
            date = "late 18th-early 19th century",
            yearInt = null,
        )

        val listItem = mapper.mapToUi(artwork)

        assertEquals(340543, listItem.id)
        assertEquals(UiText.DynamicString("Sparrows in Bamboo"), listItem.title)
        assertEquals(UiText.DynamicString("Watanabe Seitei 渡辺省亭"), listItem.artist)
        assertEquals("https://nrs.harvard.edu/urn-3:HUAM:766954", listItem.imageUrl)
        assertEquals(0.40156862f, listItem.aspectRatio)
        assertEquals("late 18th-early 19th century", listItem.year)
    }

    @Test
    fun `map Artwork to ArtworkListItem handles null dimensions (default ratio)`() {
        val artwork = Artwork(
            id = 1,
            title = "Test",
            artist = "Artist",
            imageUrl = "url",
            imageDimensions = null,
            date = null,
            yearInt = null,
        )

        val listItem = mapper.mapToUi(artwork)

        assertEquals(0.75f, listItem.aspectRatio, 0.0f)
        assertEquals("", listItem.year)
    }

    @Test
    fun `map Artwork to ArtworkListItem handles zero height (division by zero protection)`() {
        val artwork = Artwork(
            id = 2,
            title = "Test",
            artist = "Artist",
            imageUrl = "url",
            imageDimensions = ImageDimensions(100, 0),
            date = "2024",
            yearInt = null,
        )

        val listItem = mapper.mapToUi(artwork)

        assertEquals(0.75f, listItem.aspectRatio, 0.0f)
    }

    @Test
    fun `mapToUi with blank title and artist returns StringResource placeholders`() {
        val artwork = Artwork(
            id = 1,
            title = "   ",
            artist = "",
            imageUrl = "url",
            imageDimensions = null,
            date = null,
            yearInt = null,
        )

        val listItem = mapper.mapToUi(artwork)

        // Убеждаемся, что маппер подставил нужные ресурсы из дизайн-системы
        assertEquals(UiText.StringResource(DSR.string.core_placeholder_title), listItem.title)
        assertEquals(UiText.StringResource(DSR.string.core_placeholder_artist), listItem.artist)
    }

    @Test
    fun `mapToUi handles null dimensions with default ratio`() {
        val artwork = Artwork(
            id = 1,
            title = "Test",
            artist = "Artist",
            imageUrl = "url",
            imageDimensions = null,
            date = null,
            yearInt = null,
        )

        val listItem = mapper.mapToUi(artwork)

        assertEquals(0.75f, listItem.aspectRatio, 0.0f)
        assertEquals("", listItem.year)
    }

    @Test
    fun `mapToUi handles zero height with division by zero protection`() {
        val artwork = Artwork(
            id = 2,
            title = "Test",
            artist = "Artist",
            imageUrl = "url",
            imageDimensions = ImageDimensions(100, 0),
            date = "2024",
            yearInt = null,
        )

        val listItem = mapper.mapToUi(artwork)

        assertEquals(0.75f, listItem.aspectRatio, 0.0f)
    }
}
