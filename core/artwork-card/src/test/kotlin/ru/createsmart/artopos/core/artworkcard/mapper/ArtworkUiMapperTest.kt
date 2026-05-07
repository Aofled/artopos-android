package ru.createsmart.artopos.core.artworkcard.mapper

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.createsmart.artopos.core.artworkcard.model.ArtworkListItem
import ru.createsmart.artopos.core.designsystem.components.UiText
import ru.createsmart.artopos.core.model.Artwork
import ru.createsmart.artopos.core.model.CreationDate
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
            creationDate = CreationDate.TextOnly("late 18th-early 19th century"),
            isFavorite = false,
        )

        val listItem = mapper.mapToUi(artwork)

        val expected = ArtworkListItem(
            id = 340543,
            title = UiText.DynamicString("Sparrows in Bamboo"),
            artist = UiText.DynamicString("Watanabe Seitei 渡辺省亭"),
            imageUrl = "https://nrs.harvard.edu/urn-3:HUAM:766954",
            aspectRatio = 0.40156862f,
            year = UiText.DynamicString("late 18th-early 19th century"),
            isFavorite = false,
        )

        assertEquals(expected, listItem)
    }

    @Test
    fun `map Artwork to ArtworkListItem handles null dimensions (default ratio)`() {
        val artwork = Artwork(
            id = 1,
            title = "Test",
            artist = "Artist",
            imageUrl = "url",
            imageDimensions = null,
            creationDate = CreationDate.Unknown,
            isFavorite = false,
        )

        val listItem = mapper.mapToUi(artwork)

        assertEquals(0.75f, listItem.aspectRatio, 0.0f)
        assertEquals(UiText.DynamicString(""), listItem.year)
    }

    @Test
    fun `map Artwork to ArtworkListItem handles zero height (division by zero protection)`() {
        val artwork = Artwork(
            id = 2,
            title = "Test",
            artist = "Artist",
            imageUrl = "url",
            imageDimensions = ImageDimensions(100, 0),
            creationDate = CreationDate.TextOnly("2024"),
            isFavorite = false,
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
            creationDate = CreationDate.Unknown,
            isFavorite = false,

        )

        val listItem = mapper.mapToUi(artwork)

        assertEquals(UiText.StringResource(DSR.string.core_placeholder_title), listItem.title)
        assertEquals(UiText.StringResource(DSR.string.core_placeholder_artist), listItem.artist)
    }
}
