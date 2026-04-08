package ru.createsmart.artopos.core.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.createsmart.artopos.core.data.mapper.toDetailsDBO
import ru.createsmart.artopos.core.data.mapper.toDomain
import ru.createsmart.artopos.core.data.mapper.toFavorite
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

    private val artworkDao get() = database.artworkDao()
    private val favoriteDao get() = database.favoriteDao()

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

    /**
     * We listen to two tables at once because the "exhibit" can be located either in the temporary feed cache,
     * or in the persistent favorites storage (or both).
     */
    override fun getArtwork(id: Int): Flow<Artwork?> {
        val fromDiscoverFlow = artworkDao.getArtworkWithDetails(id)
        val fromFavoritesFlow = favoriteDao.getArtworkFavoriteWithDetails(id)
        return combine(fromDiscoverFlow, fromFavoritesFlow) { fromDiscover, fromFavorites ->
            when {
                // PRIORITY 1: Take from Discover Feed first.
                // It holds the most recent network data (handles 'isFavorite' flag internally).
                fromDiscover != null -> fromDiscover.toDomain()

                // PRIORITY 2: Fallback to Favorites.
                // Useful if the feed cache was cleared, but the user saved this artwork previously.
                fromFavorites != null -> fromFavorites.toDomain()

                // PRIORITY 3: Not found locally. UI should show Loading/Error.
                else -> null
            }
        }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }

    override suspend fun syncArtworkDetails(id: Int): Result<Unit> {
        return runCatching {
            withContext(Dispatchers.IO) {
                // 1. Download fresh JSON with full details
                val dto = api.getArtworkDetails(id)

                // 2. Save the "heavy" part of the data in a separate table (artwork_details).
                // Thanks to @Relation, the getArtwork() method will immediately see this new data.
                val detailsEntity = dto.toDetailsDBO()
                artworkDao.insertDetails(detailsEntity)
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
                // Logic: Copy the current state of the artwork from the Main feed to the Favorites table.
                // This creates an offline backup of the artwork that survives cache clearing.
                val snapshot = artworkDao.getArtworkSnapshot(artworkId)
                if (snapshot != null) {
                    favoriteDao.insertFavorite(snapshot.toFavorite())
                }
            }
        }
    }

    // To clear unused fields from the detail_table
    override suspend fun clearDatabaseCache() {
        withContext(Dispatchers.IO) {
            try {
                // val deletedCount = database.artworkDao().clearOrphanedDetails()
                // Log.d("DatabaseCache", "Cleared $deletedCount orphaned artwork details.")
            } catch (ignored: Exception) {
            }
        }
    }
}
