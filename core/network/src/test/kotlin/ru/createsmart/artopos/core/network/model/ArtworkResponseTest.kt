package ru.createsmart.artopos.core.network.model

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.createsmart.artopos.core.network.di.NetworkJson
import ru.createsmart.artopos.core.network.util.TestResourceReader

/**
 * Test Case:
 * 1. Load JSON from resources.
 * 2. Parse using kotlinx.serialization.
 * 3. Assert ID, Title, and Images are correct.
 */
class ArtworkResponseTest {

    @Test
    fun `parsing valid json returns correct ArtworkDto list`() {
        val jsonString = TestResourceReader.loadJson("artworks_response.json")
        // We use the PRODUCTION parser configuration
        val response = NetworkJson.decodeFromString<NetworkResponse<ArtworkDTO>>(jsonString)

        assertEquals(5535, response.info.totalRecords)

        val firstItem = response.records.first()

        assertEquals(357597, firstItem.id)
        assertEquals("Blossoming Plum with Moon and Snow (left scroll)", firstItem.title)
        assertEquals("late 18th-early 19th century", firstItem.date)

        val images = requireNotNull(firstItem.images)
        val firstImage = images.first()

        assertEquals(1165, firstImage.width)
        assertEquals("https://nrs.harvard.edu/urn-3:HUAM:765757", firstImage.url)
    }
}
