package ru.createsmart.artopos.core.data.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import ru.createsmart.artopos.core.data.mapper.ArtworkMapper
import ru.createsmart.artopos.core.database.HarvardDatabase
import ru.createsmart.artopos.core.database.model.ArtworkFeedWithFavoriteFlagDBO
import ru.createsmart.artopos.core.database.model.ArtworkRemoteKeysDBO
import ru.createsmart.artopos.core.database.model.DiscoverFeedItemDBO
import ru.createsmart.artopos.core.model.FilterParams
import ru.createsmart.artopos.core.model.FilterSortOption
import ru.createsmart.artopos.core.model.error.AppError
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

private const val SORT_DESC = "desc"
private const val SORT_ASC = "asc"

@OptIn(ExperimentalPagingApi::class)
class ArtworkRemoteMediator(
    private val database: HarvardDatabase,
    private val api: HarvardAPI,
    private val params: FilterParams,
    private val mapper: ArtworkMapper,
) : RemoteMediator<Int, ArtworkFeedWithFavoriteFlagDBO>() {
    @Suppress("ReturnCount")
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ArtworkFeedWithFavoriteFlagDBO>,
    ): MediatorResult {
        val effectiveOrder = resolveEffectiveOrder()
        val effectiveSort = resolveEffectiveSort()

        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1)
                    ?: remoteKeys?.prevKey?.plus(1)
                    ?: 1
            }

            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey ?: return MediatorResult.Success(
                    endOfPaginationReached = remoteKeys != null,
                )
                prevKey
            }

            LoadType.APPEND -> {
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
                classification = params.classification?.id?.toString(),
                century = params.century?.id?.toString(),
                culture = params.culture?.id?.toString(),
                sort = effectiveOrder,
                sortOrder = effectiveSort,
            )
            val validRecords = apiResponse.records.filter { !it.images.isNullOrEmpty() }

            val endOfPaginationReached = apiResponse.info.nextUrl == null || apiResponse.records.isEmpty()

            updateDatabase(loadType, page, validRecords, state, endOfPaginationReached)

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            MediatorResult.Error(AppError.Network(cause = exception))
        } catch (exception: HttpException) {
            MediatorResult.Error(
                AppError.ExternalSystem(code = exception.code(), message = exception.message(), cause = exception),
            )
        } catch (exception: SerializationException) {
            MediatorResult.Error(
                AppError.DataFormat(message = exception.message, cause = exception),
            )
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
            FILTER_RANDOM // For the main page, always "random"
        }
    }

    private fun resolveEffectiveSort(): String {
        return if (hasFilters()) {
            if (params.sort == FilterSortOption.DATE_BEGIN) SORT_ASC else SORT_DESC
        } else {
            SORT_DESC // For the main page, always "desс"
        }
    }

    private fun hasFilters(): Boolean =
        params.classification != null || params.century != null || params.culture != null

    private suspend fun updateDatabase(
        loadType: LoadType,
        page: Int,
        validRecords: List<ArtworkDTO>,
        state: PagingState<Int, ArtworkFeedWithFavoriteFlagDBO>,
        endOfPaginationReached: Boolean,
    ) {
        database.withTransaction {
            if (loadType == LoadType.REFRESH) {
                database.artworkRemoteKeysDao().clearRemoteKeys()
                // Clears discover feed and removes non-favorite artworks
                database.artworkDao().clearArtworks()
            }

            val prevKey = if (page == 1) null else page - 1
            val nextKey = if (endOfPaginationReached) null else page + 1

            // 1. Pagination keys
            val keys = validRecords.map { dto ->
                ArtworkRemoteKeysDBO(artworkId = dto.id, prevKey = prevKey, nextKey = nextKey)
            }

            // 2. Artworks catalog entities
            val artworks = validRecords.map { dto ->
                mapper.mapDtoToDbo(dto)
            }

            // 3. Discover feed items with session sorting index
            val feedItems = validRecords.mapIndexed { index, dto ->
                val globalIndex = (page - 1) * state.config.pageSize + index
                DiscoverFeedItemDBO(
                    artworkId = dto.id,
                    sortingIndex = globalIndex,
                )
            }

            // Insert parent artwork entities first (foreign key requirement)
            database.artworkDao().insertArtworks(artworks)

            // Insert discover feed items (child entities)
            database.artworkDao().insertDiscoverFeedItems(feedItems)

            // Save pagination keys
            database.artworkRemoteKeysDao().insertAll(keys)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, ArtworkFeedWithFavoriteFlagDBO>,
    ): ArtworkRemoteKeysDBO? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.artwork?.id?.let { artworkId ->
                database.artworkRemoteKeysDao().remoteKeyArtworkId(artworkId)
            }
        }
    }

    private suspend fun getRemoteKeyForFirstItem(
        state: PagingState<Int, ArtworkFeedWithFavoriteFlagDBO>,
    ): ArtworkRemoteKeysDBO? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { artwork ->
                database.artworkRemoteKeysDao().remoteKeyArtworkId(artwork.artwork.id)
            }
    }

    private suspend fun getRemoteKeyForLastItem(
        state: PagingState<Int, ArtworkFeedWithFavoriteFlagDBO>,
    ): ArtworkRemoteKeysDBO? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { artwork ->
                database.artworkRemoteKeysDao().remoteKeyArtworkId(artwork.artwork.id)
            }
    }
}
