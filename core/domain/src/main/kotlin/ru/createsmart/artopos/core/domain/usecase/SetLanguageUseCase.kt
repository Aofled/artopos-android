package ru.createsmart.artopos.core.domain.usecase

import ru.createsmart.artopos.core.domain.repository.SettingsRepository
import javax.inject.Inject

class SetLanguageUseCase @Inject constructor(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(languageCode: String) {
        repository.setLanguage(languageCode)
    }
}
