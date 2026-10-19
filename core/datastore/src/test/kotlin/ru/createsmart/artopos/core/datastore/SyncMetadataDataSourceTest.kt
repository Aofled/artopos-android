package ru.createsmart.artopos.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

private const val TEST_SYNC_PREFERENCES = "test_sync_metadata.preferences_pb"

@OptIn(ExperimentalCoroutinesApi::class)
class SyncMetadataDataSourceTest {

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var dataSource: SyncMetadataDataSource

    @Before
    fun setup() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope.backgroundScope,
            produceFile = { tmpFolder.newFile(TEST_SYNC_PREFERENCES) },
        )
        dataSource = SyncMetadataDataSource(dataStore)
    }

    @Test
    fun `lastFiltersSyncTimestampStream returns 0 initially when no sync occurred`() = testScope.runTest {
        dataSource.lastFiltersSyncTimestampStream.test {
            assertEquals(0L, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setLastFiltersSyncTimestamp updates value in stream and getter`() = testScope.runTest {
        val expectedTimestamp = 1_700_000_000_000L

        dataSource.lastFiltersSyncTimestampStream.test {
            assertEquals(0L, awaitItem())

            // WHEN
            dataSource.setLastFiltersSyncTimestamp(expectedTimestamp)

            // THEN: Stream emits new value
            assertEquals(expectedTimestamp, awaitItem())

            // THEN: One-shot getter also returns current value
            assertEquals(expectedTimestamp, dataSource.getLastFiltersSyncTimestamp())

            cancelAndIgnoreRemainingEvents()
        }
    }
}
