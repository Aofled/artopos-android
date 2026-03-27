package ru.createsmart.artopos.feature.favorites.model

data class FavoritesActions(
    val onRefresh: () -> Unit,
    val onArtworkClick: (Int) -> Unit,
    val onToggleFavorite: (Int) -> Unit,
)
