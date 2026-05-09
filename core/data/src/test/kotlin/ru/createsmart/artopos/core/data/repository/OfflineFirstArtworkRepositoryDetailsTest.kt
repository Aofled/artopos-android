package ru.createsmart.artopos.core.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
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
import org.robolectric.RobolectricTestRunner
import ru.createsmart.artopos.core.data.mapper.ArtworkMapper
import ru.createsmart.artopos.core.database.HarvardDatabase
import ru.createsmart.artopos.core.database.converters.StoredImage
import ru.createsmart.artopos.core.database.model.ArtworkDBO
import ru.createsmart.artopos.core.database.model.ArtworkDetailsDBO
import ru.createsmart.artopos.core.model.CreationDate
import ru.createsmart.artopos.core.network.api.HarvardAPI
import ru.createsmart.artopos.core.network.model.ArtworkDTO
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
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
            id = 1, sortingIndex = 0, title = "Test Title", artist = "Artist",
            imageUrl = "url", imageDimensions = null, date = "1890", yearInt = 1890,
            technique = null, description = null, url = null,
            galleryImages = listOf(StoredImage("url", 100, 100)),
            inDiscoverFeed = true,
        )
        database.artworkDao().insertArtworks(listOf(dbo))

        // WHEN
        val result = repository.getArtwork(1).first() // Это ArtworkDetails?

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
            id = 1, sortingIndex = 5, title = "Base Title", artist = "",
            imageUrl = "", imageDimensions = null, date = null, yearInt = null,
            technique = null, description = "Description loaded from list",
            url = null, galleryImages = null, inDiscoverFeed = true,
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
            id = 99, sortingIndex = 0, title = "Mona Lisa", artist = "",
            imageUrl = "", imageDimensions = null, date = null, yearInt = null,
            technique = null, description = null, url = null, galleryImages = null,
            inDiscoverFeed = true,
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
    fun `clearDatabaseCache removes details ONLY for non-favorite artworks`() = runTest {
        // GIVEN
        val favId = 10
        val nonFavId = 20

        database.artworkDao().insertArtworks(listOf(createArtwork(favId), createArtwork(nonFavId)))
        database.artworkDetailDao().insertDetails(createDetails(favId, "Keep"))
        database.artworkDetailDao().insertDetails(createDetails(nonFavId, "Delete"))

        repository.toggleFavorite(favId)

        assertEquals("Keep", repository.getArtwork(10).first()?.provenance)
        assertEquals("Delete", repository.getArtwork(20).first()?.provenance)

        // WHEN
        repository.clearDatabaseCache()

        // THEN
        assertEquals("Keep", repository.getArtwork(10).first()?.provenance)

        assertNull(repository.getArtwork(20).first()?.provenance)
    }

    private fun createArtwork(id: Int, title: String = "Title", inFeed: Boolean = true) = ArtworkDBO(
        id = id,
        sortingIndex = 0,
        title = title,
        artist = "",
        imageUrl = "",
        imageDimensions = null,
        date = null,
        yearInt = null,
        technique = null,
        description = null,
        url = null,
        galleryImages = null,
        inDiscoverFeed = inFeed,
    )

    private fun createDetails(id: Int, provenance: String?) = ArtworkDetailsDBO(
        id = id,
        provenance = provenance,
        creditLine = null,
        classification = null,
        century = null,
        culture = null,
        medium = null,
        period = null,
        style = null,
        dimensions = null,
        copyright = null,
        galleryLocation = null,
    )
}
