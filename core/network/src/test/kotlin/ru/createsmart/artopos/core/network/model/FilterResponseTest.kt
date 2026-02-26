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
 * 3. Assert ID, Name, and Count are correct.
 */
class FilterResponseTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Test
    fun `parsing classification json maps correctly`() {
        val jsonString = loadJsonFromResources("classification_response.json")

        val response = json.decodeFromString<NetworkResponse<FilterItemDTO>>(jsonString)
        val item = response.records.first()

        assertEquals(17L, item.id)
        assertEquals(85915, item.count)
        assertEquals("Photographs", item.name)
    }

    @Test
    fun `parsing century json maps correctly`() {
        val jsonString = loadJsonFromResources("century_response.json")

        val response = json.decodeFromString<NetworkResponse<FilterItemDTO>>(jsonString)
        val item = response.records.first()

        assertEquals(37525815, item.id)
        assertEquals("20th century", item.name)
        assertEquals(46, item.order)
        assertNotNull(item.order)
    }

    @Test
    fun `parsing culture json maps correctly`() {
        val jsonString = loadJsonFromResources("culture_response.json")

        val response = json.decodeFromString<NetworkResponse<FilterItemDTO>>(jsonString)
        val item = response.records.first()

        assertEquals(37526778L, item.id)
        assertEquals(91330, item.count)
        assertEquals("American", item.name)
    }

    private fun loadJsonFromResources(fileName: String): String {
        val classLoader = javaClass.classLoader
        val resource = classLoader?.getResource(fileName)
            ?: throw IllegalArgumentException("File not found: $fileName")
        return File(resource.path).readText()
    }
}
