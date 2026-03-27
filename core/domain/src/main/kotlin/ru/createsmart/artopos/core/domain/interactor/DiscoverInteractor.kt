package ru.createsmart.artopos.core.domain.interactor

import ru.createsmart.artopos.core.domain.usecase.GetArtworksUseCase
import ru.createsmart.artopos.core.domain.usecase.GetFiltersUseCase
import ru.createsmart.artopos.core.domain.usecase.GetUserSettingsUseCase
import ru.createsmart.artopos.core.domain.usecase.InitializeFiltersUseCase
import ru.createsmart.artopos.core.domain.usecase.PreloadTranslationModelUseCase
import ru.createsmart.artopos.core.domain.usecase.ToggleFavoriteUseCase
import javax.inject.Inject

class DiscoverInteractor @Inject constructor(
    val getArtworks: GetArtworksUseCase,
    val getFilters: GetFiltersUseCase,
    val getUserSettings: GetUserSettingsUseCase,
    val toggleFavorite: ToggleFavoriteUseCase,
    val preloadTranslationModel: PreloadTranslationModelUseCase,
    val initializeFilters: InitializeFiltersUseCase,
)
