package ru.createsmart.artopos.core.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.createsmart.artopos.core.model.Artwork
import ru.createsmart.artopos.core.model.FilterParams

interface ArtworkRepository {
    fun getPagedArtworks(
        params: FilterParams,
    ): Flow<PagingData<Artwork>>
}
