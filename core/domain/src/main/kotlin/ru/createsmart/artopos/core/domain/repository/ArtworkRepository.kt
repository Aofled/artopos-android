package ru.createsmart.artopos.core.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.createsmart.artopos.core.model.Artwork
import ru.createsmart.artopos.core.model.ArtworkDetails
import ru.createsmart.artopos.core.model.FilterParams

public interface ArtworkRepository {
    public fun getPagedArtworks(
        params: FilterParams,
    ): Flow<PagingData<Artwork>>

    public fun getArtwork(id: Int): Flow<ArtworkDetails?>
    public suspend fun syncArtworkDetails(id: Int): Result<Unit>
    public fun getFavoriteArtworks(): Flow<List<Artwork>>
    public suspend fun toggleFavorite(artworkId: Int)
    public suspend fun clearDatabaseCache()
}
