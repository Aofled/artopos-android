package ru.createsmart.artopos.core.data.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.createsmart.artopos.core.data.mapper.ArtworkMapper
import ru.createsmart.artopos.core.database.HarvardDatabase
import ru.createsmart.artopos.core.database.model.ArtworkRemoteKeysEntity
import ru.createsmart.artopos.core.database.model.ArtworkWithFavoriteFlagDBO
import ru.createsmart.artopos.core.model.FilterParams
import ru.createsmart.artopos.core.model.FilterSortOption
import ru.createsmart.artopos.core.network.api.HarvardAPI
import ru.createsmart.artopos.core.network.model.ArtworkDTO
import java.io.IOException

/**
 * Orchestrates loading data from Network into the Database.
 * Handles pagination keys (next/prev pages) and caching strategy.
 */

private const val FILTER_RANK = "rank"
private const val FILTER_TOTAL_PAGE_VIEWS = "totalpageviews"
private const val FILTER_ACCESSION_YEAR = "accessionyear"
private const val FILTER_DATE_BEGIN = "datebegin"
private const val FILTER_RANDOM = "random"

private const val SORT_DESK = "desc"
private const val SORT_ASC = "asc"

@OptIn(ExperimentalPagingApi::class)
class ArtworkRemoteMediator(
    private val database: HarvardDatabase,
    private val api: HarvardAPI,
    private val params: FilterParams,
    private val mapper: ArtworkMapper,
) : RemoteMediator<Int, ArtworkWithFavoriteFlagDBO>() {
    @Suppress("ReturnCount")
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ArtworkWithFavoriteFlagDBO>,
    ): MediatorResult {
        val effectiveOrder = resolveEffectiveOrder()
        val effectiveSort = resolveEffectiveSort()

        val page = when (loadType) {
            LoadType.REFRESH -> {
                // Logic: Find the page key closest to the current scroll position.
                // If Refresh -> the closest key to the current position or start from 1
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1)
                    ?: remoteKeys?.prevKey?.plus(1)
                    ?: 1
            }

            LoadType.PREPEND -> {
                // Loading "top" (scrolling up).
                // If remoteKeys is null, we are at the start -> Success(endOfPaginationReached = true)
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey ?: return MediatorResult.Success(
                    endOfPaginationReached = remoteKeys != null,
                )
                prevKey
            }

            LoadType.APPEND -> {
                // Loading "down" (scrolling down).
                // If remoteKeys is null, we are at the end -> Success(endOfPaginationReached = true)
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey ?: return MediatorResult.Success(
                    endOfPaginationReached = remoteKeys != null,
                )
                nextKey
            }
        }

        return try {
            val apiResponse = api.getArtworks(
                page = page,
                size = state.config.pageSize,
                classification = params.classification,
                century = params.century,
                culture = params.culture,
                sort = effectiveOrder,
                sortOrder = effectiveSort,
            )
            val validRecords = apiResponse.records.filter { !it.images.isNullOrEmpty() }

            val endOfPaginationReached = apiResponse.info.nextUrl == null || apiResponse.records.isEmpty()

            updateDatabase(loadType, page, validRecords, state, endOfPaginationReached)

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            MediatorResult.Error(exception)
        }
    }

    private fun resolveEffectiveOrder(): String {
        return if (hasFilters()) {
            when (params.sort) {
                // For the "rank", "totalpageviews", accessionyear", "datebegin", "random" filters
                // "accessionyear" is the default.
                FilterSortOption.RANK -> FILTER_RANK
                FilterSortOption.TOTAL_PAGE_VIEWS -> FILTER_TOTAL_PAGE_VIEWS
                FilterSortOption.ACCESSION_YEAR -> FILTER_ACCESSION_YEAR
                FilterSortOption.DATE_BEGIN -> FILTER_DATE_BEGIN
                FilterSortOption.RANDOM -> FILTER_RANDOM
            }
        } else {
            FILTER_ACCESSION_YEAR // For the main page, always "accessionyear"
        }
    }

    private fun resolveEffectiveSort(): String {
        return if (hasFilters()) {
            if (params.sort == FilterSortOption.DATE_BEGIN) SORT_ASC else SORT_DESK
        } else {
            SORT_DESK // For the main page, always "desk"
        }
    }

    private fun hasFilters(): Boolean =
        params.classification != null || params.century != null || params.culture != null

    private suspend fun updateDatabase(
        loadType: LoadType,
        page: Int,
        validRecords: List<ArtworkDTO>,
        state: PagingState<Int, ArtworkWithFavoriteFlagDBO>,
        endOfPaginationReached: Boolean,
    ) {
        database.withTransaction {
            if (loadType == LoadType.REFRESH) {
                // Clear cache only on full Refresh (Pull-to-Refresh or initial load)
                database.artworkRemoteKeysDao().clearRemoteKeys()
                database.artworkDao().clearArtworks()
            }

            val prevKey = if (page == 1) null else page - 1
            val nextKey = if (endOfPaginationReached) null else page + 1

            val keys = validRecords.map { dto ->
                ArtworkRemoteKeysEntity(artworkId = dto.id, prevKey = prevKey, nextKey = nextKey)
            }

            val artworks = validRecords.mapIndexed { index, dto ->
                // Sort Order: Explicitly save the order index.
                // Room does not guarantee insertion order, so we need a column to sort by later.
                val globalIndex = (page - 1) * state.config.pageSize + index
                mapper.mapDtoToDbo(dto).copy(sortingIndex = globalIndex, inDiscoverFeed = true)
            }

            database.artworkRemoteKeysDao().insertAll(keys)
            database.artworkDao().insertArtworks(artworks)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, ArtworkWithFavoriteFlagDBO>,
    ): ArtworkRemoteKeysEntity? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.artwork?.id?.let { artworkId ->
                database.artworkRemoteKeysDao().remoteKeyArtworkId(artworkId)
            }
        }
    }

    private suspend fun getRemoteKeyForFirstItem(
        state: PagingState<Int, ArtworkWithFavoriteFlagDBO>,
    ): ArtworkRemoteKeysEntity? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { artwork ->
                database.artworkRemoteKeysDao().remoteKeyArtworkId(artwork.artwork.id)
            }
    }

    private suspend fun getRemoteKeyForLastItem(
        state: PagingState<Int, ArtworkWithFavoriteFlagDBO>,
    ): ArtworkRemoteKeysEntity? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { artwork ->
                database.artworkRemoteKeysDao().remoteKeyArtworkId(artwork.artwork.id)
            }
    }
}
