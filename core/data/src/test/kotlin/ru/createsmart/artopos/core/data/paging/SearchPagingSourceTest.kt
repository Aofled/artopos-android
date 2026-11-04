package ru.createsmart.artopos.core.data.paging

import androidx.paging.PagingSource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.createsmart.artopos.core.model.FilterParams
import ru.createsmart.artopos.core.model.error.AppError
import ru.createsmart.artopos.core.network.api.HarvardAPI
import ru.createsmart.artopos.core.network.model.ArtworkDTO
import ru.createsmart.artopos.core.network.model.ImageDTO
import ru.createsmart.artopos.core.network.model.NetworkResponse
import ru.createsmart.artopos.core.network.model.PageInfo
import java.io.IOException

class SearchPagingSourceTest {

    private val api: HarvardAPI = mockk()
    private val params = FilterParams()

    @Test
    fun `load returns Page with filtered valid records`() = runTest {
        // GIVEN
        val validDto = ArtworkDTO(id = 1, title = "Valid", images = listOf(ImageDTO(100, 100, "url")))
        val invalidDto = ArtworkDTO(id = 2, title = "No Image", images = emptyList())

        val response = NetworkResponse(
            info = PageInfo(page = 1, totalPages = 5, totalRecords = 50, nextUrl = "next_url"),
            records = listOf(validDto, invalidDto),
        )

        coEvery {
            api.getArtworks(
                query = any(),
                page = 1,
                size = 20,
                classification = any(),
                century = any(),
                culture = any(),
                sort = any(),
                sortOrder = any(),
            )
        } returns response

        val pagingSource = SearchPagingSource(api, "Monet", params)

        // WHEN
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false),
        )

        // THEN
        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals(1, page.data.size)
        assertEquals(1, page.data.first().id)
        assertNull(page.prevKey)
        assertEquals(2, page.nextKey)
    }

    @Test
    fun `load returns Error on network exception mapped to AppError Network`() = runTest {
        // GIVEN
        coEvery {
            api.getArtworks(
                query = any(),
                page = any(),
                size = any(),
                classification = any(),
                century = any(),
                culture = any(),
                sort = any(),
                sortOrder = any(),
            )
        } throws IOException("No connection")

        val pagingSource = SearchPagingSource(api, "Monet", params)

        // WHEN
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false),
        )

        // THEN
        assertTrue(result is PagingSource.LoadResult.Error)
        val errorResult = result as PagingSource.LoadResult.Error
        assertTrue(errorResult.throwable is AppError.Network)
    }

    @Test
    fun `load sets nextKey to null when last page reached`() = runTest {
        // GIVEN
        val validDto = ArtworkDTO(id = 1, title = "Last", images = listOf(ImageDTO(100, 100, "url")))
        val response = NetworkResponse(
            info = PageInfo(page = 3, totalPages = 3, totalRecords = 1, nextUrl = null),
            records = listOf(validDto),
        )

        coEvery {
            api.getArtworks(
                query = any(),
                page = any(),
                size = any(),
                classification = any(),
                century = any(),
                culture = any(),
                sort = any(),
                sortOrder = any(),
            )
        } returns response

        val pagingSource = SearchPagingSource(api, "Monet", params)

        // WHEN
        val result = pagingSource.load(
            PagingSource.LoadParams.Append(key = 3, loadSize = 20, placeholdersEnabled = false),
        )

        // THEN
        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals(2, page.prevKey)
        assertNull(page.nextKey)
    }

    @Test
    fun `load calls onPageLoaded callback with valid records`() = runTest {
        val validDto = ArtworkDTO(id = 1, title = "Valid", images = listOf(ImageDTO(100, 100, "url")))
        val response = NetworkResponse(
            info = PageInfo(page = 1, totalPages = 1, totalRecords = 1, nextUrl = null),
            records = listOf(validDto),
        )
        coEvery {
            api.getArtworks(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns response

        var cachedDtos: List<ArtworkDTO> = emptyList()
        val pagingSource = SearchPagingSource(api, "Monet", params) {
            cachedDtos = it
        }

        pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false),
        )

        assertEquals(1, cachedDtos.size)
        assertEquals(1, cachedDtos.first().id)
    }
}
