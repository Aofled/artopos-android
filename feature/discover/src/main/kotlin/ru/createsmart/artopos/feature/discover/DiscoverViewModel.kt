package ru.createsmart.artopos.feature.discover

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
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
import kotlinx.coroutines.withContext
import ru.createsmart.artopos.core.artworkcard.mapper.ArtworkUiMapper
import ru.createsmart.artopos.core.common.util.LocaleHelper
import ru.createsmart.artopos.core.designsystem.components.toUiText
import ru.createsmart.artopos.core.designsystem.util.FilterNameHelper
import ru.createsmart.artopos.core.domain.interactor.DiscoverInteractor
import ru.createsmart.artopos.core.model.FilterItem
import ru.createsmart.artopos.core.model.FilterParams
import ru.createsmart.artopos.core.model.FilterSortOption
import ru.createsmart.artopos.core.model.FilterType
import ru.createsmart.artopos.core.uicomponents.manager.UiMessageManager
import ru.createsmart.artopos.feature.discover.model.DiscoverEvent
import ru.createsmart.artopos.feature.discover.model.DiscoverIntent
import ru.createsmart.artopos.feature.discover.model.FilterListItem
import ru.createsmart.artopos.feature.discover.model.FiltersUiState
import javax.inject.Inject

@Suppress("TooManyFunctions")
@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val useCases: DiscoverInteractor,
    private val messageManager: UiMessageManager,
    private val mapper: ArtworkUiMapper,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _contentVersion = MutableStateFlow(0)
    val contentVersion =
        _contentVersion.asStateFlow() // Exposed to UI to force image reload on Pull-to-Refresh

    private val _currentLanguage = MutableStateFlow("")

    val uiEffect = messageManager.uiEffect

    private val _actions = Channel<DiscoverEvent>(Channel.BUFFERED) // ScrollToTop
    val actions = _actions.receiveAsFlow()

    private val _searchQuery = MutableStateFlow("")

    private val _activeFilterParams = MutableStateFlow(FilterParams())
    private val _draftFilterParams = MutableStateFlow(FilterParams())

    val activeFilterParams = _activeFilterParams.asStateFlow() // For TopAppBar

    internal fun onIntent(intent: DiscoverIntent) {
        when (intent) {
            is DiscoverIntent.Refresh -> onRefresh()
            is DiscoverIntent.Retry -> onRetryAction()
            is DiscoverIntent.ErrorOccurred -> onError(intent.error)
            is DiscoverIntent.FilterSelected -> onFilterSelect(intent.type, intent.value)
            is DiscoverIntent.FilterApply -> onFilterApply()
            is DiscoverIntent.FilterReset -> onFilterReset()
            is DiscoverIntent.FilterOpen -> onFilterOpen()
            is DiscoverIntent.RemoveFilter -> onRemoveFilter(intent.type)
            is DiscoverIntent.ToggleFilterSort -> onToggleFilterSort()
            is DiscoverIntent.ToggleFavorite -> onToggleFavorite(intent.id)
            is DiscoverIntent.SearchQueryChanged -> onSearchQueryChanged(intent.query)
            is DiscoverIntent.ArtworkClicked -> Unit
        }
    }

    // --- UI FLOWS (Depends on DRAFT) ---

    private fun combineAndFilterFlow(
        filtersFlow: Flow<List<FilterItem>>,
        extractSelectedValue: (FilterParams) -> String?,
    ): Flow<List<FilterListItem>> {
        return combine(
            filtersFlow,
            _draftFilterParams,
            _searchQuery,
            _currentLanguage,
        ) { list, params, query, languageCode ->
            val selectedValue = extractSelectedValue(params)

            withContext(Dispatchers.Default) {
                val locContext = LocaleHelper.getLocalizedContext(context, languageCode)

                list.map { item ->
                    val locName = FilterNameHelper.getLocalizedName(locContext, item.name)
                    FilterListItem(
                        id = item.id,
                        type = item.type,
                        name = item.name,
                        localizedName = locName,
                        count = item.count,
                        isSelected = item.name == selectedValue,
                    )
                }.filter { uiItem ->
                    // 1. If the search is empty, we return all list
                    if (query.isBlank()) return@filter true

                    // 2. Always keep the selected element visible
                    if (uiItem.isSelected) return@filter true

                    // 3. Else looking for matches in the text
                    uiItem.localizedName.contains(query, ignoreCase = true) ||
                        uiItem.name.contains(query, ignoreCase = true)
                }
            }
        }
    }

    private val isFiltersAvailableFlow = combine(
        useCases.getFilters(FilterType.CLASSIFICATION),
        useCases.getFilters(FilterType.CENTURY),
        useCases.getFilters(FilterType.CULTURE),
    ) { classDb, centDb, cultDb ->
        classDb.isNotEmpty() && centDb.isNotEmpty() && cultDb.isNotEmpty()
    }

    private val _classificationsFlow = combineAndFilterFlow(
        useCases.getFilters(FilterType.CLASSIFICATION),
    ) { it.classification }

    private val _centuriesFlow = combineAndFilterFlow(
        useCases.getFilters(FilterType.CENTURY),
    ) { it.century }

    private val _culturesFlow = combineAndFilterFlow(
        useCases.getFilters(FilterType.CULTURE),
    ) { it.culture }

    val filtersUiState: StateFlow<FiltersUiState> = combine(
        combine(_classificationsFlow, _centuriesFlow, _culturesFlow) { c, cen, cul ->
            Triple(c, cen, cul)
        },
        combine(_draftFilterParams, _searchQuery, isFiltersAvailableFlow) { params, query, available ->
            Triple(params, query, available)
        },
    ) { (classList, centList, cultList), (draftParams, query, isAvailable) ->
        FiltersUiState(
            classifications = classList.toImmutableList(),
            centuries = centList.toImmutableList(),
            cultures = cultList.toImmutableList(),
            sort = draftParams.sort,
            isAvailable = isAvailable,
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
                _currentLanguage.value = settings.languageCode
                useCases.preloadTranslationModel(settings.languageCode) // ML Kit Translation dictionary preloading
            }
        }
    }

    // --- ACTIONS ---

    private fun onFilterSelect(type: FilterType, value: String?) {
        val currentDraft = _draftFilterParams.value
        _draftFilterParams.value = when (type) {
            FilterType.CLASSIFICATION -> currentDraft.copy(classification = value)
            FilterType.CENTURY -> currentDraft.copy(century = value)
            FilterType.CULTURE -> currentDraft.copy(culture = value)
        }
    }

    private fun onFilterReset() {
        _draftFilterParams.value = FilterParams()
        _searchQuery.value = ""
    }

    private fun onFilterApply() {
        if (_activeFilterParams.value != _draftFilterParams.value) {
            _activeFilterParams.value = _draftFilterParams.value

            viewModelScope.launch {
                _actions.send(DiscoverEvent.ScrollToTop)
            }
        }
    }

    private fun onFilterOpen() {
        _draftFilterParams.value = _activeFilterParams.value
        _searchQuery.value = ""
    }

    private fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    private fun onRemoveFilter(type: FilterType) { // For TopAppBar
        val current = _activeFilterParams.value

        val newParams = when (type) {
            FilterType.CLASSIFICATION -> current.copy(classification = null)
            FilterType.CENTURY -> current.copy(century = null)
            FilterType.CULTURE -> current.copy(culture = null)
        }

        _activeFilterParams.value = newParams
        _draftFilterParams.value = newParams
    }

    private fun onToggleFilterSort() {
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

    private fun onToggleFavorite(id: Int) {
        viewModelScope.launch {
            useCases.toggleFavorite(id)
        }
    }

    // --- ERROR HANDLING ---

    private fun onRefresh() {
        if (!messageManager.checkInternetAndNotify()) return

        viewModelScope.launch {
            val result = useCases.initializeFilters()

            result.onFailure { error ->
                messageManager.sendSideEffect(error.toUiText())
            }
        }

        _contentVersion.value++
        messageManager.resetLastEmittedMessage() // Reset debounce history so new errors can be shown fresh
    }

    private fun onRetryAction() {
        if (messageManager.checkInternetAndNotify()) {
            viewModelScope.launch {
                val result = useCases.initializeFilters()
                result.onFailure { error -> messageManager.sendSideEffect(error.toUiText()) }
            }
        }
    }

    private fun onError(error: Throwable) {
        if (messageManager.checkInternetAndNotify()) {
            messageManager.sendSideEffect(error.toUiText())
        }
    }
}
