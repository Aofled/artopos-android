package ru.createsmart.artopos.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.createsmart.artopos.core.model.settings.ThemeConfig
import ru.createsmart.artopos.core.model.settings.UserSettings
import javax.inject.Inject
import javax.inject.Singleton

private const val THEME_CONFIG_KEY = "theme_config"
private const val LANGUAGE_KEY = "language"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

interface SettingsRepository {
    val userSettingsStream: Flow<UserSettings>
    suspend fun setThemeConfig(themeConfig: ThemeConfig)
    suspend fun setLanguage(languageCode: String)
}

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : SettingsRepository {

    private object PreferencesKeys {
        val THEME_CONFIG = stringPreferencesKey(THEME_CONFIG_KEY)
        val LANGUAGE = stringPreferencesKey(LANGUAGE_KEY)
    }

    override val userSettingsStream: Flow<UserSettings> = context.dataStore.data
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
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_CONFIG] = themeConfig.name
        }
    }

    override suspend fun setLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = languageCode
        }
    }
}
