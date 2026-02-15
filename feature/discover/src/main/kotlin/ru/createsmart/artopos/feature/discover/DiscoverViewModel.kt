package ru.createsmart.artopos.feature.discover

import UiText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import ru.createsmart.artopos.core.common.util.NetworkMonitor
import ru.createsmart.artopos.core.domain.usecase.GetArtworksUseCase
import ru.createsmart.artopos.core.ui.R
import ru.createsmart.artopos.core.ui.theme.components.toUiText
import ru.createsmart.artopos.feature.discover.mapper.toUi
import ru.createsmart.artopos.feature.discover.model.ArtworkListItem
import javax.inject.Inject

private const val ERROR_DEBOUNCE_MS = 3000L

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    getArtworks: GetArtworksUseCase,
    private val networkMonitor: NetworkMonitor,
) : ViewModel() {
    val artworksFlow: Flow<PagingData<ArtworkListItem>> = getArtworks()
        .map { pagingData -> pagingData.map { it.toUi() } }
        .cachedIn(viewModelScope)

    private val _contentVersion = MutableStateFlow(0)
    val contentVersion = _contentVersion.asStateFlow() // Exposed to UI to force image reload on Pull-to-Refresh

    private val _uiEffect = Channel<UiText>(Channel.BUFFERED) // For Snackbar
    val uiEffect = _uiEffect.receiveAsFlow()

    private var lastEmittedMessage: UiText? = null
    private var lastEmittedTime: Long = 0L

    fun onRefresh(): Boolean {
        if (!checkInternetAndNotify()) return false

        _contentVersion.value++
        lastEmittedMessage = null // Reset debounce history so new errors can be shown fresh

        return true
    }

    fun onRetryAction(): Boolean {
        return checkInternetAndNotify()
    }

    fun onError(error: Throwable) {
        if (checkInternetAndNotify()) {
            sendSideEffect(error.toUiText())
        }
    }

    private fun checkInternetAndNotify(): Boolean {
        if (!networkMonitor.isOnline()) {
            sendSideEffect(UiText.StringResource(R.string.error_no_internet))
            return false
        }
        return true
    }

    private fun sendSideEffect(message: UiText) {
        val currentTime = System.currentTimeMillis()

        // Debounce Logic: prevent spamming the user with identical Snackbars (e.g. multiple image failures at once).
        if (message == lastEmittedMessage && (currentTime - lastEmittedTime) < ERROR_DEBOUNCE_MS) {
            return
        }

        lastEmittedMessage = message
        lastEmittedTime = currentTime
        _uiEffect.trySend(message)
    }
}
