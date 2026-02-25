package ru.createsmart.artopos.feature.discover.model

import androidx.compose.runtime.Immutable
import ru.createsmart.artopos.core.model.FilterSortOption

@Immutable
data class FiltersUiState(
    val classifications: List<FilterListItem> = emptyList(),
    val centuries: List<FilterListItem> = emptyList(),
    val cultures: List<FilterListItem> = emptyList(),
    val isAvailable: Boolean = false, // If ALL filters are in the database
    val sort: FilterSortOption = FilterSortOption.RANK,
)
