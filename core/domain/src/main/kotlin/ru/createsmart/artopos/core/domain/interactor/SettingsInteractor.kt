package ru.createsmart.artopos.core.domain.interactor

import ru.createsmart.artopos.core.domain.usecase.ClearAppCacheUseCase
import ru.createsmart.artopos.core.domain.usecase.GetImageCacheSizeUseCase
import javax.inject.Inject

class SettingsInteractor @Inject constructor(
    val clearAppCacheUseCase: ClearAppCacheUseCase,
    val getImageCacheSizeUseCase: GetImageCacheSizeUseCase,
)
