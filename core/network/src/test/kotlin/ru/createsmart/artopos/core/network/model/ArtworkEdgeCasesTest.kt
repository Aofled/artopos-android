package ru.createsmart.artopos.core.network.model

import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.createsmart.artopos.core.network.di.NetworkJson

class ArtworkEdgeCasesTest {

    @Test
    fun `when title is missing then parse as null`() {
        val jsonString = """
            {
                "id": 12345
            }
        """.trimIndent()

        val artwork = NetworkJson.decodeFromString<ArtworkDTO>(jsonString)

        // The identifier must be parsed.
        assertEquals(12345, artwork.id)

        Assert.assertNull(artwork.title)

        Assert.assertTrue(artwork.artists.isEmpty())
    }

    @Test
    fun `when people array is null then coerce to empty list`() {
        val jsonString = """
            {
                "id": 12345,
                "title": "Mona Lisa",
                "people": null
            }
        """.trimIndent()

        val artwork = NetworkJson.decodeFromString<ArtworkDTO>(jsonString)

        assertEquals(12345, artwork.id)

        // When receiving null, the artists collection should be resolved to an empty list.
        assertTrue(artwork.artists.isEmpty())
    }

    @Test
    fun `when unknown keys are present then ignore them without crashing`() {
        // The backend rolled out API v2 and added a bunch of new fields that the application doesn't know about.
        val jsonString = """
            {
                "id": 12345,
                "title": "Mona Lisa",
                "future_api_field": "Some unexpected value",
                "nested_unknown_object": { 
                    "key": "value" 
                },
                "unknown_array": [1, 2, 3]
            }
        """.trimIndent()

        // Main check: the method must not throw SerializationException
        val artwork = NetworkJson.decodeFromString<ArtworkDTO>(jsonString)

        assertEquals(12345, artwork.id)
        assertEquals("Mona Lisa", artwork.title)
    }

    @Test
    fun `when json is malformed then throw SerializationException`() {
        // Simulate a broken connection or a backend bug (no closing parentheses)
        val brokenJson = """
            {
                "id": 12345,
                "title": "Unfinished
        """.trimIndent()

        Assert.assertThrows(kotlinx.serialization.SerializationException::class.java) {
            NetworkJson.decodeFromString<ArtworkDTO>(brokenJson)
        }
    }

    @Test
    fun `when response is html instead of json then throw SerializationException`() {
        // the server crashed and Cloudflare/Nginx returned an HTML page (503 Service Unavailable)
        val htmlResponse = "<html><body>503 Service Unavailable</body></html>"

        Assert.assertThrows(kotlinx.serialization.SerializationException::class.java) {
            NetworkJson.decodeFromString<ArtworkDTO>(htmlResponse)
        }
    }
}
