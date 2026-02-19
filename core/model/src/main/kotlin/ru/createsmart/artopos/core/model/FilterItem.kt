package ru.createsmart.artopos.core.model

data class FilterItem(
    val uId: Long,
    val id: Long,
    val type: FilterType,
    val name: String,
    val count: Int,
    val order: Int? = null,
)

enum class FilterType {
    CLASSIFICATION,
    CENTURY,
    CULTURE,
}
