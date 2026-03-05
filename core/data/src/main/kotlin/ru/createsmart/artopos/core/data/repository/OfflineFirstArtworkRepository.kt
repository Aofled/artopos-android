package ru.createsmart.artopos.core.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.createsmart.artopos.core.data.mapper.toDetailsDBO
import ru.createsmart.artopos.core.data.mapper.toDomain
import ru.createsmart.artopos.core.data.mediator.ArtworkRemoteMediator
import ru.createsmart.artopos.core.database.HarvardDatabase
import ru.createsmart.artopos.core.domain.repository.ArtworkRepository
import ru.createsmart.artopos.core.model.Artwork
import ru.createsmart.artopos.core.model.FilterParams
import ru.createsmart.artopos.core.network.api.HarvardAPI
import javax.inject.Inject

/**
 * Pattern: Offline First.
 * The Database is the Single Source of Truth (SSOT).
 */
class OfflineFirstArtworkRepository @Inject constructor(
    private val database: HarvardDatabase,
    private val api: HarvardAPI,
) : ArtworkRepository {

    private val dao get() = database.artworkDao()

    private val pagingConfig = PagingConfig(
        pageSize = 50,
        initialLoadSize = 50,
        prefetchDistance = 20,
        enablePlaceholders = true,
    )

    @OptIn(ExperimentalPagingApi::class)
    override fun getPagedArtworks(
        params: FilterParams,
    ): Flow<PagingData<Artwork>> {
        return Pager(
            config = pagingConfig,
            // 1. Check DB -> 2. If empty/more needed -> Call API -> 3. Save to DB -> 4. UI updates automatically
            remoteMediator = ArtworkRemoteMediator(
                database = database,
                api = api,
                params = params,
            ),
            pagingSourceFactory = {
                database.artworkDao().getArtworks()
            },
        ).flow
            .map { pagingData ->
                pagingData.map { it.toDomain() }
            }
    }

    override fun getArtwork(id: Int): Flow<Artwork?> {
        return dao.getArtworkWithDetails(id)
            .map { relation ->
                relation?.toDomain()
            }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun syncArtworkDetails(id: Int): Result<Unit> {
        return runCatching {
            withContext(Dispatchers.IO) {
                val dto = api.getArtworkDetails(id)
                val detailsEntity = dto.toDetailsDBO()
                dao.insertDetails(detailsEntity)
            }
        }
    }
}
