package ru.createsmart.artopos.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncMetadataDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    private companion object {
        val KEY_LAST_FILTERS_SYNC_TIMESTAMP = longPreferencesKey("last_filters_sync_timestamp")
    }

    // Returns 0L if filters were never synced.
    val lastFiltersSyncTimestampStream: Flow<Long> = dataStore.data
        // Handle IO errors
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_LAST_FILTERS_SYNC_TIMESTAMP] ?: 0L
        }

    suspend fun getLastFiltersSyncTimestamp(): Long {
        return lastFiltersSyncTimestampStream.first()
    }

    suspend fun setLastFiltersSyncTimestamp(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[KEY_LAST_FILTERS_SYNC_TIMESTAMP] = timestamp
        }
    }
}
