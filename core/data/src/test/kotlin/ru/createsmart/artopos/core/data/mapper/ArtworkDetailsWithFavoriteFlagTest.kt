package ru.createsmart.artopos.core.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ru.createsmart.artopos.core.database.model.ArtworkDBO
import ru.createsmart.artopos.core.database.model.ArtworkDetailsDBO
import ru.createsmart.artopos.core.database.model.ArtworkDetailsWithFavoriteFlagDBO
import ru.createsmart.artopos.core.database.model.ArtworkFavoriteDBO
import ru.createsmart.artopos.core.database.model.ArtworkWithDetailsDBO
import ru.createsmart.artopos.core.model.ImageDimensions

class ArtworkDetailsWithFavoriteFlagTest {

    @Test
    fun `map ArtworkDetailsWithFavoriteFlagDBO to Domain merges details and sets isFavorite flag`() {
        // GIVEN
        val baseDbo = ArtworkDBO(
            id = 1, sortingIndex = 0, title = "Mona Lisa", artist = "Da Vinci",
            imageUrl = "url", imageDimensions = null, date = null, yearInt = null,
            technique = null, description = "Basic Desc", url = null, galleryImages = null,
        )

        // GIVEN
        val detailsDbo = ArtworkDetailsDBO(
            id = 1,
            provenance = "Louvre",
            creditLine = "Gift",
            classification = "Painting",
            century = "16th C",
            culture = "Italian",
            medium = "Oil",
            period = "Renaissance",
            style = "Sfumato",
            dimensions = "77x53",
            copyright = "Public Domain",
            galleryLocation = "Room 711",
        )

        val relation = ArtworkWithDetailsDBO(artwork = baseDbo, details = detailsDbo)
        val wrapper = ArtworkDetailsWithFavoriteFlagDBO(
            artworkWithDetails = relation,
            isFavorite = true,
        )

        // WHEN
        val domain = wrapper.toDomain()

        // THEN
        assertEquals("Mona Lisa", domain.title)
        assertEquals("Louvre", domain.provenance)
        assertEquals("Italian", domain.culture)

        assertEquals(true, domain.isFavorite)
    }

    @Test
    fun `map ArtworkFavoriteDBO to Domain sets isFavorite to true and leaves details null`() {
        // GIVEN
        val favoriteDbo = ArtworkFavoriteDBO(
            id = 10,
            title = "Starry Night",
            artist = "Van Gogh",
            imageUrl = "url2",
            imageDimensions = ImageDimensions(100, 200),
            date = "1889",
            yearInt = 1889,
            technique = "Oil",
            description = "Swirls",
            url = "link",
            galleryImages = emptyList(),
            savedAtTimestamp = 123456789L,
        )

        // WHEN
        val domain = favoriteDbo.toDomain()

        // THEN
        assertEquals("Starry Night", domain.title)
        assertEquals(1889, domain.yearInt)
        assertEquals(true, domain.isFavorite)
        assertNull(domain.provenance)
        assertNull(domain.culture)
        assertNull(domain.galleryLocation)
    }
}
