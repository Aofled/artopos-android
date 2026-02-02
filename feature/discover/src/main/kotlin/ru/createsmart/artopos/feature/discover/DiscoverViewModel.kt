package ru.createsmart.artopos.feature.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.createsmart.artopos.core.domain.usecase.GetArtworksUseCase
import ru.createsmart.artopos.core.domain.usecase.SyncArtworksUseCase
import ru.createsmart.artopos.feature.discover.mapper.toUi
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val getArtworks: GetArtworksUseCase,
    private val syncArtworks: SyncArtworksUseCase,
) : ViewModel() {
    private val _isRefreshing = MutableStateFlow(false)
    private val _isError = MutableStateFlow(false)

    val uiState: StateFlow<DiscoverUiState> = combine(
        getArtworks(),
        _isRefreshing,
        _isError,
    ) { artworks, isRefreshing, isError ->

        when {
            artworks.isEmpty() && isError -> DiscoverUiState.Error

            artworks.isEmpty() -> DiscoverUiState.Loading

            else -> DiscoverUiState.Success(
                artworks = artworks.map { it.toUi() },
                isRefreshing = isRefreshing,
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
            _isRefreshing.value = true
            _isError.value = false

            val result = syncArtworks()

            result.onFailure {
                _isError.value = true
            }
            _isRefreshing.value = false
        }
    }
}
