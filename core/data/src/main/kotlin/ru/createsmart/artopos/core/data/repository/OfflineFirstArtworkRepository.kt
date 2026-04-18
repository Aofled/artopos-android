package ru.createsmart.artopos.core.data.repository

import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.createsmart.artopos.core.common.result.suspendRunCatching
import ru.createsmart.artopos.core.data.mapper.toDetailsDBO
import ru.createsmart.artopos.core.data.mapper.toDomain
import ru.createsmart.artopos.core.data.mediator.ArtworkRemoteMediator
import ru.createsmart.artopos.core.database.HarvardDatabase
import ru.createsmart.artopos.core.database.model.FavoriteDBO
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

    private val artworkDao get() = database.artworkDao()
    private val favoriteDao get() = database.favoriteDao()
    private val artworkDetailDao get() = database.artworkDetailDao()

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
                artworkDao.getArtworksWithFavoriteFlags()
            },
        ).flow
            .map { pagingData ->
                pagingData.map { it.toDomain() }
            }
    }

    override fun getArtwork(id: Int): Flow<Artwork?> {
        return artworkDetailDao.getArtworkWithDetails(id)
            .map { it?.toDomain() }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }

    override suspend fun syncArtworkDetails(id: Int): Result<Unit> {
        return suspendRunCatching {
            withContext(Dispatchers.IO) {
                // 1. Download fresh JSON with full details
                val dto = api.getArtworkDetails(id)

                // 2. Save the "heavy" part of the data in a separate table (artwork_details).
                // Thanks to @Relation, the getArtwork() method will immediately see this new data.
                val detailsEntity = dto.toDetailsDBO()
                artworkDetailDao.insertDetails(detailsEntity)
            }
        }
    }

    override fun getFavoriteArtworks(): Flow<List<Artwork>> {
        return favoriteDao.getFavorites()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun toggleFavorite(artworkId: Int) {
        withContext(Dispatchers.IO) {
            val isFavorite = favoriteDao.isFavorite(artworkId)

            if (isFavorite) {
                favoriteDao.removeFavorite(artworkId)
            } else {
                favoriteDao.insertFavorite(FavoriteDBO(id = artworkId))
            }
        }
    }

    // To clear unused fields from the detail_table
    override suspend fun clearDatabaseCache() {
        withContext(Dispatchers.IO) {
            try {
                // val deletedCount = artworkDao.clearDetailsCacheFromSettings()
                // Log.d("DatabaseCache", "Cleared $deletedCount cached artwork details.")
            } catch (e: SQLiteException) {
                Log.e("DatabaseCache", "Failed to clear details cache", e)
            }
        }
    }
}
