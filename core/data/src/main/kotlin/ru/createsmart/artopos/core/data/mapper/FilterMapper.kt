package ru.createsmart.artopos.core.data.mapper

import ru.createsmart.artopos.core.database.model.FilterItemDBO
import ru.createsmart.artopos.core.model.FilterItem
import ru.createsmart.artopos.core.model.FilterType
import ru.createsmart.artopos.core.network.model.FilterItemDTO
import javax.inject.Inject

class FilterMapper @Inject constructor() {

    fun mapDtoToDbo(dto: FilterItemDTO, type: FilterType): FilterItemDBO {
        return FilterItemDBO(
            id = dto.id,
            type = type.name,
            name = dto.name,
            count = dto.count,
            order = dto.order,
        )
    }

    fun mapDboToDomain(dbo: FilterItemDBO): FilterItem {
        return FilterItem(
            id = dbo.id,
            type = FilterType.valueOf(dbo.type),
            name = dbo.name,
            count = dbo.count,
            order = dbo.order,
        )
    }
}
