package ru.createsmart.artopos.core.network.model

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.createsmart.artopos.core.network.di.NetworkJson
import ru.createsmart.artopos.core.network.util.TestResourceReader

/**
 * Test Case:
 * 1. Load JSON from resources.
 * 2. Parse using kotlinx.serialization.
 * 3. Assert ID, Name, and Count are correct.
 */
class FilterResponseTest {

    @Test
    fun `parsing classification json maps correctly`() {
        val jsonString = TestResourceReader.loadJson("classification_response.json")
        // We use the PRODUCTION parser configuration
        val response = NetworkJson.decodeFromString<NetworkResponse<FilterItemDTO>>(jsonString)
        val item = response.records.first()

        assertEquals(17L, item.id)
        assertEquals(85915, item.count)
        assertEquals("Photographs", item.name)
    }

    @Test
    fun `parsing century json maps correctly`() {
        val jsonString = TestResourceReader.loadJson("century_response.json")
        // We use the PRODUCTION parser configuration
        val response = NetworkJson.decodeFromString<NetworkResponse<FilterItemDTO>>(jsonString)
        val item = response.records.first()

        assertEquals(37525815, item.id)
        assertEquals("20th century", item.name)

        val order = requireNotNull(item.order) { "Order field should not be null" }
        assertEquals(46, order)
    }

    @Test
    fun `parsing culture json maps correctly`() {
        val jsonString = TestResourceReader.loadJson("culture_response.json")
        // We use the PRODUCTION parser configuration
        val response = NetworkJson.decodeFromString<NetworkResponse<FilterItemDTO>>(jsonString)
        val item = response.records.first()

        assertEquals(37526778L, item.id)
        assertEquals(91330, item.count)
        assertEquals("American", item.name)
    }
}
