package ru.createsmart.artopos.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.createsmart.artopos.core.model.FilterItem
import ru.createsmart.artopos.core.model.FilterType

interface FilterRepository {
    fun getFilters(type: FilterType): Flow<List<FilterItem>>

    suspend fun initializeFilters()
}
