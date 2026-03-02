package ru.createsmart.artopos.feature.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.createsmart.artopos.core.domain.usecase.GetArtworkDetailsUseCase
import ru.createsmart.artopos.core.domain.usecase.SyncArtworkDetailsUseCase
import ru.createsmart.artopos.core.navigation.DetailsRoute
import ru.createsmart.artopos.feature.details.mapper.toDetailUi
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getArtworkDetails: GetArtworkDetailsUseCase,
    private val syncArtworkDetails: SyncArtworkDetailsUseCase,
) : ViewModel() {
    private val routeArgs = savedStateHandle.toRoute<DetailsRoute>()
    private val artworkId = routeArgs.artworkId

    private val _contentVersion = MutableStateFlow(0)
    val contentVersion = _contentVersion.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val uiState: StateFlow<ArtworkDetailUiState> = getArtworkDetails(artworkId)
        .map { artwork ->
            if (artwork != null) {
                ArtworkDetailUiState.Success(artwork.toDetailUi())
            } else {
                ArtworkDetailUiState.Loading
            }
        }
        .catch { emit(ArtworkDetailUiState.Error) }
        .stateIn(
            scope = viewModelScope,
            // Keep flow active for 5s after UI unsubscribe (avoids reloads on fast navigation)
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ArtworkDetailUiState.Loading,
        )

    init {
        viewModelScope.launch {
            syncArtworkDetails(artworkId)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true

            _contentVersion.value++
            syncArtworkDetails(artworkId)

            _isRefreshing.value = false
        }
    }
}
