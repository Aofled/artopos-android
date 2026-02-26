package ru.createsmart.artopos.core.network.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.File

/**
 * Test Case:
 * 1. Load JSON from resources.
 * 2. Parse using kotlinx.serialization.
 * 3. Assert ID, Title, and Images are correct.
 */
class ArtworkResponseTest {

    private val json = Json {
        ignoreUnknownKeys = true // Stability: Don't crash if API adds new fields
        coerceInputValues = true // Stability: Convert nulls/errors to default values (safe parsing)
        encodeDefaults = true
    }

    @Test
    fun `parsing valid json returns correct ArtworkDto list`() {
        val jsonString = loadJsonFromResources("artworks_response.json")

        val response = json.decodeFromString<NetworkResponse<ArtworkDTO>>(jsonString)

        assertNotNull(response.info)

        assertEquals(5535, response.info.totalRecords)

        assertNotNull(response.records)
        val firstItem = response.records.first()

        assertEquals(357597, firstItem.id)
        assertEquals("Blossoming Plum with Moon and Snow (left scroll)", firstItem.title)
        assertEquals("late 18th-early 19th century", firstItem.date)

        assertNotNull(firstItem.images)
        val firstImage = firstItem.images?.first()
        assertEquals(1165, firstImage?.width)
        assertEquals("https://nrs.harvard.edu/urn-3:HUAM:765757", firstImage?.url)
    }

    private fun loadJsonFromResources(fileName: String): String {
        val classLoader = javaClass.classLoader
        val resource = classLoader?.getResource(fileName)
            ?: throw IllegalArgumentException("File not found: $fileName")
        return File(resource.path).readText()
    }
}
