package ru.createsmart.artopos.core.data.repository

import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.createsmart.artopos.core.common.result.suspendRunCatching
import ru.createsmart.artopos.core.data.mapper.ArtworkMapper
import ru.createsmart.artopos.core.data.mediator.ArtworkRemoteMediator
import ru.createsmart.artopos.core.data.paging.SearchPagingSource
import ru.createsmart.artopos.core.database.HarvardDatabase
import ru.createsmart.artopos.core.domain.repository.ArtworkRepository
import ru.createsmart.artopos.core.model.Artwork
import ru.createsmart.artopos.core.model.ArtworkDetails
import ru.createsmart.artopos.core.model.FilterParams
import ru.createsmart.artopos.core.network.api.HarvardAPI
import ru.createsmart.artopos.core.network.model.ArtworkDTO
import java.util.Collections
import java.util.LinkedHashMap
import javax.inject.Inject

/**
 * Pattern: Offline First.
 * The Database is the Single Source of Truth (SSOT).
 */
private const val MAX_SEARCH_CACHE_SIZE = 300

class OfflineFirstArtworkRepository @Inject constructor(
    private val database: HarvardDatabase,
    private val api: HarvardAPI,
    private val mapper: ArtworkMapper,
) : ArtworkRepository {

    private val artworkDao get() = database.artworkDao()
    private val favoriteDao get() = database.favoriteDao()
    private val artworkDetailDao get() = database.artworkDetailDao()

    // Thread-safe search session cache
    @androidx.annotation.VisibleForTesting(otherwise = androidx.annotation.VisibleForTesting.PRIVATE)
    internal val searchSessionCache: MutableMap<Int, ArtworkDTO> = Collections.synchronizedMap(
        object : LinkedHashMap<Int, ArtworkDTO>(MAX_SEARCH_CACHE_SIZE, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, ArtworkDTO>?): Boolean {
                return size > MAX_SEARCH_CACHE_SIZE
            }
        },
    )

    private val pagingConfig = PagingConfig(
        pageSize = 50,
        initialLoadSize = 50,
        prefetchDistance = 20,
        enablePlaceholders = true,
    )

    private val searchPagingConfig = PagingConfig(
        pageSize = 50,
        initialLoadSize = 50,
        prefetchDistance = 20,
        enablePlaceholders = false,
    )

    @OptIn(ExperimentalPagingApi::class)
    override fun getPagedArtworks(
        params: FilterParams,
    ): Flow<PagingData<Artwork>> {
        return Pager(
            config = pagingConfig,
            // Fetch from API and save to database when needed
            remoteMediator = ArtworkRemoteMediator(
                database = database,
                api = api,
                params = params,
                mapper = mapper,
            ),
            pagingSourceFactory = {
                artworkDao.getArtworksWithFavoriteFlags()
            },
        ).flow
            .map { pagingData ->
                pagingData.map { mapper.mapToDomain(it) }
            }
    }

    override fun searchArtworks(
        query: String,
        params: FilterParams,
    ): Flow<PagingData<Artwork>> {
        // Clean PagingData stream without combine
        return Pager(
            config = searchPagingConfig,
            pagingSourceFactory = {
                SearchPagingSource(
                    api = api,
                    rawQuery = query,
                    params = params,
                    onPageLoaded = { dtos ->
                        dtos.forEach { searchSessionCache[it.id] = it }
                    },
                )
            },
        ).flow
            .map { pagingData ->
                pagingData.map { dto -> mapper.mapDtoToDomain(dto) }
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getFavoriteArtworkIds(): Flow<Set<Int>> {
        return favoriteDao.getFavoriteIds()
            .map { it.toSet() }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }

    override fun getArtwork(id: Int): Flow<ArtworkDetails?> {
        return artworkDetailDao.getArtworkWithDetails(id)
            .map { it?.let { dbo -> mapper.mapDetailsToDomain(dbo) } }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }

    override suspend fun syncArtworkDetails(id: Int): Result<Unit> {
        return suspendRunCatching {
            val dto = api.getArtworkDetails(id)
            val baseEntity = mapper.mapDtoToDbo(dto)
            val detailsEntity = mapper.mapDtoToDetailsDbo(dto)

            // Save artwork and details in one transaction
            database.withTransaction {
                artworkDao.insertArtworkIfAbsent(baseEntity)
                artworkDetailDao.insertDetails(detailsEntity)
            }
        }
    }

    override fun getFavoriteArtworks(): Flow<List<Artwork>> {
        return favoriteDao.getFavorites()
            .map { list -> list.map { mapper.mapToDomain(it) } }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun toggleFavorite(artworkId: Int) {
        database.withTransaction {
            // If artwork is not in DB yet, take DTO from memory cache
            val existing = artworkDao.getArtworkSnapshot(artworkId)
            if (existing == null) {
                val cachedDto = searchSessionCache[artworkId]
                if (cachedDto != null) {
                    val fullDbo = mapper.mapDtoToDbo(cachedDto)
                    artworkDao.insertArtworkIfAbsent(fullDbo)
                }
            }
            favoriteDao.toggleFavorite(artworkId)
        }
    }

    // To clear unused fields from the detail_table
    override suspend fun clearDatabaseCache() {
        try {
            val deletedCount = artworkDetailDao.clearDetailsCacheFromSettings()
            Log.d("DatabaseCache", "Cleared $deletedCount cached artwork details.")
        } catch (e: SQLiteException) {
            Log.e("DatabaseCache", "Failed to clear details cache", e)
        }
    }
}
