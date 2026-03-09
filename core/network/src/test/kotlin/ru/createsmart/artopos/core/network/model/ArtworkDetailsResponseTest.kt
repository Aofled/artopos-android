package ru.createsmart.artopos.core.network.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Test Case:
 * 1. Load JSON from resources.
 * 2. Parse using kotlinx.serialization.
 * 3. Assert ID, Title, and Images are correct.
 */
class ArtworkDetailsResponseTest {

    private val json = Json {
        ignoreUnknownKeys = true // Stability: Don't crash if API adds new fields
        coerceInputValues = true // Stability: Convert nulls/errors to default values (safe parsing)
        encodeDefaults = true
    }

    @Test
    fun `parsing details json maps all required fields correctly`() {
        val jsonString = loadJsonFromResources("artworks_details_response.json")

        val artwork = json.decodeFromString<ArtworkDTO>(jsonString)

        assertEquals(357597, artwork.id)
        assertEquals("Blossoming Plum with Moon and Snow (left scroll)", artwork.title)
        assertEquals("Japanese", artwork.culture)
        assertEquals("Paintings", artwork.classification)
        assertEquals("Edo period, 1615-1868", artwork.period)
        assertEquals("Shijo", artwork.style)
        assertEquals("https://www.harvardartmuseums.org/collections/object/357597", artwork.webUrl)

        assertNotNull(artwork.description)
        assertTrue(artwork.description!!.startsWith("Pair of scrolls depicting blossoming plum trees."))

        assertEquals("Left scroll of a pair of hanging scrolls; ink and light color on paper.", artwork.medium)
        assertEquals("Promised gift of Robert S. and Betsy G. Feinberg", artwork.creditLine)

        assertEquals(null, artwork.provenance)

        assertNotNull(artwork.gallery)
        assertEquals("2600", artwork.gallery?.number)
        assertEquals("East Asian Art", artwork.gallery?.name)

        assertNotNull(artwork.images)
        assertEquals(7, artwork.images?.size)

        val firstImage = artwork.images?.first()
        assertEquals(1165, firstImage?.width)
        assertEquals("https://nrs.harvard.edu/urn-3:HUAM:765757", firstImage?.url)

        val lastImage = artwork.images?.last()
        assertEquals(953, lastImage?.width)
        assertEquals("https://nrs.harvard.edu/urn-3:HUAM:765758", lastImage?.url)
    }

    private fun loadJsonFromResources(fileName: String): String {
        val classLoader = javaClass.classLoader
        val resource = classLoader?.getResource(fileName)
            ?: throw IllegalArgumentException("File not found: $fileName")
        return File(resource.path).readText()
    }
}
