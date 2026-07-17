package ru.createsmart.artopos.core.datastore

import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.createsmart.artopos.core.model.settings.ThemeConfig
import ru.createsmart.artopos.core.model.settings.UserSettings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreSettingsDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    companion object {
        @VisibleForTesting
        internal const val KEY_THEME_CONFIG = "theme_config"

        @VisibleForTesting
        internal const val KEY_LANGUAGE = "language"

        private val THEME_CONFIG = stringPreferencesKey(KEY_THEME_CONFIG)
        private val LANGUAGE = stringPreferencesKey(KEY_LANGUAGE)
    }

    val userSettingsStream: Flow<UserSettings> = dataStore.data
        .map { preferences ->
            val themeString =
                preferences[THEME_CONFIG] ?: ThemeConfig.FOLLOW_SYSTEM.name
            val themeConfig = try {
                ThemeConfig.valueOf(themeString)
            } catch (ignored: IllegalArgumentException) {
                ThemeConfig.FOLLOW_SYSTEM
            }

            val language = preferences[LANGUAGE] ?: "" // "" = System Default

            UserSettings(
                themeConfig = themeConfig,
                languageCode = language,
            )
        }

    suspend fun setThemeConfig(themeConfig: ThemeConfig) {
        dataStore.edit { preferences ->
            preferences[THEME_CONFIG] = themeConfig.name
        }
    }

    suspend fun setLanguage(languageCode: String) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE] = languageCode
        }
    }
}
