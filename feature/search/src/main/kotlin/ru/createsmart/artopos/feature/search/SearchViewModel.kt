package ru.createsmart.artopos.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.createsmart.artopos.core.artworkcard.mapper.ArtworkUiMapper
import ru.createsmart.artopos.core.artworkcard.model.ArtworkListItem
import ru.createsmart.artopos.core.common.translation.FilterTranslator
import ru.createsmart.artopos.core.designsystem.components.UiText
import ru.createsmart.artopos.core.domain.usecase.GetFavoriteArtworkIdsUseCase
import ru.createsmart.artopos.core.domain.usecase.GetFiltersUseCase
import ru.createsmart.artopos.core.domain.usecase.InitializeFiltersUseCase
import ru.createsmart.artopos.core.domain.usecase.SearchArtworksUseCase
import ru.createsmart.artopos.core.domain.usecase.ToggleFavoriteUseCase
import ru.createsmart.artopos.core.filter.model.FilterListItem
import ru.createsmart.artopos.core.filter.model.FiltersUiState
import ru.createsmart.artopos.core.model.FilterItem
import ru.createsmart.artopos.core.model.FilterParamItem
import ru.createsmart.artopos.core.model.FilterParams
import ru.createsmart.artopos.core.model.FilterSortOption
import ru.createsmart.artopos.core.model.FilterType
import ru.createsmart.artopos.core.uicomponents.manager.UiMessageManager
import ru.createsmart.artopos.core.uicomponents.util.toUiText
import ru.createsmart.artopos.feature.search.model.ActiveFilterItemUi
import ru.createsmart.artopos.feature.search.model.SearchEvent
import ru.createsmart.artopos.feature.search.model.SearchIntent
import ru.createsmart.artopos.feature.search.model.SearchUiState
import javax.inject.Inject

private const val SEARCH_DEBOUNCE_MS = 500L

