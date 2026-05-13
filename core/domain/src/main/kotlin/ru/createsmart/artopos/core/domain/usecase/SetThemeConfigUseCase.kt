package ru.createsmart.artopos.core.domain.usecase

import ru.createsmart.artopos.core.domain.repository.SettingsRepository
import ru.createsmart.artopos.core.model.settings.ThemeConfig
import javax.inject.Inject

public class SetThemeConfigUseCase @Inject constructor(
    private val repository: SettingsRepository,
) {
    public suspend operator fun invoke(themeConfig: ThemeConfig) {
        repository.setThemeConfig(themeConfig)
    }
}
