package ru.createsmart.artopos.feature.search.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import ru.createsmart.artopos.core.designsystem.components.UiText
import ru.createsmart.artopos.core.filter.model.FiltersUiState
import ru.createsmart.artopos.core.model.FilterParams
import ru.createsmart.artopos.core.model.FilterType

@Immutable
data class ActiveFilterItemUi(
    val type: FilterType,
    val label: UiText,
)

@Immutable
data class SearchUiState(
    val searchQuery: String = "",
    val filtersUiState: FiltersUiState = FiltersUiState(),
    val activeFilterParams: FilterParams = FilterParams(),
    val activeFilters: ImmutableList<ActiveFilterItemUi> = persistentListOf(),
    val favoriteArtworkIds: Set<Int> = emptySet(),
    val contentVersion: Int = 0,
) {
    val hasActiveFilters: Boolean
        get() = activeFilters.isNotEmpty()
}
