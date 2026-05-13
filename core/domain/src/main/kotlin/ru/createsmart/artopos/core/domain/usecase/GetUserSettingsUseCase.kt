package ru.createsmart.artopos.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.createsmart.artopos.core.domain.repository.SettingsRepository
import ru.createsmart.artopos.core.model.settings.UserSettings
import javax.inject.Inject

public class GetUserSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository,
) {
    public operator fun invoke(): Flow<UserSettings> {
        return repository.userSettingsStream
    }
}
