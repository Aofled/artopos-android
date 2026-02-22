package ru.createsmart.artopos.feature.discover.mapper

import ru.createsmart.artopos.core.model.FilterItem
import ru.createsmart.artopos.feature.discover.model.FilterListItem

/**
 * @param selectedValue The currently selected value for this category (e.g., "French").
 * If null, then nothing is selected.
 */
fun FilterItem.toUi(selectedValue: String?): FilterListItem {
    return FilterListItem(
        id = id,
        type = type,
        name = name,
        count = count,
        isSelected = name == selectedValue,
    )
}

fun List<FilterItem>.toUi(selectedValue: String?): List<FilterListItem> {
    return map { it.toUi(selectedValue) }
}
