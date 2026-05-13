package ru.createsmart.artopos.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.createsmart.artopos.core.model.settings.ThemeConfig
import ru.createsmart.artopos.core.model.settings.UserSettings

public interface SettingsRepository {
    public val userSettingsStream: Flow<UserSettings>
    public suspend fun setThemeConfig(themeConfig: ThemeConfig)
    public suspend fun setLanguage(languageCode: String)
}
