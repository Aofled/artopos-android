package ru.createsmart.artopos.feature.discover.model

import androidx.compose.runtime.Immutable

@Immutable
data class FiltersUiState(
    val classifications: List<FilterListItem> = emptyList(),
    val centuries: List<FilterListItem> = emptyList(),
    val cultures: List<FilterListItem> = emptyList(),
    val isAvailable: Boolean = false,
)
