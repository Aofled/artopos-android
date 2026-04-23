package ru.createsmart.artopos.core.domain.usecase

import ru.createsmart.artopos.core.domain.repository.SettingsRepository
import ru.createsmart.artopos.core.model.settings.ThemeConfig
import javax.inject.Inject

class SetThemeConfigUseCase @Inject constructor(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(themeConfig: ThemeConfig) {
        repository.setThemeConfig(themeConfig)
    }
}
