package ru.createsmart.artopos.core.model

data class FilterItem(
    val uId: Long, // Unique ID for Compose/Room to prevent collisions between different FilterTypes
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
