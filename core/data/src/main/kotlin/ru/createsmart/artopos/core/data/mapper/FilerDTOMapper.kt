package ru.createsmart.artopos.core.data.mapper

import ru.createsmart.artopos.core.database.model.FilterItemDBO
import ru.createsmart.artopos.core.model.FilterType
import ru.createsmart.artopos.core.network.model.FilterItemDTO

fun FilterItemDTO.toDBO(type: FilterType): FilterItemDBO {
    return FilterItemDBO(
        id = id,
        type = type.name,
        name = name,
        count = count,
        order = order,
    )
}
