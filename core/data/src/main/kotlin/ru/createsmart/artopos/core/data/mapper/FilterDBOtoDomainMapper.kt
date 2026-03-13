package ru.createsmart.artopos.core.data.mapper

import ru.createsmart.artopos.core.database.model.FilterItemDBO
import ru.createsmart.artopos.core.model.FilterItem
import ru.createsmart.artopos.core.model.FilterType

fun FilterItemDBO.toDomain(): FilterItem {
    return FilterItem(
        uId = uId,
        id = id,
        type = FilterType.valueOf(type),
        name = name,
        count = count,
        order = order,
    )
}
