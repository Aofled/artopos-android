package ru.createsmart.artopos.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.createsmart.artopos.core.model.FilterItem
import ru.createsmart.artopos.core.model.FilterType

public interface FilterRepository {
    public fun getFilters(type: FilterType): Flow<List<FilterItem>>

    public suspend fun initializeFilters(): Result<Unit>
}
