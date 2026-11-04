package ru.createsmart.artopos.core.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.createsmart.artopos.core.data.mapper.ArtworkMapper
import ru.createsmart.artopos.core.database.HarvardDatabase
import ru.createsmart.artopos.core.database.converters.StoredImage
import ru.createsmart.artopos.core.database.model.ArtworkDBO
import ru.createsmart.artopos.core.model.CreationDate
import ru.createsmart.artopos.core.network.api.HarvardAPI
import ru.createsmart.artopos.core.network.model.ArtworkDTO
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class OfflineFirstArtworkRepositoryDetailsTest {

    private val api: HarvardAPI = mockk()
    private lateinit var database: HarvardDatabase
    private lateinit var repository: OfflineFirstArtworkRepository
    private val mapper = ArtworkMapper()

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, HarvardDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        repository = OfflineFirstArtworkRepository(database, api, mapper)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `getArtwork returns null when database is empty`() = runTest {
        val result = repository.getArtwork(1).first()
        assertNull("Should return null if artwork is not in DB", result)
    }

    @Test
    fun `getArtwork returns artwork when present in database without details`() = runTest {
        // GIVEN
        val dbo = ArtworkDBO(
            id = 1, title = "Test Title", artist = "Artist",
            imageUrl = "url", imageDimensions = null, date = "1890", yearInt = 1890,
            technique = null, description = null, url = null,
            galleryImages = listOf(StoredImage("url", 100, 100)),
        )
        database.artworkDao().insertArtworks(listOf(dbo))

        // WHEN
        val result = repository.getArtwork(1).first()

        // THEN
        assertNotNull(result)

        assertEquals("Test Title", result?.baseArtwork?.title)
        assertEquals(CreationDate.ExactYear(1890), result?.baseArtwork?.creationDate)
        assertFalse(result?.baseArtwork?.isFavorite ?: true)
        assertEquals(1, result?.images?.size)
        assertNull("Description should be null as details are missing", result?.description)
    }

    @Test
    fun `syncArtworkDetails fetches data and saves to details table`() = runTest {
        // GIVEN
        val baseDbo = ArtworkDBO(
            id = 1, title = "Base Title", artist = "",
            imageUrl = "", imageDimensions = null, date = null, yearInt = null,
            technique = null, description = "Description loaded from list",
            url = null, galleryImages = null,
        )
        database.artworkDao().insertArtworks(listOf(baseDbo))

        // GIVEN
        val detailDto = ArtworkDTO(id = 1, title = "Ignored Title", culture = "French", provenance = "Gift")
        coEvery { api.getArtworkDetails(1) } returns detailDto

        // WHEN
        val result = repository.syncArtworkDetails(1)

        // THEN
        assertTrue(result.isSuccess)

        repository.getArtwork(1).test {
            val updatedArtwork = awaitItem()
            assertNotNull(updatedArtwork)
            assertEquals("Description loaded from list", updatedArtwork?.description)
            assertEquals("French", updatedArtwork?.culture)
            assertEquals("Gift", updatedArtwork?.provenance)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `syncArtworkDetails returns failure on network error`() = runTest {
        // GIVEN
        coEvery { api.getArtworkDetails(any()) } throws IOException("No internet")

        // WHEN
        val result = repository.syncArtworkDetails(1)

        // THEN
        assertTrue(result.isFailure)
    }

    @Test
    fun `toggleFavorite correctly adds and removes artwork from favorites table`() = runTest {
        // GIVEN
        val baseDbo = ArtworkDBO(
            id = 99, title = "Mona Lisa", artist = "",
            imageUrl = "", imageDimensions = null, date = null, yearInt = null,
            technique = null, description = null, url = null, galleryImages = null,
        )
        database.artworkDao().insertArtworks(listOf(baseDbo))

        repository.getArtwork(99).test {
            val initial = awaitItem()
            assertFalse(initial!!.baseArtwork.isFavorite)

            // WHEN
            repository.toggleFavorite(99)
            val favAdded = awaitItem()

            // THEN
            assertTrue(favAdded!!.baseArtwork.isFavorite)

            // WHEN
            repository.toggleFavorite(99)
            val favRemoved = awaitItem()

            // THEN
            assertFalse(favRemoved!!.baseArtwork.isFavorite)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleFavorite retrieves DTO from search session cache and saves full snapshot`() = runTest {
        // Given artwork found in search
        val searchDto = ArtworkDTO(
            id = 555,
            title = "Search Artwork",
            artists = listOf(ru.createsmart.artopos.core.network.model.PersonDTO("Search Artist", "Artist")),
            imageUrl = "https://example.com/art.jpg",
            images = listOf(
                ru.createsmart.artopos.core.network.model.ImageDTO(
                    width = 1200,
                    height = 1600,
                    url = "https://example.com/art.jpg",
                ),
            ),
            date = "1900",
        )

        // Put DTO into search cache
        repository.searchSessionCache[searchDto.id] = searchDto

        // Database is empty before click
        assertNull(database.artworkDao().getArtworkSnapshot(555))

        // When user clicks favorite
        repository.toggleFavorite(555)

        // Then
        // 1. Snapshot is saved in SQLite
        val snapshot = database.artworkDao().getArtworkSnapshot(555)
        assertNotNull(snapshot)
        assertEquals("Search Artwork", snapshot?.title)
        assertEquals("Search Artist", snapshot?.artist)
        assertEquals(1200, snapshot?.imageDimensions?.width)
        assertEquals(1600, snapshot?.imageDimensions?.height)

        // 2. Artwork appears in favorites
        val favorites = repository.getFavoriteArtworks().first()
        assertEquals(1, favorites.size)
        assertEquals(555, favorites.first().id)
        assertTrue(favorites.first().isFavorite)
    }
}
