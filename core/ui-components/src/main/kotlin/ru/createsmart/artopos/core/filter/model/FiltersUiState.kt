package ru.createsmart.artopos.core.filter.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import ru.createsmart.artopos.core.model.FilterSortOption

@Immutable
data class FiltersUiState(
    val classifications: ImmutableList<FilterListItem> = persistentListOf(),
    val centuries: ImmutableList<FilterListItem> = persistentListOf(),
    val cultures: ImmutableList<FilterListItem> = persistentListOf(),
    val searchQuery: String = "",
    val isAvailable: Boolean = false, // Если категории загружены в базу
    val sort: FilterSortOption = FilterSortOption.RANK,
)
