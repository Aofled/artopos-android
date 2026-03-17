package ru.createsmart.artopos.feature.settings

import ru.createsmart.artopos.core.model.settings.UserSettings

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Success(val settings: UserSettings) : SettingsUiState
}
