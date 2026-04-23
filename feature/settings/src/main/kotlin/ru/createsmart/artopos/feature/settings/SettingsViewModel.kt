package ru.createsmart.artopos.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.createsmart.artopos.core.designsystem.components.UiText
import ru.createsmart.artopos.core.domain.interactor.SettingsInteractor
import ru.createsmart.artopos.core.model.settings.ThemeConfig
import ru.createsmart.artopos.core.uicomponents.manager.UiMessageManager
import ru.createsmart.artopos.feature.settings.model.SettingsIntent
import javax.inject.Inject

private const val BYTES_IN_MEGABYTE = 1024L * 1024L

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val useCases: SettingsInteractor,
    private val uiMessageManager: UiMessageManager,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = useCases.getUserSettings()
        .map { settings ->
            SettingsUiState.Success(settings)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState.Loading,
        )

    val uiEffect = uiMessageManager.uiEffect

    private val _cacheSizeMb = MutableStateFlow<Long?>(null)
    val cacheSizeMb = _cacheSizeMb.asStateFlow()

    internal fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.UpdateTheme -> updateTheme(intent.themeConfig)
            is SettingsIntent.UpdateLanguage -> updateLanguage(intent.languageCode)
            is SettingsIntent.ClearCache -> clearAppCache()
        }
    }

    private fun updateTheme(themeConfig: ThemeConfig) {
        viewModelScope.launch {
            useCases.setThemeConfig(themeConfig)
        }
    }

    private fun updateLanguage(languageCode: String) {
        viewModelScope.launch {
            useCases.setLanguage(languageCode)
        }
    }

    internal fun calculateCacheSize() {
        viewModelScope.launch {
            val bytes = useCases.getImageCacheSizeUseCase()
            val megabytes = bytes / (BYTES_IN_MEGABYTE)
            _cacheSizeMb.value = megabytes
        }
    }

    private fun clearAppCache() {
        viewModelScope.launch {
            uiMessageManager.sendSideEffect(
                UiText.StringResource(R.string.settings_msg_cache_clearing),
            )

            val freedBytes = useCases.clearAppCacheUseCase()
            val freedMb = freedBytes / (BYTES_IN_MEGABYTE)

            val message = if (freedMb > 0) {
                UiText.StringResource(R.string.settings_msg_cache_cleared, freedMb)
            } else {
                UiText.StringResource(R.string.settings_msg_cache_empty)
            }

            uiMessageManager.sendSideEffect(message)

            calculateCacheSize()
        }
    }
}
