package ru.createsmart.artopos.core.domain.usecase

import ru.createsmart.artopos.core.domain.repository.FilterRepository
import javax.inject.Inject

public class InitializeFiltersUseCase @Inject constructor(
    private val repository: FilterRepository,
) {
    public suspend operator fun invoke(): Result<Unit> {
        return repository.initializeFilters()
    }
}
