package ru.createsmart.artopos

import ru.createsmart.artopos.core.model.settings.UserSettings

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data class Success(
        val settings: UserSettings,
    ) : MainActivityUiState
}
