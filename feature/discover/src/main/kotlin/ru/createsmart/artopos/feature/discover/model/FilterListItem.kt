package ru.createsmart.artopos.feature.discover.model

import androidx.compose.runtime.Immutable
import ru.createsmart.artopos.core.model.FilterType

@Immutable
data class FilterListItem(
    val id: String,
    val type: FilterType,
    val name: String, // "19th century"
    val localizedName: String, // Translated "name"
    val count: Int, // "53970"
    val isSelected: Boolean = false,
)
