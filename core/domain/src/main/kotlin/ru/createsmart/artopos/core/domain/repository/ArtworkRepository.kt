package ru.createsmart.artopos.core.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.createsmart.artopos.core.model.Artwork

interface ArtworkRepository {
    fun getArtworksStream(): Flow<List<Artwork>>

    suspend fun refreshArtwork(): Result<Unit>
}
