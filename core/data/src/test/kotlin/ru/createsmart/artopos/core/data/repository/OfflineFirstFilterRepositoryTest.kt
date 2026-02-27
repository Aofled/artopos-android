package ru.createsmart.artopos.core.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import ru.createsmart.artopos.core.database.dao.FilterItemDao
import ru.createsmart.artopos.core.database.model.FilterItemDBO
import ru.createsmart.artopos.core.network.api.HarvardAPI
import ru.createsmart.artopos.core.network.model.FilterItemDTO
import ru.createsmart.artopos.core.network.model.NetworkResponse
import ru.createsmart.artopos.core.network.model.PageInfo
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class OfflineFirstFilterRepositoryTest {

    private val dao: FilterItemDao = mockk(relaxed = true)
    private val api: HarvardAPI = mockk()

    private val repository = OfflineFirstFilterRepository(dao, api)

    @Test
    fun `initializeFilters skips network call if db is not empty`() = runTest {
        // GIVEN
        coEvery { dao.hasAllCategories() } returns true

        // WHEN
        repository.initializeFilters()

        // THEN
        coVerify(exactly = 0) { api.getClassification() }
        coVerify(exactly = 0) { dao.insertFilters(any()) }
    }

    @Test
    fun `initializeFilters fetches data and saves to db if db is empty`() = runTest {
        // GIVEN
        coEvery { dao.hasAllCategories() } returns false

        // GIVEN
        val mockResponse = NetworkResponse(
            info = PageInfo(1, 1, 1, ""),
            records = listOf(FilterItemDTO(1, "Test", 10)),
        )
        coEvery { api.getClassification() } returns mockResponse
        coEvery { api.getCentury() } returns mockResponse
        coEvery { api.getCulture() } returns mockResponse

        // WHEN
        repository.initializeFilters()

        // THEN
        coVerify(exactly = 1) { api.getClassification() }
        coVerify(exactly = 1) { api.getCentury() }
        coVerify(exactly = 1) { api.getCulture() }

        // THEN
        val slot = slot<List<FilterItemDBO>>()
        coVerify { dao.insertFilters(capture(slot)) }

        assertEquals(3, slot.captured.size)
    }

    @Test
    fun `initializeFilters handles network error gracefully`() = runTest {
        // GIVEN
        coEvery { dao.hasAllCategories() } returns false

        // GIVEN
        coEvery { api.getClassification() } throws IOException("Network error")

        // WHEN
        repository.initializeFilters()

        // THEN
        coVerify(exactly = 0) { dao.insertFilters(any()) }
    }
}
