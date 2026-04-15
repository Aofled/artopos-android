package ru.createsmart.artopos.core.data.repository

import kotlinx.coroutines.flow.Flow
import ru.createsmart.artopos.core.datastore.DataStoreSettingsDataSource
import ru.createsmart.artopos.core.domain.repository.SettingsRepository
import ru.createsmart.artopos.core.model.settings.ThemeConfig
import ru.createsmart.artopos.core.model.settings.UserSettings
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataSource: DataStoreSettingsDataSource,
) : SettingsRepository {

    override val userSettingsStream: Flow<UserSettings>
        get() = dataSource.userSettingsStream

    override suspend fun setThemeConfig(themeConfig: ThemeConfig) {
        dataSource.setThemeConfig(themeConfig)
    }

    override suspend fun setLanguage(languageCode: String) {
        dataSource.setLanguage(languageCode)
    }
}
