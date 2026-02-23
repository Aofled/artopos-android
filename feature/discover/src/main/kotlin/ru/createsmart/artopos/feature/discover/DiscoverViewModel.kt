package ru.createsmart.artopos.feature.discover

import UiText
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
import ru.createsmart.artopos.feature.discover.model.FiltersUiState
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

    private val _activeFilterParams = MutableStateFlow(FilterParams())
    private val _draftFilterParams = MutableStateFlow(FilterParams())

    // --- UI FLOWS (Depends on DRAFT) ---

    private val _classificationsFlow = combine(
        getFiltersUseCase(FilterType.CLASSIFICATION),
        _draftFilterParams,
    ) { list, params ->
        list.toUi(params.classification)
    }

    private val _centuriesFlow = combine(
        getFiltersUseCase(FilterType.CENTURY),
        _draftFilterParams,
    ) { list, params ->
        list.toUi(params.century)
    }

    private val _culturesFlow = combine(
        getFiltersUseCase(FilterType.CULTURE),
        _draftFilterParams,
    ) { list, params ->
        list.toUi(params.culture)
    }

    val filtersUiState: StateFlow<FiltersUiState> = combine(
        _classificationsFlow,
        _centuriesFlow,
        _culturesFlow,
    ) { classList, centList, cultList ->
        FiltersUiState(
            classifications = classList,
            centuries = centList,
            cultures = cultList,
            isAvailable = classList.isNotEmpty() && centList.isNotEmpty() && cultList.isNotEmpty(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FiltersUiState())

    // --- PAGING FLOW (Depends on ACTIVE) ---

    @OptIn(ExperimentalCoroutinesApi::class)
    val artworksFlow = _activeFilterParams
        .flatMapLatest { params ->
            Log.d("artworksFlow", " $params")
            getArtworks(params) // It depends on the filter
        }
        .map { pagingData -> pagingData.map { it.toUi() } }
        .cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            initializeFiltersUseCase()
        }
    }

    // --- ACTIONS ---

    fun onFilterSelect(type: FilterType, value: String?) {
        val currentDraft = _draftFilterParams.value
        _draftFilterParams.value = when (type) {
            FilterType.CLASSIFICATION -> currentDraft.copy(classification = value)
            FilterType.CENTURY -> currentDraft.copy(century = value)
            FilterType.CULTURE -> currentDraft.copy(culture = value)
        }
    }

    fun onFilterReset() {
        _draftFilterParams.value = FilterParams()
    }

    fun onFilterApply() {
        if (_activeFilterParams.value != _draftFilterParams.value) {
            _activeFilterParams.value = _draftFilterParams.value
        }
    }

    fun onFilterOpen() {
        _draftFilterParams.value = _activeFilterParams.value
    }

    // --- ERROR HANDLING ---

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
