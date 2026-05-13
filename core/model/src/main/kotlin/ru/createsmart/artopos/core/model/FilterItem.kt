package ru.createsmart.artopos.core.model

public data class FilterItem(
    val id: Long,
    val type: FilterType,
    val name: String,
    val count: Int,
    val order: Int? = null,
)

public enum class FilterType {
    CLASSIFICATION,
    CENTURY,
    CULTURE,
}
