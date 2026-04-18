package ru.createsmart.artopos.core.network.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.createsmart.artopos.core.network.di.NetworkJson

class ArtworkEdgeCasesTest {

    @Test
    fun `when title is missing then default to Untitled`() {
        val jsonString = """
            {
                "id": 12345
            }
        """.trimIndent()

        val artwork = NetworkJson.decodeFromString<ArtworkDTO>(jsonString)

        // The identifier must be parsed.
        assertEquals(12345, artwork.id)

        // If the title field is missing, the default 'Untitled' should be used.
        assertEquals("Untitled", artwork.title)

        // The artists list must be empty
        assertTrue(artwork.artists.isEmpty())
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
}
