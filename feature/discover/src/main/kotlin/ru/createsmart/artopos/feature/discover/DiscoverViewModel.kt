package ru.createsmart.artopos.feature.discover

import UiText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.createsmart.artopos.core.common.util.NetworkMonitor
import ru.createsmart.artopos.core.domain.usecase.GetArtworksUseCase
import ru.createsmart.artopos.core.domain.usecase.GetFiltersUseCase
import ru.createsmart.artopos.core.domain.usecase.InitializeFiltersUseCase
import ru.createsmart.artopos.core.model.FilterParams
import ru.createsmart.artopos.core.model.FilterType
import ru.createsmart.artopos.core.ui.R
import ru.createsmart.artopos.core.ui.theme.components.toUiText
import ru.createsmart.artopos.feature.discover.mapper.toUi
import javax.inject.Inject

private const val ERROR_DEBOUNCE_MS = 3000L

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    getArtworks: GetArtworksUseCase,
    private val networkMonitor: NetworkMonitor,
    getFiltersUseCase: GetFiltersUseCase,
    private val initializeFiltersUseCase: InitializeFiltersUseCase,
) : ViewModel() {

    private val _contentVersion = MutableStateFlow(0)
    val contentVersion = _contentVersion.asStateFlow() // Exposed to UI to force image reload on Pull-to-Refresh

    private val _uiEffect = Channel<UiText>(Channel.BUFFERED) // For Snackbar
    val uiEffect = _uiEffect.receiveAsFlow()

    private var lastEmittedMessage: UiText? = null
    private var lastEmittedTime: Long = 0L

    private val _filterParams = MutableStateFlow(FilterParams())
    val filterParams = _filterParams.asStateFlow()

    val classifications = combine(
        getFiltersUseCase(FilterType.CLASSIFICATION),
        _filterParams,
    ) { list, state -> list.toUi(state.classification) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val centuries = combine(
        getFiltersUseCase(FilterType.CENTURY),
        _filterParams,
    ) { list, state -> list.toUi(state.century) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cultures = combine(
        getFiltersUseCase(FilterType.CULTURE),
        _filterParams,
    ) { list, state -> list.toUi(state.culture) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val artworksFlow = _filterParams
        .flatMapLatest { params ->
            getArtworks(params) // It depends on the filter
        }
        .map { pagingData -> pagingData.map { it.toUi() } }
        .cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            initializeFiltersUseCase()
        }
    }

    fun onFilterChanged(type: FilterType, value: String?) {
        _filterParams.value = when (type) {
            FilterType.CLASSIFICATION -> _filterParams.value.copy(classification = value)
            FilterType.CENTURY -> _filterParams.value.copy(century = value)
            FilterType.CULTURE -> _filterParams.value.copy(culture = value)
        }
    }

    fun onRefresh(): Boolean {
        if (!checkInternetAndNotify()) return false

        viewModelScope.launch {
            initializeFiltersUseCase()
        }

        _contentVersion.value++
        lastEmittedMessage = null // Reset debounce history so new errors can be shown fresh

        return true
    }

    fun onRetryAction(): Boolean {
        if (checkInternetAndNotify()) {
            viewModelScope.launch {
                initializeFiltersUseCase()
            }
            return true
        }
        return false
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