@Suppress("LongParameterList", "TooManyFunctions")
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchArtworks: SearchArtworksUseCase,
    private val getFilters: GetFiltersUseCase,
    private val initializeFilters: InitializeFiltersUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val getFavoriteArtworkIds: GetFavoriteArtworkIdsUseCase,
    private val messageManager: UiMessageManager,
    private val mapper: ArtworkUiMapper,
    private val filterTranslator: FilterTranslator,
) : ViewModel() {

    private val _artworkSearchQuery = MutableStateFlow("")
    private val _filterSheetSearchQuery = MutableStateFlow("")
    private val _activeFilterParams = MutableStateFlow(FilterParams(sort = FilterSortOption.RANK))
    private val _draftFilterParams = MutableStateFlow(FilterParams(sort = FilterSortOption.RANK))

    private val _contentVersion = MutableStateFlow(0)
    val contentVersion = _contentVersion.asStateFlow()

    val uiEffect = messageManager.uiEffect

    private val _actions = Channel<SearchEvent>(Channel.BUFFERED)
    val actions = _actions.receiveAsFlow()

    init {
        viewModelScope.launch {
            initializeFilters()
        }
    }

    private fun combineAndFilterFlow(
        filtersFlow: Flow<List<FilterItem>>,
        extractSelectedValue: (FilterParams) -> FilterParamItem?,
    ): Flow<List<FilterListItem>> {
        return combine(filtersFlow, _draftFilterParams) { list, params ->
            val selectedValue = extractSelectedValue(params)
            list.map { item ->
                FilterListItem(
                    id = "${item.type.name}_${item.id}",
                    backendId = item.id,
                    type = item.type,
                    name = item.name,
                    count = item.count,
                    isSelected = item.id == selectedValue?.id,
                )
            }
        }
    }

    private val isFiltersAvailableFlow = combine(
        getFilters(FilterType.CLASSIFICATION),
        getFilters(FilterType.CENTURY),
        getFilters(FilterType.CULTURE),
    ) { classDb, centDb, cultDb ->
        classDb.isNotEmpty() && centDb.isNotEmpty() && cultDb.isNotEmpty()
    }

    private val classificationsFlow = combineAndFilterFlow(
        getFilters(FilterType.CLASSIFICATION),
    ) { it.classification }

    private val centuriesFlow = combineAndFilterFlow(
        getFilters(FilterType.CENTURY),
    ) { it.century }

    private val culturesFlow = combineAndFilterFlow(
        getFilters(FilterType.CULTURE),
    ) { it.culture }

    private val filtersUiStateFlow: Flow<FiltersUiState> = combine(
        combine(classificationsFlow, centuriesFlow, culturesFlow) { c, cen, cul ->
            Triple(c, cen, cul)
        },
        combine(_draftFilterParams, _filterSheetSearchQuery, isFiltersAvailableFlow) { params, query, available ->
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
    }

    // Combine filters, query and favorites into UI state
    val uiState: StateFlow<SearchUiState> = combine(
        _artworkSearchQuery,
        filtersUiStateFlow,
        _activeFilterParams,
        getFavoriteArtworkIds(),
        _contentVersion,
    ) { query, filtersState, activeParams, favoriteIds, contentVersion ->
        val activeFilters = listOfNotNull(
            activeParams.classification?.let { item ->
                ActiveFilterItemUi(
                    type = FilterType.CLASSIFICATION,
                    label = UiText.DynamicString(filterTranslator.translate(item.rawName)),
                )
            },
            activeParams.century?.let { item ->
                ActiveFilterItemUi(
                    type = FilterType.CENTURY,
                    label = UiText.DynamicString(filterTranslator.translate(item.rawName)),
                )
            },
            activeParams.culture?.let { item ->
                ActiveFilterItemUi(
                    type = FilterType.CULTURE,
                    label = UiText.DynamicString(filterTranslator.translate(item.rawName)),
                )
            },
        ).toImmutableList()

        SearchUiState(
            searchQuery = query,
            filtersUiState = filtersState,
            activeFilterParams = activeParams,
            activeFilters = activeFilters,
            favoriteArtworkIds = favoriteIds,
            contentVersion = contentVersion,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SearchUiState(),
    )

    // Clean pagination stream
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val artworksFlow: Flow<PagingData<ArtworkListItem>> = combine(
        _artworkSearchQuery
            .map { it.trim() }
            .debounce(SEARCH_DEBOUNCE_MS)
            .distinctUntilChanged(),
        _activeFilterParams,
    ) { query, params ->
        query to params
    }
        .flatMapLatest { (query, params) ->
            if (query.isBlank()) {
                flowOf(PagingData.empty())
            } else {
                searchArtworks(query = query, params = params)
                    .map { pagingData ->
                        pagingData.map { mapper.mapToUi(it) }
                    }
            }
        }
        .cachedIn(viewModelScope)

    internal fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.QueryChanged -> onQueryChanged(intent.query)
            is SearchIntent.ClearQuery -> onClearQuery()
            is SearchIntent.FilterSearchQueryChanged -> onFilterSearchQueryChanged(intent.query)
            is SearchIntent.FilterOpen -> onFilterOpen()
            is SearchIntent.FilterSelected -> onFilterSelected(intent.type, intent.value)
            is SearchIntent.FilterApply -> onFilterApply()
            is SearchIntent.FilterReset -> onFilterReset()
            is SearchIntent.RemoveActiveFilter -> onRemoveActiveFilter(intent.type)
            is SearchIntent.ToggleFilterSort -> onToggleFilterSort()
            is SearchIntent.ToggleFavorite -> onToggleFavorite(intent.id)
            is SearchIntent.Refresh -> onRefresh()
            is SearchIntent.Retry -> onRetry()
            is SearchIntent.ErrorOccurred -> onError(intent.error)
        }
    }

    private fun onQueryChanged(query: String) {
        _artworkSearchQuery.value = query
    }

    private fun onClearQuery() {
        _artworkSearchQuery.value = ""
    }

    private fun onFilterSearchQueryChanged(query: String) {
        _filterSheetSearchQuery.value = query
    }

    private fun onFilterOpen() {
        _draftFilterParams.value = _activeFilterParams.value
        _filterSheetSearchQuery.value = ""
    }

    private fun onFilterSelected(type: FilterType, value: FilterParamItem?) {
        val currentDraft = _draftFilterParams.value
        _draftFilterParams.value = when (type) {
            FilterType.CLASSIFICATION -> currentDraft.copy(classification = value)
            FilterType.CENTURY -> currentDraft.copy(century = value)
            FilterType.CULTURE -> currentDraft.copy(culture = value)
        }
    }

    private fun onFilterReset() {
        _draftFilterParams.value = FilterParams(sort = FilterSortOption.RANK)
        _filterSheetSearchQuery.value = ""
    }

    private fun onFilterApply() {
        if (_activeFilterParams.value != _draftFilterParams.value) {
            _activeFilterParams.value = _draftFilterParams.value
            viewModelScope.launch {
                _actions.send(SearchEvent.ScrollToTop)
            }
        }
    }

    private fun onRemoveActiveFilter(type: FilterType) {
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

    private fun onToggleFavorite(artworkId: Int) {
        viewModelScope.launch {
            toggleFavorite(artworkId)
        }
    }

    private fun onRefresh() {
        if (!messageManager.checkInternetAndNotify()) return
        viewModelScope.launch {
            val result = initializeFilters()
            result.onFailure { error ->
                messageManager.sendSideEffect(error.toUiText())
            }
        }
        _contentVersion.value++
        messageManager.resetLastEmittedMessage()
    }

    private fun onRetry() {
        if (messageManager.checkInternetAndNotify()) {
            viewModelScope.launch {
                val result = initializeFilters()
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
