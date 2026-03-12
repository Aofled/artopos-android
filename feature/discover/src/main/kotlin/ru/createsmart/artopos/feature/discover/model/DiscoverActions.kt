package ru.createsmart.artopos.feature.discover.model

import androidx.compose.runtime.Immutable
import ru.createsmart.artopos.core.model.FilterType

@Immutable
data class DiscoverActions(
    val onRefresh: () -> Unit,
    val onRetry: () -> Unit,
    val onArtworkClick: (Int) -> Unit,
    val onError: (Throwable) -> Unit,
    val onFilterSelected: (FilterType, String?) -> Unit,
    val onFilterApply: () -> Unit,
    val onFilterReset: () -> Unit,
    val onFilterOpen: () -> Unit,
    val onRemoveFilter: (FilterType) -> Unit,
    val onToggleFilterSort: () -> Unit,
    val onSearchQueryChanged: (String) -> Unit,
)
