package ru.createsmart.artopos.core.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.createsmart.artopos.core.data.mapper.FilterMapper
import ru.createsmart.artopos.core.database.dao.FilterItemDao
import ru.createsmart.artopos.core.database.model.FilterItemDBO
import ru.createsmart.artopos.core.datastore.SyncMetadataDataSource
import ru.createsmart.artopos.core.domain.time.TimeProvider
import ru.createsmart.artopos.core.model.FilterType
import ru.createsmart.artopos.core.network.api.HarvardAPI
import ru.createsmart.artopos.core.network.model.FilterItemDTO
import ru.createsmart.artopos.core.network.model.NetworkResponse
import ru.createsmart.artopos.core.network.model.PageInfo
import java.io.IOException
import kotlin.time.Duration.Companion.days

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class OfflineFirstFilterRepositoryTest {

    private val dao: FilterItemDao = mockk(relaxed = true)
    private val api: HarvardAPI = mockk()
    private val mapper = FilterMapper()
    private val syncMetadata: SyncMetadataDataSource = mockk(relaxed = true)
    private val timeProvider: TimeProvider = mockk()

    private lateinit var repository: OfflineFirstFilterRepository

    private val currentTime = 1_700_000_000_000L // Fixed timestamp for tests

    @Before
    fun setUp() {
        coEvery { timeProvider.getCurrentTimeMillis() } returns currentTime

        repository = OfflineFirstFilterRepository(
            dao = dao,
            api = api,
            mapper = mapper,
            syncMetadata = syncMetadata,
            timeProvider = timeProvider,
        )
    }

    @Test
    fun `initializeFilters skips network call when db is not empty and cache is fresh`() = runTest {
        // GIVEN: Filters exist in database and last sync was 10 days ago (< 30 days)
        val tenDaysAgo = currentTime - 10.days.inWholeMilliseconds
        coEvery { dao.getDistinctFilterTypes() } returns FilterType.entries.map { it.name }
        coEvery { syncMetadata.getLastFiltersSyncTimestamp() } returns tenDaysAgo

        // WHEN
        val result = repository.initializeFilters()

        // THEN: Do not call network and do not overwrite database
        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { api.getClassification() }
        coVerify(exactly = 0) { dao.replaceFilters(any()) }
        coVerify(exactly = 0) { syncMetadata.setLastFiltersSyncTimestamp(any()) }
    }

    @Test
    fun `initializeFilters fetches from network when cache is expired (more than 30 days)`() = runTest {
        // GIVEN: Filters exist but last sync was 31 days ago (> 30 days)
        val thirtyOneDaysAgo = currentTime - 31.days.inWholeMilliseconds
        coEvery { dao.getDistinctFilterTypes() } returns FilterType.entries.map { it.name }
        coEvery { syncMetadata.getLastFiltersSyncTimestamp() } returns thirtyOneDaysAgo

        val mockResponse = createMockFilterResponse()
        coEvery { api.getClassification() } returns mockResponse
        coEvery { api.getCentury() } returns mockResponse
        coEvery { api.getCulture() } returns mockResponse

        // WHEN
        val result = repository.initializeFilters()

        // THEN: Network is called, database is updated, timestamp is updated with current time
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { api.getClassification() }
        coVerify(exactly = 1) { api.getCentury() }
        coVerify(exactly = 1) { api.getCulture() }

        val slot = slot<List<FilterItemDBO>>()
        coVerify(exactly = 1) { dao.replaceFilters(capture(slot)) }
        assertEquals(3, slot.captured.size)

        coVerify(exactly = 1) { syncMetadata.setLastFiltersSyncTimestamp(currentTime) }
    }

    @Test
    fun `initializeFilters fetches from network on first launch when db is empty regardless of timestamp`() = runTest {
        // GIVEN: First launch - database is empty (hasAllCategories = false)
        coEvery { dao.getDistinctFilterTypes() } returns emptyList()

        val mockResponse = createMockFilterResponse()
        coEvery { api.getClassification() } returns mockResponse
        coEvery { api.getCentury() } returns mockResponse
        coEvery { api.getCulture() } returns mockResponse

        // WHEN
        val result = repository.initializeFilters()

        // THEN: Network is called and data is saved
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { dao.replaceFilters(any()) }
        coVerify(exactly = 1) { syncMetadata.setLastFiltersSyncTimestamp(currentTime) }
    }

    @Test
    fun `initializeFilters handles network error on expired cache without wiping database`() = runTest {
        // GIVEN: 31 days passed, but network failed with error
        val thirtyOneDaysAgo = currentTime - 31.days.inWholeMilliseconds
        coEvery { dao.getDistinctFilterTypes() } returns FilterType.entries.map { it.name }
        coEvery { syncMetadata.getLastFiltersSyncTimestamp() } returns thirtyOneDaysAgo
        coEvery { api.getClassification() } throws IOException("No internet connection")

        // WHEN
        val result = repository.initializeFilters()

        // THEN:
        // 1. Method returns Result.failure
        // 2. Database is NOT cleared or overwritten (old data is safe)
        // 3. Timestamp is NOT updated (retry on next launch)
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { dao.replaceFilters(any()) }
        coVerify(exactly = 0) { syncMetadata.setLastFiltersSyncTimestamp(any()) }
    }

    @Test
    fun `initializeFilters forces fetch when forceRefresh is true even if cache is fresh`() = runTest {
        // GIVEN: Sync was 2 days ago, but forceRefresh = true is passed
        val twoDaysAgo = currentTime - 2.days.inWholeMilliseconds
        coEvery { dao.getDistinctFilterTypes() } returns FilterType.entries.map { it.name }
        coEvery { syncMetadata.getLastFiltersSyncTimestamp() } returns twoDaysAgo

        val mockResponse = createMockFilterResponse()
        coEvery { api.getClassification() } returns mockResponse
        coEvery { api.getCentury() } returns mockResponse
        coEvery { api.getCulture() } returns mockResponse

        // WHEN
        val result = repository.initializeFilters(forceRefresh = true)

        // THEN: Network is called forcefully
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { api.getClassification() }
        coVerify(exactly = 1) { dao.replaceFilters(any()) }
        coVerify(exactly = 1) { syncMetadata.setLastFiltersSyncTimestamp(currentTime) }
    }

    private fun createMockFilterResponse() = NetworkResponse(
        info = PageInfo(1, 1, 1, ""),
        records = listOf(FilterItemDTO(1, "Test", 10, null)),
    )
}
