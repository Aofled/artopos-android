package ru.createsmart.artopos.core.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.createsmart.artopos.core.common.result.suspendRunCatching
import ru.createsmart.artopos.core.data.mapper.FilterMapper
import ru.createsmart.artopos.core.database.dao.FilterItemDao
import ru.createsmart.artopos.core.database.model.FilterItemDBO
import ru.createsmart.artopos.core.domain.repository.FilterRepository
import ru.createsmart.artopos.core.model.FilterItem
import ru.createsmart.artopos.core.model.FilterType
import ru.createsmart.artopos.core.network.api.HarvardAPI
import javax.inject.Inject

class OfflineFirstFilterRepository @Inject constructor(
    private val dao: FilterItemDao,
    private val api: HarvardAPI,
    private val mapper: FilterMapper,
) : FilterRepository {
    override fun getFilters(type: FilterType): Flow<List<FilterItem>> {
        return dao.getFiltersByType(type.name)
            .map { list -> list.map { mapper.mapDboToDomain(it) } }
    }

    override suspend fun initializeFilters(): Result<Unit> {
        return suspendRunCatching {
            withContext(Dispatchers.IO) {
                val isInitialized = dao.hasAllCategories()
                if (isInitialized) {
                    return@withContext
                }
                coroutineScope {
                    val classificationsDeferred = async { api.getClassification() }
                    val centuriesDeferred = async { api.getCentury() }
                    val culturesDeferred = async { api.getCulture() }

                    val cResult = classificationsDeferred.await()
                    val cenResult = centuriesDeferred.await()
                    val culResult = culturesDeferred.await()

                    val allFilters = mutableListOf<FilterItemDBO>()

                    allFilters.addAll(cResult.records.map { mapper.mapDtoToDbo(it, FilterType.CLASSIFICATION) })
                    allFilters.addAll(cenResult.records.map { mapper.mapDtoToDbo(it, FilterType.CENTURY) })
                    allFilters.addAll(culResult.records.map { mapper.mapDtoToDbo(it, FilterType.CULTURE) })

                    dao.insertFilters(allFilters)
                }
            }
        }
    }
}
