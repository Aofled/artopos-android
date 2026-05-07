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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.createsmart.artopos.core.data.mapper.ArtworkMapper
import ru.createsmart.artopos.core.database.HarvardDatabase
import ru.createsmart.artopos.core.database.model.ArtworkDBO
import ru.createsmart.artopos.core.database.model.ArtworkFeedProjectionDBO
import ru.createsmart.artopos.core.database.model.ArtworkFeedWithFavoriteFlagDBO
import ru.createsmart.artopos.core.database.model.ArtworkRemoteKeysEntity
import ru.createsmart.artopos.core.database.model.FavoriteDBO
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
    private val mapper = ArtworkMapper()

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

        mediator = ArtworkRemoteMediator(database, api, param, mapper)
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
        } returns networkResponse

        // Mock the PagingState
        val pagingState = PagingState<Int, ArtworkFeedWithFavoriteFlagDBO>(
            pages = listOf(),
            anchorPosition = null,
            config = PagingConfig(pageSize = 20),
            leadingPlaceholderCount = 0,
        )

        // WHEN
        val result = mediator.load(LoadType.REFRESH, pagingState)

        // THEN
        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)

        // Verify Data: Check if API data is actually saved to Room
        val cachedArtworks = database.artworkDao().getAllArtworksForTest()
        assertEquals(1, cachedArtworks.size)
        assertEquals("Test Artwork", cachedArtworks.first().title)
        assertEquals(0, cachedArtworks.first().sortingIndex)
        assertTrue(cachedArtworks.first().inDiscoverFeed)

        // Verify Keys: Check if next page key (2) is calculated correctly
        val key = database.artworkRemoteKeysDao().remoteKeyArtworkId(1)
        assertEquals(null, key?.prevKey)
        assertEquals(2, key?.nextKey)
    }

    @Test
    fun `append load success fetching next page`() = runTest {
        // GIVEN
        val initialId = 1

        val projectionDbo = ArtworkFeedProjectionDBO(
            id = initialId,
            title = "First Page",
            artist = "",
            imageUrl = "",
            imageDimensions = null,
        )

        val mockFlagDbo = ArtworkFeedWithFavoriteFlagDBO(artwork = projectionDbo, isFavorite = false)

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
                any(),
                any(), any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } returns NetworkResponse(
            info = PageInfo(page = 2, totalPages = 10, totalRecords = 100, nextUrl = "url"),
            records = listOf(nextDto),
        )

        // Mock State: Tell Mediator that we have one page with one item
        val pagingState = PagingState<Int, ArtworkFeedWithFavoriteFlagDBO>(
            pages = listOf(
                androidx.paging.PagingSource.LoadResult.Page(
                    data = listOf(mockFlagDbo),
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

        val pagingState = PagingState<Int, ArtworkFeedWithFavoriteFlagDBO>(
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

    @Test
    fun `prepend load returns success with endOfPaginationReached when at the top of list`() = runTest {
        // GIVEN: The database already contains the first page, and it does not have a previous one (prevKey = null)
        val initialId = 1
        val projectionDbo = ArtworkFeedProjectionDBO(
            id = initialId,
            title = "First Page",
            artist = "",
            imageUrl = "",
            imageDimensions = null,
        )
        val mockFlagDbo = ArtworkFeedWithFavoriteFlagDBO(artwork = projectionDbo, isFavorite = false)

        database.artworkRemoteKeysDao().insertAll(
            listOf(ArtworkRemoteKeysEntity(initialId, prevKey = null, nextKey = 2)),
        )

        val pagingState = PagingState<Int, ArtworkFeedWithFavoriteFlagDBO>(
            pages = listOf(
                // Simulate that the first page has already been loaded
                androidx.paging.PagingSource.LoadResult.Page(data = listOf(mockFlagDbo), prevKey = null, nextKey = 2),
            ),
            anchorPosition = null,
            config = PagingConfig(pageSize = 20),
            leadingPlaceholderCount = 0,
        )

        // WHEN
        val result = mediator.load(LoadType.PREPEND, pagingState)

        // THEN
        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun `refresh load filters empty images and reaches end of pagination`() = runTest {
        // GIVEN: 1 valid DTO, 1 invalid DTO (no images)
        val validDto = ArtworkDTO(id = 1, title = "Valid", images = listOf(ImageDTO(100, 100, "url")))
        val invalidDto = ArtworkDTO(id = 2, title = "Invalid", images = emptyList())

        val networkResponse = NetworkResponse(
            info = PageInfo(page = 1, totalPages = 1, totalRecords = 2, nextUrl = null), // nextUrl = null means end
            records = listOf(validDto, invalidDto),
        )

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
        } returns networkResponse

        val pagingState = PagingState<Int, ArtworkFeedWithFavoriteFlagDBO>(
            pages = listOf(),
            anchorPosition = null,
            config = PagingConfig(pageSize = 20),
            leadingPlaceholderCount = 0,
        )

        // WHEN
        val result = mediator.load(LoadType.REFRESH, pagingState)

        // THEN
        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)

        val cachedArtworks = database.artworkDao().getAllArtworksForTest()
        // Invalid item (ID = 2) should be filtered out
        assertEquals(1, cachedArtworks.size)
        assertEquals(1, cachedArtworks.first().id)
    }

    @Test
    fun `CRITICAL - refresh clears old feed but keeps favorites intact`() = runTest {
        // GIVEN: The database contains an old film and one of the pictures has been added to favorites.
        val oldFeedArtwork = ArtworkDBO(
            id = 10, sortingIndex = 0, title = "Old Feed", artist = "", imageUrl = "",
            imageDimensions = null, date = null, yearInt = null, technique = null,
            description = null, url = null, galleryImages = null, inDiscoverFeed = true,
        )
        val favoriteArtwork = ArtworkDBO(
            id = 20, sortingIndex = 1, title = "Favorite", artist = "", imageUrl = "",
            imageDimensions = null, date = null, yearInt = null, technique = null,
            description = null, url = null, galleryImages = null, inDiscoverFeed = true,
        )

        database.artworkDao().insertArtworks(listOf(oldFeedArtwork, favoriteArtwork))
        database.favoriteDao().insertFavorite(FavoriteDBO(id = 20, savedAtTimestamp = 123L))

        val newDto = ArtworkDTO(id = 30, title = "New Feed", images = listOf(ImageDTO(100, 100, "url")))
        coEvery {
            api.getArtworks(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns NetworkResponse(
            info = PageInfo(page = 1, totalPages = 10, totalRecords = 100, nextUrl = "next"),
            records = listOf(newDto),
        )

        val pagingState = PagingState<Int, ArtworkFeedWithFavoriteFlagDBO>(
            pages = listOf(),
            anchorPosition = null,
            config = PagingConfig(pageSize = 20),
            leadingPlaceholderCount = 0,
        )

        // WHEN: Refresh (e.g., user changes culture from America to Japan)
        mediator.load(LoadType.REFRESH, pagingState)

        // THEN
        val cache = database.artworkDao().getAllArtworksForTest()

        // There should be 2 pictures left (Selected old + New from the feed)
        assertEquals(2, cache.size)

        // 1. Old un-featured painting (ID 10) MUST BE REMOVED
        assertTrue(cache.none { it.id == 10 })

        // 2. The old Featured Painting (ID 20) SHOULD REMAIN, but its ribbon flag should be reset to false
        val savedFavorite = cache.find { it.id == 20 }!!
        assertEquals(false, savedFavorite.inDiscoverFeed)

        // 3. New painting (ID 30) MUST BE ADDED with ribbon flag = true
        val newFeedItem = cache.find { it.id == 30 }!!
        assertEquals(true, newFeedItem.inDiscoverFeed)
    }
}
