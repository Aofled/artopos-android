package ru.createsmart.artopos.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.createsmart.artopos.core.domain.interactor.FavoritesInteractor
import ru.createsmart.artopos.core.uicomponents.manager.UiMessageManager
import ru.createsmart.artopos.feature.artworkcard.mapper.ArtworkUiMapper
import javax.inject.Inject

/**
 * This screen contains a PullToRefreshBox for refreshing undownloaded images.
 * Case: A user added a painting to their Favorites,
 * but Coil hasn't finished downloading it yet, or the cache has been cleared.
 * The user opens Favorites without internet access and sees red placeholders. They pull the PullToRefresh button.
 */

private const val REFRESH_DELAY_MS = 300L

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val useCases: FavoritesInteractor,
    private val messageManager: UiMessageManager,
    private val mapper: ArtworkUiMapper,
) : ViewModel() {

    private val _contentVersion = MutableStateFlow(0)
    val contentVersion = _contentVersion.asStateFlow()

    val uiEffect = messageManager.uiEffect

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val uiState: StateFlow<FavoritesUiState> = useCases.getFavoritesUseCase()
        .map { list ->
            if (list.isEmpty()) {
                FavoritesUiState.Empty
            } else {
                FavoritesUiState.Success(list.map { mapper.mapToUi(it) })
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FavoritesUiState.Empty,
        )

    fun onToggleFavorite(id: Int) {
        viewModelScope.launch {
            useCases.toggleFavoriteUseCase(id)
        }
    }

    fun onRefresh() {
        if (!messageManager.checkInternetAndNotify()) {
            return
        }
        viewModelScope.launch {
            _isRefreshing.value = true
            _contentVersion.value++
            delay(REFRESH_DELAY_MS) // Artificial delay for UI smoothness
            _isRefreshing.value = false
        }
        messageManager.resetLastEmittedMessage()
    }
}
