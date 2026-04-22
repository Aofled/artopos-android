package ru.createsmart.artopos.feature.discover

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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.createsmart.artopos.core.designsystem.components.toUiText
import ru.createsmart.artopos.core.domain.interactor.DiscoverInteractor
import ru.createsmart.artopos.core.model.FilterParams
import ru.createsmart.artopos.core.model.FilterSortOption
import ru.createsmart.artopos.core.model.FilterType
import ru.createsmart.artopos.core.uicomponents.manager.UiMessageManager
import ru.createsmart.artopos.feature.artworkcard.mapper.ArtworkUiMapper
import ru.createsmart.artopos.feature.discover.mapper.toUi
import ru.createsmart.artopos.feature.discover.model.DiscoverEvent
import ru.createsmart.artopos.feature.discover.model.FiltersUiState
import javax.inject.Inject

@Suppress("TooManyFunctions")
@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val useCases: DiscoverInteractor,
    private val messageManager: UiMessageManager,
    private val mapper: ArtworkUiMapper,
) : ViewModel() {

    private val _contentVersion = MutableStateFlow(0)
    val contentVersion = _contentVersion.asStateFlow() // Exposed to UI to force image reload on Pull-to-Refresh

    val uiEffect = messageManager.uiEffect

    private val _actions = Channel<DiscoverEvent>(Channel.BUFFERED) // ScrollToTop
    val actions = _actions.receiveAsFlow()

    private val _searchQuery = MutableStateFlow("")

    private val _activeFilterParams = MutableStateFlow(FilterParams())
    private val _draftFilterParams = MutableStateFlow(FilterParams())

    val activeFilterParams = _activeFilterParams.asStateFlow() // For TopAppBar

    // --- UI FLOWS (Depends on DRAFT) ---

    private val _classificationsFlow = combine(
        useCases.getFilters(FilterType.CLASSIFICATION),
        _draftFilterParams,
    ) { list, params ->
        list.toUi(params.classification)
    }

    private val _centuriesFlow = combine(
        useCases.getFilters(FilterType.CENTURY),
        _draftFilterParams,
    ) { list, params ->
        list.toUi(params.century)
    }

    private val _culturesFlow = combine(
        useCases.getFilters(FilterType.CULTURE),
        _draftFilterParams,
    ) { list, params ->
        list.toUi(params.culture)
    }

    val filtersUiState: StateFlow<FiltersUiState> = combine(
        _classificationsFlow,
        _centuriesFlow,
        _culturesFlow,
        _draftFilterParams,
        _searchQuery,
    ) { classList, centList, cultList, draftParams, query ->
        FiltersUiState(
            classifications = classList,
            centuries = centList,
            cultures = cultList,
            sort = draftParams.sort,
            isAvailable = classList.isNotEmpty() && centList.isNotEmpty() && cultList.isNotEmpty(),
            searchQuery = query,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FiltersUiState())

    // --- PAGING FLOW (Depends on ACTIVE) ---

    @OptIn(ExperimentalCoroutinesApi::class)
    val artworksFlow = _activeFilterParams
        .flatMapLatest { params ->
            useCases.getArtworks(params) // It depends on the filter
        }
        .map { pagingData -> pagingData.map { mapper.mapToUi(it) } }
        .cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            useCases.initializeFilters()
        }

        viewModelScope.launch {
            useCases.getUserSettings().collectLatest { settings ->
                useCases.preloadTranslationModel(settings.languageCode) // ML Kit Translation dictionary preloading
            }
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
        _searchQuery.value = ""
    }

    fun onFilterApply() {
        if (_activeFilterParams.value != _draftFilterParams.value) {
            _activeFilterParams.value = _draftFilterParams.value

            viewModelScope.launch {
                _actions.send(DiscoverEvent.ScrollToTop)
            }
        }
    }

    fun onFilterOpen() {
        _draftFilterParams.value = _activeFilterParams.value
        _searchQuery.value = ""
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
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
            FilterSortOption.TOTAL_PAGE_VIEWS -> FilterSortOption.ACCESSION_YEAR
            FilterSortOption.ACCESSION_YEAR -> FilterSortOption.DATE_BEGIN
            FilterSortOption.DATE_BEGIN -> FilterSortOption.RANDOM
            FilterSortOption.RANDOM -> FilterSortOption.RANK
        }

        _draftFilterParams.value = currentDraft.copy(sort = newSort)
    }

    fun onToggleFavorite(id: Int) {
        viewModelScope.launch {
            useCases.toggleFavorite(id)
        }
    }

    // --- ERROR HANDLING ---

    fun onRefresh(): Boolean {
        if (!messageManager.checkInternetAndNotify()) return false

        viewModelScope.launch {
            val result = useCases.initializeFilters()

            result.onFailure { error ->
                messageManager.sendSideEffect(error.toUiText())
            }
        }

        _contentVersion.value++
        messageManager.resetLastEmittedMessage() // Reset debounce history so new errors can be shown fresh

        return true
    }

    fun onRetryAction(): Boolean {
        if (messageManager.checkInternetAndNotify()) {
            viewModelScope.launch {
                val result = useCases.initializeFilters()

                result.onFailure { error ->
                    messageManager.sendSideEffect(error.toUiText())
                }
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
