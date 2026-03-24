package ru.createsmart.artopos.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.createsmart.artopos.core.model.settings.ThemeConfig
import ru.createsmart.artopos.core.model.settings.UserSettings

interface SettingsRepository {
    val userSettingsStream: Flow<UserSettings>
    suspend fun setThemeConfig(themeConfig: ThemeConfig)
    suspend fun setLanguage(languageCode: String)
}
