package ru.createsmart.artopos.core.data.mediator

import android.content.Context
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.createsmart.artopos.core.database.HarvardDatabase
import ru.createsmart.artopos.core.database.model.ArtworkDBO
import ru.createsmart.artopos.core.database.model.ArtworkRemoteKeysEntity
import ru.createsmart.artopos.core.model.FilterParams
import ru.createsmart.artopos.core.network.api.HarvardAPI
import ru.createsmart.artopos.core.network.model.ArtworkDTO
import ru.createsmart.artopos.core.network.model.ImageDTO
import ru.createsmart.artopos.core.network.model.NetworkResponse
import ru.createsmart.artopos.core.network.model.PageInfo
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
@RunWith(AndroidJUnit4::class)
class ArtworkRemoteMediatorTest {

    private val api: HarvardAPI = mockk()
    private lateinit var database: HarvardDatabase
    private lateinit var param: FilterParams
    private lateinit var mediator: ArtworkRemoteMediator

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Test Strategy: Use an In-Memory database.
        database = Room.inMemoryDatabaseBuilder(context, HarvardDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        param = FilterParams(
            classification = null,
            century = null,
            culture = null,
        )

        mediator = ArtworkRemoteMediator(database, api, param)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `refresh load success saves data and returns success`() = runTest {
        // GIVEN
        val mockDto = ArtworkDTO(
            id = 1,
            title = "Test Artwork",
            images = listOf(ImageDTO(100, 100, "url")),
        )
        val networkResponse = NetworkResponse(
            info = PageInfo(page = 1, totalPages = 10, totalRecords = 100, nextUrl = "next"),
            records = listOf(mockDto),
        )
        coEvery {
            api.getArtworks(
                page = 1,
                size = 20,
                classification = any(),
                century = any(),
                culture = any(),
                sort = any(),
            )
        } returns networkResponse

        // Mock the PagingState
        val pagingState = PagingState<Int, ArtworkDBO>(
            pages = listOf(),
            anchorPosition = null,
            config = PagingConfig(pageSize = 20),
            leadingPlaceholderCount = 0,
        )

        // WHEN
        val result = mediator.load(LoadType.REFRESH, pagingState)

        // THEN
        assertTrue(result is RemoteMediator.MediatorResult.Success)

        // Verify Data: Check if API data is actually saved to Room
        val cachedArtworks = database.artworkDao().getAllArtworksForTest()
        assertEquals(1, cachedArtworks.size)
        assertEquals("Test Artwork", cachedArtworks.first().title)
        assertEquals(0, cachedArtworks.first().sortingIndex)

        // Verify Keys: Check if next page key (2) is calculated correctly
        val key = database.artworkRemoteKeysDao().remoteKeyArtworkId(1)
        assertEquals(null, key?.prevKey)
        assertEquals(2, key?.nextKey)
    }

    @Test
    fun `append load success fetching next page`() = runTest {
        // GIVEN
        val initialId = 1
        // Pre-condition: Simulate that Page 1 is already loaded in the DB.
        // RemoteMediator needs to find the 'nextKey' from the database to know what to load next.
        database.artworkRemoteKeysDao().insertAll(
            listOf(ArtworkRemoteKeysEntity(initialId, prevKey = null, nextKey = 2)),
        )

        val nextDto = ArtworkDTO(
            id = 2,
            title = "Next Page Artwork",
            images = listOf(ImageDTO(100, 100, "url2")),
        )
        // Expect call for Page 2
        coEvery {
            api.getArtworks(
                page = 2,
                size = 20,
                classification = any(),
                century = any(),
                culture = any(),
                sort = any(),
            )
        } returns NetworkResponse(
            info = PageInfo(page = 2, totalPages = 10, totalRecords = 100, nextUrl = "url"),
            records = listOf(nextDto),
        )

        // Mock State: Tell Mediator that we have one page with one item
        val pagingState = PagingState(
            pages = listOf(
                androidx.paging.PagingSource.LoadResult.Page(
                    data = listOf(mockk<ArtworkDBO> { coEvery { id } returns initialId }),
                    prevKey = null,
                    nextKey = 2,
                ),
            ),
            anchorPosition = null,
            config = PagingConfig(pageSize = 20),
            leadingPlaceholderCount = 0,
        )

        // WHEN
        val result = mediator.load(LoadType.APPEND, pagingState)

        // THEN
        assertTrue(result is RemoteMediator.MediatorResult.Success)

        val key = database.artworkRemoteKeysDao().remoteKeyArtworkId(2)
        assertEquals(1, key?.prevKey)
        assertEquals(3, key?.nextKey)
    }

    @Test
    fun `refresh load error returns mediator result error`() = runTest {
        // GIVEN
        coEvery {
            api.getArtworks(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } throws IOException("No internet")

        val pagingState = PagingState<Int, ArtworkDBO>(
            pages = listOf(),
            anchorPosition = null,
            config = PagingConfig(20),
            leadingPlaceholderCount = 0,
        )

        // WHEN
        val result = mediator.load(LoadType.REFRESH, pagingState)

        // THEN
        assertTrue(result is RemoteMediator.MediatorResult.Error)
        // Verify Safety: DB should be empty if refresh fails
        assertTrue(database.artworkDao().getAllArtworksForTest().isEmpty())
    }
}
