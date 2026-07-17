package ru.createsmart.artopos.feature.discover.model

import ru.createsmart.artopos.core.model.FilterParamItem
import ru.createsmart.artopos.core.model.FilterType

sealed interface DiscoverIntent {
    data object Refresh : DiscoverIntent
    data object Retry : DiscoverIntent
    data class ErrorOccurred(val error: Throwable) : DiscoverIntent

    data class FilterSelected(val type: FilterType, val value: FilterParamItem?) : DiscoverIntent
    data object FilterApply : DiscoverIntent
    data object FilterReset : DiscoverIntent
    data object FilterOpen : DiscoverIntent
    data class RemoveFilter(val type: FilterType) : DiscoverIntent
    data object ToggleFilterSort : DiscoverIntent

    data class ToggleFavorite(val id: Int) : DiscoverIntent
    data class SearchQueryChanged(val query: String) : DiscoverIntent
}
