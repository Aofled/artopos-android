package ru.createsmart.artopos.feature.search.model

import ru.createsmart.artopos.core.model.FilterParamItem
import ru.createsmart.artopos.core.model.FilterType

sealed interface SearchIntent {
    // Main search query
    data class QueryChanged(val query: String) : SearchIntent
    data object ClearQuery : SearchIntent

    // Filter search query
    data class FilterSearchQueryChanged(val query: String) : SearchIntent

    // Filter sheet actions
    data object FilterOpen : SearchIntent
    data class FilterSelected(val type: FilterType, val value: FilterParamItem?) : SearchIntent
    data object FilterApply : SearchIntent
    data object FilterReset : SearchIntent
    data object ToggleFilterSort : SearchIntent
    data class RemoveActiveFilter(val type: FilterType) : SearchIntent

    // Artwork interaction
    data class ToggleFavorite(val id: Int) : SearchIntent
    data object Refresh : SearchIntent
    data object Retry : SearchIntent
    data class ErrorOccurred(val error: Throwable) : SearchIntent
}
