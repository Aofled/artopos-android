package ru.createsmart.artopos.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.createsmart.artopos.core.artworkcard.mapper.ArtworkUiMapper
import ru.createsmart.artopos.core.domain.usecase.GetFavoriteArtworksUseCase
import ru.createsmart.artopos.core.domain.usecase.ToggleFavoriteUseCase
import ru.createsmart.artopos.core.uicomponents.manager.UiMessageManager
import ru.createsmart.artopos.feature.favorites.model.FavoritesIntent
import javax.inject.Inject

/**
 * This screen contains a PullToRefreshBox for refreshing undownloaded images.
 * Case: A user added a painting to their Favorites,
 * but Coil hasn't finished downloading it yet, or the cache has been cleared.
 * The user opens Favorites without internet access and sees red placeholders. They pull the PullToRefresh button.
 */

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val getFavoritesUseCase: GetFavoriteArtworksUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val messageManager: UiMessageManager,
    private val mapper: ArtworkUiMapper,
) : ViewModel() {

    private val _contentVersion = MutableStateFlow(0)
    val contentVersion = _contentVersion.asStateFlow()

    val uiEffect = messageManager.uiEffect

    internal fun onIntent(intent: FavoritesIntent) {
        when (intent) {
            is FavoritesIntent.Refresh -> handleRefresh()
            is FavoritesIntent.ToggleFavorite -> handleToggleFavorite(intent.id)
            is FavoritesIntent.ArtworkClick -> Unit
        }
    }

    val uiState: StateFlow<FavoritesUiState> = getFavoritesUseCase()
        .map { list ->
            if (list.isEmpty()) {
                FavoritesUiState.Empty
            } else {
                FavoritesUiState.Success(list.map { mapper.mapToUi(it) }.toImmutableList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FavoritesUiState.Loading,
        )

    private fun handleToggleFavorite(id: Int) {
        viewModelScope.launch {
            toggleFavoriteUseCase(id)
        }
    }

    private fun handleRefresh() {
        if (!messageManager.checkInternetAndNotify()) return

        _contentVersion.value++

        messageManager.resetLastEmittedMessage()
    }
}
