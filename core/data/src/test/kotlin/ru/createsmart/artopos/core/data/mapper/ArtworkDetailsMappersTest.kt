package ru.createsmart.artopos.core.data.mapper

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test
import ru.createsmart.artopos.core.database.model.ArtworkDBO
import ru.createsmart.artopos.core.database.model.ArtworkDetailsDBO
import ru.createsmart.artopos.core.database.model.ArtworkDetailsWithFavoriteFlagDBO
import ru.createsmart.artopos.core.database.model.ArtworkWithDetailsDBO
import ru.createsmart.artopos.core.network.model.ArtworkDTO
import ru.createsmart.artopos.core.network.model.GalleryDTO

class ArtworkDetailsMappersTest {
    @Test
    fun `map ArtworkDetailsWithFavoriteFlagDBO to Domain correctly merges data`() {
        // GIVEN
        val baseDbo = ArtworkDBO(
            id = 1, sortingIndex = 0, title = "Base Title", artist = "Artist",
            imageUrl = "url", imageDimensions = null, date = null, yearInt = null,
            technique = null, description = "Base Desc", url = null, galleryImages = null,
        )

        // GIVEN
        val detailsDbo = ArtworkDetailsDBO(
            id = 1,
            provenance = "Old Owner",
            creditLine = "Gift",
            classification = "Painting",
            century = "17th century",
            culture = "French",
            medium = "Oil",
            period = "19th C",
            style = "Impressionism",
            dimensions = "10x10",
            copyright = "CC",
            galleryLocation = "Room 1",
        )

        // 1. Connect the base and parts
        val relation = ArtworkWithDetailsDBO(artwork = baseDbo, details = detailsDbo)

        // 2. Wrap it in a class with a flag (simulate the DAO response)
        val finalDbo = ArtworkDetailsWithFavoriteFlagDBO(
            artworkWithDetails = relation,
            isFavorite = true,
        )

        // WHEN
        val domain = finalDbo.toDomain()

        // THEN
        assertEquals("Base Title", domain.title)
        assertEquals("Base Desc", domain.description)
        assertEquals("Old Owner", domain.provenance)
        assertEquals("French", domain.culture)
        assertEquals(true, domain.isFavorite)
    }

    @Test
    fun `map ArtworkDetailsWithFavoriteFlagDBO to Domain handles null details safely`() {
        // GIVEN
        val baseDbo = ArtworkDBO(
            id = 1, sortingIndex = 0, title = "Base Title", artist = "Artist",
            imageUrl = "url", imageDimensions = null, date = null, yearInt = null,
            technique = null, description = "Base Desc", url = null, galleryImages = null,
        )

        val relation = ArtworkWithDetailsDBO(artwork = baseDbo, details = null)
        val finalDbo = ArtworkDetailsWithFavoriteFlagDBO(artworkWithDetails = relation, isFavorite = false)

        // WHEN
        val domain = finalDbo.toDomain()

        // THEN
        assertEquals("Base Title", domain.title)
        assertEquals("Base Desc", domain.description)
        assertNull(domain.provenance)
        assertEquals(false, domain.isFavorite)
    }

    @Test
    fun `dto to DetailsDBO maps gallery correctly`() {
        // GIVEN
        val dto = ArtworkDTO(
            id = 1,
            gallery = GalleryDTO(number = null, name = "Main Hall"),
        )

        // WHEN
        val detailsDbo = dto.toDetailsDBO()

        // THEN
        assertEquals("Main Hall", detailsDbo.galleryLocation)
    }
}
