package ru.createsmart.artopos.core.model

public data class FilterParamItem(
    val id: Long,
    val rawName: String,
)

public data class FilterParams(
    val classification: FilterParamItem? = null,
    val century: FilterParamItem? = null,
    val culture: FilterParamItem? = null,
    val sort: FilterSortOption = FilterSortOption.RANK,
)
