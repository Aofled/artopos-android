package ru.createsmart.artopos.feature.discover

import UiText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.createsmart.artopos.core.common.util.NetworkMonitor
import ru.createsmart.artopos.core.domain.usecase.GetArtworksUseCase
import ru.createsmart.artopos.core.domain.usecase.SyncArtworksUseCase
import ru.createsmart.artopos.core.ui.R
import ru.createsmart.artopos.core.ui.theme.components.toUiText
import ru.createsmart.artopos.feature.discover.mapper.toUi
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val getArtworks: GetArtworksUseCase,
    private val syncArtworks: SyncArtworksUseCase,
    private val networkMonitor: NetworkMonitor,
) : ViewModel() {
    private val _isRefreshing = MutableStateFlow(false)
    private val _isError = MutableStateFlow(false)
    private val _contentVersion = MutableStateFlow(0)

    private val _uiEffect = Channel<UiText>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    val uiState: StateFlow<DiscoverUiState> = combine(
        getArtworks(),
        _isRefreshing,
        _isError,
        _contentVersion,
    ) { artworks, isRefreshing, isError, contentVersion ->

        when {
            artworks.isEmpty() && isError -> DiscoverUiState.Error

            artworks.isEmpty() -> DiscoverUiState.Loading

            else -> DiscoverUiState.Success(
                artworks = artworks.map { it.toUi() },
                isRefreshing = isRefreshing,
                contentVersion = contentVersion,
            )
        }
    }
        .catch {
            emit(DiscoverUiState.Error)
        }
        .stateIn(
            scope = viewModelScope,
            // Important: Keeps data for 5 seconds during screen rotation.
            // Prevents restarting the Flow when the Activity is recreated.
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DiscoverUiState.Loading,
        )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isError.value = false

            if (!networkMonitor.isOnline()) {
                _uiEffect.send(UiText.StringResource(R.string.error_no_internet))
                _isError.value = true
                _isRefreshing.value = false
                return@launch
            }

            _isRefreshing.value = true
            _contentVersion.value++ // Increment version to force Coil to clear cache/retry for all images

            val result = syncArtworks()

            result.onFailure {
                _isError.value = true
                val message = it.toUiText()
                _uiEffect.send(message)
            }
            _isRefreshing.value = false
        }
    }
}
