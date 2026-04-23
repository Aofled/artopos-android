package ru.createsmart.artopos.feature.settings.model

import ru.createsmart.artopos.core.model.settings.ThemeConfig

sealed interface SettingsIntent {
    data class UpdateTheme(val themeConfig: ThemeConfig) : SettingsIntent
    data class UpdateLanguage(val languageCode: String) : SettingsIntent
    data object ClearCache : SettingsIntent
}
