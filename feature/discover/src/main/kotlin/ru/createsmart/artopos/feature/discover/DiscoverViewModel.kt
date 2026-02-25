package ru.createsmart.artopos.feature.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.createsmart.artopos.core.domain.usecase.GetArtworksUseCase
import ru.createsmart.artopos.core.domain.usecase.GetFiltersUseCase
import ru.createsmart.artopos.core.domain.usecase.InitializeFiltersUseCase
import ru.createsmart.artopos.core.model.FilterParams
import ru.createsmart.artopos.core.model.FilterSortOption
import ru.createsmart.artopos.core.model.FilterType
import ru.createsmart.artopos.core.ui.theme.components.toUiText
import ru.createsmart.artopos.feature.discover.mapper.toUi
import ru.createsmart.artopos.feature.discover.model.FiltersUiState
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    getArtworks: GetArtworksUseCase,
    getFiltersUseCase: GetFiltersUseCase,
    private val initializeFiltersUseCase: InitializeFiltersUseCase,
    private val messageManager: UiMessageManager,
) : ViewModel() {

    private val _contentVersion = MutableStateFlow(0)
    val contentVersion = _contentVersion.asStateFlow() // Exposed to UI to force image reload on Pull-to-Refresh

    val uiEffect = messageManager.uiEffect

    private val _activeFilterParams = MutableStateFlow(FilterParams())
    private val _draftFilterParams = MutableStateFlow(FilterParams())

    val activeFilterParams = _activeFilterParams.asStateFlow() // For TopAppBar

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
        _draftFilterParams,
    ) { classList, centList, cultList, draftParams ->
        FiltersUiState(
            classifications = classList,
            centuries = centList,
            cultures = cultList,
            sort = draftParams.sort,
            isAvailable = classList.isNotEmpty() && centList.isNotEmpty() && cultList.isNotEmpty(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FiltersUiState())

    // --- PAGING FLOW (Depends on ACTIVE) ---

    @OptIn(ExperimentalCoroutinesApi::class)
    val artworksFlow = _activeFilterParams
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

    fun onRemoveFilter(type: FilterType) { // For TopAppBar
        val current = _activeFilterParams.value

        val newParams = when (type) {
            FilterType.CLASSIFICATION -> current.copy(classification = null)
            FilterType.CENTURY -> current.copy(century = null)
            FilterType.CULTURE -> current.copy(culture = null)
        }

        _activeFilterParams.value = newParams
        _draftFilterParams.value = newParams
    }

    fun onToggleFilterSort() {
        val currentDraft = _draftFilterParams.value

        val newSort = when (currentDraft.sort) {
            FilterSortOption.RANK -> FilterSortOption.TOTAL_PAGE_VIEWS
            FilterSortOption.TOTAL_PAGE_VIEWS -> FilterSortOption.RANDOM
            FilterSortOption.RANDOM -> FilterSortOption.RANK
        }

        _draftFilterParams.value = currentDraft.copy(sort = newSort)
    }

    // --- ERROR HANDLING ---

    fun onRefresh(): Boolean {
        if (!messageManager.checkInternetAndNotify()) return false

        viewModelScope.launch {
            initializeFiltersUseCase()
        }

        _contentVersion.value++
        messageManager.resetLastEmittedMessage() // Reset debounce history so new errors can be shown fresh

        return true
    }

    fun onRetryAction(): Boolean {
        if (messageManager.checkInternetAndNotify()) {
            viewModelScope.launch {
                initializeFiltersUseCase()
            }
            return true
        }
        return false
    }

    fun onError(error: Throwable) {
        if (messageManager.checkInternetAndNotify()) {
            messageManager.sendSideEffect(error.toUiText())
        }
    }
}
