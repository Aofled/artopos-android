package ru.createsmart.artopos.core.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.createsmart.artopos.core.model.Artwork

interface ArtworkRepository {
    fun getPagedArtworks(): Flow<PagingData<Artwork>>
}
