package ru.createsmart.artopos.core.domain.interactor

import ru.createsmart.artopos.core.domain.usecase.ClearAppCacheUseCase
import ru.createsmart.artopos.core.domain.usecase.GetImageCacheSizeUseCase
import ru.createsmart.artopos.core.domain.usecase.GetUserSettingsUseCase
import ru.createsmart.artopos.core.domain.usecase.SetLanguageUseCase
import ru.createsmart.artopos.core.domain.usecase.SetThemeConfigUseCase
import javax.inject.Inject

class SettingsInteractor @Inject constructor(
    val clearAppCacheUseCase: ClearAppCacheUseCase,
    val getImageCacheSizeUseCase: GetImageCacheSizeUseCase,
    val getUserSettings: GetUserSettingsUseCase,
    val setThemeConfig: SetThemeConfigUseCase,
    val setLanguage: SetLanguageUseCase,
)
