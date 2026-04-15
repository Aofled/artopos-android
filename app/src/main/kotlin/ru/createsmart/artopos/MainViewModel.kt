package ru.createsmart.artopos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ru.createsmart.artopos.core.domain.repository.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<MainActivityUiState> = settingsRepository.userSettingsStream
        .map { settings ->
            MainActivityUiState.Success(settings)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            // Use Eagerly to apply the theme before the first frame is drawn
            initialValue = MainActivityUiState.Loading,
        )
}
