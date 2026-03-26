package ru.createsmart.artopos.core.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.createsmart.artopos.core.model.Artwork
import ru.createsmart.artopos.core.model.FilterParams

interface ArtworkRepository {
    fun getPagedArtworks(
        params: FilterParams,
    ): Flow<PagingData<Artwork>>

    fun getArtwork(id: Int): Flow<Artwork?>
    suspend fun syncArtworkDetails(id: Int): Result<Unit>
    fun getFavoriteArtworks(): Flow<List<Artwork>>
    suspend fun toggleFavorite(artworkId: Int)
}
