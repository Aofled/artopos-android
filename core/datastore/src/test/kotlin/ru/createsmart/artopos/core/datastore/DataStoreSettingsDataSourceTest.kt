package ru.createsmart.artopos.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import ru.createsmart.artopos.core.model.settings.ThemeConfig

const val TEST_FILE_PREFERENCES = "test_settings.preferences_pb"
const val TEST_THEME_CONFIG = "test_settings.theme_config"

class DataStoreSettingsDataSourceTest {

    // Temporary folder that will be automatically deleted after the test
    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: DataStoreSettingsDataSource

    @Before
    fun setup() {
        // Create a real DataStore, write to a temporary file
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope.backgroundScope,
            produceFile = { tmpFolder.newFile(TEST_FILE_PREFERENCES) },
        )

        repository = DataStoreSettingsDataSource(dataStore)
    }

    @Test
    fun `userSettingsStream emits default values initially`() = testScope.runTest {
        // WHEN
        val settings = repository.userSettingsStream.first()

        // THEN
        assertEquals(ThemeConfig.FOLLOW_SYSTEM, settings.themeConfig)
        assertEquals("", settings.languageCode)
    }

    @Test
    fun `setThemeConfig updates value in stream`() = testScope.runTest {
        repository.userSettingsStream.test {
            awaitItem()

            // WHEN
            repository.setThemeConfig(ThemeConfig.DARK)

            // THEN
            val updatedSettings = awaitItem()
            assertEquals(ThemeConfig.DARK, updatedSettings.themeConfig)
        }
    }

    @Test
    fun `setLanguage updates value in stream`() = testScope.runTest {
        repository.userSettingsStream.test {
            awaitItem()

            // WHEN
            repository.setLanguage("fr")

            // THEN
            val updatedSettings = awaitItem()
            assertEquals("fr", updatedSettings.languageCode)
        }
    }

    @Test
    fun `handles corrupted or invalid enum string gracefully`() = testScope.runTest {
        dataStore.updateData { preferences ->
            preferences.toMutablePreferences().apply {
                val themeConfigKey = androidx.datastore.preferences.core.stringPreferencesKey(TEST_THEME_CONFIG)
                this[themeConfigKey] = "SOME_INVALID_THEME_STRING"
            }
        }

        val settings = repository.userSettingsStream.first()

        // THEN
        // Must return the fallback(FOLLOW_SYSTEM).
        assertEquals(ThemeConfig.FOLLOW_SYSTEM, settings.themeConfig)
    }
}
