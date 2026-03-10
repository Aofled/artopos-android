package ru.createsmart.artopos.feature.discover.mapper

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.createsmart.artopos.core.model.Artwork
import ru.createsmart.artopos.core.model.ImageDimensions

class ArtworkUiMapperTest {
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
            technique = null,
            description = null,
            url = "https://www.harvardartmuseums.org/collections/object/340543",
        )

        val listItem = artwork.toUi()

        assertEquals(340543, listItem.id)
        assertEquals("Sparrows in Bamboo", listItem.title)
        assertEquals("Watanabe Seitei 渡辺省亭", listItem.artist)
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
            technique = null,
            description = null,
            url = null,
        )

        val listItem = artwork.toUi()

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
            technique = null,
            description = null,
            url = null,
        )

        val listItem = artwork.toUi()

        assertEquals(0.75f, listItem.aspectRatio, 0.0f)
    }
}
