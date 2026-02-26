package ru.createsmart.artopos.core.data.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import ru.createsmart.artopos.core.data.mapper.toDBO
import ru.createsmart.artopos.core.data.mapper.toDomain
import ru.createsmart.artopos.core.database.dao.FilterItemDao
import ru.createsmart.artopos.core.database.model.FilterItemDBO
import ru.createsmart.artopos.core.domain.repository.FilterRepository
import ru.createsmart.artopos.core.model.FilterItem
import ru.createsmart.artopos.core.model.FilterType
import ru.createsmart.artopos.core.network.api.HarvardAPI
import java.io.IOException
import java.sql.SQLException
import javax.inject.Inject

class OfflineFirstFilterRepository @Inject constructor(
    private val dao: FilterItemDao,
    private val api: HarvardAPI,
) : FilterRepository {
    override fun getFilters(type: FilterType): Flow<List<FilterItem>> {
        return dao.getFiltersByType(type.name)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun initializeFilters() {
        withContext(Dispatchers.IO) {
            val count = dao.hasAllCategories()
            if (count) {
                return@withContext
            }

            try {
                supervisorScope {
                    val classificationsDeferred = async { api.getClassification() }
                    val centuriesDeferred = async { api.getCentury() }
                    val culturesDeferred = async { api.getCulture() }

                    val cResult = classificationsDeferred.await()
                    val cenResult = centuriesDeferred.await()
                    val culResult = culturesDeferred.await()

                    val allFilters = mutableListOf<FilterItemDBO>()

                    allFilters.addAll(cResult.records.map { it.toDBO(FilterType.CLASSIFICATION) })
                    allFilters.addAll(cenResult.records.map { it.toDBO(FilterType.CENTURY) })
                    allFilters.addAll(culResult.records.map { it.toDBO(FilterType.CULTURE) })

                    dao.insertFilters(allFilters)
                }
            } catch (e: IOException) {
                Log.e("Filters", "Network error", e)
            } catch (e: SQLException) {
                Log.e("Filters", "Database error", e)
            }
        }
    }
}
