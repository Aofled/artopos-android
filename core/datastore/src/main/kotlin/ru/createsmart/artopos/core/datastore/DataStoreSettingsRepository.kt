package ru.createsmart.artopos.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.createsmart.artopos.core.domain.repository.SettingsRepository
import ru.createsmart.artopos.core.model.settings.ThemeConfig
import ru.createsmart.artopos.core.model.settings.UserSettings
import javax.inject.Inject
import javax.inject.Singleton

private const val THEME_CONFIG_KEY = "theme_config"
private const val LANGUAGE_KEY = "language"

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    private object PreferencesKeys {
        val THEME_CONFIG = stringPreferencesKey(THEME_CONFIG_KEY)
        val LANGUAGE = stringPreferencesKey(LANGUAGE_KEY)
    }

    override val userSettingsStream: Flow<UserSettings> = dataStore.data
        .map { preferences ->
            val themeString = preferences[PreferencesKeys.THEME_CONFIG] ?: ThemeConfig.FOLLOW_SYSTEM.name
            val themeConfig = try {
                ThemeConfig.valueOf(themeString)
            } catch (ignored: IllegalArgumentException) {
                ThemeConfig.FOLLOW_SYSTEM
            }

            val language = preferences[PreferencesKeys.LANGUAGE] ?: "" // "" = System Default

            UserSettings(
                themeConfig = themeConfig,
                languageCode = language,
            )
        }

    override suspend fun setThemeConfig(themeConfig: ThemeConfig) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_CONFIG] = themeConfig.name
        }
    }

    override suspend fun setLanguage(languageCode: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = languageCode
        }
    }
}
