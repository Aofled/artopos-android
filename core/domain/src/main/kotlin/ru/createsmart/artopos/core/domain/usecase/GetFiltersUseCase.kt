package ru.createsmart.artopos.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.createsmart.artopos.core.domain.repository.FilterRepository
import ru.createsmart.artopos.core.model.FilterItem
import ru.createsmart.artopos.core.model.FilterType
import javax.inject.Inject

class GetFiltersUseCase @Inject constructor(
    private val repository: FilterRepository,
) {
    operator fun invoke(type: FilterType): Flow<List<FilterItem>> {
        return repository.getFilters(type)
    }
}
