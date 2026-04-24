package ru.createsmart.artopos.core.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ru.createsmart.artopos.core.database.converters.StoredImage
import ru.createsmart.artopos.core.database.model.ArtworkDBO
import ru.createsmart.artopos.core.database.model.ArtworkDetailsDBO
import ru.createsmart.artopos.core.database.model.ArtworkDetailsWithFavoriteFlagDBO
import ru.createsmart.artopos.core.database.model.ArtworkWithDetailsDBO
import ru.createsmart.artopos.core.model.ImageDimensions
import ru.createsmart.artopos.core.network.model.ArtworkDTO
import ru.createsmart.artopos.core.network.model.ImageDTO
import ru.createsmart.artopos.core.network.model.PersonDTO
import ru.createsmart.artopos.core.network.model.PlaceDTO

class ArtworkMappersTest {

    private val mapper = ArtworkMapper()

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
        val dbo = mapper.mapDtoToDbo(dto)

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
            artists = emptyList(),
            images = emptyList(),
            places = emptyList(),
        )

        // WHEN
        val dbo = mapper.mapDtoToDbo(dto)

        // THEN
        assertEquals("https://nrs.harvard.edu/urn-3:HUAM:765757", dbo.imageUrl)
        assertEquals(null, dbo.imageDimensions)
    }

    @Test
    fun `map ArtworkDBO to ArtworkDetails correctly`() {
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

        val wrapper = ArtworkDetailsWithFavoriteFlagDBO(ArtworkWithDetailsDBO(dbo, null), true)

        // WHEN
        val domain = mapper.mapDetailsToDomain(wrapper)

        // THEN
        assertEquals(357596, domain.baseArtwork.id)
        assertEquals("Blossoming Plum with Moon and Snow (right scroll)", domain.baseArtwork.title)
        assertEquals("Owari", domain.baseArtwork.artist)
        assertEquals("https://nrs.harvard.edu/urn-3:HUAM:765750", domain.baseArtwork.imageUrl)
        assertEquals(
            ImageDimensions(
                width = 2550,
                height = 1301,
            ),
            domain.baseArtwork.imageDimensions,
        )
        assertEquals("late 18th-early 19th century", domain.baseArtwork.date)
        assertEquals(null, domain.baseArtwork.yearInt)
        assertEquals(null, domain.technique)
        assertEquals("Pair of scrolls depicting blossoming plum trees.", domain.description)
        assertEquals("https://www.harvardartmuseums.org/collections/object/357596", domain.url)
        assertEquals(3, domain.images.size)
        assertEquals("https://nrs.harvard.edu/urn-3:HUAM:765757", domain.images[0].url)
        assertEquals(1165, domain.images[0].width)
        assertEquals(true, domain.baseArtwork.isFavorite)
    }

    @Test
    fun `mapDetailsToDomain merges details and sets isFavorite flag`() {
        // GIVEN
        val baseDbo = ArtworkDBO(
            id = 1, sortingIndex = 0, title = "Mona Lisa", artist = "Da Vinci",
            imageUrl = "url", imageDimensions = null, date = null, yearInt = null,
            technique = null, description = "Basic Desc", url = null, galleryImages = null,
            inDiscoverFeed = true,
        )
        // GIVEN
        val detailsDbo = ArtworkDetailsDBO(
            id = 1, provenance = "Louvre", creditLine = "Gift", classification = "Painting",
            century = "16th C", culture = "Italian", medium = "Oil", period = "Renaissance",
            style = "Sfumato", dimensions = "77x53", copyright = "Public Domain", galleryLocation = "Room 711",
        )

        val wrapper = ArtworkDetailsWithFavoriteFlagDBO(
            artworkWithDetails = ArtworkWithDetailsDBO(baseDbo, detailsDbo),
            isFavorite = true,
        )

        // WHEN
        val domain = mapper.mapDetailsToDomain(wrapper)

        // THEN
        assertEquals("Mona Lisa", domain.baseArtwork.title)
        assertEquals("Louvre", domain.provenance)
        assertEquals("Room 711", domain.galleryLocation)
        assertEquals(true, domain.baseArtwork.isFavorite)
    }

    @Test
    fun `mapDtoToDbo parses year integer from mixed date string via Regex`() {
        // GIVEN
        val dto = ArtworkDTO(id = 1, date = "Circa 1885, autumn")

        // WHEN
        val dbo = mapper.mapDtoToDbo(dto)

        // THEN
        assertEquals(1885, dbo.yearInt)
    }

    @Test
    fun `mapDtoToDbo returns null yearInt if date contains no 4-digit number`() {
        // GIVEN
        val dto = ArtworkDTO(id = 1, date = "17th century")

        // WHEN
        val dbo = mapper.mapDtoToDbo(dto)

        // THEN
        assertNull(dbo.yearInt)
    }

    @Test
    fun `mapDtoToDbo filters artists by role case-insensitively and joins them`() {
        // GIVEN
        val artists = listOf(
            PersonDTO(name = "Donor Name", role = "Donor"),
            PersonDTO(name = "Da Vinci", role = "artist"), // Маленькая буква!
            PersonDTO(name = "Michelangelo", role = "Artist"),
        )
        val dto = ArtworkDTO(id = 1, artists = artists)

        // WHEN
        val dbo = mapper.mapDtoToDbo(dto)

        // THEN
        assertEquals("Da Vinci, Michelangelo", dbo.artist)
    }

    @Test
    fun `mapDtoToDbo ignores image dimensions if width or height is invalid`() {
        // GIVEN
        val images = listOf(ImageDTO(width = 0, height = -100, url = "url"))
        val dto = ArtworkDTO(id = 1, images = images)

        // WHEN
        val dbo = mapper.mapDtoToDbo(dto)

        // THEN
        assertNull(dbo.imageDimensions)
        assertEquals("url", dbo.imageUrl)
    }
}
