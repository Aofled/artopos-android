package ru.createsmart.artopos.core.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.createsmart.artopos.core.database.converters.StoredImage
import ru.createsmart.artopos.core.database.model.ArtworkDBO
import ru.createsmart.artopos.core.model.ImageDimensions
import ru.createsmart.artopos.core.network.model.ArtworkDTO
import ru.createsmart.artopos.core.network.model.ImageDTO
import ru.createsmart.artopos.core.network.model.PersonDTO
import ru.createsmart.artopos.core.network.model.PlaceDTO

class ArtworkMappersTest {

    @Test
    fun `map ArtworkDTO to ArtworkDBO correctly`() {
        // GIVEN
        val artists = PersonDTO(
            name = "Goshun 呉春  (Matsumura Gekkei 松村月渓)",
            role = "Artist",
        )

        // GIVEN
        val images = ImageDTO(
            width = 1165,
            height = 2550,
            url = "https://ids.lib.harvard.edu/ids/iiif/437167239",
        )

        // GIVEN
        val places = PlaceDTO(
            name = "East Asia, Japan",
        )

        // GIVEN
        val dto = ArtworkDTO(
            id = 357597,
            title = "Blossoming Plum with Moon and Snow (left scroll)",
            date = "Dated: 1895",
            technique = null,
            imageUrl = "https://nrs.harvard.edu/urn-3:HUAM:765757",
            description = "Pair of scrolls depicting blossoming plum trees.",
            webUrl = "https://www.harvardartmuseums.org/collections/object/357597",
            artists = listOf(artists),
            images = listOf(images),
            places = listOf(places),
        )

        // WHEN
        val dbo = dto.toDBO()

        // THEN
        assertEquals(357597, dbo.id)
        assertEquals("Blossoming Plum with Moon and Snow (left scroll)", dbo.title)
        assertEquals("Goshun 呉春  (Matsumura Gekkei 松村月渓)", dbo.artist)
        assertEquals("https://ids.lib.harvard.edu/ids/iiif/437167239", dbo.imageUrl)
        assertEquals(
            ImageDimensions(
                1165,
                2550,
            ),
            dbo.imageDimensions,
        )
        assertEquals("Dated: 1895", dbo.date)
        assertEquals(1895, dbo.yearInt)
        assertEquals(null, dbo.technique)
        assertEquals("Pair of scrolls depicting blossoming plum trees.", dbo.description)
        assertEquals("https://www.harvardartmuseums.org/collections/object/357597", dbo.url)
    }

    @Test
    fun `dto to dbo falls back to primary image when images array is empty`() {
        // GIVEN
        val dto = ArtworkDTO(
            id = 357597,
            title = "Blossoming Plum with Moon and Snow (left scroll)",
            date = "Dated: 1895",
            technique = null,
            imageUrl = "https://nrs.harvard.edu/urn-3:HUAM:765757",
            description = null,
            webUrl = null,
            artists = null,
            images = emptyList(),
            places = null,
        )

        // WHEN
        val dbo = dto.toDBO()

        // THEN
        assertEquals("https://nrs.harvard.edu/urn-3:HUAM:765757", dbo.imageUrl)
        assertEquals(null, dbo.imageDimensions)
    }

    @Test
    fun `map ArtworkDBO to Artwork correctly`() {
        // GIVEN
        val imageDimensions = ImageDimensions(
            width = 2550,
            height = 1301,
        )

        // GIVEN
        val galleryImages = listOf(
            StoredImage(
                url = "https://nrs.harvard.edu/urn-3:HUAM:765757",
                width = 1165,
                height = 2550,
            ),
            StoredImage(
                url = "https://nrs.harvard.edu/urn-3:HUAM:765752",
                width = 1921,
                height = 2550,
            ),
            StoredImage(
                url = "https://nrs.harvard.edu/urn-3:HUAM:765753",
                width = 1602,
                height = 2550,
            ),
        )

        // GIVEN
        val dbo = ArtworkDBO(
            id = 357596,
            sortingIndex = 10,
            title = "Blossoming Plum with Moon and Snow (right scroll)",
            artist = "Owari",
            imageUrl = "https://nrs.harvard.edu/urn-3:HUAM:765750",
            imageDimensions = imageDimensions,
            date = "late 18th-early 19th century",
            yearInt = null,
            technique = null,
            description = "Pair of scrolls depicting blossoming plum trees.",
            url = "https://www.harvardartmuseums.org/collections/object/357596",
            galleryImages = galleryImages,
        )

        // WHEN
        val domain = dbo.toDomain()

        // THEN
        assertEquals(357596, domain.id)
        assertEquals("Blossoming Plum with Moon and Snow (right scroll)", domain.title)
        assertEquals("Owari", domain.artist)
        assertEquals("https://nrs.harvard.edu/urn-3:HUAM:765750", domain.imageUrl)
        assertEquals(
            ImageDimensions(
                width = 2550,
                height = 1301,
            ),
            domain.imageDimensions,
        )
        assertEquals("late 18th-early 19th century", domain.date)
        assertEquals(null, domain.yearInt)
        assertEquals(null, domain.technique)
        assertEquals("Pair of scrolls depicting blossoming plum trees.", domain.description)
        assertEquals("https://www.harvardartmuseums.org/collections/object/357596", domain.url)
        assertEquals(3, domain.images.size)
        assertEquals("https://nrs.harvard.edu/urn-3:HUAM:765757", domain.images[0].url)
        assertEquals(1165, domain.images[0].width)
    }
}
