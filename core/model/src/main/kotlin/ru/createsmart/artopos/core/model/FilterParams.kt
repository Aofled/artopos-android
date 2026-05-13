package ru.createsmart.artopos.core.model

public data class FilterParams(
    val classification: String? = null,
    val century: String? = null,
    val culture: String? = null,
    val sort: FilterSortOption = FilterSortOption.RANK,
)
