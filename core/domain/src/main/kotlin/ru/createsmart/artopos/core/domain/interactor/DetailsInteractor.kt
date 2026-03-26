package ru.createsmart.artopos.core.domain.interactor

import ru.createsmart.artopos.core.domain.usecase.GetArtworkDetailsUseCase
import ru.createsmart.artopos.core.domain.usecase.GetUserSettingsUseCase
import ru.createsmart.artopos.core.domain.usecase.SyncArtworkDetailsUseCase
import ru.createsmart.artopos.core.domain.usecase.ToggleFavoriteUseCase
import javax.inject.Inject

class DetailsInteractor @Inject constructor(
    val getArtworkDetails: GetArtworkDetailsUseCase,
    val getUserSettings: GetUserSettingsUseCase,
    val syncArtworkDetails: SyncArtworkDetailsUseCase,
    val toggleFavorite: ToggleFavoriteUseCase,
)
