package ru.createsmart.artopos.core.domain.interactor

import ru.createsmart.artopos.core.domain.usecase.GetFavoriteArtworksUseCase
import ru.createsmart.artopos.core.domain.usecase.ToggleFavoriteUseCase
import javax.inject.Inject

class FavoritesInteractor @Inject constructor(
    val getFavoritesUseCase: GetFavoriteArtworksUseCase,
    val toggleFavoriteUseCase: ToggleFavoriteUseCase,
)
