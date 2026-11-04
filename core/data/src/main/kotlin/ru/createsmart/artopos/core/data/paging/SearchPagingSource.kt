package ru.createsmart.artopos.core.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import ru.createsmart.artopos.core.data.util.SearchQueryBuilder
import ru.createsmart.artopos.core.model.FilterParams
import ru.createsmart.artopos.core.model.FilterSortOption
import ru.createsmart.artopos.core.model.error.AppError
import ru.createsmart.artopos.core.network.api.HarvardAPI
import ru.createsmart.artopos.core.network.model.ArtworkDTO
import java.io.IOException

private const val SORT_DESC = "desc"
private const val SORT_ASC = "asc"

/**
 * Network-only PagingSource for dynamic Search queries.
 * Does NOT persist search results in SQLite/Room.
 */
class SearchPagingSource(
    private val api: HarvardAPI,
    private val rawQuery: String,
    private val params: FilterParams,
    private val onPageLoaded: (List<ArtworkDTO>) -> Unit = {},
) : PagingSource<Int, ArtworkDTO>() {

    override fun getRefreshKey(state: PagingState<Int, ArtworkDTO>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ArtworkDTO> {
        val page = params.key ?: 1
        val formattedQuery = SearchQueryBuilder.buildArtworkSearchQuery(rawQuery)

        val sortField = when (this.params.sort) {
            FilterSortOption.RANK -> "rank"
            FilterSortOption.TOTAL_PAGE_VIEWS -> "totalpageviews"
            FilterSortOption.ACCESSION_YEAR -> "accessionyear"
            FilterSortOption.DATE_BEGIN -> "datebegin"
            FilterSortOption.RANDOM -> "random"
        }
        val sortOrder = if (this.params.sort == FilterSortOption.DATE_BEGIN) SORT_ASC else SORT_DESC

        return try {
            val response = api.getArtworks(
                query = formattedQuery,
                page = page,
                size = params.loadSize,
                classification = this.params.classification?.id?.toString(),
                century = this.params.century?.id?.toString(),
                culture = this.params.culture?.id?.toString(),
                sort = sortField,
                sortOrder = sortOrder,
            )

            val validRecords = response.records.filter { !it.images.isNullOrEmpty() }

            // Save raw DTOs in session cache for favorite toggling
            onPageLoaded(validRecords)

            val endOfPaginationReached = response.info.nextUrl == null || response.records.isEmpty()

            val prevKey = if (page == 1) null else page - 1
            val nextKey = if (endOfPaginationReached) null else page + 1

            LoadResult.Page(
                data = validRecords,
                prevKey = prevKey,
                nextKey = nextKey,
            )
        } catch (exception: IOException) {
            LoadResult.Error(AppError.Network(cause = exception))
        } catch (exception: HttpException) {
            LoadResult.Error(
                AppError.ExternalSystem(
                    code = exception.code(),
                    message = exception.message(),
                    cause = exception,
                ),
            )
        } catch (exception: SerializationException) {
            LoadResult.Error(
                AppError.DataFormat(message = exception.message, cause = exception),
            )
        }
    }
}
