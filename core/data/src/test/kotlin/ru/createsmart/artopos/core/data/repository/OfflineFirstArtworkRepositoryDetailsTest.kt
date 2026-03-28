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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.createsmart.artopos.core.database.HarvardDatabase
import ru.createsmart.artopos.core.database.converters.StoredImage
import ru.createsmart.artopos.core.database.model.ArtworkDBO
import ru.createsmart.artopos.core.network.api.HarvardAPI
import ru.createsmart.artopos.core.network.model.ArtworkDTO
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class OfflineFirstArtworkRepositoryDetailsTest {

    private val api: HarvardAPI = mockk()
    private lateinit var database: HarvardDatabase
    private lateinit var repository: OfflineFirstArtworkRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, HarvardDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        repository = OfflineFirstArtworkRepository(database, api)
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
            imageUrl = "url", imageDimensions = null, date = null, yearInt = null,
            technique = null, description = null, url = null,
            galleryImages = listOf(StoredImage("url", 100, 100)),
        )
        database.artworkDao().insertArtworks(listOf(dbo))

        // WHEN
        val result = repository.getArtwork(1).first()

        // THEN
        assertNotNull(result)
        assertEquals("Test Title", result?.title)
        assertNull("Description should be null as details are missing", result?.description)
        assertEquals(1, result?.images?.size)
    }

    @Test
    fun `syncArtworkDetails fetches data and saves to details table`() = runTest {
        // GIVEN
        val baseDbo = ArtworkDBO(
            id = 1, sortingIndex = 5, title = "Base Title", artist = "",
            imageUrl = "", imageDimensions = null, date = null, yearInt = null,
            technique = null,
            description = "Description loaded from list",
            url = null, galleryImages = null,
        )
        database.artworkDao().insertArtworks(listOf(baseDbo))

        // GIVEN
        val detailDto = ArtworkDTO(
            id = 1,
            culture = "French",
            provenance = "Gift",
        )
        coEvery { api.getArtworkDetails(1) } returns detailDto

        // WHEN
        repository.syncArtworkDetails(1)

        // THEN
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

        val wrapper = database.artworkDao().getArtworkWithDetails(1).first()
        assertNull(wrapper?.artworkWithDetails?.details)
    }
}
