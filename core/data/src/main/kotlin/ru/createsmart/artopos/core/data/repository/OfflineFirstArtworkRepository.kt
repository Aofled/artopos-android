package ru.createsmart.artopos.core.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.createsmart.artopos.core.data.mapper.toDBO
import ru.createsmart.artopos.core.data.mapper.toDomain
import ru.createsmart.artopos.core.database.dao.ArtworkDao
import ru.createsmart.artopos.core.domain.repository.ArtworkRepository
import ru.createsmart.artopos.core.model.Artwork
import ru.createsmart.artopos.core.network.api.HarvardAPI
import javax.inject.Inject

/**
 * Pattern: Offline First.
 * The Database is the Single Source of Truth (SSOT).
 */
class OfflineFirstArtworkRepository @Inject constructor(
    private val dao: ArtworkDao,
    private val api: HarvardAPI,
) : ArtworkRepository {
    override fun getArtworksStream(): Flow<List<Artwork>> {
        // Observer: Automatically emits new data when DB changes
        return dao.getArtworks()
            .map { entities ->
                entities.map { it.toDomain() }
            }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun refreshArtwork(): Result<Unit> {
        // Safe Call: Catch network errors (No Internet), return Result.failure
        return runCatching {
            withContext(Dispatchers.IO) {
                val response = api.getArtworks()

                // Filter Logic: Drop broken items.
                val validRecords = response.records.filter { dto ->
                    !dto.images.isNullOrEmpty()
                }

                val newArtwork = validRecords.map { it.toDBO() }
                dao.insertArtworks(newArtwork) // Cache Update: This triggers the Flow above
            }
        }
    }
}
