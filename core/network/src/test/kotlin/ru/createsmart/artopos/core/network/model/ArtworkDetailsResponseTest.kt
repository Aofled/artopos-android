package ru.createsmart.artopos.core.network.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.createsmart.artopos.core.network.di.NetworkJson
import ru.createsmart.artopos.core.network.util.TestResourceReader

/**
 * Test Case:
 * 1. Load JSON from resources.
 * 2. Parse using kotlinx.serialization.
 * 3. Assert ID, Title, and Images are correct.
 */
class ArtworkDetailsResponseTest {

    @Test
    fun `parsing details json maps all required fields correctly`() {
        val jsonString = TestResourceReader.loadJson("artworks_details_response.json")
        // We use the PRODUCTION parser configuration
        val artwork = NetworkJson.decodeFromString<ArtworkDTO>(jsonString)

        assertEquals(357597, artwork.id)
        assertEquals("Blossoming Plum with Moon and Snow (left scroll)", artwork.title)
        assertEquals("Japanese", artwork.culture)
        assertEquals("Paintings", artwork.classification)
        assertEquals("Edo period, 1615-1868", artwork.period)
        assertEquals("Shijo", artwork.style)
        assertEquals("https://www.harvardartmuseums.org/collections/object/357597", artwork.webUrl)

        val description = requireNotNull(artwork.description)
        assertTrue(description.startsWith("Pair of scrolls depicting blossoming plum trees."))

        assertEquals("Left scroll of a pair of hanging scrolls; ink and light color on paper.", artwork.medium)
        assertEquals("Promised gift of Robert S. and Betsy G. Feinberg", artwork.creditLine)

        assertEquals(null, artwork.provenance)

        val gallery = requireNotNull(artwork.gallery)
        assertEquals("2600", gallery.number)
        assertEquals("East Asian Art", gallery.name)

        val images = requireNotNull(artwork.images)
        assertEquals(7, images.size)

        val firstImage = images.first()
        assertEquals(1165, firstImage.width)
        assertEquals("https://nrs.harvard.edu/urn-3:HUAM:765757", firstImage.url)

        val lastImage = images.last()
        assertEquals(953, lastImage.width)
        assertEquals("https://nrs.harvard.edu/urn-3:HUAM:765758", lastImage.url)
    }
}
