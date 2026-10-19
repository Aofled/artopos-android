package ru.createsmart.artopos.core.data.repository

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.createsmart.artopos.core.common.result.suspendRunCatching
import ru.createsmart.artopos.core.data.mapper.FilterMapper
import ru.createsmart.artopos.core.database.dao.FilterItemDao
import ru.createsmart.artopos.core.database.model.FilterItemDBO
import ru.createsmart.artopos.core.datastore.SyncMetadataDataSource
import ru.createsmart.artopos.core.domain.policy.FilterCachePolicy
import ru.createsmart.artopos.core.domain.repository.FilterRepository
import ru.createsmart.artopos.core.domain.time.TimeProvider
import ru.createsmart.artopos.core.model.FilterItem
import ru.createsmart.artopos.core.model.FilterType
import ru.createsmart.artopos.core.network.api.HarvardAPI
import javax.inject.Inject

class OfflineFirstFilterRepository @Inject constructor(
    private val dao: FilterItemDao,
    private val api: HarvardAPI,
    private val mapper: FilterMapper,
    private val syncMetadata: SyncMetadataDataSource,
    private val timeProvider: TimeProvider,
) : FilterRepository {

    override fun getFilters(type: FilterType): Flow<List<FilterItem>> {
        return dao.getFiltersByType(type.name)
            .map { list -> list.map { mapper.mapDboToDomain(it) } }
    }

    override suspend fun initializeFilters(forceRefresh: Boolean): Result<Unit> {
        return suspendRunCatching {
            val hasCategories = dao.hasAllCategories()
            val now = timeProvider.getCurrentTimeMillis()

            // 1. Check cache only if DB has data and forceRefresh is not set
            if (hasCategories && !forceRefresh) {
                val lastSyncTimestamp = syncMetadata.getLastFiltersSyncTimestamp()
                val isExpired = FilterCachePolicy.isCacheExpired(
                    lastSyncTimestamp = lastSyncTimestamp,
                    currentTime = now,
                )

                // Cache is still fresh — skip network call
                if (!isExpired) {
                    return@suspendRunCatching
                }
            }

            // 2. Fetch fresh filters from API (parallel calls)
            val allFilters = fetchFiltersFromNetwork()

            // 3. Replace all filters in DB inside one transaction
            dao.replaceFilters(allFilters)

            // 4. Save sync timestamp only after DB write succeeds
            syncMetadata.setLastFiltersSyncTimestamp(now)
        }
    }

    private suspend fun fetchFiltersFromNetwork(): List<FilterItemDBO> = coroutineScope {
        val classificationsDeferred = async { api.getClassification() }
        val centuriesDeferred = async { api.getCentury() }
        val culturesDeferred = async { api.getCulture() }

        val cResult = classificationsDeferred.await()
        val cenResult = centuriesDeferred.await()
        val culResult = culturesDeferred.await()

        val allFilters = ArrayList<FilterItemDBO>(
            cResult.records.size + cenResult.records.size + culResult.records.size,
        )

        allFilters.addAll(
            cResult.records.map { mapper.mapDtoToDbo(it, FilterType.CLASSIFICATION) },
        )
        allFilters.addAll(
            cenResult.records.map { mapper.mapDtoToDbo(it, FilterType.CENTURY) },
        )
        allFilters.addAll(
            culResult.records.map { mapper.mapDtoToDbo(it, FilterType.CULTURE) },
        )

        allFilters
    }
}
