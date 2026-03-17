package ru.createsmart.artopos

import ru.createsmart.artopos.core.model.settings.ThemeConfig

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data class Success(val themeConfig: ThemeConfig) : MainActivityUiState
}
