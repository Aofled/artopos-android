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
import ru.createsmart.artopos.core.datastore.SettingsRepository
import ru.createsmart.artopos.core.domain.usecase.ClearImageCacheUseCase
import ru.createsmart.artopos.core.domain.usecase.GetImageCacheSizeUseCase
import ru.createsmart.artopos.core.model.settings.ThemeConfig
import ru.createsmart.artopos.core.ui.manager.UiMessageManager
import javax.inject.Inject

private const val BYTES_IN_MEGABYTE = 1024L * 1024L

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val clearImageCacheUseCase: ClearImageCacheUseCase,
    private val getImageCacheSizeUseCase: GetImageCacheSizeUseCase,
    private val uiMessageManager: UiMessageManager,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = settingsRepository.userSettingsStream
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

    fun updateTheme(themeConfig: ThemeConfig) {
        viewModelScope.launch {
            settingsRepository.setThemeConfig(themeConfig)
        }
    }

    fun updateLanguage(languageCode: String) {
        viewModelScope.launch {
            settingsRepository.setLanguage(languageCode)
        }
    }

    fun calculateCacheSize() {
        viewModelScope.launch {
            val bytes = getImageCacheSizeUseCase()
            val megabytes = bytes / (BYTES_IN_MEGABYTE)
            _cacheSizeMb.value = megabytes
        }
    }

    fun clearImageCache() {
        viewModelScope.launch {
            uiMessageManager.sendSideEffect(
                UiText.StringResource(R.string.msg_clearing_cache),
            )

            val freedBytes = clearImageCacheUseCase()
            val freedMb = freedBytes / (BYTES_IN_MEGABYTE)

            val message = if (freedMb > 0) {
                UiText.StringResource(R.string.msg_cache_cleared, freedMb)
            } else {
                UiText.StringResource(R.string.msg_cache_empty)
            }

            uiMessageManager.sendSideEffect(message)

            calculateCacheSize()
        }
    }
}
