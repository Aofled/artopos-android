package ru.createsmart.artopos.core.domain.usecase

import ru.createsmart.artopos.core.domain.repository.FilterRepository
import javax.inject.Inject

class InitializeFiltersUseCase @Inject constructor(
    private val repository: FilterRepository,
) {
    suspend operator fun invoke() {
        repository.initializeFilters()
    }
}
