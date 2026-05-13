package ru.createsmart.artopos.core.domain.usecase

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.createsmart.artopos.core.domain.repository.ArtworkRepository
import ru.createsmart.artopos.core.model.Artwork
import ru.createsmart.artopos.core.model.FilterParams
import javax.inject.Inject

public class GetArtworksUseCase @Inject constructor(
    private val repository: ArtworkRepository,
) {
    public operator fun invoke(
        params: FilterParams,
    ): Flow<PagingData<Artwork>> {
        return repository.getPagedArtworks(params)
    }
}
